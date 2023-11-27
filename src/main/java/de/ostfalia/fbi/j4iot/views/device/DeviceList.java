package de.ostfalia.fbi.j4iot.views.device;

import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.data.renderer.ComponentRenderer;
import com.vaadin.flow.router.*;
import de.ostfalia.fbi.j4iot.data.entity.Device;
import de.ostfalia.fbi.j4iot.data.entity.Project;
import de.ostfalia.fbi.j4iot.data.service.DeviceService;
import de.ostfalia.fbi.j4iot.data.service.ProjectService;
import de.ostfalia.fbi.j4iot.views.GenericList;
import de.ostfalia.fbi.j4iot.views.MainLayout;
import jakarta.annotation.security.PermitAll;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Optional;

@PermitAll
@Route(value="/projects/:id?/devices", layout = MainLayout.class)
public class DeviceList extends GenericList<Device> implements HasDynamicTitle, BeforeEnterObserver, AfterNavigationObserver {

    // ************************************************************************

    public final static String ID_ROUTING_PARAMETER = "id";
    private Logger log = LoggerFactory.getLogger(DeviceList.class);
    private ProjectService projectService;
    private DeviceService deviceService;

    private Project project;

    // ************************************************************************

    public static RouteParameters getRouteParameters(Project project) {
        if (project != null) {
            return new RouteParameters(new RouteParam(ID_ROUTING_PARAMETER, project.getId()));
        } else {
            return RouteParameters.empty();
        }
    }

    public static void navigateTo(Project project) {
        RouteParameters rp = getRouteParameters(project);
        UI.getCurrent().navigate(DeviceList.class, rp);
    }

    // ************************************************************************

    public DeviceList(ProjectService projectService, DeviceService deviceService) {
        super(Device.class);
        this.projectService = projectService;
        this.deviceService = deviceService;
    }

    // ************************************************************************

    @Override
    protected void configureGrid() {
        grid.setColumns();
        grid.addComponentColumn(item -> new Button(VaadinIcon.SLIDERS.create(), click -> {
            DeviceSettings.navigateTo(item.getProject(), item);
        })).setTooltipGenerator(item -> "Dashboard").setFlexGrow(0);
        grid.addColumns("name", "tags");
        grid.addColumn(new ComponentRenderer<>(device -> {
            if (device.getProvisioningApproved()) { return VaadinIcon.CHECK_CIRCLE_O.create(); }
            else { return VaadinIcon.CIRCLE_THIN.create(); }
        })).setHeader("Provisioning OK").setTooltipGenerator(item -> "Provisioning approved").setFlexGrow(0);
        grid.addColumns("lastProvisionedAt", "lastSeenAt");
        super.configureGrid();
    }

    // ************************************************************************

    @Override
    protected void confirmDeleteItem(Device item) {
        confirmDeleteDialog.setHeader("Danger: Delete Device " + item.getName());
        String msg = String.format("Do you really want to device %s?", item.getName());
        confirmDeleteDialog.setText(msg);
        super.confirmDeleteItem(item);
    }

    // ************************************************************************

    @Override
    public String getPageTitle() {
        String title = "Devices in any project";
        if (project != null) {
            title = "Devices in " + project.getName() + " project";
        }
        return title;
    }

    @Override
    public void beforeEnter(BeforeEnterEvent event) {
        super.beforeEnter(event);
        project = null;
        Optional<Long> id = routeParameters.getLong(ID_ROUTING_PARAMETER);
        if (id.isPresent()) {
            project = projectService.findByAuthAndId(id.get()).orElse(null);
        }
    }

    @Override
    public void afterNavigation(AfterNavigationEvent event) {
        super.afterNavigation(event);
        withMainLayout(m -> m.setCurrentProject(project));
    }

    // ************************************************************************

    @Override
    protected void addItem() {
        DeviceSettings.navigateTo(project, null);
    }

    @Override
    protected void editItem(Device item) {
        DeviceSettings.navigateTo(item.getProject(), item);
    }

    @Override
    protected boolean removeItem(Device item) {
        try {
            deviceService.delete(item);
        } catch (Exception e) {
            log.error("Error in removeItem(Device id={} name={}): {}", item.getId(), item.getName(), e.getMessage());
            return false;
        }
        return true;
    }

    @Override
    protected void updateItems() {
        if (deviceService == null) {
            grid.setItems();
        } else {
            if (project == null) {
                grid.setItems(deviceService.findAllByAuth());
            } else {
                grid.setItems(deviceService.findAllByAuthAndProjectId(project.getId()));
            }
        }
    }

    // ************************************************************************

}
