package de.ostfalia.fbi.j4iot.views.device;

import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.BeforeEnterEvent;
import com.vaadin.flow.router.BeforeEnterObserver;
import com.vaadin.flow.router.HasDynamicTitle;
import com.vaadin.flow.router.Route;
import de.ostfalia.fbi.j4iot.data.service.IotService;
import de.ostfalia.fbi.j4iot.views.MainLayout;
import jakarta.annotation.security.PermitAll;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@PermitAll
@Route(value="/projects/:projectName/devices/:deviceName/dashboard", layout = MainLayout.class)
public class DeviceDashboard extends VerticalLayout implements HasDynamicTitle, BeforeEnterObserver {

    Logger log = LoggerFactory.getLogger(DeviceDashboard.class);

    public final static String PROJECT_NAME_RP = "projectName";
    public final static String DEVICE_NAME_RP = "deviceName";

    String projectName;
    String deviceName;
    IotService service;

    public DeviceDashboard(IotService service) {
        this.service = service;
        addClassName("dashboard");
        setSizeFull();
    }

    @Override
    public void beforeEnter(BeforeEnterEvent event) {
        this.projectName = event.getRouteParameters().get(PROJECT_NAME_RP).orElse("");
        this.deviceName = event.getRouteParameters().get(DEVICE_NAME_RP).orElse("");
    }

    @Override
    public String getPageTitle() {
        String title;
        if ((projectName == null) || projectName.isEmpty() || deviceName == null || deviceName.isEmpty()) {
            title = "Dashboard for unknown";
        } else {
            title = "Dashboard for " + projectName + "/" + deviceName;
        }
        return title;
    }

}
