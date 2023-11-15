package de.ostfalia.fbi.j4iot.views.devicelist;

import com.vaadin.flow.component.ComponentEvent;
import com.vaadin.flow.component.ComponentEventListener;
import com.vaadin.flow.component.Key;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.checkbox.Checkbox;
import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.binder.BeanValidationBinder;
import com.vaadin.flow.shared.Registration;
import de.ostfalia.fbi.j4iot.data.entity.Device;

public class DeviceForm extends FormLayout {
    TextField name = new TextField("Device name");
    TextField location = new TextField("Localtion");
    TextField description = new TextField("Description");
    Checkbox active = new Checkbox("Active");
    Checkbox autocreateDevices = new Checkbox("Autocreate devices");
    Checkbox provisioningAutoapproval = new Checkbox("Provisioning autoapproval");

    Button save = new Button("Save");
    Button delete = new Button("Delete");
    Button close = new Button ("Cancel");

    BeanValidationBinder<Device> binder = new BeanValidationBinder<>(Device.class);

    public DeviceForm() {
        addClassName("device-form");
        binder.bindInstanceFields(this);

        add(name, location, description, active, autocreateDevices, provisioningAutoapproval, createButtonsLayout());
    }

    public void setDevice(Device device) {
        binder.setBean(device);
    }

    private HorizontalLayout createButtonsLayout() {
        save.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        delete.addThemeVariants(ButtonVariant.LUMO_ERROR);
        close.addThemeVariants(ButtonVariant.LUMO_TERTIARY);

        save.addClickShortcut(Key.ENTER);
        close.addClickShortcut(Key.ESCAPE);

        save.addClickListener(event -> validateAndSave());
        delete.addClickListener(event -> fireEvent(new DeleteEvent(this, binder.getBean())));
        close.addClickListener(event -> fireEvent(new CloseEvent(this)));

        binder.addStatusChangeListener(e -> save.setEnabled(binder.isValid()));
        return new HorizontalLayout(save, delete, close);
    }

    private void validateAndSave() {
        if (binder.isValid()) {
            fireEvent(new SaveEvent(this, binder.getBean()));
        }
    }

    public static abstract class DeviceFormEvent extends ComponentEvent<DeviceForm> {
        private final Device device;

        protected DeviceFormEvent(DeviceForm source, Device device) {
            super(source, false);
            this.device = device;
        }

        public Device getDevice() {
            return device;
        }
    }

    public static class SaveEvent extends DeviceFormEvent {
        SaveEvent(DeviceForm source, Device device) {
            super(source, device);
        }
    }

    public static class DeleteEvent extends DeviceFormEvent {
        DeleteEvent(DeviceForm source, Device device) {
            super(source, device);
        }
    }

    public static class CloseEvent extends DeviceFormEvent {
        CloseEvent(DeviceForm source) {
            super(source, null);
        }
    }

    public Registration addDeleteListener(ComponentEventListener<DeleteEvent> listener) {
        return addListener(DeleteEvent.class, listener);
    }

    public Registration addSaveListener(ComponentEventListener<SaveEvent> listener) {
        return addListener(SaveEvent.class, listener);
    }

    public Registration addCloseListener(ComponentEventListener<CloseEvent> listener) {
        return addListener(CloseEvent.class, listener);
    }
}
