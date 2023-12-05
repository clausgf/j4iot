package de.ostfalia.fbi.j4iot.security;

import de.ostfalia.fbi.j4iot.configuration.ApiConfiguration;
import de.ostfalia.fbi.j4iot.data.entity.DeviceToken;
import de.ostfalia.fbi.j4iot.data.service.DeviceService;
import jakarta.servlet.*;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletRequestWrapper;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;

import java.io.IOException;
import java.util.Collection;
import java.util.LinkedList;

public class DeviceAuthenticationFilter extends GenericFilter {

    private final Logger log = LoggerFactory.getLogger(RestSecurityConfig.class);
    public static final String DEVICE_API_AUTHORITY = "DEVICE_API";
    private final String AUTH_TOKEN_HEADER_NAME = "Authorization";

    private final DeviceService iotService;
    private final int apiPort;
    private final String apiPathPrefix;

    public DeviceAuthenticationFilter(ApiConfiguration apiConfiguration, DeviceService iotService) {
        this.apiPort = apiConfiguration.getPort();
        this.apiPathPrefix = apiConfiguration.getApiPathPrefix();
        this.iotService = iotService;
    }

    // ***********************************************************************

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain filterChain) throws ServletException, IOException {
        try {
            String uri = ((HttpServletRequestWrapper) request).getRequestURI();
            boolean isApiPort = request.getLocalPort() == apiPort;
            boolean isApiPath = uri.startsWith(apiPathPrefix);

            if (isApiPort && isApiPath) {
                String token = extractToken((HttpServletRequest) request);
                log.info("DeviceAuthenticationFilter filtering port={} uri={} token={}", request.getLocalPort(), uri, token);
                if (token != null) {
                    Authentication authentication = authenticate(token);
                    SecurityContextHolder.getContext().setAuthentication(authentication);
                }
            } else {
                log.info("DeviceAuthenticationFilter _not_ filtering port={} uri={}", request.getLocalPort(), uri);
            }
        } catch (Exception e) {
            HttpServletResponse httpResponse = (HttpServletResponse) response;
            httpResponse.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            httpResponse.setContentType(MediaType.APPLICATION_JSON_VALUE);
            // PrintWriter writer = httpResponse.getWriter();
            // writer.print(e.getMessage());
            // writer.flush();
            // writer.close();
        }

        filterChain.doFilter(request, response);
    }

    // ***********************************************************************

    private String extractToken(HttpServletRequest request) throws BadCredentialsException {
        String token = request.getHeader(AUTH_TOKEN_HEADER_NAME);
        if (token != null) {
            //throw new BadCredentialsException("Invalid device token in " + AUTH_TOKEN_HEADER_NAME + " header.");
            token = token.replaceAll("(?i)bearer ", "");
        }
        return token;
    }

    private Authentication authenticate(String token) throws BadCredentialsException {
        DeviceToken deviceToken = iotService.authenticateDeviceToken(token);
        if (deviceToken == null) {
            throw new BadCredentialsException("Invalid device token in " + AUTH_TOKEN_HEADER_NAME + " header.");
        }
        Collection<GrantedAuthority> authorities = new LinkedList<>();
        authorities.add(new SimpleGrantedAuthority(DEVICE_API_AUTHORITY));
        TokenAuthenticationToken authentication = new TokenAuthenticationToken(token, authorities);
        authentication.setDetails(deviceToken.getDevice());
        return authentication;
    }

    // ***********************************************************************

}
