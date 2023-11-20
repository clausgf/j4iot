package de.ostfalia.fbi.j4iot.views.device;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.value.ValueChangeMode;
import com.vaadin.flow.router.*;
import de.ostfalia.fbi.j4iot.data.entity.Device;
import de.ostfalia.fbi.j4iot.data.service.IotService;
import de.ostfalia.fbi.j4iot.views.MainLayout;
import jakarta.annotation.security.PermitAll;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.vaadin.lineawesome.LineAwesomeIcon;

import java.util.List;
import java.util.Optional;

@PermitAll
@Route(value="/projects/:projectName?/devices", layout = MainLayout.class)
public class DeviceList extends VerticalLayout implements HasDynamicTitle, BeforeEnterObserver {

    Logger log = LoggerFactory.getLogger(DeviceList.class);

    public final static String PROJECT_NAME_RP = "projectName";
    public final static String DEVICE_NAME_RP = "deviceName";

    Grid<Device> grid = new Grid<>(Device.class);
    TextField filterText = new TextField();

    String projectName;
    DeviceForm form;
    IotService service;

    public DeviceList(IotService service) {
        this.service = service;
        addClassName("list-view");
        setSizeFull();
        configureGrid();
        configureForm();
        add(getToolbar(), getContent());
        updateList();
        closeEditor();
    }

    private Component getContent() {
        HorizontalLayout content = new HorizontalLayout(grid, form);
        content.setFlexGrow(2, grid);
        content.setFlexGrow(1, form);
        content.addClassNames("content");
        content.setSizeFull();
        return content;
    }

    private void configureForm() {
        form = new DeviceForm();
        form.setWidth("30em");
        form.addSaveListener(this::saveDevice);
        form.addDeleteListener(this::deleteDevice);
        form.addCloseListener(e->closeEditor());
    }

    private void saveDevice(DeviceForm.SaveEvent event) {
        service.updateDevice(event.getDevice());
        updateList();
        closeEditor();
    }

    private void deleteDevice(DeviceForm.DeleteEvent event) {
        service.deleteDevice(event.getDevice());
        updateList();
        closeEditor();
    }

    private void configureGrid() {
        grid.addClassNames("grid");
        grid.setSizeFull();
        grid.setColumns();
        grid.addComponentColumn(item -> new Button(LineAwesomeIcon.LINK_SOLID.create(), click -> {
            RouteParameters rp = new RouteParameters(new RouteParam(PROJECT_NAME_RP, item.getProject().getName()), new RouteParam(DEVICE_NAME_RP, item.getName()));
            UI.getCurrent().navigate(DeviceDashboard.class, rp);
        })).setHeader("Devices");
        grid.addColumns("name", "location", "tags", "provisioningApproved", "lastProvisioningRequestAt", "lastProvisionedAt", "lastSeenAt");
        grid.getColumns().forEach(col -> col.setAutoWidth(true));

        grid.asSingleSelect().addValueChangeListener(event -> editDevice(event.getValue()));
    }

    private HorizontalLayout getToolbar() {
        filterText.setPlaceholder("Filter by name or tag");
        filterText.setClearButtonVisible(true);
        filterText.setValueChangeMode(ValueChangeMode.LAZY);
        filterText.addValueChangeListener(e -> updateList());

        Button addButton = new Button("Add Device");
        addButton.addClickListener(click -> addDevice());

        var toolbar = new HorizontalLayout(filterText, addButton);
        toolbar.addClassName("toolbar");
        return toolbar;
    }

    public void editDevice(Device device) {
        if (device == null) {
            closeEditor();
        } else {
            form.setDevice(device);
            form.setVisible(true);
            addClassName("editing");
        }
    }

    private void closeEditor() {
        form.setDevice(null);
        form.setVisible(false);
        removeClassName("editing");
    }

    private void addDevice() {
        grid.asSingleSelect().clear();
        editDevice(new Device());
    }

    private void updateList() {
        List<Device> devices;
        if ((projectName == null) || projectName.isEmpty()) {
            devices = service.searchAllDevices(filterText.getValue());
        } else {
            devices = service.searchAllDevicesByProjectName(projectName, filterText.getValue());
        }
        grid.setItems(devices);
    }

    @Override
    public void beforeEnter(BeforeEnterEvent event) {
        Optional<String> projectName = event.getRouteParameters().get(PROJECT_NAME_RP);
        this.projectName = projectName.orElse("");
        updateList();
    }

    @Override
    public String getPageTitle() {
        String title;
        if ((projectName == null) || projectName.isEmpty()) {
            title = "Devices in any project";
        } else {
            title = "Devices in " + projectName + " project";
        }
        return title;
    }

}
