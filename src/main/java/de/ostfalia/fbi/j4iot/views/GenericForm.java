package de.ostfalia.fbi.j4iot.views;

import com.vaadin.flow.component.Key;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.notification.NotificationVariant;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.data.binder.BeanValidationBinder;
import de.ostfalia.fbi.j4iot.data.entity.AbstractEntity;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class GenericForm<T extends AbstractEntity> extends FormLayout {

    private Logger log = LoggerFactory.getLogger(GenericForm.class);
    protected final Class<T> modelClass;
    protected Button reset = new Button ("Reset");
    protected Button save = new Button("Save");

    protected T item;
    protected BeanValidationBinder<T> binder;

    public GenericForm(Class<T> modelClass) {
        this.modelClass = modelClass;
        addClassName("generic-form");
        binder = new BeanValidationBinder<>(modelClass);
        binder.bindInstanceFields(this);
    }

    public void populateForm(T item) {
        this.item = item;
        binder.readBean(item);
    }

    protected HorizontalLayout createButtonsLayout() {
        reset.addThemeVariants(ButtonVariant.LUMO_TERTIARY);
        save.addThemeVariants(ButtonVariant.LUMO_PRIMARY);

        reset.addClickShortcut(Key.ESCAPE);
        save.addClickShortcut(Key.ENTER);

        reset.addClickListener(event -> binder.readBean(item));
        save.addClickListener(event -> validateAndSave());

        binder.addStatusChangeListener(e -> save.setEnabled(binder.isValid()));
        return new HorizontalLayout(reset, save);
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

    protected abstract T save(T item);
}
