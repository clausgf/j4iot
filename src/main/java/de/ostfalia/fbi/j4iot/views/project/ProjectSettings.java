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
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.router.HasDynamicTitle;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.router.RouteParam;
import com.vaadin.flow.router.RouteParameters;
import de.ostfalia.fbi.j4iot.data.entity.Forwarding;
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

    private final Grid<ProvisioningToken> provisioningTokenGrid = new Grid<>(ProvisioningToken.class);
    private final Button addProvisioningTokenButton = new Button("Add provisioning token");
    private final ProvisioningTokenDialog provisioningTokenDialog;

    private final Grid<Forwarding> forwardingGrid = new Grid<>(Forwarding.class);
    private final Button addForwardingButton = new Button("Add http forwarding");
    private final ForwardingDialog forwardingDialog;

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

        provisioningTokenDialog = new ProvisioningTokenDialog(ProvisioningToken.class, (isCreateMode, savedItem) -> {
            Project project = binder.getBean();
            if (project != null) {
                if (isCreateMode) {
                    savedItem.setProject(project);
                    project.getProvisioningTokens().add(savedItem);
                }
                provisioningTokenGrid.setItems(project.getProvisioningTokens());
                return savedItem;
            }
            return null;
        });

        forwardingDialog = new ForwardingDialog(Forwarding.class, (isCreateMode, savedItem) -> {
            Project project = binder.getBean();
            if (project != null) {
                if (isCreateMode) {
                    savedItem.setProject(project);
                    project.getForwardings().add(savedItem);
                }
                forwardingGrid.setItems(project.getForwardings());
                return savedItem;
            }
            return null;
        });

        FormLayout main = addForm();
        Arrays.asList(createdAt, updatedAt).forEach(e -> e.setReadOnly(true));
        Arrays.asList(provisioningTokenGrid, forwardingGrid).forEach(e -> main.setColspan(e, 2));
        Arrays.asList(addProvisioningTokenButton, addForwardingButton).forEach(e -> main.setColspan(e, 2));
        Arrays.asList(name, description, tags).forEach(e -> main.setColspan(e, 2));
        addSectionTo(main, "General settings", name, description, tags, createdAt, updatedAt);
        addSectionTo(main, "Specific settings", autocreateDevices, provisioningAutoapproval);

        provisioningTokenGrid.setColumns();
        provisioningTokenGrid.addColumn("token");
        provisioningTokenGrid.addColumn(new InstantRenderer<>(ProvisioningToken::getExpiresAt))
                .setHeader("Expires")
                .setAutoWidth(true).setFlexGrow(0);
        provisioningTokenGrid.addComponentColumn(item -> { // row copy, edit & delete buttons
                    Button copyButton = new Button(new Icon(VaadinIcon.CLIPBOARD), click -> {
                            ClientsideClipboard.writeToClipboard(item.getToken());
                        });
                    copyButton.setTooltipText("Copy token to clipboard");
                    Button editButton = new Button(new Icon(VaadinIcon.EDIT), click -> provisioningTokenDialog.openForEdit(item) );
                    editButton.setTooltipText("Edit token");
                    Button deleteButton = new Button(new Icon(VaadinIcon.TRASH),click -> {
                            item.getProject().getProvisioningTokens().remove(item);
                            provisioningTokenGrid.setItems(item.getProject().getProvisioningTokens());
                        });
                    deleteButton.setTooltipText("Delete token");
                    return new HorizontalLayout(copyButton, editButton, deleteButton);
                })
                .setAutoWidth(true).setFlexGrow(0);
        provisioningTokenGrid.setAllRowsVisible(true);
        provisioningTokenGrid.addThemeVariants(GridVariant.LUMO_COMPACT);
        addProvisioningTokenButton.addClickListener(event -> {
                Project project = binder.getBean();
                if (project != null) {
                    ProvisioningToken pt = service.addNewProvisioningToken(project);
                    provisioningTokenGrid.setItems(project.getProvisioningTokens());
                    provisioningTokenDialog.openForEdit(pt); // edit is correct, token already created above!
                }
            });
        addSectionTo(main, "Provisioning Tokens", provisioningTokenGrid, addProvisioningTokenButton);

        forwardingGrid.setColumns();
        forwardingGrid.addColumn("name").setAutoWidth(true);
        forwardingGrid.addColumn("forwardToUrl").setAutoWidth(true);
        forwardingGrid.addComponentColumn(item -> {
                Button editButton = new Button( new Icon(VaadinIcon.EDIT), click -> forwardingDialog.openForEdit(item) );
                editButton.setTooltipText("Edit forwarding");
                Button deleteButton = new Button( new Icon(VaadinIcon.TRASH), click -> {
                    item.getProject().getForwardings().remove(item);
                    forwardingGrid.setItems(item.getProject().getForwardings());
                } );
                deleteButton.setTooltipText("Delete forwarding");
                return new HorizontalLayout(editButton, deleteButton);
            })
            .setAutoWidth(true).setFlexGrow(0);
        forwardingGrid.setAllRowsVisible(true);
        forwardingGrid.addThemeVariants(GridVariant.LUMO_COMPACT);
        addForwardingButton.addClickListener( event -> {
                Project project = binder.getBean();
                if (project != null) {
                    Forwarding f = new Forwarding(project);
                    forwardingDialog.openForCreate(f);
                }
            } );
        addSectionTo(main, "Forwarding", forwardingGrid, addForwardingButton);

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
            provisioningTokenGrid.setItems(new LinkedList<>());
            forwardingGrid.setItems(new LinkedList<>());
        } else {
            provisioningTokenGrid.setItems(item.getProvisioningTokens());
            forwardingGrid.setItems(item.getForwardings());
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
