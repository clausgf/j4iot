package de.ostfalia.fbi.j4iot.views;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.Key;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.H3;
import com.vaadin.flow.component.html.H4;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.notification.NotificationVariant;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.data.binder.BeanValidationBinder;
import com.vaadin.flow.router.*;
import de.ostfalia.fbi.j4iot.data.entity.AbstractEntity;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Arrays;
import java.util.Optional;

public abstract class GenericForm<T extends AbstractEntity> extends Div implements BeforeEnterObserver, AfterNavigationObserver {

    // ***********************************************************************

    private final Logger log = LoggerFactory.getLogger(GenericForm.class);
    public final static String ID_ROUTING_PARAMETER = "id";
    protected final Class<T> modelClass;
    protected RouteParameters routeParameters = null;

    protected Button resetButton = new Button ("Reset");
    protected Button saveCreateButton = new Button("Save/Create");

    protected T item;
    protected BeanValidationBinder<T> binder;

    // ***********************************************************************

    public GenericForm(Class<T> modelClass) {
        this.modelClass = modelClass;
        addClassName("generic-form");

        binder = new BeanValidationBinder<>(modelClass);
        binder.setBean(item);
        binder.addStatusChangeListener(e -> saveCreateButton.setEnabled(binder.isValid()));
    }

    protected void addHeader(String title) {
        H3 header = new H3(title);
        header.addClassName("generic-form-header");
        add(header);
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
        resetButton.addClickListener(event -> reset());
        buttonLayout.add(resetButton);

        saveCreateButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        saveCreateButton.addClickShortcut(Key.ENTER);
        saveCreateButton.addClickListener(event -> saveCreate());
        buttonLayout.add(saveCreateButton);

        buttonLayout.addClassName("generic-form-footer");
        add(buttonLayout);
    }

    // ***********************************************************************

    @Override
    public void beforeEnter(BeforeEnterEvent event) {
        routeParameters = event.getRouteParameters();
        item = null;
        Optional<Long> id = routeParameters.getLong(ID_ROUTING_PARAMETER);
        if (id.isPresent()) {
            item = load(id.get());
            if (item == null) {
                Notification.show("The requested item was not found, id=" + id.get());
            }
        }
    }

    @Override
    public void afterNavigation(AfterNavigationEvent event) {
        populateForm(item);
    }

    @FunctionalInterface
    protected interface WithMainLayout { void execute(MainLayout m); }

    protected void withMainLayout(WithMainLayout toExecute) {
        Optional<Component> parent = getParent();
        if (parent.isPresent()) {
            if (parent.get() instanceof MainLayout m) {
                toExecute.execute(m);
                m.updatePageTitle();
            } else {
                log.error("withMainLayout: found a parent which is not a MainLayout");
            }
        } else {
            log.error("withMainLayout: parent not found");
        }
    }

    protected void saveCreate() {
        try {
            if (item == null) {
                item = modelClass.newInstance();
            }
            if (binder.writeBeanIfValid(item)) {
                try {
                    item = save();
                    if (item != null) {
                        Notification.show("Item saved");
                        populateForm(item);
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

    protected void populateForm(T item) {
        this.item = item;
        binder.setBean(item);
        saveCreateButton.setText(item == null ? "Create" : "Save");
    }

    protected void reset() {
        //binder.readBean(item);
        if (item != null) {
            item = load(item.getId());
        }
        populateForm(item);
    }

    protected abstract T load(long id);

    protected abstract T save();

    // ***********************************************************************

}
