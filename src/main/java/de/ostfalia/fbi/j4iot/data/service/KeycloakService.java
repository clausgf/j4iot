package de.ostfalia.fbi.j4iot.data.service;

import de.ostfalia.fbi.j4iot.data.entity.User;
import org.jboss.resteasy.client.jaxrs.ResteasyClientBuilder;
import org.keycloak.admin.client.Keycloak;
import org.keycloak.admin.client.KeycloakBuilder;
import org.keycloak.admin.client.resource.UserResource;
import org.keycloak.representations.idm.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.client.registration.ClientRegistration;
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository;
import org.springframework.security.oauth2.client.registration.InMemoryClientRegistrationRepository;
import org.springframework.security.oauth2.core.AuthorizationGrantType;
import org.springframework.security.oauth2.core.ClientAuthenticationMethod;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Service
public class KeycloakService {

    public static final String REGISTRATION_ID = "keycloak";

    public final String SERVER_URL;
    public final String REALM_NAME;
    private final String USERNAME;
    private final String PASSWORD;
    public final String CLIENT_ID;
    public final String CLIENT_SECRET;
    public final String REDIRECT_URI;
    private final String PROJECT_ROLES_SUFFIX = "project_";

    private final ClientRegistrationRepository CLIENT_REGISTRATION;

    Logger log = LoggerFactory.getLogger(UserService.class);


    public final Keycloak keycloak;

    public KeycloakService(
            @Value("${keycloak.restapi.server-url}") String serverUrl,
            @Value("${spring.security.oauth2.client.registration.keycloak.client-id}") String client_id,
            @Value("${spring.security.oauth2.client.registration.keycloak.client-secret}") String client_secret,
            @Value("${spring.security.oauth2.client.registration.keycloak.redirect-uri}") String redirectUri,
            @Value("${keycloak.restapi.realm}")  String realm,
            @Value("${keycloak.restapi.username}") String username,
            @Value("${keycloak.restapi.password}") String password
    ){
        SERVER_URL = serverUrl;
        CLIENT_ID = client_id;
        CLIENT_SECRET = client_secret;
        REDIRECT_URI = redirectUri;
        REALM_NAME = realm;
        USERNAME = username;
        PASSWORD = password;
        CLIENT_REGISTRATION = new InMemoryClientRegistrationRepository(initClientRegistration());
        String ss = getClientRegistration().getRedirectUri();
        keycloak = keycloak();
        keycloak.tokenManager().getAccessToken();
    }

    private Keycloak keycloak() {
        return KeycloakBuilder.builder()
                .serverUrl(SERVER_URL)
                .realm(REALM_NAME)
                .username(USERNAME)
                .password(PASSWORD)
                .clientId(getClientRegistration().getClientId())
                .clientSecret(getClientRegistration().getClientSecret())
                .resteasyClient(ResteasyClientBuilder.newBuilder().build())
                .build();
    }

    private ClientRegistration initClientRegistration(){
        return ClientRegistration.withRegistrationId(REGISTRATION_ID)
                .clientId(CLIENT_ID)
                .clientSecret(CLIENT_SECRET)
                .clientAuthenticationMethod(ClientAuthenticationMethod.CLIENT_SECRET_BASIC)
                .authorizationGrantType(AuthorizationGrantType.AUTHORIZATION_CODE)
                .redirectUri(REDIRECT_URI)
                .authorizationUri(SERVER_URL+"realms/"+REALM_NAME+"/protocol/openid-connect/auth")
                .tokenUri(SERVER_URL+"realms/"+REALM_NAME+"/protocol/openid-connect/token")
                .userInfoUri(SERVER_URL+"realms/"+REALM_NAME+"/protocol/openid-connect/userinfo")
                .jwkSetUri(SERVER_URL+"realms/"+REALM_NAME+"/protocol/openid-connect/certs")
                .userNameAttributeName("preferred_username")
                .scope("openid")
                .build();
    }

    public List<UserSessionRepresentation> getUserSessions(){
        List<UserSessionRepresentation> result = keycloak.realm(REALM_NAME)
                .clients().get(getClientPK()).getUserSessions(0,100000);
        return removeAPIUserInSession(result);
    }

    public UserResource getUserResource(OidcUser user){
        return keycloak.realm(REALM_NAME).users().get(getUserPK(user.getName()));
    }

    public List<User> getKeycloakUsers() {
        log.info("GET all Keycloak users");
        List<UserRepresentation> users = getUserRepresentations();
        log.info("Users found {}", users.stream()
                .map(user -> user.getUsername())
                .collect(Collectors.toList()));
        List<User> result = new ArrayList<>();
        for(UserRepresentation kcUser : users){
            Map<String, ClientMappingsRepresentation> map = keycloak.realm(REALM_NAME).users().get(kcUser.getId()).roles().getAll().getClientMappings();
            List<RoleRepresentation> roleList = new ArrayList<>();
            if (map != null && map.containsKey(CLIENT_ID)){
                roleList = map.get(CLIENT_ID).getMappings();
            }
            User newUser = new User(kcUser.getUsername(), kcUser.getFirstName(), kcUser.getLastName(), kcUser.getEmail(), Instant.now(),
                roleList.stream().map(x -> new SimpleGrantedAuthority("ROLE_" + x.getName().toUpperCase())).toList());
            result.add(newUser);
            if (map != null && map.containsKey(CLIENT_ID)){
                newUser.getProjects().addAll(getProjects(roleList));
            }
        }
        return result;
    }

    public List<String> getKeycloakUsersByProject(String project){
        log.info("GET all Keycloak users");
        List<String> result = new ArrayList<>();
        List<UserRepresentation> users = getUserRepresentations();
        for(UserRepresentation user : users){
            Map<String, ClientMappingsRepresentation> map = keycloak.realm(REALM_NAME).users().get(user.getId()).roles().getAll().getClientMappings();
            if (map != null && map.containsKey(CLIENT_ID)){
                List<RoleRepresentation> roleR = map.get(CLIENT_ID).getMappings();
                System.out.println("Role Mapping:" + roleR);
                List<String> roleN = roleR.stream().map(x -> x.getName()).toList();
                System.out.println("Role Names: " + roleN);
                List<String> roleI = roleR.stream().map(x -> x.getId()).toList();
                System.out.println("Role Ids: " + roleI);

                for(RoleRepresentation role : roleR){
                    result.add(role.getName());
                }
            }
        }
        return result;
    }

    public List<String> getKeycloakProjects(){
        log.info("GET all Keycloak projects");
        List<String> result = getProjects(keycloak.realm(REALM_NAME).clients().get(getClientPK()).roles().list());
        log.info("Projects found {}", result);
        return result;
    }

    private List<String> getProjects(List<RoleRepresentation> roleList){
        return roleList.stream().map(x -> x.getName())
                .filter(x -> x.startsWith(PROJECT_ROLES_SUFFIX))
                .map(x -> x.substring(PROJECT_ROLES_SUFFIX.length()))
                .toList();
    }

    public ClientRegistration getClientRegistration(){
        return CLIENT_REGISTRATION.findByRegistrationId(REGISTRATION_ID);
    }

    public ClientRegistrationRepository getClientRegistrationRepository(){
        return CLIENT_REGISTRATION;
    }

    private String getUserPK(String username){
        List<UserRepresentation> users = keycloak.realm(REALM_NAME).users().list()
                .stream().filter(x -> x.getUsername().equals(username)).toList();
        return users != null && users.size() > 0 ? users.get(0).getId() : null;
    }

    private String getClientPK(){
        return keycloak.realm(REALM_NAME).clients().findAll().stream().filter(x -> x.getName().equals(CLIENT_ID)).findFirst().get().getId();
    }

    private List<UserRepresentation> getUserRepresentations(){
        return keycloak.realm(REALM_NAME).users().list().stream().filter(x -> !x.getUsername().equals(USERNAME)).toList();
    }

    private List<UserSessionRepresentation> removeAPIUserInSession(List<UserSessionRepresentation> list){
        if (list != null){
            list.removeIf(x -> x.getUsername().equals(USERNAME));
        }
        return list;
    }
}
