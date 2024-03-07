package de.ostfalia.fbi.j4iot.views.device;

import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.data.provider.hierarchy.TreeData;
import com.vaadin.flow.router.*;
import de.ostfalia.fbi.j4iot.data.entity.Device;
import de.ostfalia.fbi.j4iot.data.entity.Project;
import de.ostfalia.fbi.j4iot.data.service.DeviceService;
import de.ostfalia.fbi.j4iot.data.service.FileService;
import de.ostfalia.fbi.j4iot.data.service.ProjectService;
import de.ostfalia.fbi.j4iot.views.GenericEditor;
import de.ostfalia.fbi.j4iot.views.MainLayout;
import jakarta.annotation.security.PermitAll;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.Resource;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;

@PermitAll
@Route(value="/projects/:projectId/devices/:id?/editor", layout = MainLayout.class)
public class DeviceEditor extends GenericEditor {
    private Logger log = LoggerFactory.getLogger(DeviceEditor.class);
    public final static String ID_ROUTING_PARAMETER = "id";
    public final static String PROJECT_ID_ROUTING_PARAMETER = "projectId";
    protected RouteParameters routeParameters = null;
    private ProjectService projectService;
    private DeviceService deviceService;
    private Project project;
    private Device device;

    public DeviceEditor(ProjectService projectService, DeviceService deviceService, FileService fileService) {
        super(fileService);
        this.projectService = projectService;
        this.deviceService = deviceService;
        tree.getColumnByKey(COLUMN_FILE_KEY).setClassNameGenerator(x -> {
            String className = null;
            System.out.println(x.getPath());
            System.out.println("Is Project:" + fileService.isProjectFile(project, x.toPath()));
            if (x.isFile() && fileService.isProjectFile(project, x.toPath())
                    && fileService.exists(device, x.getName())) {
                className = "editor-overwriteööö";
            }
            return className;
        });
    }

    public static RouteParameters getRouteParameters(Project project, Device device) {
        if ((project != null) && (device != null)) {
            return new RouteParameters(
                    new RouteParam(PROJECT_ID_ROUTING_PARAMETER, project.getId()),
                    new RouteParam(ID_ROUTING_PARAMETER, device.getId()));
        } else if ((project != null) && (device == null)) {
            return new RouteParameters(new RouteParam(PROJECT_ID_ROUTING_PARAMETER, project.getId()));
        } else {
            return RouteParameters.empty();
        }
    }

    @Override
    public void beforeEnter(BeforeEnterEvent event) {
        routeParameters = event.getRouteParameters();
        Optional<Long> projectId = routeParameters.getLong(PROJECT_ID_ROUTING_PARAMETER);
        if (projectId.isPresent()) {
            project = projectService.findByAuthAndId(projectId.get()).orElse(null);
        }
        if (project == null) {
            String msg = "No access to project or not found id=" + projectId.orElse(null);
            log.error(msg);
            Notification.show(msg);
        }

        Optional<Long> deviceId = routeParameters.getLong(ID_ROUTING_PARAMETER);
        if (deviceId.isPresent()) {
            device = deviceService.findById(deviceId.get()).orElse(null);
        }
        if (device == null) {
            String msg = "No access to device or not found id=" + deviceId.orElse(null);
            log.error(msg);
            Notification.show(msg);
        }
        super.beforeEnter(event);
    }

    @Override
    protected TreeData<File> getData() {
        TreeData<File> result = new TreeData<>();
        try{
            fileService.createFolderFor(project);
            fileService.createFolderFor(device);
            List<Resource> files = fileService.loadFilesAsResources(project);
            File root = files.get(0).getFile();
            result.addRootItems(root);
            for(int i = 1; i < files.size(); i++){
                if (files.get(i).getFile().isFile()){
                    result.addItem(root, files.get(i).getFile());
                }
            }
            files = fileService.loadFilesAsResources(device);
            result.addItem(root, files.get(0).getFile());
            for(int i = 1; i < files.size(); i++){
                if (files.get(i).getFile().isFile()) {
                    result.addItem(files.get(0).getFile(), files.get(i).getFile());
                }
            }
        }catch(IOException ex){
            throw new RuntimeException(ex);
        }
        return result;
    }
}
