package de.ostfalia.fbi.j4iot.views.project;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.router.RouteParam;
import com.vaadin.flow.router.RouteParameters;
import de.ostfalia.fbi.j4iot.data.entity.Project;
import de.ostfalia.fbi.j4iot.data.service.IotService;
import de.ostfalia.fbi.j4iot.views.GenericList;
import de.ostfalia.fbi.j4iot.views.MainLayout;
import de.ostfalia.fbi.j4iot.views.device.DeviceList;
import jakarta.annotation.security.PermitAll;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.vaadin.lineawesome.LineAwesomeIcon;

import java.util.Optional;

// TODO base this list on a generic one
@PermitAll
@Route(value="/projects", layout = MainLayout.class)
@PageTitle("Projects")
public class ProjectList extends GenericList<Project> {

    public final static String ID_ROUTING_PARAMETER = "id";
    private Logger log = LoggerFactory.getLogger(ProjectList.class);
    private final IotService service;

    public ProjectList(IotService service) {
        super(Project.class);
        this.service = service;
        updateItems();
    }

    @Override
    protected void configureGrid() {
        grid.setColumns();
        grid.addComponentColumn(project -> new Button(LineAwesomeIcon.LINK_SOLID.create(), click -> {
            onNavigateToDevices(project);
        })).setHeader("Devices");
        grid.addColumns("name", "tags", "autocreateDevices", "provisioningAutoapproval");
        //grid.addColumn(new InstantRenderer<>(Project::getCreatedAt)).setHeader("Created");
        //grid.addColumn(new InstantRenderer<>(Project::getCreatedAt)).setHeader("Updated");
        // TODO add number of provisioning tokens (linking to some editor?)
        // TODO add number of devices (linking to some editor?)
        // TODO render tags as badges with color code
        super.configureGrid(); // call the base class method at the end to allow further modifications
    }

    @Override
    protected void confirmDeleteItem(Project item) {
        confirmDeleteDialog.setHeader("Danger: Delete Project " + item.getName());
        String msg = String.format("Do you really want to project %s containing %s devices?", item.getName(), service.countDevicesInProject(item));
        confirmDeleteDialog.setText(msg);
        super.confirmDeleteItem(item);
    }

    @Override
    protected boolean addItem() {
        return false;
    }

    @Override
    protected void editItem(Project item) {
        setMainLayoutProject(item);
        RouteParameters rp = new RouteParameters(new RouteParam(ID_ROUTING_PARAMETER, item.getId()));
        UI.getCurrent().navigate(ProjectForm.class, rp);
    }

    @Override
    protected boolean removeItem(Project item) {
        try {
            service.deleteProject(item);
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
            grid.setItems(service.findAllProjects(filterText.getValue()));
        }
    }

    private void onNavigateToDevices(Project project)
    {
        setMainLayoutProject(project);
        RouteParameters rp = new RouteParameters(new RouteParam(ID_ROUTING_PARAMETER, project.getId()));
        UI.getCurrent().navigate(DeviceList.class, rp);
    }

    private void setMainLayoutProject(Project project) {
        Optional<Component> parent = getParent();
        if (parent.isPresent()) {
            if (parent.get() instanceof MainLayout m) {
                log.info("parent is present and a MainLayout");
                m.setProjectName(project.getName()); // TODO
            }
            log.info("parent is present");
        } else {
            log.info("ProjectList has no parent");
        }
    }
}
