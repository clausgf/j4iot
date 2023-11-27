package de.ostfalia.fbi.j4iot.views;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.confirmdialog.ConfirmDialog;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.value.ValueChangeMode;
import com.vaadin.flow.router.*;
import de.ostfalia.fbi.j4iot.data.entity.AbstractEntity;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Optional;

public abstract class GenericList<T extends AbstractEntity> extends VerticalLayout implements BeforeEnterObserver, AfterNavigationObserver {

    private Logger log = LoggerFactory.getLogger(GenericList.class);
    protected final Class<T> modelClass;
    protected RouteParameters routeParameters = null;
    protected TextField filterText = new TextField();
    protected Grid<T> grid;
    protected ConfirmDialog confirmDeleteDialog = createConfirmDeleteDialog();


    public GenericList(Class<T> modelClass) {
        this.modelClass = modelClass;
        this.grid = new Grid<>(modelClass);
        addClassName("generic-list");
        setSizeFull();
        configureGrid();
        add(createToolbar(), createContent());
    }

    protected HorizontalLayout createToolbar() {
        var toolbar = new HorizontalLayout();
        toolbar.addClassName("toolbar");

        filterText.setPlaceholder("Filter by name or tag");
        filterText.setClearButtonVisible(true);
        filterText.setValueChangeMode(ValueChangeMode.LAZY);
        filterText.addValueChangeListener(e -> updateItems());
        toolbar.add(filterText);

        Button refreshButton = new Button("Refresh");
        refreshButton.addClickListener(click -> updateItems());
        toolbar.add(refreshButton);

        Button addButton = new Button("Add");
        addButton.addClickListener( click -> addItem() );
        toolbar.add(addButton);

        return toolbar;
    }

    protected Component createContent() {
        HorizontalLayout content = new HorizontalLayout(grid);
        content.addClassNames("content");
        content.setSizeFull();
        //content.setFlexGrow(2, grid);
        content.add(grid);
        return content;
    }

    protected ConfirmDialog createConfirmDeleteDialog() {
        ConfirmDialog dialog = new ConfirmDialog();
        dialog.setHeader("Danger: Delete item");
        dialog.setText("Do you really want to delete the item?");
        dialog.setCancelable(true);
        dialog.setConfirmText("Delete");
        // TODO delete.addThemeVariants(ButtonVariant.LUMO_ERROR);
        return dialog;
    }

    protected void configureGrid() {
        grid.addComponentColumn(item -> {
                    Button b1 = new Button(new Icon(VaadinIcon.EDIT), click -> editItem(item) );
                    b1.setTooltipText("Edit list item");
                    Button b2 = new Button(new Icon(VaadinIcon.TRASH), click -> confirmDeleteItem(item) );
                    b2.setTooltipText("Delete list item");
                    return new HorizontalLayout(b1, b2);
                })
                .setAutoWidth(true).setFlexGrow(0);
        grid.addClassNames("grid");
        grid.setSizeFull();
        grid.getColumns().forEach(col -> col.setAutoWidth(true));
    }

    @Override
    public void beforeEnter(BeforeEnterEvent event) {
        routeParameters = event.getRouteParameters();
    }

    @Override
    public void afterNavigation(AfterNavigationEvent event) {
        updateItems();
    }

    @FunctionalInterface
    protected interface WithMainLayout { void execute(MainLayout m); }

    protected void withMainLayout(WithMainLayout toExecute) {
        Optional<Component> parent = getParent();
        if (parent.isPresent()) {
            if (parent.get() instanceof MainLayout m) {
                toExecute.execute(m);
            } else {
                log.error("withMainLayout: found a parent which is not a MainLayout");
            }
        } else {
            log.error("withMainLayout: parent not found - thanks, vaadin :-(");
        }
    }

    protected void confirmDeleteItem(T item) {
        confirmDeleteDialog.addCancelListener(event -> {
            Notification.show("Canceled item deletion");
        });
        confirmDeleteDialog.addConfirmListener(event -> {
            if (removeItem(item)) {
                Notification.show("Item deleted");
            } else {
                Notification.show("Could not delete the item");
            }
            updateItems();
        });
        confirmDeleteDialog.open();
    }

    protected abstract void addItem();

    protected abstract void editItem(T item);

    protected abstract boolean removeItem(T item);

    protected abstract void updateItems();
}

