package de.ostfalia.fbi.j4iot.security;

import com.vaadin.flow.spring.security.AuthenticationContext;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.annotation.Bean;
import org.springframework.security.authentication.AuthenticationEventPublisher;
import org.springframework.security.authentication.DefaultAuthenticationEventPublisher;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.core.oidc.user.DefaultOidcUser;
import org.springframework.stereotype.Component;

import java.util.Collection;
import java.util.LinkedList;
import java.util.Map;
import java.util.Optional;

@Component
public class SecurityService {

    @Value("${keycloak.client.admin}") String ADMIN_ROLE_NAME;

    private final AuthenticationContext authenticationContext;

    public SecurityService(AuthenticationContext authenticationContext) {
        this.authenticationContext = authenticationContext;
    }

    public Optional<DefaultOidcUser> getAuthenticatedUserDetails() {
        return authenticationContext.getAuthenticatedUser(DefaultOidcUser.class);
    }

    public String getAuthenticatedUsername() {
        Optional<DefaultOidcUser> userDetails = getAuthenticatedUserDetails();
        return userDetails.map(DefaultOidcUser::getName).orElse(null);
    }

    public boolean isAuthenticatedUserAdmin(){
        Collection<? extends GrantedAuthority> list = SecurityContextHolder.getContext().getAuthentication().getAuthorities();
        if (list != null && list.contains(new SimpleGrantedAuthority("ROLE_" + ADMIN_ROLE_NAME.toUpperCase()))) {
            return true;
        }
        return false;
    }

    public String getAuthenticatedUserId() {
        Optional<DefaultOidcUser> userDetails = getAuthenticatedUserDetails();
        if (userDetails.isPresent()) {
            return userDetails.get().getName();
        }
        return null;
    }

    public String getAuthenticatedUserFullName() {
        Optional<DefaultOidcUser> optUser = getAuthenticatedUserDetails();
        if (optUser.isPresent()) {
            return optUser.get().getFullName();
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
