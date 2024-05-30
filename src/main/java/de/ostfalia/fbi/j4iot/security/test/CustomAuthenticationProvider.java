package de.ostfalia.fbi.j4iot.security.test;

import de.ostfalia.fbi.j4iot.data.entity.User;
import de.ostfalia.fbi.j4iot.data.service.KeycloakService;
import de.ostfalia.fbi.j4iot.data.service.UserService;
import org.springframework.http.*;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.oauth2.client.authentication.OAuth2LoginAuthenticationToken;
import org.springframework.security.oauth2.client.oidc.authentication.OidcIdTokenDecoderFactory;
import org.springframework.security.oauth2.client.registration.ClientRegistration;
import org.springframework.security.oauth2.core.OAuth2AccessToken;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.OAuth2Error;
import org.springframework.security.oauth2.core.endpoint.OAuth2AccessTokenResponse;
import org.springframework.security.oauth2.core.oidc.OidcIdToken;
import org.springframework.security.oauth2.core.oidc.endpoint.OidcParameterNames;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtDecoderFactory;
import org.springframework.security.oauth2.jwt.JwtException;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.*;

//@Component
public class CustomAuthenticationProvider implements AuthenticationProvider {

    //private final LoginValidator validator = new LoginValidatorREST();
    //private final LoginValidator validator = new LoginValidatorKeycloakClass();
    private final LoginValidator validator;
    private UserService userService;
    private KeycloakService keycloakService;

    public CustomAuthenticationProvider(UserService userService, KeycloakService keycloakService){
        this.userService = userService;
        this.keycloakService = keycloakService;
        validator = new LoginValidatorWithSessions(keycloakService);
        //validator = new LoginValidatorProxy(keycloakService);
        //validator = new LoginValidatorREST();
    }

    @Override
    public Authentication authenticate(final Authentication authentication) throws AuthenticationException {
        String name = authentication.getName();
        String password = authentication.getCredentials().toString();
        if (authentication instanceof OAuth2LoginAuthenticationToken tt){
            Object body = test(tt);
            OAuth2AccessTokenResponse tokenResponse = (OAuth2AccessTokenResponse) body;
            OidcIdToken idToken = createOidcToken(tt.getClientRegistration(), tokenResponse);
            List<? extends GrantedAuthority> authorities = new ArrayList<>();
            OidcUser user = new User("fuehner", "Claus", "Fuehner", "email",
                    Instant.now().plus(5, ChronoUnit.HOURS), authorities);
            OAuth2LoginAuthenticationToken authenticationResult = new OAuth2LoginAuthenticationToken(
                    tt.getClientRegistration(),
                    tt.getAuthorizationExchange(), user, user.getAuthorities(),
                    tokenResponse.getAccessToken(), tokenResponse.getRefreshToken());
            authenticationResult.setDetails(tt.getDetails());
            return authenticationResult;
        }
        if (!validator.loginIsValid(name, password)) {
            return null;
        }
        name = validator.getName();
        password = validator.getPassword();
        return authenticateAgainstThirdPartyAndGetAuthentication(name, password);
    }
    private UsernamePasswordAuthenticationToken authenticateAgainstThirdPartyAndGetAuthentication(String name, String password) {
        final UserDetails principal = userService.loadUserByUsername(name);
        return new UsernamePasswordAuthenticationToken(principal, password, principal.getAuthorities());
    }

    private Object test(OAuth2LoginAuthenticationToken token){
        RestTemplate restTemplate = new RestTemplate();

        // URL des Endpunkts
        String url = "http://localhost:8280/realms/thomseddon/protocol/openid-connect/token";

        // Request-Header
        LinkedMultiValueMap<String, String> body = new LinkedMultiValueMap<>();
        body.add("grant_type", "authorization_code");
        body.add("code", token.getAuthorizationExchange().getAuthorizationResponse().getCode());
        body.add("redirect_uri", token.getAuthorizationExchange().getAuthorizationRequest().getRedirectUri());

        HttpHeaders headers = new HttpHeaders();
        HttpHeaders defaultHeaders = new HttpHeaders();
        defaultHeaders.setAccept(Collections.singletonList(MediaType.APPLICATION_JSON_UTF8));
        final MediaType contentType = MediaType.valueOf(MediaType.APPLICATION_FORM_URLENCODED_VALUE + ";charset=UTF-8");
        defaultHeaders.setContentType(contentType);
        headers.addAll(defaultHeaders);
        String clientId = encodeClientCredential("j4iot");
        String clientSecret = encodeClientCredential("OW9zETMvGJ4h3HtQLm0sgQpUEHHlBZBE");
        headers.setBasicAuth(clientId, clientSecret);

        // Request-Entity mit Header und Body erstellen
        HttpEntity<MultiValueMap<String, String>> requestEntity = new HttpEntity<>(body, headers);
        Map<String, String> map = new HashMap<>();
        ResponseEntity<String> responseEntity = null;
        try {
            // Senden der POST-Anfrage und Empfangen der Antwort
            responseEntity = restTemplate.exchange(url, HttpMethod.POST, requestEntity, String.class);
            String str = responseEntity.getBody().replace("\"", "");
            str = str.substring(1, str.length()-1);
            for(String pair : str.split(",")){
                String[] keyValue = pair.split(":");
                map.put(keyValue[0], keyValue[1]);
            }
        }
        catch(Exception ex){
            return null;
        }
        Map<String, Object> additional = new HashMap<>();
        additional.put("refresh_expires_in", Long.valueOf(map.get("refresh_expires_in")));
        additional.put("not-before-policy", Long.valueOf(map.get("not-before-policy")));
        additional.put("id_token", map.get("id_token"));
        additional.put("session_state", map.get("session_state"));
        OAuth2AccessTokenResponse response = OAuth2AccessTokenResponse
                .withToken(map.get("access_token"))
                .expiresIn(Long.valueOf(map.get("expires_in")))
                .refreshToken(map.get("refresh_token"))
                .scopes(new HashSet<>(Arrays.asList(map.get("scope").split(" "))))
                .tokenType(OAuth2AccessToken.TokenType.BEARER)
                .additionalParameters(additional)
                .build();
        return response;
    }

    private JwtDecoderFactory<ClientRegistration> jwtDecoderFactory = new OidcIdTokenDecoderFactory();

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
            OAuth2Error invalidIdTokenError = new OAuth2Error("INVALID_ID_TOKEN_ERROR_CODE", ex.getMessage(), null);
            throw new OAuth2AuthenticationException(invalidIdTokenError, invalidIdTokenError.toString(), ex);
        }
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

    @Override
    public boolean supports(Class<?> authentication) {
        return (UsernamePasswordAuthenticationToken.class.isAssignableFrom(authentication))
                //|| (OAuth2LoginAuthenticationToken.class.isAssignableFrom(authentication))
                ;
    }
}
