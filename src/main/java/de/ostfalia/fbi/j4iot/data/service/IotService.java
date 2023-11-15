package de.ostfalia.fbi.j4iot.data.service;

import de.ostfalia.fbi.j4iot.data.entity.Device;
import de.ostfalia.fbi.j4iot.data.entity.Project;
import de.ostfalia.fbi.j4iot.data.repository.DeviceRepository;
import de.ostfalia.fbi.j4iot.data.repository.ProjectRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class IotService {

    private static final Logger log = LoggerFactory.getLogger(IotService.class);

    private final ProjectRepository projectRepository;
    private final DeviceRepository deviceRepository;

    // ************************************************************************

    public IotService(ProjectRepository projectRepository, DeviceRepository deviceRepository) {
        this.projectRepository = projectRepository;
        this.deviceRepository = deviceRepository;
    }

    public List<Project> findAllProjects(String stringFilter) {
        // TODO return projects related to the user only
        if (stringFilter == null || stringFilter.isEmpty()) {
            return projectRepository.findAll();
        }
        return projectRepository.searchAll(stringFilter);
    }

    public List<String> findAllProjectNames(String stringFilter) {
        if (stringFilter == null || stringFilter.isEmpty()) {
            return projectRepository.findAllNames();
        }
        return projectRepository.searchAllNames(stringFilter);
    }

    public long countProjects() {
        return projectRepository.count();
    }

    public Project findProjectByName(String name) {
        return projectRepository.findByName(name);
    }

    public void deleteProject(Project project) {
        projectRepository.delete(project);
    }

    public void saveProject(Project project) {
        if (project == null) {
            System.err.println("Project is null. Are you sure you have connected your form to the application?");
            return;
        }
        projectRepository.save(project);
    }

    // ************************************************************************

    public List<Device> findAllDevices(String stringFilter) {
        // TODO return devices related to the user only
        if (stringFilter == null || stringFilter.isEmpty()) {
            return deviceRepository.findAll();
        } else {
            return deviceRepository.searchAll(stringFilter);
        }
    }

    public List<String> findAllDeviceNames(String stringFilter) {
        if (stringFilter == null || stringFilter.isEmpty()) {
            return deviceRepository.findAllNames();
        }
        return deviceRepository.searchAllNames(stringFilter);
    }

    public List<String> findAllDeviceNamesByProject(Project project, String stringFilter) {
        if (stringFilter == null || stringFilter.isEmpty()) {
            return deviceRepository.findAllNamesByProject(project);
        }
        return deviceRepository.searchAllNamesByProject(project, stringFilter);
    }

    public Device findDeviceByProjectAndName(Project project, String name) {
        return deviceRepository.findByProjectAndName(project, name);
    }

    public long countDevices() { return deviceRepository.count(); }

    public void deleteDevice(Device device) { deviceRepository.delete(device); }

    public void saveDevice(Device device) {
        if (device == null) {
            System.err.println("Device is null. Are you sure you have connected your form to the application?");
            return;
        }
        deviceRepository.save(device);
    }

    // ************************************************************************

}
