package de.ostfalia.fbi.j4iot.views.projectlist;

import com.vaadin.flow.component.ComponentEvent;
import com.vaadin.flow.component.ComponentEventListener;
import com.vaadin.flow.component.Key;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.checkbox.Checkbox;
import com.vaadin.flow.component.datetimepicker.DateTimePicker;
import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.binder.BeanValidationBinder;
import com.vaadin.flow.shared.Registration;
import de.ostfalia.fbi.j4iot.data.entity.Project;

// TODO base this form on a generic one
public class ProjectForm extends FormLayout {
    TextField name = new TextField("Project name");
    TextField description = new TextField("Description");
    TextField tags = new TextField("Tags");
    DateTimePicker createdAt = new DateTimePicker("Created at");
    DateTimePicker updatedAt = new DateTimePicker("Update at");
    Checkbox autocreateDevices = new Checkbox("Autocreate devices");
    Checkbox provisioningAutoapproval = new Checkbox("Provisioning autoapproval");

    // TODO list of provisioning tokens, editor
    // TODO list of devices, editor

    Button save = new Button("Save");
    Button delete = new Button("Delete");
    Button close = new Button ("Cancel");

    BeanValidationBinder<Project> binder = new BeanValidationBinder<>(Project.class);

    public ProjectForm() {
        addClassName("project-form");
        binder.bindInstanceFields(this);
        createdAt.setReadOnly(true);
        updatedAt.setReadOnly(true);

        add(name, description, tags, createdAt, updatedAt, autocreateDevices, provisioningAutoapproval, createButtonsLayout());
    }

    public void setProject(Project project) {
        binder.setBean(project);
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
