/**
 * j4iot  IoT infrastructure in Java with Spring Boot and Vaadin
 * Copyright (c) 2023 clausgf@github.com. Refer to license.md for legal information.
 */

/*
 * Questionable design decisions:
 * - use ResponseStatusException for exception handling
 */

package de.ostfalia.fbi.j4iot.data.service;

import de.ostfalia.fbi.j4iot.data.entity.Device;
import de.ostfalia.fbi.j4iot.data.entity.DeviceToken;
import de.ostfalia.fbi.j4iot.data.entity.Project;
import de.ostfalia.fbi.j4iot.data.entity.ProvisioningToken;
import de.ostfalia.fbi.j4iot.data.repository.DeviceRepository;
import de.ostfalia.fbi.j4iot.data.repository.DeviceTokenRepository;
import de.ostfalia.fbi.j4iot.data.repository.ProjectRepository;
import de.ostfalia.fbi.j4iot.data.repository.ProvisioningTokenRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.security.SecureRandom;
import java.time.Instant;
import java.util.Base64;
import java.util.List;
import java.util.Optional;

@Service
public class IotService {

    private static final Logger log = LoggerFactory.getLogger(IotService.class);

    private final ProjectRepository projectRepository;
    private final DeviceRepository deviceRepository;
    private final ProvisioningTokenRepository provisioningTokenRepository;
    private final DeviceTokenRepository deviceTokenRepository;

    private static final SecureRandom secureRandom = new SecureRandom();
    private static final Base64.Encoder base64Encoder = Base64.getUrlEncoder();

    // ************************************************************************

    public IotService(ProjectRepository projectRepository, DeviceRepository deviceRepository, ProvisioningTokenRepository provisioningTokenRepository, DeviceTokenRepository deviceTokenRepository) {
        this.projectRepository = projectRepository;
        this.deviceRepository = deviceRepository;
        this.provisioningTokenRepository = provisioningTokenRepository;
        this.deviceTokenRepository = deviceTokenRepository;
    }

    // ************************************************************************

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

    public Optional<Project> findProjectById(Long id) {
        return projectRepository.findById(id);
    }

    public Optional<Project> findProjectByName(String name) {
        return projectRepository.findOneByName(name);
    }

    public long countDevicesInProject(Project project) {
        return deviceRepository.countByProjectId(project.getId());
    }

    public Boolean projectExistsByName(String name) { return projectRepository.existsByName(name); }

    @Transactional
    public void deleteProject(Project project) {
        // TODO make sure to delete really everything, might need to add some cascading
        Long projectId = project.getId();
        project = projectRepository.findById(projectId).orElseThrow( () -> new RuntimeException("Project to delete not found id=" + projectId) );
        provisioningTokenRepository.deleteAll(project.getProvisioningTokens());
        deviceRepository.deleteAll(project.getDevices());
        projectRepository.delete(project);
    }

    public Project updateProject(Project project) {
        if (project == null) {
            log.error("updateProject: Project is null. Are you sure you have connected your form to the application?");
            return null;
        }
        List<ProvisioningToken> tokensInDb = findAllProvisioningTokensByProject(project);
        for (ProvisioningToken newToken: project.getProvisioningTokens()) {
            int i = tokensInDb.indexOf(newToken);
            if (i>=0) {
                // found the token id in the database: update if necessary
                ProvisioningToken oldToken = tokensInDb.get(i);
                Boolean isTokenSame = newToken.getToken().equals(oldToken.getToken())
                        && newToken.getExpiresAt().equals(oldToken.getExpiresAt());
                if (!isTokenSame) {
                    provisioningTokenRepository.save(newToken);
                }
                tokensInDb.remove(i);
            } else {
                // did not find the token id in the database: save it to the db
                provisioningTokenRepository.save(newToken);
            }
        }
        // delete tokens not in in the project from db
        provisioningTokenRepository.deleteAll(tokensInDb);
        return projectRepository.save(project);
    }

    // ************************************************************************

    public List<Device> searchAllDevices(String stringFilter) {
        // TODO return devices related to the user only
        if (stringFilter == null || stringFilter.isEmpty()) {
            return deviceRepository.findAll();
        }
        return deviceRepository.searchAll(stringFilter);
    }

    public List<Device> searchAllDevicesByProjectId(Long projectId, String stringFilter) {
        if (stringFilter == null || stringFilter.isEmpty()) {
            return deviceRepository.findAllByProjectId(projectId);
        }
        return deviceRepository.searchAllByProjectId(projectId, stringFilter);
    }

    public List<String> searchAllDeviceNamesByProjectId(Long projectId, String stringFilter) {
        if (stringFilter == null || stringFilter.isEmpty()) {
            return deviceRepository.findAllNamesByProjectId(projectId);
        }
        return deviceRepository.searchAllNamesByProjectId(projectId, stringFilter);
    }

    public Optional<Device> findDeviceById(Long id) {
        return deviceRepository.findById(id);
    }

    public Optional<Device> findDeviceByProjectIdAndNameAndName(Long projectId, String deviceName) {
        return deviceRepository.findOneByProjectIdAndName(projectId, deviceName);
    }

    public Boolean deviceExistsByProjectNameAndDeviceName(String projectName, String deviceName) { return deviceRepository.existsByProjectNameAndDeviceName(projectName, deviceName); }

    public void deleteDevice(Device device) {
        // TODO make sure to delete really everything, might need to add some cascading
        Long deviceId = device.getId();
        device = deviceRepository.findById(deviceId).orElseThrow( () -> new RuntimeException("Device to delete not found id=" + deviceId) );
        deviceTokenRepository.deleteAll(device.getDeviceTokens());
        deviceRepository.delete(device);
    }

    public Device updateDevice(Device device) {
        if (device == null) {
            System.err.println("Device is null. Are you sure you have connected your form to the application?");
            return null;
        }
        return deviceRepository.save(device);
    }

    // ************************************************************************

    public List<ProvisioningToken> findAllProvisioningTokensByProject(Project project) {
        return provisioningTokenRepository.findAllByProject(project);
    }

    public ProvisioningToken updateProvisioningToken(ProvisioningToken provisioningToken) {
        if (provisioningToken == null) {
            log.error("updateProvisioningToken: provisioningToken is null. Are you sure you have connected your form to the application?");
            return null;
        }
        return provisioningTokenRepository.save(provisioningToken);
    }

    public ProvisioningToken addProvisioningToken(Project project) {
        byte[] randomBytes = new byte[project.getDefaultProvisioningTokenLength()];
        secureRandom.nextBytes(randomBytes);
        String prefix = String.format("P-%s%d-", project.getName(), project.getId());
        String tokenValue = base64Encoder.encodeToString(prefix.getBytes()) + base64Encoder.encodeToString(randomBytes);
        Instant expiresAt = Instant.now().plusSeconds(project.getDefaultProvisioningTokenExpiresInSeconds());
        ProvisioningToken provisioningToken = new ProvisioningToken(
                project,
                tokenValue,
                expiresAt);
        project.getProvisioningTokens().add(provisioningToken);
        return provisioningToken;
    }

    public ProvisioningToken createProvisioningToken(Project project) {
        ProvisioningToken provisioningToken = addProvisioningToken(project);
        return provisioningTokenRepository.save(provisioningToken);
    }

    // ************************************************************************

    /**
     * Add a device token to a device from the user logged in.
     * This method does not store information to the database!
     */
    public DeviceToken addDeviceToken(Device device) {
        final Project project = device.getProject();
        String prefix = String.format("D-%d-%d-", project.getId(), device.getId());
        byte[] randomBytes = new byte[project.getDefaultDeviceTokenLength()];
        secureRandom.nextBytes(randomBytes);
        String tokenValue = base64Encoder.encodeToString(prefix.getBytes()) + base64Encoder.encodeToString(randomBytes);
        Instant expiresAt = Instant.now().plusSeconds(project.getDefaultDeviceTokenExpiresInSeconds());
        DeviceToken deviceToken = new DeviceToken(
                device,
                tokenValue,
                expiresAt);
        device.getDeviceTokens().add(deviceToken);
        return deviceToken;
    }


    public DeviceToken addDeviceToken(ProvisioningToken provisioningToken, Device device) {
        Project project = provisioningToken.getProject();
        String prefix = String.format("D-%d-%d-", project.getId(), device.getId());
        byte[] randomBytes = new byte[project.getDefaultDeviceTokenLength()];
        secureRandom.nextBytes(randomBytes);
        String tokenValue = base64Encoder.encodeToString(prefix.getBytes()) + base64Encoder.encodeToString(randomBytes);
        Instant expiresAt = Instant.now().plusSeconds(project.getDefaultDeviceTokenExpiresInSeconds());
        DeviceToken deviceToken = new DeviceToken(
                device,
                tokenValue,
                expiresAt);
        device.getDeviceTokens().add(deviceToken);
        return deviceToken;
    }

    public DeviceToken createDeviceToken(ProvisioningToken provisioningToken, Device device) {
        DeviceToken deviceToken = addDeviceToken(provisioningToken, device);
        return deviceTokenRepository.save(deviceToken);
    }

    /**
     * Return authenticated DeviceToken on success or null on authentication failure
     */
    public DeviceToken authenticateDeviceToken(String token) {
        Optional<DeviceToken> deviceTokenOptional = deviceTokenRepository.findOneByToken(token);
        if (deviceTokenOptional.isEmpty()) {
            String msg = String.format("Not found device token '%s'", token);
            log.info(msg);
            return null;
        }

        DeviceToken deviceToken = deviceTokenOptional.get();
        deviceToken.setLastUseAt(Instant.now());
        deviceToken = deviceTokenRepository.save(deviceToken);

        if (deviceToken.getExpiresAt().isBefore(Instant.now())) {
            String msg = String.format("Expired device token '%s'", deviceToken);
            log.info(msg);
            return null;
        }
        return deviceToken;
    }

    // ************************************************************************

    final String namePattern = "^[a-zA-Z0-9][a-zA-Z0-9_\\-+]*$";

    public Boolean isNamePatternValid(String name) {
        return name.matches(namePattern);
    }

    public Project checkProject(String name) {
        if (!isNamePatternValid(name)) {
            String msg = String.format("Project name '%s' invalid", name);
            log.info("invalid provision parameters: " + msg);
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, msg);
        }

        Optional<Project> optProject = projectRepository.findOneByName(name);
        if (optProject.isEmpty()) {
            String msg = String.format("Project '%s' not found", name);
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, msg);
        }

        return optProject.get();
    }

    public Device checkOrAutocreateDevice(Project project, String name) {
        if (!isNamePatternValid(name)) {
            String msg = String.format("Device name '%s' invalid", name);
            log.info("invalid provision parameters: " + msg);
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, msg);
        }

        Optional<Device> optDevice = deviceRepository.findOneByProjectAndName(project, name);
        if (optDevice.isEmpty() && project.getAutocreateDevices()) {
            Device device = new Device(project, name, project.getProvisioningAutoapproval());
            project.getDevices().add(device);
            optDevice = Optional.of(device);
        }

        if (optDevice.isEmpty()) {
            String msg = String.format("Project '%s': device '%s' not found or autocreate failed", project.getName(), name);
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, msg);
        }

        return optDevice.get();
    }

    public ProvisioningToken checkProvisioningTokenExists(Project project, String provisioningToken) {
        Optional<ProvisioningToken> optProvisioningToken = provisioningTokenRepository.findOneByProjectAndToken(project, provisioningToken);
        if (optProvisioningToken.isEmpty()) {
            String msg = String.format("Project '%s': Not found provisioning token '%s'", project.getName(), provisioningToken);
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, msg);
        }
        return optProvisioningToken.get();
    }

    public ProvisioningToken checkProvisioningTokenValid(Project project, String provisioningToken) {
        ProvisioningToken token = checkProvisioningTokenExists(project, provisioningToken);
        if (token.getExpiresAt().isBefore(Instant.now())) {
            String msg = String.format("Project '%s': Expired provisioning token '%s'", project.getName(), provisioningToken);
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, msg);
        }
        return token;
    }

    public String provision(String projectName, String deviceName, String provisioningTokenStr) {
        Project project = checkProject(projectName);

        ProvisioningToken provisioningToken = checkProvisioningTokenExists(project, provisioningTokenStr);
        provisioningToken.setLastUseAt(Instant.now());
        provisioningToken = provisioningTokenRepository.save(provisioningToken);

        Device device = checkOrAutocreateDevice(project, deviceName);
        device.setLastProvisioningRequestAt(Instant.now());
        device = deviceRepository.save(device);

        provisioningToken = checkProvisioningTokenValid(project, provisioningTokenStr);
        device.setLastProvisionedAt(Instant.now());
        device = deviceRepository.save(device);
        log.info("provision project='{}' device='{} provisioningToken='{}' ok", projectName, deviceName, provisioningTokenStr);

        DeviceToken deviceToken = createDeviceToken(provisioningToken, device);
        deviceToken.setLastUseAt(Instant.now());
        deviceToken = deviceTokenRepository.save(deviceToken);

        return deviceToken.getToken();
    }

    // ************************************************************************

}
