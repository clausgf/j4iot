package de.ostfalia.fbi.j4iot.views.device;

import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.checkbox.Checkbox;
import com.vaadin.flow.component.datetimepicker.DateTimePicker;
import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.grid.GridVariant;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.textfield.TextArea;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.router.*;
import de.ostfalia.fbi.j4iot.data.entity.Device;
import de.ostfalia.fbi.j4iot.data.entity.DeviceToken;
import de.ostfalia.fbi.j4iot.data.service.IotService;
import de.ostfalia.fbi.j4iot.views.GenericForm;
import de.ostfalia.fbi.j4iot.views.InstantRenderer;
import de.ostfalia.fbi.j4iot.views.MainLayout;
import jakarta.annotation.security.PermitAll;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.vaadin.addon.stefan.clipboard.ClientsideClipboard;

import java.util.Arrays;
import java.util.LinkedList;
import java.util.Optional;

@PermitAll
@PageTitle("Device settings")
@Route(value="/devices/:id/settings", layout = MainLayout.class)
public class DeviceSettings extends GenericForm<Device> implements BeforeEnterObserver {

    private Logger log = LoggerFactory.getLogger(DeviceSettings.class);
    public final static String ID_ROUTING_PARAMETER = "id";
    IotService service;

    TextField name = new TextField("Device name");
    TextArea description = new TextArea("Description");
    TextField location = new TextField("Location");
    TextField tags = new TextField("Tags");
    DateTimePicker createdAt = new DateTimePicker("Created at");
    DateTimePicker updatedAt = new DateTimePicker("Updated at");
    Checkbox provisioningApproved = new Checkbox("Provisioning approved");
    DateTimePicker lastProvisioningRequestAt = new DateTimePicker("Last provisioning request at");
    DateTimePicker lastProvisionedAt = new DateTimePicker("Last provisioned at");
    DateTimePicker lastSeenAt = new DateTimePicker("Last seen at");

    Grid<DeviceToken> deviceTokens = new Grid<>(DeviceToken.class);
    Button addDeviceTokenButton = new Button("Add device token");


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


    public DeviceSettings(IotService service) {
        super(Device.class);
        binder.bindInstanceFields(this);
        this.service = service;

        //addHeader("Device");
        FormLayout main = addForm();
        Arrays.asList(createdAt, updatedAt, lastProvisioningRequestAt, lastProvisionedAt, lastSeenAt).forEach(e -> e.setReadOnly(true));
        Arrays.asList(name, description, tags, location).forEach(e -> main.setColspan(e, 2));
        main.setColspan(deviceTokens, 2);
        main.setColspan(addDeviceTokenButton, 2);
        addSectionTo(main, "General settings", name, description, tags, createdAt, updatedAt);
        addSectionTo(main, "Specific settings", location, lastSeenAt, provisioningApproved, lastProvisioningRequestAt, lastProvisionedAt);

        deviceTokens.setColumns();
        deviceTokens.addColumn("token")
                .setTooltipGenerator(DeviceToken::getToken)
                .setHeader("Token");
        deviceTokens.addColumn(new InstantRenderer<>(DeviceToken::getExpiresAt))
                .setTooltipGenerator(item -> item.getExpiresAt().toString())
                .setHeader("Expires");
        deviceTokens.addComponentColumn(item ->
                new Button(new Icon(VaadinIcon.CLIPBOARD), click -> {
                    ClientsideClipboard.writeToClipboard(item.getToken());
                }))
                .setTooltipGenerator(item -> "Copy token to clipboard")
                .setAutoWidth(true).setFlexGrow(0);
        deviceTokens.addComponentColumn(item ->
                new Button(new Icon(VaadinIcon.TRASH),click -> {
                    item.getDevice().getDeviceTokens().remove(item);
                    deviceTokens.setItems(item.getDevice().getDeviceTokens());
                }))
                .setTooltipGenerator(item -> "Delete token")
                .setAutoWidth(true).setFlexGrow(0);
        deviceTokens.setAllRowsVisible(true);
        deviceTokens.addThemeVariants(GridVariant.LUMO_COMPACT);
        addDeviceTokenButton.addClickListener(event -> {
            if (binder.getBean() != null) {
                Device device = binder.getBean();
                service.addDeviceToken(device);
                deviceTokens.setItems(device.getDeviceTokens());
            }
        });
        addSectionTo(main, "Device Tokens", deviceTokens, addDeviceTokenButton);

        addFooter();
    }

    @Override
    protected void populateForm(Device item) {
        super.populateForm(item);
        if (item == null) {
            deviceTokens.setItems(new LinkedList<>());
        } else {
            deviceTokens.setItems(item.getDeviceTokens());
        }
        withMainLayout(m -> m.setCurrentDevice(item));
    }

    @Override
    protected Device save(Device item) {
        return service.updateDevice(item);
    }

    @Override
    protected Optional<Device> load(Long id) {
        return service.findDeviceById(id);
    }

    @Override
    public void beforeEnter(BeforeEnterEvent event) {
        super.beforeEnter(event);
        item = null;
        Optional<Long> id = routeParameters.getLong(ID_ROUTING_PARAMETER);
        if (id.isPresent()) {
            item = load(id.get()).orElse(null);
            if (item == null) {
                Notification.show("The requested item was not found, id=" + id.get());
            }
        } else {
            String msg = "Invalid route parameter, expected projectId";
            Notification.show(msg);
            log.error(msg);
        }
    }

}
