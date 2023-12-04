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
import de.ostfalia.fbi.j4iot.data.repository.ProvisioningTokenRepository;
import de.ostfalia.fbi.j4iot.security.SecurityService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.Assert;
import org.springframework.web.server.ResponseStatusException;

import java.security.SecureRandom;
import java.time.Instant;
import java.util.Base64;
import java.util.LinkedList;
import java.util.List;
import java.util.Optional;

@Service
public class DeviceService {

    private static final Logger log = LoggerFactory.getLogger(DeviceService.class);

    private final SecurityService securityService;
    private final DeviceRepository deviceRepository;
    private final ProvisioningTokenRepository provisioningTokenRepository;
    private final DeviceTokenRepository deviceTokenRepository;

    private static final SecureRandom secureRandom = new SecureRandom();
    private static final Base64.Encoder base64Encoder = Base64.getUrlEncoder();

    // ************************************************************************

    public DeviceService(SecurityService securityService, ProvisioningTokenRepository provisioningTokenRepository, DeviceRepository deviceRepository, DeviceTokenRepository deviceTokenRepository) {
        this.securityService = securityService;
        this.provisioningTokenRepository = provisioningTokenRepository;
        this.deviceRepository = deviceRepository;
        this.deviceTokenRepository = deviceTokenRepository;
    }

    // ************************************************************************

    // TODO generally check auth in all finder methods!
    /*
    public List<Project> findAll() {
        return deviceRepository.findAll();
    }
    */

    public Optional<Device> findById(Long id) {
        return deviceRepository.findById(id);
    }

    public Optional<Device> findByProjectNameAndName(String projectName, String deviceName) {
        Optional<Device> device = deviceRepository.findByProjectNameAndName(projectName, deviceName);
        if (device.isPresent()) {
            device.get().setLastSeenAt( Instant.now() );
            device = Optional.of( deviceRepository.save(device.get()) );
        }
        return device;
    }

    public List<Device> findAllByUserAuth() {
        Long userId = securityService.getAuthenticatedUserId();
        if (userId != null) {
            return deviceRepository.findAllByUserId(userId);
        } else { // user id not present: return empty list
            return new LinkedList<>();
        }
    }

    public Optional<Device> findByUserAuthAndId(Long id) {
        Long userId = securityService.getAuthenticatedUserId();
        if (userId != null) {
            return deviceRepository.findByUserIdAndId(userId, id);
        } else {
            return Optional.empty();
        }
    }

    public List<Device> findAllByUserAuthAndProjectId(Long projectId) {
        Long userId = securityService.getAuthenticatedUserId();
        if (userId != null) {
            return deviceRepository.findAllByUserIdAndProjectId(userId, projectId);
        } else {
            return new LinkedList<>();
        }
    }

    public Optional<Device> findByUserAuthAndProjectIdAndName(Long projectId, String name) {
        Long userId = securityService.getAuthenticatedUserId();
        if (userId != null) {
            return deviceRepository.findByUserIdAndProjectIdAndName(userId, projectId, name);
        } else {
            return Optional.empty();
        }
    }

    public List<String> findAllNamesByUserAuthAndProjectId(Long projectId) {
        Long userId = securityService.getAuthenticatedUserId();
        if (userId != null) {
            return deviceRepository.findAllNamesByUserIdAndProjectId(userId, projectId);
        } else {
            return new LinkedList<>();
        }
    }

    public Device updateOrCreate(Device device) {
        Assert.notNull(device, "Device null cannot be updated");
        // since orphanRemoval is true, deletion of provisioning tokens and forwadings is handled automatically
        return deviceRepository.save(device);
    }

    @Transactional
    public void delete(Device device) {
        Assert.notNull(device, "Device null cannot be deleted");
        Long deviceId = device.getId();
        device = deviceRepository.findById(deviceId).orElseThrow( () -> new RuntimeException("Device to delete not found id=" + deviceId) );
        deviceRepository.delete(device);
    }


    // ************************************************************************

    /**
     * Add a device token to the device given. This method does not persist anything to the database!
     */
    public DeviceToken addNewDeviceToken(Device device) {
        final Project project = device.getProject();
        byte[] randomBytes = new byte[ project.getDefaultDeviceTokenLength() ];
        secureRandom.nextBytes( randomBytes );
        String prefix64 = String.format("D-%d-%d-", project.getId(), device.getId());
        String token64 = base64Encoder.encodeToString( randomBytes );
        Instant expiresAt = Instant.now().plusSeconds( project.getDefaultDeviceTokenExpiresInSeconds() );
        DeviceToken deviceToken = new DeviceToken( device, prefix64 + token64, expiresAt );
        device.getDeviceTokens().add( deviceToken );
        return deviceToken;
    }

    /**
     * Return authenticated DeviceToken on success or null on authentication failure
     */
    public DeviceToken authenticateDeviceToken(String token) {
        // check the database for the device token
        Optional<DeviceToken> deviceTokenOptional = deviceTokenRepository.findByToken(token);
        if (deviceTokenOptional.isEmpty()) {
            String msg = String.format("Device token not found: token=%s", token);
            log.info(msg);
            return null;
        }
        DeviceToken deviceToken = deviceTokenOptional.get();

        // immediately update "last use" info in the database, no transaction needed
        deviceToken.setLastUseAt(Instant.now());
        deviceToken = deviceTokenRepository.save(deviceToken);

        // check expiry
        if (deviceToken.getExpiresAt().isBefore(Instant.now())) {
            String msg = String.format("Device token expired: token=%s", deviceToken);
            log.info(msg);
            return null;
        }
        return deviceToken;
    }

    // ************************************************************************

    @Transactional
    public String provision(String projectName, String deviceName, String provisioningTokenStr) {
        // check for provisioning token existence
        Optional<ProvisioningToken> pto = provisioningTokenRepository.findByToken(provisioningTokenStr);
        if (pto.isEmpty()) { // provisioning token does not exists
            String msg = String.format("Provisioning token not found projectName=%s deviceName=%s token=%s",
                    projectName, deviceName, provisioningTokenStr);
            log.info(msg);
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, msg);
        }
        ProvisioningToken pt = pto.get();
        pt.setLastUseAt(Instant.now());  // record "last use" info of provisioning token
        pt = provisioningTokenRepository.save(pt);
        Project project = pt.getProject();

        // check correctness of project name
        if ( !projectName.equals( project.getName() ) ) {
            String msg = String.format( "ProjectName does not match provisioning token projectName=%s deviceName=%s token=%s",
                    projectName, deviceName, provisioningTokenStr);
            log.info(msg);
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, msg);
        }

        // if device exists: update lastProvisionedAt (in case of an exception
        Device device = deviceRepository.findByProjectIdAndName( project.getId(), deviceName ).orElse(null);
        if ( device != null ) {
            device.setLastProvisioningRequestAt( Instant.now() );
            device = deviceRepository.save(device);
        }

        // check provisioning token expiry
        if (pt.getExpiresAt().isBefore(Instant.now())) {
            String msg = String.format("Provisioning token expired projectName=%s deviceName=%s token=%s",
                    projectName, deviceName, provisioningTokenStr);
            log.info(msg);
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, msg);
        }

        // device autocreation
        if ( device == null ) {
            if ( project.getAutocreateDevices() ) {
                device = new Device(project, deviceName, project.getProvisioningAutoapproval());
                project.getDevices().add( device );
            } else { // no device, no autocration: bail out
                String msg = String.format("Device not found or autocreate failed: projectName=%s deviceName=%s",
                        project.getName(), deviceName);
                log.info(msg);
                throw new ResponseStatusException(HttpStatus.FORBIDDEN, msg);
            }
        }

        // device exists and provisioning ok: record provisioning
        device.setLastProvisioningRequestAt( Instant.now() );
        device.setLastProvisionedAt( Instant.now() );
        device = updateOrCreate( device );  // write device as we need its id

        // create & return device token, transaction will save everything
        DeviceToken deviceToken = addNewDeviceToken( device );
        log.info("provisioned project={} device={} provisioningToken={}", projectName, deviceName, provisioningTokenStr);
        return deviceToken.getToken();
    }

    // ************************************************************************

}
