package de.ostfalia.fbi.j4iot.views.project;

import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.checkbox.Checkbox;
import com.vaadin.flow.component.datetimepicker.DateTimePicker;
import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.grid.GridVariant;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.router.HasDynamicTitle;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.router.RouteParam;
import com.vaadin.flow.router.RouteParameters;
import de.ostfalia.fbi.j4iot.data.entity.Project;
import de.ostfalia.fbi.j4iot.data.entity.ProvisioningToken;
import de.ostfalia.fbi.j4iot.data.service.ProjectService;
import de.ostfalia.fbi.j4iot.views.GenericForm;
import de.ostfalia.fbi.j4iot.views.InstantRenderer;
import de.ostfalia.fbi.j4iot.views.MainLayout;
import jakarta.annotation.security.PermitAll;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.vaadin.addon.stefan.clipboard.ClientsideClipboard;

import java.util.Arrays;
import java.util.LinkedList;

@PermitAll
@Route(value="/projects/:id?/settings", layout = MainLayout.class)
public class ProjectSettings extends GenericForm<Project> implements HasDynamicTitle {

    // ***********************************************************************

    private final Logger log = LoggerFactory.getLogger(ProjectSettings.class);
    private final ProjectService service;

    private final TextField name = new TextField("Project name");
    private final TextField description = new TextField("Description");
    private final TextField tags = new TextField("Tags");
    private final DateTimePicker createdAt = new DateTimePicker("Created at");
    private final DateTimePicker updatedAt = new DateTimePicker("Update at");
    private final Checkbox autocreateDevices = new Checkbox("Autocreate devices");
    private final Checkbox provisioningAutoapproval = new Checkbox("Provisioning autoapproval");

    private final Grid<ProvisioningToken> provisioningTokens = new Grid<>(ProvisioningToken.class);
    private final Button addProvisioningTokenButton = new Button("Add provisioning token");

    // ***********************************************************************

    public static RouteParameters getRouteParameters(Project project) {
        if (project != null) {
            return new RouteParameters(new RouteParam(ID_ROUTING_PARAMETER, project.getId()));
        } else {
            return RouteParameters.empty();
        }
    }

    public static void navigateTo(Project project) {
        RouteParameters rp = getRouteParameters(project);
        UI.getCurrent().navigate(ProjectSettings.class, rp);
    }

    // ***********************************************************************

    public ProjectSettings(ProjectService service) {
        super(Project.class);
        this.service = service;
        binder.bindInstanceFields(this);

        FormLayout main = addForm();
        Arrays.asList(createdAt, updatedAt).forEach(e -> e.setReadOnly(true));
        main.setColspan(provisioningTokens, 2);
        main.setColspan(addProvisioningTokenButton, 2);
        Arrays.asList(name, description, tags).forEach(e -> main.setColspan(e, 2));
        addSectionTo(main, "General settings", name, description, tags, createdAt, updatedAt);
        addSectionTo(main, "Specific settings", autocreateDevices, provisioningAutoapproval);

        provisioningTokens.setColumns();
        provisioningTokens.addColumn("token")
                .setTooltipGenerator(ProvisioningToken::getToken)
                .setHeader("Token");
        provisioningTokens.addColumn(new InstantRenderer<>(ProvisioningToken::getExpiresAt))
                .setTooltipGenerator(item -> item.getExpiresAt().toString())
                .setHeader("Expires");
        provisioningTokens.addComponentColumn(item ->
                        new Button(new Icon(VaadinIcon.CLIPBOARD), click -> {
                            ClientsideClipboard.writeToClipboard(item.getToken());
                        }))
                .setTooltipGenerator(item -> "Copy token to clipboard")
                .setAutoWidth(true).setFlexGrow(0);
        provisioningTokens.addComponentColumn(item ->
                        new Button(new Icon(VaadinIcon.TRASH),click -> {
                            item.getProject().getProvisioningTokens().remove(item);
                            provisioningTokens.setItems(item.getProject().getProvisioningTokens());
                        }))
                .setTooltipGenerator(item -> "Delete token")
                .setAutoWidth(true).setFlexGrow(0);
        provisioningTokens.setAllRowsVisible(true);
        provisioningTokens.addThemeVariants(GridVariant.LUMO_COMPACT);
        addProvisioningTokenButton.addClickListener(event -> {
            if (binder.getBean() != null) {
                Project project = binder.getBean();
                service.addNewProvisioningToken(project);
                provisioningTokens.setItems(project.getProvisioningTokens());
            }
        });
        addSectionTo(main, "Provisioning Tokens", provisioningTokens, addProvisioningTokenButton);

        addFooter();
    }

    // ***********************************************************************

    @Override
    public String getPageTitle() {
        String title = "Create project";
        if (item != null) {
            title = "Project settings: " + item.getName();
        }
        return title;
    }

    @Override
    public void populateForm(Project item) {
        super.populateForm(item);
        name.setReadOnly( item != null );
        if (item == null) {
            provisioningTokens.setItems(new LinkedList<>());
        } else {
            provisioningTokens.setItems(item.getProvisioningTokens());
        }
        withMainLayout(m -> m.setCurrentProject(item));
    }

    @Override
    protected Project load(long id) {
        return service.findByAuthAndId(id).orElse(null);
    }

    @Override
    protected Project save() {
        return service.updateOrCreate(item);
    }

}
