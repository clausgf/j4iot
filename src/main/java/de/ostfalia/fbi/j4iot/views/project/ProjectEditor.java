package de.ostfalia.fbi.j4iot.views.project;

import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.data.provider.hierarchy.TreeData;
import com.vaadin.flow.router.*;
import de.ostfalia.fbi.j4iot.data.entity.Project;
import de.ostfalia.fbi.j4iot.data.service.FileService;
import de.ostfalia.fbi.j4iot.data.service.ProjectService;
import de.ostfalia.fbi.j4iot.views.GenericEditor;
import de.ostfalia.fbi.j4iot.views.MainLayout;
import de.ostfalia.fbi.j4iot.views.device.DeviceEditor;
import jakarta.annotation.security.PermitAll;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.Resource;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Optional;

@PermitAll
@Route(value="/projects/:id?/editor", layout = MainLayout.class)
public class ProjectEditor extends GenericEditor implements HasDynamicTitle {
    private Logger log = LoggerFactory.getLogger(DeviceEditor.class);
    public final static String ID_ROUTING_PARAMETER = "id";
    protected RouteParameters routeParameters = null;
    private final ProjectService projectService;
    private Project project;

    public ProjectEditor(ProjectService projectService, FileService fileService) {
        super(fileService);
        this.projectService = projectService;
    }

    public static RouteParameters getRouteParameters(Project project) {
        if (project != null) {
            return new RouteParameters(new RouteParam(ID_ROUTING_PARAMETER, project.getId()));
        } else {
            return RouteParameters.empty();
        }
    }

    @Override
    public void beforeEnter(BeforeEnterEvent event) {
        routeParameters = event.getRouteParameters();
        Optional<Long> projectId = routeParameters.getLong(ID_ROUTING_PARAMETER);
        if (projectId.isPresent()) {
            project = projectService.findByAuthAndId(projectId.get()).orElse(null);
        }
        if (project == null) {
            String msg = "No access to project or not found id=" + projectId.orElse(null);
            log.error(msg);
            Notification.show(msg);
        }
        super.beforeEnter(event);
    }

    @Override
    protected TreeData<File> getData() {
        TreeData<File> result = new TreeData<>();
        try {
            fileService.createFolderFor(project);
            List<Resource> files = fileService.loadFilesAsResources(project);
            result.addRootItems(files.get(0).getFile());
            for(int i = 1; i < files.size(); i++){
                if (files.get(i).getFile().isFile()) {
                    result.addItem(files.get(0).getFile(), files.get(i).getFile());
                }
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return result;
    }

    @Override
    public String getPageTitle() {
        return ProjectUtil.getPageTitle("Editor", project);
    }
}
