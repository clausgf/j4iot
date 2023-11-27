package de.ostfalia.fbi.j4iot.security;

import com.vaadin.flow.spring.security.VaadinWebSecurity;
import de.ostfalia.fbi.j4iot.views.LoginView;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;

@Configuration
@EnableWebSecurity
public class VaadinSecurityConfig extends VaadinWebSecurity {

    @Override
    @Order(2)
    protected void configure(HttpSecurity http) throws Exception {
        //http.authorizeHttpRequests(auth -> auth.requestMatchers(AntPathRequestMatcher.antMatcher(HttpMethod.GET, "/images/*.png")).permitAll());
        http.securityMatcher(AntPathRequestMatcher.antMatcher("/ui/**")); // TODO /**
        super.configure(http);
        setLoginView(http, LoginView.class);
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
