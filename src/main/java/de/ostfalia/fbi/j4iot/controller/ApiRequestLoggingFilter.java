package de.ostfalia.fbi.j4iot.controller;

import de.ostfalia.fbi.j4iot.configuration.ApiConfiguration;
import jakarta.servlet.*;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletRequestWrapper;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
public class ApiRequestLoggingFilter extends GenericFilter {

    private final Logger log = LoggerFactory.getLogger(ApiRequestLoggingFilter.class);
    private final String apiPathPrefix;
    private final Boolean isApiLogging;

    public ApiRequestLoggingFilter(ApiConfiguration apiConfiguration) {
        this.apiPathPrefix = apiConfiguration.getApiPathPrefix();
        this.isApiLogging = apiConfiguration.getLogging();
    }

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {
        HttpServletRequest req = (HttpServletRequest) request;
        String uri = ((HttpServletRequestWrapper) request).getRequestURI();
        boolean isApiPath = uri.startsWith(apiPathPrefix);

        if (isApiPath && isApiLogging) {
            log.info("HTTP {} {} contentType={} If-None-Match={} If-Modified-Since={}",
                    req.getMethod(), uri,
                    response.getContentType(),
                    req.getHeader("If-None-Match"),
                    req.getHeader("If-Modified-Since"));
        }

        chain.doFilter(request, response);

        HttpServletResponse res = (HttpServletResponse) response;
        if (isApiPath && isApiLogging) {
            log.info("HTTP response status={} contentType={} ETag={} Last-Modified={}",
                    res.getStatus(),
                    res.getContentType(),
                    res.getHeader("ETag"),
                    res.getHeader("Last-Modified"));
        }
    }
}
