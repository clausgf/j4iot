package de.ostfalia.fbi.j4iot.security.provider;

import de.ostfalia.fbi.j4iot.data.entity.User;
import de.ostfalia.fbi.j4iot.data.service.KeycloakService;
import de.ostfalia.fbi.j4iot.data.service.UserService;
import org.keycloak.representations.AccessTokenResponse;
import org.keycloak.representations.idm.UserSessionRepresentation;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.mapping.GrantedAuthoritiesMapper;
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
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.*;

@Component
public class OAuth2SessionAuthenticationProvider implements AuthenticationProvider {
    private static final long MAX_DIFF = 2000;
    private JwtDecoderFactory<ClientRegistration> jwtDecoderFactory = new OidcIdTokenDecoderFactory();

    private final KeycloakService keycloakService;
    private final UserService userService;
    private final GrantedAuthoritiesMapper authoritiesMapper ;


    public OAuth2SessionAuthenticationProvider(KeycloakService keycloakService, UserService userService, GrantedAuthoritiesMapper authoritiesMapper){
        this.keycloakService = keycloakService;
        this.userService = userService;
        this.authoritiesMapper = authoritiesMapper;
    }

    @Override
    public Authentication authenticate(Authentication authentication) throws AuthenticationException {
        OAuth2LoginAuthenticationToken token = (OAuth2LoginAuthenticationToken) authentication;

        List<UserSessionRepresentation> sessions = keycloakService.getUserSessions();
        sessions.sort((a1, a2) -> Long.valueOf(a1.getStart() - a2.getStart()).intValue());
        UserSessionRepresentation session = sessions.get(0);
        if (Instant.now().toEpochMilli() - session.getStart() > MAX_DIFF){
            return null;
        }
        AccessTokenResponse aToken = keycloakService.keycloak.tokenManager().getAccessToken();
        Set<String> scopes = new HashSet<>();
        scopes.add("email");
        scopes.add("openid");
        Map<String, Object> additional = new HashMap<>();
        additional.put("refresh_expires_in", aToken.getRefreshExpiresIn());
        additional.put("not-before-policy", aToken.getNotBeforePolicy());
        additional.put("id_token", aToken.getIdToken());
        additional.put("session_state", aToken.getSessionState());

        OAuth2AccessTokenResponse tokenResponse = OAuth2AccessTokenResponse
                .withToken(aToken.getToken())
                .expiresIn(aToken.getExpiresIn())
                .refreshToken(aToken.getRefreshToken())
                .scopes(scopes)
                .tokenType(OAuth2AccessToken.TokenType.BEARER)
                .additionalParameters(additional)
                .build();
        Collection<? extends GrantedAuthority> mappedAuthorities;
        OidcUser user = (User) userService.loadUserByUsername(session.getUsername());
        mappedAuthorities = user.getAuthorities();
        OAuth2LoginAuthenticationToken authenticationResult = new OAuth2LoginAuthenticationToken(
                token.getClientRegistration(),
                token.getAuthorizationExchange(), user, mappedAuthorities,
                tokenResponse.getAccessToken(), tokenResponse.getRefreshToken());
        authenticationResult.setDetails(token.getDetails());
        return authenticationResult;
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
            OAuth2Error invalidIdTokenError = new OAuth2Error("INVALID_ID_TOKEN_ERROR_CODE", ex.getMessage(), null);
            throw new OAuth2AuthenticationException(invalidIdTokenError, invalidIdTokenError.toString(), ex);
        }
    }


    @Override
    public boolean supports(Class<?> authentication) {
        return OAuth2LoginAuthenticationToken.class.isAssignableFrom(authentication);
    }
}
