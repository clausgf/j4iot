package de.ostfalia.fbi.j4iot.security;

import de.ostfalia.fbi.j4iot.data.service.KeycloakService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.keycloak.admin.client.resource.UserResource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;
import org.springframework.security.web.authentication.logout.LogoutHandler;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.util.List;


@Component
public class KeycloakLogoutHandler implements LogoutHandler {

    private static final Logger LOGGER = LoggerFactory.getLogger(KeycloakLogoutHandler.class);
    private final RestTemplate restTemplate;
    private final KeycloakService keycloakService;

    public KeycloakLogoutHandler(KeycloakService keycloakService) {
        this.restTemplate = new RestTemplateBuilder().build();
        this.keycloakService = keycloakService;
    }

    @Override
    public void logout(HttpServletRequest request, HttpServletResponse response, Authentication auth) {
        logoutFromKeycloak((OidcUser) auth.getPrincipal());
    }

    private void logoutFromKeycloak(OidcUser user) {
        UserResource res = keycloakService.getUserResource(user);
        res.logout();
        
        /*String endSessionEndpoint = user.getIssuer() + "/protocol/openid-connect/logout";
        UriComponentsBuilder builder = UriComponentsBuilder
                .fromUriString(endSessionEndpoint)
                .queryParam("id_token_hint", user.getIdToken().getTokenValue());
        ResponseEntity<String> logoutResponse = restTemplate.getForEntity(
                builder.toUriString(), String.class);
        if (logoutResponse.getStatusCode().is2xxSuccessful()) {
            LOGGER.debug("Successfully logged out from Keycloak");
        } else {
            LOGGER.error("Could not propagate logout to Keycloak");
        }*/
    }

}
