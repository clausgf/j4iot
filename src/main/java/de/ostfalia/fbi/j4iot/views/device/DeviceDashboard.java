package de.ostfalia.fbi.j4iot.views.device;

import com.vaadin.flow.component.Text;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.H4;
import com.vaadin.flow.component.orderedlayout.Scroller;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.binder.BeanValidationBinder;
import com.vaadin.flow.router.BeforeEnterEvent;
import com.vaadin.flow.router.BeforeEnterObserver;
import com.vaadin.flow.router.HasDynamicTitle;
import com.vaadin.flow.router.Route;
import de.ostfalia.fbi.j4iot.data.entity.Device;
import de.ostfalia.fbi.j4iot.data.service.IotService;
import de.ostfalia.fbi.j4iot.views.MainLayout;
import jakarta.annotation.security.PermitAll;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Optional;

@PermitAll
@Route(value="/projects/:projectName/devices/:deviceName/dashboard", layout = MainLayout.class)
public class DeviceDashboard extends Div implements HasDynamicTitle, BeforeEnterObserver {

    Logger log = LoggerFactory.getLogger(DeviceDashboard.class);

    public final static String PROJECT_NAME_RP = "projectName";
    public final static String DEVICE_NAME_RP = "deviceName";

    private final TextField description = new TextField("description");
    private final TextField location = new TextField("location");

    private String projectName;
    private String deviceName;
    private Device device;

    private final IotService service;
    private final BeanValidationBinder<Device> binder;


    public DeviceDashboard(IotService service) {
        this.service = service;
        addClassName("dashboard");
        setSizeFull();

        VerticalLayout layout = new VerticalLayout();
        layout.addClassName("layout");
        Scroller scroller = new Scroller(layout);
        scroller.addClassName("scroller");
        scroller.setScrollDirection(Scroller.ScrollDirection.VERTICAL);
        scroller.getStyle().set("padding", "var(--lumo-space-m)");
        add(scroller);
        layout.add(createOverviewCard());

        binder = new BeanValidationBinder<>(Device.class);
        binder.bindInstanceFields(this);
    }

    @Override
    public void beforeEnter(BeforeEnterEvent event) {
        projectName = event.getRouteParameters().get(PROJECT_NAME_RP).orElse("");
        deviceName = event.getRouteParameters().get(DEVICE_NAME_RP).orElse("");
        binder.readBean(device);
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

    public Div createOverviewCard() {
        Div card = new Div();
        card.setClassName("dashboard-card");
        card.setMaxWidth("300em");
        H4 title = new H4("Overview");
        card.add(title);

        Optional<Device> deviceOptional = service.findDeviceByProjectNameAndName(projectName, deviceName);
        if (deviceOptional.isEmpty()) {
            Text empty = new Text("Device " + deviceName + " not found in project " + projectName+ ".");
            card.add(empty);
            return card;
        }

        Device device = deviceOptional.get();

        Div lastProvisioningRequest = new Div(new Text("Last provisioning request"), new Text(device.getLastProvisioningRequestAt().toString()));
        Div lastProvisioning = new Div(new Text("Last provisioning"), new Text(device.getLastProvisionedAt().toString()));
        Div lastSeen = new Div(new Text("Last seen"), new Text(device.getLastSeenAt().toString()));
        card.add(description, location, lastProvisioningRequest, lastProvisioning, lastSeen);

        return card;
    }

}
