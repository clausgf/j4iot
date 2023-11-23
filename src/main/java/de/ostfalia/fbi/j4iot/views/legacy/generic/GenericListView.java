package de.ostfalia.fbi.j4iot.views.legacy.generic;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.value.ValueChangeMode;
import de.ostfalia.fbi.j4iot.data.entity.AbstractEntity;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class GenericListView<T extends AbstractEntity> extends VerticalLayout {
    Logger log = LoggerFactory.getLogger(GenericListView.class);
    protected final Class<T> modelClass;
    protected Grid<T> grid;
    protected GenericListViewForm<T> form;
    protected TextField filterText = new TextField();

    public GenericListView(Class<T> modelClass, GenericListViewForm<T> form) {
        this.modelClass = modelClass;
        this.grid = new Grid<>(modelClass);
        this.form = form;
        addClassName("generic-list-view");
        setSizeFull();
        configureGrid();
        configureForm();
        add(getToolbar(), getContent());
        closeEditor();
    }

    protected HorizontalLayout getToolbar() {
        filterText.setPlaceholder("Filter by name or tag");
        filterText.setClearButtonVisible(true);
        filterText.setValueChangeMode(ValueChangeMode.LAZY);
        filterText.addValueChangeListener(e -> updateItems());

        Button addProjectButton = new Button("Add project");
        addProjectButton.addClickListener(click -> addItem());

        var toolbar = new HorizontalLayout(filterText, addProjectButton);
        toolbar.addClassName("toolbar");
        return toolbar;
    }

    protected Component getContent() {
        HorizontalLayout content = new HorizontalLayout(grid, form);
        content.setFlexGrow(2, grid);
        content.setFlexGrow(1, form);
        content.addClassNames("content");
        content.setSizeFull();
        return content;
    }

    protected void configureGrid() {
        grid.addClassNames("generic-list-view-grid");
        grid.setSizeFull();
        grid.getColumns().forEach(col -> col.setAutoWidth(true));
        grid.asSingleSelect().addValueChangeListener(event -> editItem(event.getValue()));
    }

    protected void configureForm() {
        form.setWidth("25em");
        form.addSaveListener(this::saveItem);
        form.addDeleteListener(this::deleteItem);
        form.addCloseListener(e->closeEditor());
    }

    public void editItem(T item) {
        if (item == null) {
            closeEditor();
        } else {
            form.setItem(item);
            form.setVisible(true);
            addClassName("editing");
        }
    }

    public void addItem() {
        grid.asSingleSelect().clear();
        try {
            T item = modelClass.getDeclaredConstructor().newInstance();
            editItem(item);
        } catch (Exception e) {
            log.error(e.toString());
        }
    }

    protected void closeEditor() {
        form.setItem(null);
        form.setVisible(false);
        removeClassName("editing");
    }

    protected void saveItem(GenericListViewForm.SaveEvent event) {
        closeEditor();
        updateItems();
    }

    protected void deleteItem(GenericListViewForm.DeleteEvent event) {
        closeEditor();
        updateItems();
    }

    abstract protected void updateItems();
}
