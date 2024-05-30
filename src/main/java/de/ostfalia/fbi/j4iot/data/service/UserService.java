package de.ostfalia.fbi.j4iot.data.service;

import de.ostfalia.fbi.j4iot.data.entity.User;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.event.EventListener;
import org.springframework.security.authentication.event.AbstractAuthenticationFailureEvent;
import org.springframework.security.authentication.event.AuthenticationSuccessEvent;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

@Service
public class UserService implements UserDetailsService {
    Logger log = LoggerFactory.getLogger(UserService.class);
    //private ProjectService projectService;
    private KeycloakService keycloakService;
    private List<User> users;

    UserService(/*ProjectService projectService, */KeycloakService keycloakService) {
        //this.projectService = projectService;
        this.keycloakService = keycloakService;
    }

    // ***********************************************************************
    // UserDetailsService
    // ***********************************************************************

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Transactional
    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        List<User> users = keycloakService.getKeycloakUsers();
        for(User user : users){
            if (user.getUsername().equals(username)){
                return user;
            }
        }
        throw new UsernameNotFoundException("User not found with username: " + username);
    }

    // ***********************************************************************

    public List<User> findAll() {
        return getUsers();
    }

    @Transactional
    public List<User> SetupUsers(){
        List<User> kcUsers = keycloakService.getKeycloakUsers();
        return kcUsers;
    }

    // ***********************************************************************
    
    public Optional<User> findById(String id){
        return getUsers().stream().filter(x -> x.getName().equals(id)).findFirst();
    }

    @EventListener
    public void onLoginSuccess(AuthenticationSuccessEvent successEvent) {
        log.info("Authentication success: {}", successEvent.toString());
        this.users = null;
    }

    @EventListener
    public void onLoginFailure(AbstractAuthenticationFailureEvent failureEvent) {
        log.info("Authentication failure: {}", failureEvent.toString());
    }

    private List<User> getUsers(){
        if (this.users == null){
            this.users = SetupUsers();
            //projectService.setupProjects();
        }
        return this.users;
    }

}
