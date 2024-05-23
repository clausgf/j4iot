package de.ostfalia.fbi.j4iot.views.project;

import de.ostfalia.fbi.j4iot.data.entity.Project;

public class ProjectUtil {
    public static String getPageTitle(String page, Project project){
        String title = String.format("%s for unknown", page);
        if (project != null) {
            title = String.format("%s: %s", page, project.getName());
        }
        return title;
    }
}
