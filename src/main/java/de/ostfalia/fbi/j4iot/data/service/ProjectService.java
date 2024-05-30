/**
 * j4iot  IoT infrastructure in Java with Spring Boot and Vaadin
 * Copyright (c) 2023 clausgf@github.com. Refer to license.md for legal information.
 */

/*
 * Questionable design decisions:
 * - use ResponseStatusException for exception handling
 */

package de.ostfalia.fbi.j4iot.data.service;

import de.ostfalia.fbi.j4iot.data.entity.*;
import de.ostfalia.fbi.j4iot.data.repository.*;
import de.ostfalia.fbi.j4iot.security.SecurityService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.Assert;

import java.security.SecureRandom;
import java.time.Instant;
import java.util.*;

@Service
public class ProjectService {

    private static final Logger log = LoggerFactory.getLogger(ProjectService.class);

    private final SecurityService securityService;
    private final KeycloakService keycloakService;
    private final UserService userService;
    private final ProjectRepository projectRepository;
    private final ProvisioningTokenRepository provisioningTokenRepository;
    private final ForwardingRepository forwardingRepository;
    private final DeviceRepository deviceRepository;

    private static final SecureRandom secureRandom = new SecureRandom();
    private static final Base64.Encoder base64Encoder = Base64.getUrlEncoder();

    // ************************************************************************

    public ProjectService(
            SecurityService securityService,
            KeycloakService keycloakService,
            UserService userService,
            ProjectRepository projectRepository,
            ProvisioningTokenRepository provisioningTokenRepository,
            ForwardingRepository forwardingRepository,
            DeviceRepository deviceRepository)
    {
        this.securityService = securityService;
        this.keycloakService = keycloakService;
        this.userService = userService;
        this.projectRepository = projectRepository;
        this.provisioningTokenRepository = provisioningTokenRepository;
        this.forwardingRepository = forwardingRepository;
        this.deviceRepository = deviceRepository;
    }

    // ************************************************************************

    public void setupProjects(){
        List<String> kcProjects = keycloakService.getKeycloakProjects();
        List<Project> j4Projects = projectRepository.findAll();
        //Nicht mehr existierende Projekte löschen
        for(int i = 0; i < j4Projects.size(); i++){
            Project j4Project = j4Projects.get(i);
            boolean found = false;
            for(String kcUser : kcProjects){
                if (kcUser.equals(j4Project.getName())){
                    found = true;
                    break;
                }
            }
            if (!found){
                delete(j4Project);
                log.info("Delete project {}, wich is not in keycloak", j4Project.getName());
            }
        }
        for(String kcProject : kcProjects){
            Project j4ProjectFound = null;
            for(Project j4Project : j4Projects){
                if (kcProject.equals(j4Project.getName())){
                    j4ProjectFound = j4Project;
                    break;
                }
            }
            if (j4ProjectFound == null){
                j4ProjectFound = new Project(kcProject, "", "");
            }
            updateOrCreate(j4ProjectFound);
        }
    }

    public List<Project> findAllByAuth() {
        String userId = securityService.getAuthenticatedUserId();
        if (userId != null) {
            return securityService.isAuthenticatedUserAdmin() ?
                    projectRepository.findAll() :
                    filterByAuth(userId, projectRepository.findAll());
        } else {
            return new LinkedList<>();
        }
    }

    public Optional<Project> findByAuthAndId(Long id) {
        String userId = securityService.getAuthenticatedUserId();
        if (userId != null) {
            return securityService.isAuthenticatedUserAdmin() ?
                    projectRepository.findById(id) :
                    filterByAuth(userId, projectRepository.findById(id));
        } else {
            return Optional.empty();
        }
    }

    public Optional<Project> findByAuthAndName(String name) {
        String userId = securityService.getAuthenticatedUserId();
        if (userId != null) {
            return securityService.isAuthenticatedUserAdmin() ?
                    projectRepository.findByName(name) :
                    filterByAuth(userId, projectRepository.findByName(name));
        } else {
            return Optional.empty();
        }
    }

    public List<String> findAllNamesByAuth() {
        String userId = securityService.getAuthenticatedUserId();
        if (userId == null) return new ArrayList<>();
        return securityService.isAuthenticatedUserAdmin() ?
                projectRepository.findAllNames():
                filterByAuth(userId, projectRepository.findAll()).stream().map(x -> x.getName()).toList();
    }

    private Optional<Project> filterByAuth(String userId, Optional<Project> project){
        if (project.isPresent()){
            return userService.findById(userId).get().getProjects().contains(project.get().getName()) ? project : Optional.empty();
        }
        return project;
    }

    private List<Project> filterByAuth(String userId, List<Project> projects){
        return projects.stream().filter(x -> userService.findById(userId).get().getProjects().contains(x.getName())).toList();
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
}
