package de.ostfalia.fbi.j4iot.security;

import de.ostfalia.fbi.j4iot.data.service.DeviceService;
import jakarta.servlet.DispatcherType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;

@Configuration
@EnableWebSecurity
public class RestSecurityConfig {

    public static final String DEVICE_API_AUTHORITY = "DEVICE_API";

    @Autowired
    private DeviceService iotService;

    @Bean
    @Order(1)
    SecurityFilterChain restApi(HttpSecurity http) throws Exception {
        http
                .securityMatcher(AntPathRequestMatcher.antMatcher("/api/**"))
                .csrf(AbstractHttpConfigurer::disable)
                .sessionManagement((session) -> session
                        .sessionCreationPolicy(SessionCreationPolicy.STATELESS)
                )
                .addFilterBefore(new DeviceAuthenticationFilter(iotService), UsernamePasswordAuthenticationFilter.class)
                .authorizeHttpRequests((authorize) -> authorize
                        .dispatcherTypeMatchers(DispatcherType.FORWARD, DispatcherType.ERROR).permitAll()
                        .requestMatchers(AntPathRequestMatcher.antMatcher(HttpMethod.POST, "/api/provision"))
                            .permitAll()
                        //.requestMatchers(AntPathRequestMatcher.antMatcher(HttpMethod.GET, "/api/hello"))
                        //    .permitAll()
                        .anyRequest().authenticated() //.hasAuthority(DEVICE_API_AUTHORITY)
                );
        return http.build();
    }
}
