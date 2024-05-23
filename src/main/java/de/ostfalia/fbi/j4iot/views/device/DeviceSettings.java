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
import com.vaadin.flow.component.notification.NotificationVariant;
import com.vaadin.flow.component.textfield.TextArea;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.router.*;
import de.ostfalia.fbi.j4iot.data.entity.Device;
import de.ostfalia.fbi.j4iot.data.entity.DeviceToken;
import de.ostfalia.fbi.j4iot.data.entity.Project;
import de.ostfalia.fbi.j4iot.data.service.DeviceService;
import de.ostfalia.fbi.j4iot.data.service.ProjectService;
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
@Route(value="/projects/:projectId/devices/:id?/settings", layout = MainLayout.class)
public class DeviceSettings extends GenericForm<Device> implements HasDynamicTitle, BeforeEnterObserver {

    private Logger log = LoggerFactory.getLogger(DeviceSettings.class);
    public final static String PROJECT_ID_ROUTING_PARAMETER = "projectId";
    ProjectService projectService;
    DeviceService deviceService;
    Project project;

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

    Grid<DeviceToken> deviceTokenGrid = new Grid<>(DeviceToken.class);
    Button addDeviceTokenButton = new Button("Add device token");


    public static RouteParameters getRouteParameters(Project project, Device device) {
        if ((project != null) && (device != null)) {
            return new RouteParameters(
                new RouteParam(PROJECT_ID_ROUTING_PARAMETER, project.getId()),
                new RouteParam(ID_ROUTING_PARAMETER, device.getId()));
        } else if ((project != null) && (device == null)) {
            return new RouteParameters(new RouteParam(PROJECT_ID_ROUTING_PARAMETER, project.getId()));
        } else {
            return RouteParameters.empty();
        }
    }

    public static void navigateTo(Project project, Device device) {
        RouteParameters rp = getRouteParameters(project, device);
        UI.getCurrent().navigate(DeviceSettings.class, rp);
    }


    public DeviceSettings(ProjectService projectService, DeviceService deviceService) {
        super(Device.class);
        this.projectService = projectService;
        this.deviceService = deviceService;
        binder.bindInstanceFields(this);

        //addHeader("Device");
        FormLayout main = addForm();
        Arrays.asList(createdAt, updatedAt, lastProvisioningRequestAt, lastProvisionedAt, lastSeenAt).forEach(e -> e.setReadOnly(true));
        main.setColspan(deviceTokenGrid, 2);
        main.setColspan(addDeviceTokenButton, 2);
        Arrays.asList(name, description, tags, location).forEach(e -> main.setColspan(e, 2));
        addSectionTo(main, "General settings", name, description, tags, createdAt, updatedAt);
        addSectionTo(main, "Specific settings", location, lastSeenAt, provisioningApproved, lastProvisioningRequestAt, lastProvisionedAt);

        deviceTokenGrid.setColumns();
        deviceTokenGrid.addColumn("token")
                .setTooltipGenerator(DeviceToken::getToken)
                .setHeader("Token");
        deviceTokenGrid.addColumn(new InstantRenderer<>(DeviceToken::getExpiresAt))
                .setTooltipGenerator(item -> item.getExpiresAt().toString())
                .setHeader("Expires");
        deviceTokenGrid.addComponentColumn(item ->
                new Button(new Icon(VaadinIcon.CLIPBOARD), click -> {
                    ClientsideClipboard.writeToClipboard(item.getToken());
                }))
                .setTooltipGenerator(item -> "Copy token to clipboard")
                .setAutoWidth(true).setFlexGrow(0);
        deviceTokenGrid.addComponentColumn(item ->
                new Button(new Icon(VaadinIcon.TRASH),click -> {
                    item.getDevice().getDeviceTokens().remove(item);
                    deviceTokenGrid.setItems(item.getDevice().getDeviceTokens());
                }))
                .setTooltipGenerator(item -> "Delete token")
                .setAutoWidth(true).setFlexGrow(0);
        deviceTokenGrid.setAllRowsVisible(true);
        deviceTokenGrid.addThemeVariants(GridVariant.LUMO_COMPACT);
        addDeviceTokenButton.addClickListener(event -> {
            if (binder.getBean() != null) {
                Device device = binder.getBean();
                deviceService.addNewDeviceToken(device);
                deviceTokenGrid.setItems(device.getDeviceTokens());
            }
        });
        addSectionTo(main, "Device Tokens", deviceTokenGrid, addDeviceTokenButton);

        addFooter();
    }

    @Override
    public void beforeEnter(BeforeEnterEvent event) {
        super.beforeEnter(event);
        project = null;
        Optional<Long> projectId = routeParameters.getLong(PROJECT_ID_ROUTING_PARAMETER);
        if (projectId.isPresent()) {
            project = projectService.findByAuthAndId(projectId.get()).orElse(null);
        }
        if (project == null) {
            String msg = "No access to project or not found id=" + projectId.orElse(null);
            log.error(msg);
            Notification.show(msg);
        }
    }

    @Override
    public String getPageTitle() {
        return DeviceUtil.getPageTitle("Device settings", item);
    }

    @Override
    protected void saveCreate() {
        try {
            if (item == null) {
                item = new Device();
                item.setProject(project);
            }
            super.saveCreate();
        } catch (Exception e) {
            String msg = "Exception creating/saving item instance class=" + modelClass.getName() + ": " + e.getMessage();
            log.error(msg);
            Notification.show(msg).addThemeVariants(NotificationVariant.LUMO_ERROR);
        }
    }

    @Override
    protected void populateForm(Device item) {
        super.populateForm(item);
        name.setReadOnly( item != null );
        if (item == null) {
            deviceTokenGrid.setItems(new LinkedList<>());
        } else {
            deviceTokenGrid.setItems(item.getDeviceTokens());
        }
        withMainLayout(m -> m.setCurrentDevice(item));
    }

    @Override
    protected Device load(long id) {
        return deviceService.findByUserAuthAndId(id).orElse(null);
    }

    @Override
    protected Device save() {
        return deviceService.updateOrCreate(item);
    }

}
