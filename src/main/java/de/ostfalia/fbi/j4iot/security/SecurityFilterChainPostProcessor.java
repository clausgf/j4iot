package de.ostfalia.fbi.j4iot.security;

import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.beans.BeansException;
import org.springframework.jmx.export.annotation.ManagedResource;
import org.springframework.security.web.DefaultSecurityFilterChain;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;
import org.springframework.stereotype.Component;

@ManagedResource(
        objectName="cloudfoundry.identity:name=FilterChainProcessor",
        description = "Ability to dump requests through JMX"
)
@Component
public class SecurityFilterChainPostProcessor implements BeanPostProcessor {
    private static final String LOGIN_FILTERCHAIN = "VaadinSecurityFilterChainBean____";

    @Override
    public Object postProcessAfterInitialization(Object bean, String beanName) throws BeansException {
        if (bean instanceof SecurityFilterChain && beanName.equals(LOGIN_FILTERCHAIN)) {
            DefaultSecurityFilterChain ch = (DefaultSecurityFilterChain) bean;
            for(int i = 0; i < ch.getFilters().size(); i++){
                if (ch.getFilters().get(i) instanceof UsernamePasswordAuthenticationFilter){
                    UsernamePasswordAuthenticationFilter loginFilter = (UsernamePasswordAuthenticationFilter)ch.getFilters().get(i);
                    loginFilter.setPostOnly(false);
                    loginFilter.setRequiresAuthenticationRequestMatcher(new AntPathRequestMatcher("/ui/login", "GET"));

                    break;
                }
            }
            return bean;
        }
        return bean;
    }

    @Override
    public Object postProcessBeforeInitialization(Object bean, String beanName) throws BeansException {
        return bean;
    }
}
