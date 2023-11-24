package de.ostfalia.fbi.j4iot.views.device;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.Text;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.H4;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.orderedlayout.Scroller;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.binder.BeanValidationBinder;
import com.vaadin.flow.router.*;
import de.ostfalia.fbi.j4iot.data.entity.Device;
import de.ostfalia.fbi.j4iot.data.service.IotService;
import de.ostfalia.fbi.j4iot.views.MainLayout;
import jakarta.annotation.security.PermitAll;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Optional;

@PermitAll
@Route(value="/devices/:id/dashboard", layout = MainLayout.class)
public class DeviceDashboard extends Div implements HasDynamicTitle, BeforeEnterObserver {

    Logger log = LoggerFactory.getLogger(DeviceDashboard.class);

    public final static String ID_ROUTING_PARAMETER = "id";

    private final TextField description = new TextField("description");
    private final TextField location = new TextField("location");

    private Device item;

    private final IotService service;
    private final BeanValidationBinder<Device> binder;


    public static RouteParameters getRouteParametersWithDevice(Device device) {
        if (device != null) {
            return new RouteParameters(new RouteParam(ID_ROUTING_PARAMETER, device.getId()));
        } else {
            return RouteParameters.empty();
        }
    }

    public static void navigateToWithDevice(Device device) {
        assert device != null;
        RouteParameters rp = new RouteParameters(new RouteParam(ID_ROUTING_PARAMETER, device.getId()));
        UI.getCurrent().navigate(DeviceSettings.class, rp);
    }


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


    private void setMainLayoutDevice(Device device) {
        Optional<Component> parent = getParent();
        if (parent.isPresent()) {
            if (parent.get() instanceof MainLayout m) {
                m.setCurrentDevice(device);
            }
        } else {
            log.error("setMainLayoutDevice did not find a parent component!");
        }
    }

    public void populateForm(Device item) {
        this.item = item;
        binder.readBean(item);
        setMainLayoutDevice(item);


    }

    @Override
    public void beforeEnter(BeforeEnterEvent event) {
        Optional<Long> id = event.getRouteParameters().getLong(ID_ROUTING_PARAMETER);
        if (id.isPresent()) {
            Optional<Device> item = service.findDeviceById(id.get());
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

    @Override
    public String getPageTitle() {
        String title = "Dashboard for unknown";
        if (item != null) {
            title = "Dashboard for " + item.getName() + " in " + item.getProject().getName();
        }
        return title;
    }

    public Div createOverviewCard() {
        Div card = new Div();
        card.setClassName("dashboard-card");
        card.setMaxWidth("300em");
        H4 title = new H4("Overview");
        card.add(title);

        if (item == null) {
            Text empty = new Text("Device not found.");
            card.add(empty);
            return card;
        }

        Device device = item;

        Div lastProvisioningRequest = new Div(new Text("Last provisioning request"), new Text(device.getLastProvisioningRequestAt().toString()));
        Div lastProvisioning = new Div(new Text("Last provisioning"), new Text(device.getLastProvisionedAt().toString()));
        Div lastSeen = new Div(new Text("Last seen"), new Text(device.getLastSeenAt().toString()));
        card.add(description, location, lastProvisioningRequest, lastProvisioning, lastSeen);

        return card;
    }

}
