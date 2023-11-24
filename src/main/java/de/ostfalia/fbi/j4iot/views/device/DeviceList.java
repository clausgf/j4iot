package de.ostfalia.fbi.j4iot.views.device;

import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.router.*;
import de.ostfalia.fbi.j4iot.data.entity.Device;
import de.ostfalia.fbi.j4iot.data.entity.Project;
import de.ostfalia.fbi.j4iot.data.service.IotService;
import de.ostfalia.fbi.j4iot.views.GenericList;
import de.ostfalia.fbi.j4iot.views.MainLayout;
import jakarta.annotation.security.PermitAll;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.vaadin.lineawesome.LineAwesomeIcon;

import java.util.Optional;

@PermitAll
@Route(value="/projects/:id?/devices", layout = MainLayout.class)
public class DeviceList extends GenericList<Device> implements HasDynamicTitle, BeforeEnterObserver, AfterNavigationObserver {

    public final static String ID_ROUTING_PARAMETER = "id";
    private Logger log = LoggerFactory.getLogger(DeviceList.class);
    private IotService service;

    private Project project;


    public static RouteParameters getRouteParametersWithProject(Project project) {
        if (project != null) {
            return new RouteParameters(new RouteParam(ID_ROUTING_PARAMETER, project.getId()));
        } else {
            return RouteParameters.empty();
        }
    }

    public static void navigateToProject(Project project) {
        if (project != null) {
            RouteParameters rp = new RouteParameters(new RouteParam(ID_ROUTING_PARAMETER, project.getId()));
            UI.getCurrent().navigate(DeviceList.class, rp);
        } else {
            UI.getCurrent().navigate(DeviceList.class);
        }
    }


    public DeviceList(IotService service) {
        super(Device.class);
        this.service = service;
        updateItems();
    }

    @Override
    protected void configureGrid() {
        grid.setColumns();
        grid.addComponentColumn(item -> new Button(LineAwesomeIcon.TACHOMETER_ALT_SOLID.create(), click -> {
            DeviceDashboard.navigateToWithDevice(item);
        })).setTooltipGenerator(item -> "Dashboard");
        grid.addColumns("name", "tags", "provisioningApproved", "lastProvisioningRequestAt", "lastProvisionedAt", "lastSeenAt");
        super.configureGrid();
    }

    @Override
    protected void confirmDeleteItem(Device item) {
        confirmDeleteDialog.setHeader("Danger: Delete Device " + item.getName());
        String msg = String.format("Do you really want to device %s?", item.getName());
        confirmDeleteDialog.setText(msg);
        super.confirmDeleteItem(item);
    }

    @Override
    protected boolean addItem() {
        return false;
    }

    @Override
    protected void editItem(Device item) {
        DeviceSettings.navigateToWithDevice(item);
    }

    @Override
    protected boolean removeItem(Device item) {
        try {
            service.deleteDevice(item);
        } catch (Exception e) {
            log.error("Error in removeItem(Device id={} name={}): {}", item.getId(), item.getName(), e.getMessage());
            return false;
        }
        return true;
    }

    @Override
    protected void updateItems() {
        if (service == null) {
            grid.setItems();
        } else {
            if (project == null) {
                grid.setItems(service.searchAllDevices(filterText.getValue()));
            } else {
                grid.setItems(service.searchAllDevicesByProjectId(project.getId(), filterText.getValue()));
            }
        }
    }

    @Override
    public String getPageTitle() {
        String title = "Devices in any project";
        if (project != null) {
            title = "Devices in " + project.getName() + " project";
        } else {
            project = null;
        }
        return title;
    }

    @Override
    public void beforeEnter(BeforeEnterEvent event) {
        super.beforeEnter(event);
        project = null;
        Optional<Long> id = routeParameters.getLong(ID_ROUTING_PARAMETER);
        if (id.isPresent()) {
            project = service.findProjectById(id.get()).orElse(null);
        }
    }

    @Override
    public void afterNavigation(AfterNavigationEvent event) {
        withMainLayout(m -> m.setCurrentProject(project));
        updateItems();
    }
}
