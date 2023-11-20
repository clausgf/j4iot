package de.ostfalia.fbi.j4iot.views.project;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.Scroller;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.value.ValueChangeMode;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.router.RouteParam;
import com.vaadin.flow.router.RouteParameters;
import de.ostfalia.fbi.j4iot.data.entity.Project;
import de.ostfalia.fbi.j4iot.data.service.IotService;
import de.ostfalia.fbi.j4iot.views.MainLayout;
import de.ostfalia.fbi.j4iot.views.device.DeviceList;
import jakarta.annotation.security.PermitAll;
import org.vaadin.lineawesome.LineAwesomeIcon;

// TODO base this list on a generic one
@PermitAll
@Route(value="/projects", layout = MainLayout.class)
@PageTitle("Projects")
public class ProjectList extends VerticalLayout {

    public final static String PROJECT_NAME_RP = "projectName";

    Grid<Project> grid = new Grid<>(Project.class);
    TextField filterText = new TextField();
    Scroller scroller = new Scroller();
    ProjectForm form;
    IotService service;

    public ProjectList(IotService service) {
        this.service = service;
        addClassName("project-list-view");
        setSizeFull();
        configureGrid();
        form = new ProjectForm(service);
        configureForm();
        add(getToolbar(), getContent());
        updateList();
        closeEditor();
    }

    private Component getContent() {
        scroller.setContent(form);
        HorizontalLayout content = new HorizontalLayout(grid, scroller);
        content.setFlexGrow(2, grid);
        content.setFlexGrow(1, form);
        content.addClassNames("content");
        content.setSizeFull();
        return content;
    }

    private void configureForm() {
        form.setWidth("25em");
        form.addSaveListener(this::saveProject);
        form.addDeleteListener(this::deleteProject);
        form.addCloseListener(e->closeEditor());
    }

    private void saveProject(ProjectForm.SaveEvent event) {
        service.updateProject(event.getProject());
        updateList();
        closeEditor();
    }

    private void deleteProject(ProjectForm.DeleteEvent event) {
        service.deleteProject(event.getProject());
        updateList();
        closeEditor();
    }

    private void configureGrid() {
        grid.addClassNames("project-grid");
        grid.setSizeFull();
        grid.setColumns();
        grid.addComponentColumn(item -> new Button(LineAwesomeIcon.LINK_SOLID.create(), click -> {
            RouteParameters rp = new RouteParameters(new RouteParam(PROJECT_NAME_RP, item.getName()));
            UI.getCurrent().navigate(DeviceList.class, rp);
        })).setHeader("Devices");
        grid.addColumns("name", "tags", "autocreateDevices", "provisioningAutoapproval");
        //grid.addColumn(new InstantRenderer<>(Project::getCreatedAt)).setHeader("Created");
        //grid.addColumn(new InstantRenderer<>(Project::getCreatedAt)).setHeader("Updated");
        // TODO add number of provisioning tokens (linking to some editor?)
        // TODO add number of devices (linking to some editor?)
        // TODO render tags as badges with color code
        // TODO add an edit button
        // TODO add a delete button
        grid.getColumns().forEach(col -> col.setAutoWidth(true));

        grid.asSingleSelect().addValueChangeListener(event -> editProject(event.getValue()));
    }

    private HorizontalLayout getToolbar() {
        filterText.setPlaceholder("Filter by name or tag");
        filterText.setClearButtonVisible(true);
        filterText.setValueChangeMode(ValueChangeMode.LAZY);
        filterText.addValueChangeListener(e -> updateList());

        Button addProjectButton = new Button("Add project");
        addProjectButton.addClickListener(click -> addProject());

        var toolbar = new HorizontalLayout(filterText, addProjectButton);
        toolbar.addClassName("toolbar");
        return toolbar;
    }

    public void editProject(Project project) {
        if (project == null) {
            closeEditor();
        } else {
            form.setProject(project);
            form.setVisible(true);
            addClassName("editing");
        }
    }

    private void closeEditor() {
        form.setProject(null);
        form.setVisible(false);
        removeClassName("editing");
    }

    private void addProject() {
        grid.asSingleSelect().clear();
        editProject(new Project());
    }

    private void updateList() {
        grid.setItems(service.findAllProjects(filterText.getValue()));
    }
}
