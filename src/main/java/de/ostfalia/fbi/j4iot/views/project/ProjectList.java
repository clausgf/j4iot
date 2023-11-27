package de.ostfalia.fbi.j4iot.views.project;

import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.data.renderer.ComponentRenderer;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import de.ostfalia.fbi.j4iot.data.entity.Project;
import de.ostfalia.fbi.j4iot.data.service.ProjectService;
import de.ostfalia.fbi.j4iot.views.GenericList;
import de.ostfalia.fbi.j4iot.views.MainLayout;
import de.ostfalia.fbi.j4iot.views.device.DeviceList;
import jakarta.annotation.security.PermitAll;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

// TODO base this list on a generic one
@PermitAll
@Route(value="/projects", layout = MainLayout.class)
@PageTitle("Projects")
public class ProjectList extends GenericList<Project> {

    // ************************************************************************

    private Logger log = LoggerFactory.getLogger(ProjectList.class);
    private final ProjectService service;

    // ************************************************************************

    public static void navigateTo() {
        UI.getCurrent().navigate(ProjectList.class);
    }

    // ************************************************************************

    public ProjectList(ProjectService service) {
        super(Project.class);
        this.service = service;
    }

    // ************************************************************************

    @Override
    protected void configureGrid() {
        grid.setColumns();
        grid.addComponentColumn(project ->
                new Button(VaadinIcon.ROCKET.create(), click -> {
                    DeviceList.navigateTo(project);
                }))
                .setTooltipGenerator(item -> "Navigate to device")
                .setHeader("Devices").setFlexGrow(0);
        grid.addColumns("name", "tags");
        grid.addColumn(new ComponentRenderer<>(project -> {
                    if (project.getAutocreateDevices()) { return VaadinIcon.CHECK_CIRCLE_O.create(); }
                    else { return VaadinIcon.CIRCLE_THIN.create(); }
                })).setHeader("Autocreate").setFlexGrow(0);
        grid.addColumn(new ComponentRenderer<>(project -> {
                    if (project.getProvisioningAutoapproval()) { return VaadinIcon.CHECK_CIRCLE_O.create(); }
                    else { return VaadinIcon.CIRCLE_THIN.create(); }
                })).setHeader("Autoprovision").setFlexGrow(0);
        // TODO add number of provisioning tokens (linking to some editor?)
        // TODO add number of devices (linking to some editor?)
        // TODO render tags as badges with color code
        super.configureGrid(); // call the base class method at the end to allow further modifications
    }

    // ************************************************************************

    @Override
    protected void confirmDeleteItem(Project item) {
        confirmDeleteDialog.setHeader("Danger: Delete Project " + item.getName());
        String msg = String.format("Do you really want to project %s containing %s devices?", item.getName(), service.countDevicesInProject(item));
        confirmDeleteDialog.setText(msg);
        super.confirmDeleteItem(item);
    }

    // ************************************************************************

    @Override
    protected void addItem() {
        ProjectSettings.navigateTo(null);
    }

    @Override
    protected void editItem(Project item) {
        ProjectSettings.navigateTo(item);
    }

    @Override
    protected boolean removeItem(Project item) {
        try {
            service.delete(item);
        } catch (Exception e) {
            log.error("Error in removeItem(Project id={} name={}): {}", item.getId(), item.getName(), e.getMessage());
            return false;
        }
        return true;
    }

    @Override
    protected void updateItems() {
        if (service == null) {
            grid.setItems();
        } else {
            grid.setItems(service.findAllByAuth());
        }
    }

    // ************************************************************************

}
