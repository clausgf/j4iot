package de.ostfalia.fbi.j4iot.security.test;

import de.ostfalia.fbi.j4iot.data.service.KeycloakService;
import org.keycloak.representations.idm.UserSessionRepresentation;

import java.util.List;

public class LoginValidatorWithSessions implements LoginValidator{

    private KeycloakService keycloakService;

    private String name;
    private String password;

    public LoginValidatorWithSessions(KeycloakService keycloakService){
        this.keycloakService = keycloakService;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public String getPassword() {
        return password;
    }

    @Override
    public boolean loginIsValid(String name, String password) {
        this.name = name;
        this.password = password;

        try {
            List<UserSessionRepresentation> sessions = keycloakService.getUserSessions();
            sessions.sort((a1, a2) -> Long.valueOf(a1.getStart() - a2.getStart()).intValue());
            if (sessions.size() != 0){
                this.name = sessions.get(0).getUsername();
            }
        }
        catch(Exception ex){
            return false;
        }
        return true;
    }
}
