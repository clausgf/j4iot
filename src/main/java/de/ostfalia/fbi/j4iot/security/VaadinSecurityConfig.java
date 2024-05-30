package de.ostfalia.fbi.j4iot.security;

import com.vaadin.flow.spring.security.VaadinWebSecurity;
import de.ostfalia.fbi.j4iot.data.service.KeycloakService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.security.authentication.ProviderManager;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.oauth2.client.OAuth2LoginConfigurer;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.authority.mapping.GrantedAuthoritiesMapper;
import org.springframework.security.oauth2.client.authentication.OAuth2LoginAuthenticationProvider;
import org.springframework.security.oauth2.client.web.OAuth2AuthorizationRequestRedirectFilter;
import org.springframework.security.oauth2.client.web.OAuth2LoginAuthenticationFilter;
import org.springframework.security.oauth2.core.oidc.user.OidcUserAuthority;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import java.util.Collection;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;


@Configuration
@EnableWebSecurity
public class VaadinSecurityConfig extends VaadinWebSecurity {
    @Autowired
    KeycloakService keycloakService;

    @Override
    @Order(2)
    protected void configure(HttpSecurity http) throws Exception {
        ProviderManager a;
        OAuth2LoginAuthenticationProvider a2;
        OAuth2LoginAuthenticationFilter a3;
        OAuth2LoginConfigurer a4;
        super.configure(http);

        // Icons from the line-awesome addon
        //http.authorizeHttpRequests(authorize -> authorize
        //        .requestMatchers(AntPathRequestMatcher.antMatcher(HttpMethod.GET, "/line-awesome/**/*.svg")).permitAll())
        //        .securityMatcher(AntPathRequestMatcher.antMatcher("/ui/**"));

        StringBuilder builder = new StringBuilder();
        builder.append("http://localhost:8080/iot");//Server name googlen.
        builder.append(OAuth2AuthorizationRequestRedirectFilter.DEFAULT_AUTHORIZATION_REQUEST_BASE_URI);
        builder.append("/");
        builder.append(KeycloakService.REGISTRATION_ID);
        final String loginURL = builder.toString();

        /*http.formLogin(x -> x.loginPage(loginURL)
                .passwordParameter("client")
                .usernameParameter("code"))
            .logout(x -> x.addLogoutHandler(new KeycloakLogoutHandler(keycloakService)))
            .addFilterBefore(new OAuth2AuthorizationRequestRedirectFilter(keycloakService.getClientRegistrationRepository()), UsernamePasswordAuthenticationFilter.class);
    */
        http.oauth2Login(x -> Customizer.withDefaults());
    }

    @Bean
    public GrantedAuthoritiesMapper userAuthoritiesMapperForKeycloak() {
        return authorities -> {
            Set<GrantedAuthority> mappedAuthorities = new HashSet<>();
            var authority = authorities.iterator().next();
            boolean isOidc = authority instanceof OidcUserAuthority;

            if (isOidc) {
                var oidcUserAuthority = (OidcUserAuthority) authority;
                var userInfo = oidcUserAuthority.getUserInfo();

                // Tokens can be configured to return roles under
                // Groups or REALM ACCESS hence have to check both
                if (userInfo.hasClaim("resource_access")) {
                    Map<String, Object> map = userInfo.getClaimAsMap("resource_access");
                    if (map.containsKey("j4iot")){
                        map = (Map<String, Object>) map.get("j4iot");
                        mappedAuthorities.addAll(((Collection<String>)map.get("roles")).stream().map(x -> new SimpleGrantedAuthority("ROLE_" + x.toUpperCase())).toList());
                    }
                }
            }
            return mappedAuthorities;
        };
    }

    /*
    @Override
    protected void configure(WebSecurity web) throws Exception {
        // WebSecurity is a layer above http security; ignoring security circumvents HttpSecurity
        //web.ignoring().requestMatchers(AntPathRequestMatcher.antMatcher("/api/**"));
        super.configure(web);
    }
*/
}
