package de.ostfalia.fbi.j4iot.views.project;

import com.vaadin.flow.component.Key;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.checkbox.Checkbox;
import com.vaadin.flow.component.datetimepicker.DateTimePicker;
import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.html.H3;
import com.vaadin.flow.component.html.Section;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.notification.NotificationVariant;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.binder.BeanValidationBinder;
import com.vaadin.flow.router.BeforeEnterEvent;
import com.vaadin.flow.router.BeforeEnterObserver;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import de.ostfalia.fbi.j4iot.data.entity.Project;
import de.ostfalia.fbi.j4iot.data.service.IotService;
import de.ostfalia.fbi.j4iot.views.MainLayout;
import jakarta.annotation.security.PermitAll;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Optional;

@PermitAll
@PageTitle("Project settings")
@Route(value="/projects/:id", layout = MainLayout.class)
public class ProjectForm extends FormLayout implements BeforeEnterObserver {

    private Logger log = LoggerFactory.getLogger(ProjectForm.class);
    protected final Class<Project> modelClass;
    protected Button reset = new Button ("Reset");
    protected Button save = new Button("Save");

    protected Project item;
    protected BeanValidationBinder<Project> binder;

    public final static String ID_ROUTING_PARAM = "id";
    IotService service;

    TextField name = new TextField("Project name");
    TextField description = new TextField("Description");
    TextField tags = new TextField("Tags");
    DateTimePicker createdAt = new DateTimePicker("Created at");
    DateTimePicker updatedAt = new DateTimePicker("Update at");
    Checkbox autocreateDevices = new Checkbox("Autocreate devices");
    Checkbox provisioningAutoapproval = new Checkbox("Provisioning autoapproval");

    public ProjectForm(IotService service) {
        //super(Project.class);
        this.modelClass = Project.class;
        addClassName("generic-form");
        setResponsiveSteps(new ResponsiveStep("0", 1));
        binder = new BeanValidationBinder<>(modelClass);
        binder.bindInstanceFields(this);

        this.service = service;
        createdAt.setReadOnly(true);
        updatedAt.setReadOnly(true);

        H2 header = new H2("Project");
        Section sectionHeader = new Section(header);
        sectionHeader.setClassName("generic-form-header");
        add(sectionHeader);

        H3 sectionGeneralTitle = new H3("General settings");
        VerticalLayout sectionGeneral = new VerticalLayout(sectionGeneralTitle, name, description, tags, createdAt, updatedAt);
        sectionGeneral.setClassName("generic-form-main");
        add(sectionGeneral);

        H3 sectionSpecificTitle = new H3("Specific settings");
        VerticalLayout sectionSpecific = new VerticalLayout(sectionSpecificTitle, autocreateDevices, provisioningAutoapproval);
        sectionSpecific.setClassName("generic-form-main");
        add(sectionSpecific);

        VerticalLayout sectionButtons = new VerticalLayout(createButtonsLayout());
        sectionButtons.setClassName("generic-form-footer");
        add(sectionButtons);
    }

    protected HorizontalLayout createButtonsLayout() {
        HorizontalLayout buttonLayout = new HorizontalLayout();

        reset.addThemeVariants(ButtonVariant.LUMO_TERTIARY);
        reset.addClickShortcut(Key.ESCAPE);
        reset.addClickListener(event -> binder.readBean(item));
        buttonLayout.add(reset);

        save.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        save.addClickShortcut(Key.ENTER);
        save.addClickListener(event -> validateAndSave());
        binder.addStatusChangeListener(e -> save.setEnabled(binder.isValid()));
        buttonLayout.add(save);

        return buttonLayout;
    }

    public void populateForm(Project item) {
        this.item = item;
        binder.readBean(item);
    }

    protected void validateAndSave() {
        if (binder.writeBeanIfValid(item)) {
            try {
                item = save(item);
                if (item != null) {
                    Notification.show("Item saved");
                    populateForm(item);
                } else {
                    Notification.show("Failure saving item").addThemeVariants(NotificationVariant.LUMO_ERROR);
                }
            } catch (Exception e) {
                log.error("Error saving item: {}", e.getMessage());
                Notification.show("Failure saving item: " + e.getMessage()).addThemeVariants(NotificationVariant.LUMO_ERROR);
            }
        } else {
            Notification.show("Failure saving item: Validation failed");
        }
    }

    protected Project save(Project item) {
        return service.updateProject(item);
    }

    @Override
    public void beforeEnter(BeforeEnterEvent event) {
        Optional<Long> id = event.getRouteParameters().getLong(ID_ROUTING_PARAM);
        if (id.isPresent()) {
            Optional<Project> item = service.findProjectById(id.get());
            if (item.isPresent()) {
                populateForm(item.get());
            } else {
                Notification.show("The requested item was not found, id=" + id.get());
                populateForm(null);
            }
        } else {
            log.error("Invalid route parameter, expected projectId");
            populateForm(null);
        }
    }

}
