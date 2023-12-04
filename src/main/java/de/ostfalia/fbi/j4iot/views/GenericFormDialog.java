package de.ostfalia.fbi.j4iot.views;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.Key;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.H4;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.notification.NotificationVariant;
import com.vaadin.flow.data.binder.BeanValidationBinder;
import de.ostfalia.fbi.j4iot.data.entity.AbstractEntity;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Arrays;

public abstract class GenericFormDialog<T extends AbstractEntity> {

    // ***********************************************************************

    private final Logger log = LoggerFactory.getLogger(GenericFormDialog.class);
    protected final Class<T> modelClass;
    protected String title;
    protected onSaveCreateListener<T> onSaveCreateListener;
    protected boolean isCreateMode;

    protected final Dialog dialog = new Dialog();
    protected Div main = new Div();
    protected FormLayout form = new FormLayout();
    protected Button cancelButton = new Button ("Cancel");
    protected Button saveCreateButton = new Button("Save/Create");

    protected BeanValidationBinder<T> binder;

    // ***********************************************************************

    @FunctionalInterface
    public interface onSaveCreateListener<T> {
        T onSaveCreate(boolean isCreateMode, T item);
    }

    // ***********************************************************************

    public GenericFormDialog(Class<T> modelClass, String title, onSaveCreateListener<T> onSaveCreateListener) {
        this.modelClass = modelClass;
        this.title = title;
        this.onSaveCreateListener = onSaveCreateListener;

        binder = new BeanValidationBinder<>(modelClass);
        // binder.bindInstanceFields(this); // unfortunately, this does not work in the base class
        //binder.setBean(item);
        binder.addStatusChangeListener(e -> saveCreateButton.setEnabled(binder.isValid()));

        dialog.addClassName("generic-form-dialog");

        // setup header
        if (title == null) {
            dialog.setHeaderTitle("Create/edit " + modelClass.getName());
        } else {
            dialog.setHeaderTitle(title);
        }

        // setup main
        main.addClassName("generic-form-main");
        main.add(form);
        dialog.add(main);
        // dialog.addThemeVariants(DialogVariant.LUMO_NO_PADDING);

        // setup footer
        cancelButton.addThemeVariants(ButtonVariant.LUMO_TERTIARY);
        cancelButton.addClickShortcut(Key.ESCAPE);
        cancelButton.addClickListener(event -> cancel());
        saveCreateButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        saveCreateButton.addClickShortcut(Key.ENTER);
        saveCreateButton.addClickListener(event -> saveCreate());
        // deleteButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY, ButtonVariant.LUMO_ERROR);
        // deleteButton.getStyle().set("margin-right", "auto");
        dialog.getFooter().add(cancelButton, saveCreateButton);
    }

    protected void addSection(String title, Component... components) {
        if (title != null && !title.isEmpty()) {
            H4 sectionTitle = new H4(title);
            sectionTitle.addClassName("generic-form-section-title");
            form.add(sectionTitle);
            form.setColspan(sectionTitle, 2);
        }
        Arrays.asList(components).forEach(e -> {
            e.addClassName("generic-form-element");
        });
        form.add(components);
    }

    // ***********************************************************************

    public void open(boolean isCreateMode, T item) {
        this.isCreateMode = isCreateMode;
        binder.setBean(item);
        saveCreateButton.setText(isCreateMode ? "Create" : "Save");
        dialog.open();
    }

    public void openForCreate(T item) {
        open(true, item);
    }

    public void openForEdit(T item) {
        open(false, item);
    }

    // ***********************************************************************

    protected  void cancel() {
        dialog.close();
    }

    // ***********************************************************************

    protected void saveCreate() {
        try {
            T item = binder.getBean();
            if (item == null) {
                item = modelClass.newInstance();
            }
            if (binder.writeBeanIfValid(item)) {
                try {
                    item = onSaveCreateListener.onSaveCreate(isCreateMode, item);
                    if (item != null) {
                        //Notification.show("Item saved").addThemeVariants(NotificationVariant.LUMO_SUCCESS);
                        dialog.close();
                    } else {
                        Notification.show("Failure saving item").addThemeVariants(NotificationVariant.LUMO_ERROR);
                    }
                } catch (Exception e) {
                    String msg = "Failure saving item: " + e.getMessage();
                    log.error(msg);
                    Notification.show(msg).addThemeVariants(NotificationVariant.LUMO_ERROR);
                }
            } else {
                Notification.show("Failure saving item: Validation failed").addThemeVariants(NotificationVariant.LUMO_ERROR);
            }
        } catch (Exception e) {
            String msg = "Exception creating item instance class=" + modelClass.getName() + ": " + e.getMessage();
            log.error(msg);
            Notification.show(msg).addThemeVariants(NotificationVariant.LUMO_ERROR);
        }
    }

    // ***********************************************************************

}
