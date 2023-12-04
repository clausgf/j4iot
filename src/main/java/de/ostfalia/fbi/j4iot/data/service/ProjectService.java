/**
 * j4iot  IoT infrastructure in Java with Spring Boot and Vaadin
 * Copyright (c) 2023 clausgf@github.com. Refer to license.md for legal information.
 */

/*
 * Questionable design decisions:
 * - use ResponseStatusException for exception handling
 */

package de.ostfalia.fbi.j4iot.data.service;

import de.ostfalia.fbi.j4iot.data.entity.Forwarding;
import de.ostfalia.fbi.j4iot.data.entity.Project;
import de.ostfalia.fbi.j4iot.data.entity.ProvisioningToken;
import de.ostfalia.fbi.j4iot.data.repository.DeviceRepository;
import de.ostfalia.fbi.j4iot.data.repository.ForwardingRepository;
import de.ostfalia.fbi.j4iot.data.repository.ProjectRepository;
import de.ostfalia.fbi.j4iot.data.repository.ProvisioningTokenRepository;
import de.ostfalia.fbi.j4iot.security.SecurityService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.Assert;

import java.security.SecureRandom;
import java.time.Instant;
import java.util.Base64;
import java.util.LinkedList;
import java.util.List;
import java.util.Optional;

@Service
public class ProjectService {

    private static final Logger log = LoggerFactory.getLogger(ProjectService.class);

    private final SecurityService securityService;
    private final ProjectRepository projectRepository;
    private final ProvisioningTokenRepository provisioningTokenRepository;
    private final ForwardingRepository forwardingRepository;
    private final DeviceRepository deviceRepository;

    private static final SecureRandom secureRandom = new SecureRandom();
    private static final Base64.Encoder base64Encoder = Base64.getUrlEncoder();

    // ************************************************************************

    public ProjectService(
            SecurityService securityService,
            ProjectRepository projectRepository,
            ProvisioningTokenRepository provisioningTokenRepository,
            ForwardingRepository forwardingRepository,
            DeviceRepository deviceRepository)
    {
        this.securityService = securityService;
        this.projectRepository = projectRepository;
        this.provisioningTokenRepository = provisioningTokenRepository;
        this.forwardingRepository = forwardingRepository;
        this.deviceRepository = deviceRepository;
    }

    // ************************************************************************

    public List<Project> findAllByAuth() {
        Long userId = securityService.getAuthenticatedUserId();
        if (userId != null) {
            return projectRepository.findAllByUserId(userId);
        } else {
            return new LinkedList<>();
        }
    }

    public Optional<Project> findByAuthAndId(Long id) {
        Long userId = securityService.getAuthenticatedUserId();
        if (userId != null) {
            return projectRepository.findByUserIdAndId(userId, id);
        } else {
            return Optional.empty();
        }
    }

    public Optional<Project> findByAuthAndName(String name) {
        Long userId = securityService.getAuthenticatedUserId();
        if (userId != null) {
            return projectRepository.findByUserIdAndName(userId, name);
        } else {
            return Optional.empty();
        }
    }

    public List<String> findAllNamesByAuth() {
        Long userId = securityService.getAuthenticatedUserId();
        return projectRepository.findAllNamesByUserId(userId);
    }

    public Project updateOrCreate(Project project) {
        Assert.notNull(project, "updateOrCreate: Project is null. Are you sure you have connected your form to the application?");
        // since orphanRemoval is true, deletion of provisioning tokens and forwadings is handled automatically
        return projectRepository.save(project);
    }

    @Transactional
    public void delete(Project project) {
        Long projectId = project.getId();
        project = projectRepository.findById(projectId).orElseThrow( () -> new RuntimeException("Project to delete not found id=" + projectId) );
        // provisioning tokens, forwardings and devices are cascaded, no delete needed
        //deviceRepository.deleteAll(project.getDevices());
        projectRepository.delete(project);
    }

    // ************************************************************************

    public long countDevicesInProject(Project project) {
        return deviceRepository.countByProjectId(project.getId());
    }

    // ************************************************************************
    // Provisioning Tokens
    // ************************************************************************

    /**
     * Add a provisioning token to the given project. This method does not persist anything to the database!
     */
    public ProvisioningToken addNewProvisioningToken(Project project) {
        byte[] randomBytes = new byte[ project.getDefaultProvisioningTokenLength() ];
        secureRandom.nextBytes( randomBytes );
        String token64 = String.format("P-%d-%s", project.getId(), base64Encoder.encodeToString( randomBytes ));
        Instant expiresAt = Instant.now().plusSeconds( project.getDefaultProvisioningTokenExpiresInSeconds() );
        ProvisioningToken provisioningToken = new ProvisioningToken( project, token64, expiresAt );
        project.getProvisioningTokens().add( provisioningToken );
        return provisioningToken;
    }

    // ************************************************************************

    public Optional<Forwarding> findForwardingByProjectNameAndForwardName(String projectName, String forwardName) {
        Optional<Forwarding> forwarding = forwardingRepository.findByProjectNameAndForwardName(projectName, forwardName);
        if (forwarding.isPresent()) {
            forwarding.get().setLastUseAt( Instant.now() );
            forwarding = Optional.of( forwardingRepository.save(forwarding.get()) );
        }
        return forwarding;

    }

    // ************************************************************************

}
