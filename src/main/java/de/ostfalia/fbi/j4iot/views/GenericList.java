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
import de.ostfalia.fbi.j4iot.data.entity.AbstractEntity;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class GenericList<T extends AbstractEntity> extends VerticalLayout {

    private Logger log = LoggerFactory.getLogger(GenericList.class);
    protected final Class<T> modelClass;
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
        addButton.addClickListener(click -> {
                    if (addItem()) {
                        Notification.show("Item added");
                    } else {
                        Notification.show("Could not add item");
                    }
                });
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
        grid.addComponentColumn(item ->
                new Button(new Icon(VaadinIcon.EDIT), click -> {
                    editItem(item);
                }))
                .setTooltipGenerator(item -> "Edit list item")
                .setAutoWidth(true);
        grid.addComponentColumn(item ->
                new Button(new Icon(VaadinIcon.TRASH), click -> {
                    confirmDeleteItem(item);
                }))
                .setTooltipGenerator(item -> "Delete list item")
                .setAutoWidth(true);
        grid.addClassNames("grid");
        grid.setSizeFull();
        grid.getColumns().forEach(col -> col.setAutoWidth(true));
    }

    protected abstract boolean addItem();

    protected abstract void editItem(T item);

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

    protected abstract boolean removeItem(T item);

    abstract protected void updateItems();
}
