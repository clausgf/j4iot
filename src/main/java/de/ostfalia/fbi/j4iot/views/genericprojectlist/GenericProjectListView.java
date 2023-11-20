package de.ostfalia.fbi.j4iot.views.genericprojectlist;

import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import de.ostfalia.fbi.j4iot.data.entity.Project;
import de.ostfalia.fbi.j4iot.data.service.IotService;
import de.ostfalia.fbi.j4iot.views.MainLayout;
import de.ostfalia.fbi.j4iot.views.generic.GenericListView;
import de.ostfalia.fbi.j4iot.views.project.ProjectForm;
import jakarta.annotation.security.PermitAll;

// TODO base this list on a generic one
@PermitAll
@Route(value="/genericprojects", layout = MainLayout.class)
@PageTitle("Projects")
public class GenericProjectListView extends GenericListView<Project> {

    IotService service;

    public GenericProjectListView(IotService service) {
        super(Project.class, new GenericProjectForm());
        this.service = service;
        updateItems();
    }

    protected void configureGrid() {
        grid.setColumns("name", "tags", "autocreateDevices", "provisioningAutoapproval");
        //grid.addColumn(new InstantRenderer<>(Project::getCreatedAt)).setHeader("Created");
        //grid.addColumn(new InstantRenderer<>(Project::getCreatedAt)).setHeader("Updated");
        // TODO add number of provisioning tokens (linking to some editor?)
        // TODO add number of devices (linking to some editor?)
        // TODO render tags as badges with color code
        // TODO add an edit button
        // TODO add a delete button
        super.configureGrid(); // call the base class method at the end to allow further modifications
    }

    protected void saveItem(ProjectForm.SaveEvent event) {
        //service.updateProject(form.getItemFromItemFormEvent(event, modelClass));  // save item to the database first
        //super.saveItem(event);
    }

    protected void deleteItem(ProjectForm.DeleteEvent event) {
        //service.deleteProject(form.getItemFromItemFormEvent(event, modelClass));  // delete item from the database first
        //super.deleteItem(event);
    }

    protected void updateItems() {
        grid.setItems(service.findAllProjects(filterText.getValue()));
    }
}
