package de.ostfalia.fbi.j4iot.security;

import com.vaadin.flow.spring.security.AuthenticationContext;
import de.ostfalia.fbi.j4iot.data.entity.User;
import de.ostfalia.fbi.j4iot.data.repository.UserRepository;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.annotation.Bean;
import org.springframework.security.authentication.AuthenticationEventPublisher;
import org.springframework.security.authentication.DefaultAuthenticationEventPublisher;
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

    public String getAuthenticatedUsername() {
        Optional<UserDetails> userDetails = getAuthenticatedUserDetails();
        return userDetails.map(UserDetails::getUsername).orElse(null);
    }

    public boolean isAuthenticatedUserAdmin() {
        return true; // TODO determine roles
    }

    public Long getAuthenticatedUserId() {
        Optional<UserDetails> userDetails = getAuthenticatedUserDetails();
        if (userDetails.isPresent()) {
            Optional<User> user = userRepository.findByName(userDetails.get().getUsername());
            return user.map(User::getId).orElse(null);
        }
        return null;
    }

    public Optional<User> getAuthenticatedUser() {
        Optional<UserDetails> userDetails = getAuthenticatedUserDetails();
        if (userDetails.isPresent()) {
            return userRepository.findByName(userDetails.get().getUsername());
        }
        return Optional.empty();
    }

    public String getAuthenticatedUserFullName() {
        Optional<User> optUser = getAuthenticatedUser();
        if (optUser.isPresent()) {
            User u = optUser.get();
            if (!u.getFirstName().isEmpty() && !u.getLastName().isEmpty()) {
                return u.getFirstName() + " " + u.getLastName();
            } else if (!u.getFirstName().isEmpty()) {
                return u.getFirstName();
            } else if (!u.getLastName().isEmpty()) {
                return u.getLastName();
            } else {
                return u.getName();
            }
        }
        return null;
    }

    public void logout() {
        authenticationContext.logout();
    }

    @Bean
    @ConditionalOnMissingBean(AuthenticationEventPublisher.class)
    DefaultAuthenticationEventPublisher defaultAuthenticationEventPublisher(ApplicationEventPublisher delegate) {
        return new DefaultAuthenticationEventPublisher(delegate);
    }
}
