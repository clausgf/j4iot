package de.ostfalia.fbi.j4iot.views.project;

import com.vaadin.flow.component.ComponentEvent;
import com.vaadin.flow.component.ComponentEventListener;
import com.vaadin.flow.component.Key;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.checkbox.Checkbox;
import com.vaadin.flow.component.datetimepicker.DateTimePicker;
import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.binder.BeanValidationBinder;
import com.vaadin.flow.shared.Registration;
import de.ostfalia.fbi.j4iot.InstantRenderer;
import de.ostfalia.fbi.j4iot.data.entity.Project;
import de.ostfalia.fbi.j4iot.data.entity.ProvisioningToken;
import de.ostfalia.fbi.j4iot.data.service.IotService;
import org.vaadin.addon.stefan.clipboard.ClientsideClipboard;

import java.util.LinkedList;

// TODO base this form on a generic one
public class ProjectForm extends FormLayout {
    IotService service;

    TextField name = new TextField("Project name");
    TextField description = new TextField("Description");
    TextField tags = new TextField("Tags");
    DateTimePicker createdAt = new DateTimePicker("Created at");
    DateTimePicker updatedAt = new DateTimePicker("Update at");
    Checkbox autocreateDevices = new Checkbox("Autocreate devices");
    Checkbox provisioningAutoapproval = new Checkbox("Provisioning autoapproval");

    TextField defaultProvisioningTokenLength = new TextField("Provisioning token length");
    TextField defaultProvisioningTokenExpiresInSeconds = new TextField("Provisioning token expiry (seconds)");
    TextField defaultDeviceTokenLength = new TextField("Device token length (default)");
    TextField defaultDeviceTokenExpiresInSeconds = new TextField("Device token expiry (default seconds)");

    // TODO list of provisioningTokens, create new, delete
    Grid<ProvisioningToken> provisioningTokens = new Grid<>(ProvisioningToken.class);
    Button addProvisioningTokenButton = new Button("Add provisioning token");
    // TODO list of devices, create new, delete

    Button save = new Button("Save");
    Button delete = new Button("Delete");
    Button close = new Button ("Cancel");

    BeanValidationBinder<Project> binder = new BeanValidationBinder<>(Project.class);

    public ProjectForm(IotService service) {
        this.service = service;
        addClassName("project-form");
        binder.bindInstanceFields(this);
        createdAt.setReadOnly(true);
        updatedAt.setReadOnly(true);
        provisioningTokens.setColumns();
        provisioningTokens.addColumn("token")
                .setTooltipGenerator(item -> item.getToken())
                .setHeader("Token");
        provisioningTokens.addColumn(new InstantRenderer<>(ProvisioningToken::getExpiresAt))
                .setTooltipGenerator(item -> item.getExpiresAt().toString())
                .setHeader("Expires")
                .setAutoWidth(true);
        provisioningTokens.addComponentColumn(item -> new Button(new Icon(VaadinIcon.CLIPBOARD), click -> {
            ClientsideClipboard.writeToClipboard(item.getToken());
        }))
                .setTooltipGenerator(item -> "Copy token to clipboard")
                .setAutoWidth(true);
        provisioningTokens.addComponentColumn(item -> new Button(new Icon(VaadinIcon.TRASH),click -> {
            item.getProject().getProvisioningTokens().remove(item);
            provisioningTokens.setItems(item.getProject().getProvisioningTokens());
        }))
                .setTooltipGenerator(item -> "Delete token")
                .setAutoWidth(true);

        provisioningTokens.setAllRowsVisible(true);
        addProvisioningTokenButton.addClickListener(event -> {
            if (binder.getBean() != null) {
                Project project = binder.getBean();
                service.addProvisioningToken(project);
                provisioningTokens.setItems(project.getProvisioningTokens());
            }
        });

        add(name, description, tags, createdAt, updatedAt,
                autocreateDevices, provisioningAutoapproval,
                defaultProvisioningTokenLength, defaultProvisioningTokenExpiresInSeconds,
                defaultDeviceTokenLength, defaultDeviceTokenExpiresInSeconds,
                provisioningTokens,
                addProvisioningTokenButton,
                createButtonsLayout());
    }

    public void setProject(Project project) {
        binder.setBean(project);
        if (project == null) {
            provisioningTokens.setItems(new LinkedList<>());
        } else {
            provisioningTokens.setItems(project.getProvisioningTokens());
        }
    }

    private HorizontalLayout createButtonsLayout() {
        save.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        delete.addThemeVariants(ButtonVariant.LUMO_ERROR);
        close.addThemeVariants(ButtonVariant.LUMO_TERTIARY);

        save.addClickShortcut(Key.ENTER);
        close.addClickShortcut(Key.ESCAPE);

        save.addClickListener(event -> validateAndSave());
        delete.addClickListener(event -> fireEvent(new DeleteEvent(this, binder.getBean())));
        close.addClickListener(event -> fireEvent(new CloseEvent(this)));

        binder.addStatusChangeListener(e -> save.setEnabled(binder.isValid()));
        return new HorizontalLayout(save, delete, close);
    }

    private void validateAndSave() {
        if (binder.isValid()) {
            fireEvent(new SaveEvent(this, binder.getBean()));
        }
    }

    public static abstract class ProjectFormEvent extends ComponentEvent<ProjectForm> {
        private final Project project;

        protected ProjectFormEvent(ProjectForm source, Project project) {
            super(source, false);
            this.project = project;
        }

        public Project getProject() {
            return project;
        }
    }

    public static class SaveEvent extends ProjectFormEvent {
        SaveEvent(ProjectForm source, Project project) {
            super(source, project);
        }
    }

    public static class DeleteEvent extends ProjectFormEvent {
        DeleteEvent(ProjectForm source, Project project) {
            super(source, project);
        }
    }

    public static class CloseEvent extends ProjectFormEvent {
        CloseEvent(ProjectForm source) {
            super(source, null);
        }
    }

    public Registration addDeleteListener(ComponentEventListener<DeleteEvent> listener) {
        return addListener(DeleteEvent.class, listener);
    }

    public Registration addSaveListener(ComponentEventListener<SaveEvent> listener) {
        return addListener(SaveEvent.class, listener);
    }

    public Registration addCloseListener(ComponentEventListener<CloseEvent> listener) {
        return addListener(CloseEvent.class, listener);
    }
}
