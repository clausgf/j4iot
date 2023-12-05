package de.ostfalia.fbi.j4iot.controller;

import de.ostfalia.fbi.j4iot.configuration.ApiConfiguration;
import jakarta.servlet.*;
import jakarta.servlet.http.HttpServletRequestWrapper;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpStatus;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import java.io.IOException;
import java.nio.charset.StandardCharsets;

// https://dmytro-lazarenko.hashnode.dev/spring-boot-2-multiple-ports-for-internal-and-external-rest-apis-jetty
@Configuration
public class ApiEndpointsPortFilterConfig implements WebMvcConfigurer {

    private final ApiConfiguration apiConfiguration;

    public ApiEndpointsPortFilterConfig(ApiConfiguration apiConfiguration) {
        this.apiConfiguration = apiConfiguration;
    }

    // ***********************************************************************

    @Bean
    public FilterRegistrationBean<ApiEndpointsPortFilter> apiEndpointsFilter() {
        return new FilterRegistrationBean<>(new ApiEndpointsPortFilter(apiConfiguration));
    }

    // ***********************************************************************

    public static class ApiEndpointsPortFilter implements Filter {

        private final Logger log = LoggerFactory.getLogger(ApiEndpointsPortFilter.class);
        private final int apiPort;
        private final String apiPathPrefix;

        private final String BAD_REQUEST = String.format("{\"code\":%d,\"error\":true,\"errorMessage\":\"%s\"}",
                HttpStatus.BAD_REQUEST.value(), HttpStatus.BAD_REQUEST.getReasonPhrase());


        public ApiEndpointsPortFilter(ApiConfiguration apiConfiguration) {
            this.apiPort = apiConfiguration.getPort();
            this.apiPathPrefix = apiConfiguration.getApiPathPrefix();
        }

        // ***********************************************************************

        @Override
        public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {
            String uri = ((HttpServletRequestWrapper) request).getRequestURI();
            //log.debug("Filtering apiPathPrefix={} uri={}", apiPathPrefix, uri);
            boolean isApiPort = request.getLocalPort() == apiPort;
            boolean isApiPath = uri.startsWith(apiPathPrefix);

            if (apiPort > 0) {
                // allow api requests on api port only
                if ( (isApiPath && !isApiPort) || (!isApiPath && isApiPort) ) {
                    log.debug("Deny request from port={} uri={}", request.getLocalPort(), uri);
                    ((HttpServletResponse) response).setStatus(HttpStatus.BAD_REQUEST.value());
                    response.getOutputStream().write(BAD_REQUEST.getBytes(StandardCharsets.UTF_8));
                    response.getOutputStream().close();
                    return;
                }
            }

            chain.doFilter(request, response);
        }
    }

    // ***********************************************************************

}
