package de.ostfalia.fbi.j4iot.views.device;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.Key;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.checkbox.Checkbox;
import com.vaadin.flow.component.datetimepicker.DateTimePicker;
import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.grid.GridVariant;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.H3;
import com.vaadin.flow.component.html.H4;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.notification.NotificationVariant;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.textfield.TextArea;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.binder.BeanValidationBinder;
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
public class DeviceSettings extends GenericForm<Device> implements BeforeEnterObserver, AfterNavigationObserver {

    private Logger log = LoggerFactory.getLogger(DeviceSettings.class);
    protected final Class<Device> modelClass;
    protected RouteParameters routeParameters = null;

    protected Button resetButton = new Button ("Reset");
    protected Button saveButton = new Button("Save");

    protected Device item;
    protected BeanValidationBinder<Device> binder;

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
        this.modelClass = Device.class;
        addClassName("generic-form");
        binder = new BeanValidationBinder<>(modelClass);
        binder.bindInstanceFields(this);

        this.service = service;

        addClassName("generic-form");
        addHeader("Device");
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

    protected void addHeader(String title) {
        H3 header = new H3(title);
        header.addClassName("generic-form-header");
        add(header);
        //setColspan(header, 2);
    }

    protected FormLayout addForm() {
        Div main = new Div();
        main.addClassName("generic-form-main");
        add(main);
        FormLayout formLayout= new FormLayout();
        main.add(formLayout);
        return formLayout;
    }

    protected void addSectionTo(FormLayout form, String title, Component... components) {
        if (title != null && !title.isEmpty()) {
            H4 sectionTitle = new H4(title);
            form.addClassName("generic-form-section-title");
            form.add(sectionTitle);
            form.setColspan(sectionTitle, 2);
        }
        Arrays.asList(components).forEach(e -> {
            e.addClassName("generic-form-element");
        });
        form.add(components);
    }

    protected void addFooter() {
        HorizontalLayout buttonLayout = new HorizontalLayout();

        resetButton.addThemeVariants(ButtonVariant.LUMO_TERTIARY);
        resetButton.addClickShortcut(Key.ESCAPE);
        resetButton.addClickListener(event -> binder.readBean(item));
        buttonLayout.add(resetButton);

        saveButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        saveButton.addClickShortcut(Key.ENTER);
        saveButton.addClickListener(event -> validateAndSave());
        binder.addStatusChangeListener(e -> saveButton.setEnabled(binder.isValid()));
        buttonLayout.add(saveButton);

        buttonLayout.addClassName("generic-form-footer");
        add(buttonLayout);
    }

    public void populateForm(Device item) {
        this.item = item;
        binder.readBean(item);
        if (item == null) {
            deviceTokens.setItems(new LinkedList<>());
        } else {
            deviceTokens.setItems(item.getDeviceTokens());
        }
        withMainLayout(m -> m.setCurrentDevice(item));
    }

    protected void validateAndSave() {
        if (binder.writeBeanIfValid(item)) {
            try {
                item = save(item);
                if (item != null) {
                    Notification.show("Item saved");
                    populateForm(item);
                } else {
                    Notification.show("Failure saving item").addThemeVariants(NotificationVariant.LUMO_ERROR);
                }
            } catch (Exception e) {
                log.error("Error saving item: {}", e.getMessage());
                Notification.show("Failure saving item: " + e.getMessage()).addThemeVariants(NotificationVariant.LUMO_ERROR);
            }
        } else {
            Notification.show("Failure saving item: Validation failed");
        }
    }

    protected Device save(Device item) {
        return service.updateDevice(item);
    }

    protected Optional<Device> load(Long id) {
        return service.findDeviceById(id);
    }

    // BaseClass
    //@Override
    public void _beforeEnter(BeforeEnterEvent event) {
        routeParameters = event.getRouteParameters();
    }

    // BaseClass
    @FunctionalInterface
    protected interface WithMainLayout { void execute(MainLayout m); }

    // BaseClass
    protected void withMainLayout(WithMainLayout toExecute) {
        Optional<Component> parent = getParent();
        if (parent.isPresent()) {
            if (parent.get() instanceof MainLayout m) {
                toExecute.execute(m);
            } else {
                log.error("withMainLayout: found a parent which is not a MainLayout");
            }
        } else {
            log.error("withMainLayout: parent not found");
        }
    }

    @Override
    public void beforeEnter(BeforeEnterEvent event) {
        _beforeEnter(event);
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

    @Override
    public void afterNavigation(AfterNavigationEvent event) {
        populateForm(item);
    }
}
