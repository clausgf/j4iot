package de.ostfalia.fbi.j4iot.views.devicelist;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.value.ValueChangeMode;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import de.ostfalia.fbi.j4iot.data.entity.Device;
import de.ostfalia.fbi.j4iot.data.service.IotService;
import de.ostfalia.fbi.j4iot.views.MainLayout;
import jakarta.annotation.security.PermitAll;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@PermitAll
@Route(value="/devices", layout = MainLayout.class)
@PageTitle("Devices")
public class DeviceListView extends VerticalLayout {

    Logger log = LoggerFactory.getLogger(DeviceListView.class);
    Grid<Device> grid = new Grid<>(Device.class);
    TextField filterText = new TextField();

    DeviceForm form;
    IotService service;

    public DeviceListView(IotService service) {
        this.service = service;
        addClassName("device-list-view");
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
        form.setWidth("25em");
        form.addSaveListener(this::saveDevice);
        form.addDeleteListener(this::deleteDevice);
        form.addCloseListener(e->closeEditor());
    }

    private void saveDevice(DeviceForm.SaveEvent event) {
        service.saveDevice(event.getDevice());
        updateList();
        closeEditor();
    }

    private void deleteDevice(DeviceForm.DeleteEvent event) {
        service.deleteDevice(event.getDevice());
        updateList();
        closeEditor();
    }

    private Span createTagBadge(String name, String color) {
        String theme;
        switch (color) {
            case "primary":
                theme = "badge primary pill";
                break;
            case "success":
                theme = "badge success primary pill";
                break;
            case "error":
                theme = "badge error primary pill";
                break;
            default:
                theme = "badge contrast primary pill";
                break;
        }
        Span badge = new Span(name);
        badge.getElement().getThemeList().add(theme);
        return badge;
    }

    private void configureGrid() {
        grid.addClassNames("device-grid");
        grid.setSizeFull();
        grid.setColumns("name", "location", "tags", "provisioningApproved", "lastProvisioningRequestAt", "lastProvisionedAt", "lastSeenAt");
        // TODO add column project.name (ganz vorne)
        // TODO add column renderer for Tags
        grid.getColumns().forEach(col -> col.setAutoWidth(true));

        grid.asSingleSelect().addValueChangeListener(event -> editDevice(event.getValue()));
    }

    private HorizontalLayout getToolbar() {
        filterText.setPlaceholder("Filter by name or tag");
        filterText.setClearButtonVisible(true);
        filterText.setValueChangeMode(ValueChangeMode.LAZY);
        filterText.addValueChangeListener(e -> updateList());

        Button addDeviceButton = new Button("Add Device");
        addDeviceButton.addClickListener(click -> addDevice());

        var toolbar = new HorizontalLayout(filterText, addDeviceButton);
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
        grid.setItems(service.findAllDevices(filterText.getValue()));
    }
}
