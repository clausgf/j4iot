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
import de.ostfalia.fbi.j4iot.data.service.DeviceService;
import de.ostfalia.fbi.j4iot.views.MainLayout;
import jakarta.annotation.security.PermitAll;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.Assert;

import java.util.Optional;

@PermitAll
@Route(value="/devices/:id/dashboard", layout = MainLayout.class)
public class DeviceDashboard extends Div implements HasDynamicTitle, BeforeEnterObserver, AfterNavigationObserver {

    // ***********************************************************************

    private final Logger log = LoggerFactory.getLogger(DeviceDashboard.class);
    public final static String ID_ROUTING_PARAMETER = "id";
    protected RouteParameters routeParameters = null;

    private final TextField description = new TextField("description");
    private final TextField location = new TextField("location");

    private Device item;
    private final BeanValidationBinder<Device> binder;

    private final DeviceService service;

    private final Text lastSeenText = new Text("");
    private final Text lastProvisioningText = new Text("");
    private final Text lastProvisiongRequestText = new Text("");

    // ***********************************************************************

    public static RouteParameters getRouteParameters(Device device) {
        if (device != null) {
            return new RouteParameters(new RouteParam(ID_ROUTING_PARAMETER, device.getId()));
        } else {
            return RouteParameters.empty();
        }
    }

    public static void navigateTo(Device device) {
        Assert.notNull(device, "NavigateTo requires a device");
        RouteParameters rp = getRouteParameters(device);
        UI.getCurrent().navigate(DeviceDashboard.class, rp);
    }

    // ***********************************************************************

    public DeviceDashboard(DeviceService service) {
        this.service = service;
        addClassName("dashboard");
        setSizeFull();

        VerticalLayout layout = new VerticalLayout();
        layout.addClassName("layout");
        Scroller scroller = new Scroller(layout);
        scroller.addClassName("scroller");
        scroller.setScrollDirection(Scroller.ScrollDirection.VERTICAL);
        scroller.getStyle().set("padding", "var(--lumo-space-m)");  // TODO move to css
        add(scroller);
        layout.add(createOverviewCard());

        binder = new BeanValidationBinder<>(Device.class);
        binder.bindInstanceFields(this);
    }

    // ***********************************************************************

    public Div createOverviewCard() {
        Div card = new Div();
        card.setClassName("dashboard-card");
        card.setMaxWidth("300em");
        H4 title = new H4("Overview");
        card.add(title);

        Div lastProvisioningRequest = new Div(new Text("Last provisioning request: "), lastProvisiongRequestText);
        Div lastProvisioning = new Div(new Text("Last provisioning: "), lastProvisioningText);
        Div lastSeen = new Div(new Text("Last seen: "), lastSeenText);
        card.add(description, location, lastProvisioningRequest, lastProvisioning, lastSeen);

        return card;
    }

    // ***********************************************************************

    @Override
    public void beforeEnter(BeforeEnterEvent event) {
        routeParameters = event.getRouteParameters();
        item = null;
        Optional<Long> id = routeParameters.getLong(ID_ROUTING_PARAMETER);
        if (id.isPresent()) {
            item = service.findByAuthAndId(id.get()).orElse(null);
            lastSeenText.setText(item.getLastSeenAt() != null ? item.getLastSeenAt().toString() : "unknown");
            lastProvisioningText.setText(item.getLastProvisionedAt() != null ? item.getLastProvisionedAt().toString() : "unknown");
            lastProvisiongRequestText.setText(item.getLastProvisioningRequestAt() != null ? item.getLastProvisioningRequestAt().toString() : "unknown");
        } else {
            log.error("Invalid route parameter, expected id");
        }
        if ( item == null ) {
            Notification.show("The requested item was not found, id=" + id.get());
        }
    }

    @Override
    public void afterNavigation(AfterNavigationEvent event) {
        Optional<Component> parent = getParent();
        if (parent.isPresent()) {
            if (parent.get() instanceof MainLayout m) {
                m.setCurrentDevice(item);
                m.updatePageTitle();
            } else {
                log.error("setMainLayoutDevice: found a parent which is not a MainLayout");
            }
        } else {
            log.error("setMainLayoutDevice parent not found");
        }

        populateForm();
    }

    @Override
    public String getPageTitle() {
        return DeviceUtil.getPageTitle("Dashboard", item);
    }

    // ***********************************************************************

    public void populateForm() {
        binder.setBean(item);
    }

    // ***********************************************************************

}
