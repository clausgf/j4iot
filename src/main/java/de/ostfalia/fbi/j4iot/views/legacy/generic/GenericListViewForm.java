package de.ostfalia.fbi.j4iot.views.legacy.generic;

import com.vaadin.flow.component.ComponentEvent;
import com.vaadin.flow.component.ComponentEventListener;
import com.vaadin.flow.component.Key;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.data.binder.BeanValidationBinder;
import com.vaadin.flow.shared.Registration;
import de.ostfalia.fbi.j4iot.data.entity.AbstractEntity;
import de.ostfalia.fbi.j4iot.views.legacy.genericprojectlist.GenericProjectForm;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class GenericListViewForm<T extends AbstractEntity> extends FormLayout {
    Logger log = LoggerFactory.getLogger(GenericListView.class);
    final Class<T> modelClass;
    Button save = new Button("Save");
    Button delete = new Button("Delete");
    Button close = new Button ("Cancel");

    BeanValidationBinder<T> binder;

    public GenericListViewForm(Class<T> modelClass) {
        this.modelClass = modelClass;
        addClassName("generic-list-view-form");
        binder = new BeanValidationBinder<>(modelClass);
        binder.bindInstanceFields(this);
    }

    public void setItem(T item) {
        binder.setBean(item);
    }

    public T getItem() {
        return binder.getBean();
    }

    protected HorizontalLayout createButtonsLayout() {
        save.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        delete.addThemeVariants(ButtonVariant.LUMO_ERROR);
        close.addThemeVariants(ButtonVariant.LUMO_TERTIARY);

        save.addClickShortcut(Key.ENTER);
        close.addClickShortcut(Key.ESCAPE);

        save.addClickListener(event -> validateAndFireSaveEvent());
        delete.addClickListener(event -> fireEvent(new GenericProjectForm.DeleteEvent(this)));
        close.addClickListener(event -> fireEvent(new GenericProjectForm.CloseEvent(this)));

        binder.addStatusChangeListener(e -> save.setEnabled(binder.isValid()));
        return new HorizontalLayout(save, delete, close);
    }

    protected void validateAndFireSaveEvent() {
        if (binder.isValid()) {
            fireEvent(new SaveEvent(this));
        }
    }

    public static abstract class ItemFormEvent extends ComponentEvent<GenericListViewForm> {

        protected ItemFormEvent(GenericListViewForm source) {
            super(source, false);
        }

    }

    public T getItemFromItemFormEvent(ItemFormEvent event, Class<T> modelClass) {
        if (!modelClass.equals(this.modelClass)) {
            log.error("getItemFromItemFormEvent called with modelClass={}, expected {}", modelClass, this.modelClass);
            return null;
        }
        log.info("getItemFromItemFormEvent event_class={}", event.getClass());
        GenericListViewForm<T> form = (GenericListViewForm<T>)event.getSource();
        return form.getItem();
    }

    public static class SaveEvent extends ItemFormEvent {
        SaveEvent(GenericListViewForm source) {
            super(source);
        }
    }

    public static class DeleteEvent extends ItemFormEvent {
        DeleteEvent(GenericListViewForm source) {
            super(source);
        }
    }

    public static class CloseEvent extends ItemFormEvent {
        CloseEvent(GenericListViewForm source) {
            super(source);
        }
    }

    public Registration addSaveListener(ComponentEventListener<SaveEvent> listener) {
        return addListener(SaveEvent.class, listener);
    }

    public Registration addDeleteListener(ComponentEventListener<DeleteEvent> listener) {
        return addListener(DeleteEvent.class, listener);
    }

    public Registration addCloseListener(ComponentEventListener<CloseEvent> listener) {
        return addListener(CloseEvent.class, listener);
    }
}
