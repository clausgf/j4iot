package de.ostfalia.fbi.j4iot.views.project;

import com.vaadin.flow.component.orderedlayout.Scroller;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import de.ostfalia.fbi.j4iot.data.service.IotService;
import de.ostfalia.fbi.j4iot.views.MainLayout;
import jakarta.annotation.security.PermitAll;

@PermitAll
@Route(value="/project_overview", layout = MainLayout.class)
@PageTitle("Project Overview")
public class ProjectOverview extends VerticalLayout {
    Scroller scroller = new Scroller();
    ProjectForm form;
    IotService service;

    public ProjectOverview(IotService service) {
        this.service = service;

        addClassName("project-overview");
        setSizeFull();
        //add(getContent());
    }
}
