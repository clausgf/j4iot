package de.ostfalia.fbi.j4iot.views.generic;

import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.TextField;
import de.ostfalia.fbi.j4iot.data.entity.AbstractEntity;
import de.ostfalia.fbi.j4iot.data.service.IotService;
import de.ostfalia.fbi.j4iot.views.projectlist.ProjectForm;

public class GenericListView<T extends AbstractEntity> extends VerticalLayout {
    final Class<T> modelClass;
    Grid<MODEL> grid = new Grid<>(Class<MODEL>);
    TextField filterText = new TextField();
    ProjectForm form;

    public GenericListView(Class<T> modelClass) {
        this.modelClass = modelClass;
        addClassName("generic-list-view");
        setSizeFull();
        configureGrid();
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
}
