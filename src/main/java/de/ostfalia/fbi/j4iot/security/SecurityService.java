package de.ostfalia.fbi.j4iot.security;

import com.vaadin.flow.spring.security.AuthenticationContext;
import de.ostfalia.fbi.j4iot.data.entity.User;
import de.ostfalia.fbi.j4iot.data.repository.UserRepository;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
public class SecurityService {
    private final AuthenticationContext authenticationContext;
    private final UserRepository userRepository;

    public SecurityService(AuthenticationContext authenticationContext, UserRepository userRepository) {
        this.authenticationContext = authenticationContext;
        this.userRepository = userRepository;
    }

    public Optional<UserDetails> getAuthenticatedUserDetails() {
        return authenticationContext.getAuthenticatedUser(UserDetails.class);
    }

    public Optional<User> getAuthenticatedUser() {
        UserDetails userDetails = authenticationContext.getAuthenticatedUser(UserDetails.class).get();
        return userRepository.findOneByName(userDetails.getUsername());
    }

    public String getAuthenticatedUsername() {
        String username = "";
        Optional<UserDetails> userDetails = getAuthenticatedUserDetails();
        if (userDetails.isPresent()) {
            username = userDetails.get().getUsername();
        }
        return username;
    }

    public String getAuthenticatedUserFullName() {
        String username = getAuthenticatedUsername();
        Optional<User> optUser = userRepository.findOneByName(username);
        if (optUser.isPresent()) {
            User u = optUser.get();
            if (!u.getFirstName().isEmpty() && !u.getLastName().isEmpty()) {
                username = u.getFirstName() + " " + u.getLastName();
            }
        }
        return username;
    }

    public void logout() {
        authenticationContext.logout();
    }
}
