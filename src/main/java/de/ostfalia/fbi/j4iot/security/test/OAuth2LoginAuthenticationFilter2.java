package de.ostfalia.fbi.j4iot.security.test;

import de.ostfalia.fbi.j4iot.data.entity.User;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.core.convert.converter.Converter;
import org.springframework.http.*;
import org.springframework.http.converter.FormHttpMessageConverter;
import org.springframework.security.authentication.InternalAuthenticationServiceException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.authority.mapping.GrantedAuthoritiesMapper;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClient;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClientService;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.security.oauth2.client.authentication.OAuth2LoginAuthenticationToken;
import org.springframework.security.oauth2.client.http.OAuth2ErrorResponseErrorHandler;
import org.springframework.security.oauth2.client.oidc.authentication.OidcIdTokenDecoderFactory;
import org.springframework.security.oauth2.client.oidc.userinfo.OidcUserRequest;
import org.springframework.security.oauth2.client.oidc.userinfo.OidcUserService;
import org.springframework.security.oauth2.client.registration.ClientRegistration;
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserService;
import org.springframework.security.oauth2.client.web.*;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.OAuth2Error;
import org.springframework.security.oauth2.core.endpoint.*;
import org.springframework.security.oauth2.core.http.converter.OAuth2AccessTokenResponseHttpMessageConverter;
import org.springframework.security.oauth2.core.oidc.OidcIdToken;
import org.springframework.security.oauth2.core.oidc.endpoint.OidcParameterNames;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtDecoderFactory;
import org.springframework.security.oauth2.jwt.JwtException;
import org.springframework.security.web.DefaultRedirectStrategy;
import org.springframework.security.web.RedirectStrategy;
import org.springframework.security.web.authentication.AbstractAuthenticationProcessingFilter;
import org.springframework.security.web.authentication.session.NullAuthenticatedSessionStrategy;
import org.springframework.security.web.authentication.session.SessionAuthenticationStrategy;
import org.springframework.security.web.util.UrlUtils;
import org.springframework.util.Assert;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.util.StringUtils;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.time.temporal.TemporalUnit;
import java.util.*;

public class OAuth2LoginAuthenticationFilter2 extends AbstractAuthenticationProcessingFilter {
    public static final String DEFAULT_FILTER_PROCESSES_URI = "/login/oauth2/code/*";
    private static final String AUTHORIZATION_REQUEST_NOT_FOUND_ERROR_CODE = "authorization_request_not_found";
    private static final String CLIENT_REGISTRATION_NOT_FOUND_ERROR_CODE = "client_registration_not_found";
    private ClientRegistrationRepository clientRegistrationRepository;
    private OAuth2AuthorizedClientRepository authorizedClientRepository;
    private AuthorizationRequestRepository<OAuth2AuthorizationRequest> authorizationRequestRepository;
    private Converter<OAuth2LoginAuthenticationToken, OAuth2AuthenticationToken> authenticationResultConverter;



    private JwtDecoderFactory<ClientRegistration> jwtDecoderFactory = new OidcIdTokenDecoderFactory();

    private static final String INVALID_ID_TOKEN_ERROR_CODE = "invalid_id_token";

    private final OAuth2UserService<OidcUserRequest, OidcUser> userService = new OidcUserService();

    public GrantedAuthoritiesMapper authoritiesMapper = ((authorities) -> authorities);

    public OAuth2LoginAuthenticationFilter2(ClientRegistrationRepository clientRegistrationRepository, OAuth2AuthorizedClientService authorizedClientService) {
        this(clientRegistrationRepository, authorizedClientService, "/login/oauth2/code/*");
    }

    public OAuth2LoginAuthenticationFilter2(ClientRegistrationRepository clientRegistrationRepository, OAuth2AuthorizedClientService authorizedClientService, String filterProcessesUrl) {
        this(clientRegistrationRepository, (OAuth2AuthorizedClientRepository)(new AuthenticatedPrincipalOAuth2AuthorizedClientRepository(authorizedClientService)), filterProcessesUrl);
    }

    public OAuth2LoginAuthenticationFilter2(ClientRegistrationRepository clientRegistrationRepository, OAuth2AuthorizedClientRepository authorizedClientRepository, String filterProcessesUrl) {
        super(filterProcessesUrl);
        this.authorizationRequestRepository = new HttpSessionOAuth2AuthorizationRequestRepository();
        this.authenticationResultConverter = this::createAuthenticationResult;
        Assert.notNull(clientRegistrationRepository, "clientRegistrationRepository cannot be null");
        Assert.notNull(authorizedClientRepository, "authorizedClientRepository cannot be null");
        this.clientRegistrationRepository = clientRegistrationRepository;
        this.authorizedClientRepository = authorizedClientRepository;
    }

    private static String encodeClientCredential(String clientCredential) {
        try {
            return URLEncoder.encode(clientCredential, StandardCharsets.UTF_8.toString());
        }
        catch (UnsupportedEncodingException ex) {
            // Will not happen since UTF-8 is a standard charset
            throw new IllegalArgumentException(ex);
        }
    }

    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {
        doFilter((HttpServletRequest) request, (HttpServletResponse) response, chain);
    }

    private RedirectStrategy authorizationRedirectStrategy = new DefaultRedirectStrategy();

    private void doFilter(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
            throws IOException, ServletException {
        Authentication auth1 = SecurityContextHolder.getContext().getAuthentication();
        boolean auth2 = false;
        if (auth1 != null){
            auth2 = auth1.isAuthenticated();
        }
        if (!requiresAuthentication(request, response)) {
            chain.doFilter(request, response);
            return;
        }
        try {
            Authentication authenticationResult = attemptAuthentication(request, response);
            if (authenticationResult == null) {
                // return immediately as subclass has indicated that it hasn't completed
                return;
            }
            final List<GrantedAuthority> grantedAuths = new ArrayList<>();
            grantedAuths.add(new SimpleGrantedAuthority("ROLE_ADMIN"));
            User user = new User("fuehner", "fuehner", "fuehner", "fuehner", Instant.now().plus(5, ChronoUnit.HOURS));
            authenticationResult = new UsernamePasswordAuthenticationToken(user, "fuehner", grantedAuths);

            this.sessionStrategy.onAuthentication(authenticationResult, request, response);
            // Authentication success
            successfulAuthentication(request, response, chain, authenticationResult);

            /*auth1 = SecurityContextHolder.getContext().getAuthentication();
            auth2 = false;
            if (auth1 != null){
                auth2 = auth1.isAuthenticated();
            }*/

            /*String encodedRedirectURL = response.encodeRedirectURL(
                    request.getContextPath() + "/ui/about");

            response.setStatus(HttpStatus.TEMPORARY_REDIRECT.value());
            response.setHeader("Location", encodedRedirectURL);*/
            //authorizationRedirectStrategy.sendRedirect(request, response, "https://localhost:8080/iot/ui/login?logout");
        }
        catch (InternalAuthenticationServiceException failed) {
            this.logger.error("An internal error occurred while trying to authenticate the user.", failed);
            unsuccessfulAuthentication(request, response, failed);
        }
        catch (AuthenticationException ex) {
            // Authentication failed
            unsuccessfulAuthentication(request, response, ex);
        }
    }

    private SessionAuthenticationStrategy sessionStrategy = new NullAuthenticatedSessionStrategy();

    public void setSessionAuthenticationStrategy(SessionAuthenticationStrategy sessionStrategy) {
        super.setSessionAuthenticationStrategy(sessionStrategy);
        this.sessionStrategy = sessionStrategy;
    }

    public Authentication attemptAuthentication(HttpServletRequest request, HttpServletResponse response) throws AuthenticationException {
        Authentication auth1 = SecurityContextHolder.getContext().getAuthentication();
        boolean auth2 = false;
        if (auth1 != null){
            auth2 = auth1.isAuthenticated();
        }
        MultiValueMap<String, String> params = toMultiMap(request.getParameterMap());
        LinkedMultiValueMap<String, String> body = new LinkedMultiValueMap<>();
        body.add("grant_type", "authorization_code");
        body.add("code", params.get("code").get(0));
        body.add("redirect_uri", "http://localhost:8080/iot/login/oauth2/code/keycloak");

        HttpHeaders headers = new HttpHeaders();
        HttpHeaders defaultHeaders = new HttpHeaders();
        defaultHeaders.setAccept(Collections.singletonList(MediaType.APPLICATION_JSON_UTF8));
        final MediaType contentType = MediaType.valueOf(MediaType.APPLICATION_FORM_URLENCODED_VALUE + ";charset=UTF-8");
        defaultHeaders.setContentType(contentType);
        headers.addAll(defaultHeaders);
        String clientId = encodeClientCredential("j4iot");
        String clientSecret = encodeClientCredential("OW9zETMvGJ4h3HtQLm0sgQpUEHHlBZBE");
        headers.setBasicAuth(clientId, clientSecret);

        RequestEntity s = new RequestEntity(body, headers, HttpMethod.POST, URI.create("http://localhost:8280/realms/thomseddon/protocol/openid-connect/token"));

        RestTemplate restTemplate = new RestTemplate(Arrays.asList(new FormHttpMessageConverter(), new OAuth2AccessTokenResponseHttpMessageConverter()));
        restTemplate.setErrorHandler(new OAuth2ErrorResponseErrorHandler());
        ResponseEntity<OAuth2AccessTokenResponse> res = restTemplate.exchange(s, OAuth2AccessTokenResponse.class);

        OAuth2AccessTokenResponse tokenResponse = res.getBody();

        ClientRegistration clientRegistration = this.clientRegistrationRepository.findByRegistrationId("keycloak");
        OAuth2AuthorizationRequest authorizationRequest = this.authorizationRequestRepository
                .removeAuthorizationRequest(request, response);

        String redirectUri = UriComponentsBuilder.fromHttpUrl(UrlUtils.buildFullRequestUrl(request))
                .replaceQuery(null)
                .build()
                .toUriString();
        OAuth2AuthorizationResponse authorizationResponse = convert(params, redirectUri);
        Object authenticationDetails = this.authenticationDetailsSource.buildDetails(request);
        OAuth2LoginAuthenticationToken authenticationRequest = new OAuth2LoginAuthenticationToken(clientRegistration,
                new OAuth2AuthorizationExchange(authorizationRequest, authorizationResponse));
        authenticationRequest.setDetails(authenticationDetails);





        OidcIdToken idToken = createOidcToken(clientRegistration, tokenResponse);
        OidcUser oidcUser = this.userService.loadUser(new OidcUserRequest(clientRegistration,
                tokenResponse.getAccessToken(), idToken, tokenResponse.getAdditionalParameters()));

        Collection<? extends GrantedAuthority> mappedAuthorities = this.authoritiesMapper
                .mapAuthorities(oidcUser.getAuthorities());
        OAuth2LoginAuthenticationToken authenticationResult = new OAuth2LoginAuthenticationToken(
                clientRegistration,
                authenticationRequest.getAuthorizationExchange(), oidcUser, mappedAuthorities,
                tokenResponse.getAccessToken(), tokenResponse.getRefreshToken());
        authenticationResult.setDetails(authenticationRequest.getDetails());



        OAuth2AuthenticationToken oauth2Authentication = (OAuth2AuthenticationToken)this.authenticationResultConverter.convert(authenticationResult);
        Assert.notNull(oauth2Authentication, "authentication result cannot be null");
        oauth2Authentication.setDetails(authenticationDetails);
        OAuth2AuthorizedClient authorizedClient = new OAuth2AuthorizedClient(authenticationResult.getClientRegistration(),
                oauth2Authentication.getName(), authenticationResult.getAccessToken(), authenticationResult.getRefreshToken());
        this.authorizedClientRepository.saveAuthorizedClient(authorizedClient, oauth2Authentication, request, response);
        System.out.println("Fertig");
        return oauth2Authentication;
    }


    private OidcIdToken createOidcToken(ClientRegistration clientRegistration,
                                        OAuth2AccessTokenResponse accessTokenResponse) {
        JwtDecoder jwtDecoder = this.jwtDecoderFactory.createDecoder(clientRegistration);
        Jwt jwt = getJwt(accessTokenResponse, jwtDecoder);
        OidcIdToken idToken = new OidcIdToken(jwt.getTokenValue(), jwt.getIssuedAt(), jwt.getExpiresAt(),
                jwt.getClaims());
        return idToken;
    }

    private Jwt getJwt(OAuth2AccessTokenResponse accessTokenResponse, JwtDecoder jwtDecoder) {
        try {
            Map<String, Object> parameters = accessTokenResponse.getAdditionalParameters();
            return jwtDecoder.decode((String) parameters.get(OidcParameterNames.ID_TOKEN));
        }
        catch (JwtException ex) {
            OAuth2Error invalidIdTokenError = new OAuth2Error(INVALID_ID_TOKEN_ERROR_CODE, ex.getMessage(), null);
            throw new OAuth2AuthenticationException(invalidIdTokenError, invalidIdTokenError.toString(), ex);
        }
    }

    public final void setAuthorizationRequestRepository(AuthorizationRequestRepository<OAuth2AuthorizationRequest> authorizationRequestRepository) {
        Assert.notNull(authorizationRequestRepository, "authorizationRequestRepository cannot be null");
        this.authorizationRequestRepository = authorizationRequestRepository;
    }

    public final void setAuthenticationResultConverter(Converter<OAuth2LoginAuthenticationToken, OAuth2AuthenticationToken> authenticationResultConverter) {
        Assert.notNull(authenticationResultConverter, "authenticationResultConverter cannot be null");
        this.authenticationResultConverter = authenticationResultConverter;
    }

    private OAuth2AuthenticationToken createAuthenticationResult(OAuth2LoginAuthenticationToken authenticationResult) {
        return new OAuth2AuthenticationToken(authenticationResult.getPrincipal(), authenticationResult.getAuthorities(), authenticationResult.getClientRegistration().getRegistrationId());
    }

    static MultiValueMap<String, String> toMultiMap(Map<String, String[]> map) {
        MultiValueMap<String, String> params = new LinkedMultiValueMap(map.size());
        map.forEach((key, values) -> {
            if (values.length > 0) {
                String[] var3 = values;
                int var4 = values.length;

                for(int var5 = 0; var5 < var4; ++var5) {
                    String value = var3[var5];
                    params.add(key, value);
                }
            }

        });
        return params;
    }

    static OAuth2AuthorizationResponse convert(MultiValueMap<String, String> request, String redirectUri) {
        String code = (String)request.getFirst("code");
        String errorCode = (String)request.getFirst("error");
        String state = (String)request.getFirst("state");
        if (StringUtils.hasText(code)) {
            return OAuth2AuthorizationResponse.success(code).redirectUri(redirectUri).state(state).build();
        } else {
            String errorDescription = (String)request.getFirst("error_description");
            String errorUri = (String)request.getFirst("error_uri");
            return OAuth2AuthorizationResponse.error(errorCode).redirectUri(redirectUri).errorDescription(errorDescription).errorUri(errorUri).state(state).build();
        }
    }

    static boolean isAuthorizationResponse(MultiValueMap<String, String> request) {
        return isAuthorizationResponseSuccess(request) || isAuthorizationResponseError(request);
    }

    static boolean isAuthorizationResponseSuccess(MultiValueMap<String, String> request) {
        return StringUtils.hasText((String)request.getFirst("code")) && StringUtils.hasText((String)request.getFirst("state"));
    }

    static boolean isAuthorizationResponseError(MultiValueMap<String, String> request) {
        return StringUtils.hasText((String)request.getFirst("error")) && StringUtils.hasText((String)request.getFirst("state"));
    }
}
