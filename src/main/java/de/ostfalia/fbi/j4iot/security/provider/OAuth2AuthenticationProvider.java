package de.ostfalia.fbi.j4iot.security.provider;

import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.mapping.GrantedAuthoritiesMapper;
import org.springframework.security.oauth2.client.authentication.OAuth2LoginAuthenticationToken;
import org.springframework.security.oauth2.client.oidc.authentication.OidcIdTokenDecoderFactory;
import org.springframework.security.oauth2.client.oidc.userinfo.OidcUserRequest;
import org.springframework.security.oauth2.client.oidc.userinfo.OidcUserService;
import org.springframework.security.oauth2.client.registration.ClientRegistration;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserService;
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

import java.util.Collection;
import java.util.Map;

/***
 * Nur ein Provider darf aktiv sein, da sonst keiner automatisch ergänzt wird. Aus diesem Grund ist dieser auskommentiert.
 */
//@Component
public class OAuth2AuthenticationProvider implements AuthenticationProvider {
    private JwtDecoderFactory<ClientRegistration> jwtDecoderFactory = new OidcIdTokenDecoderFactory();

    private final OAuth2AuthenticationValidator validator;
    private final OAuth2UserService<OidcUserRequest, OidcUser> userService = new OidcUserService();
    private final GrantedAuthoritiesMapper authoritiesMapper ;

    public OAuth2AuthenticationProvider(GrantedAuthoritiesMapper authoritiesMapper){
        validator = new OAuth2RESTAuthenticationValidator();
        this.authoritiesMapper = authoritiesMapper;
    }

    @Override
    public Authentication authenticate(Authentication authentication) throws AuthenticationException {
        OAuth2LoginAuthenticationToken token = (OAuth2LoginAuthenticationToken) authentication;
        OAuth2AccessTokenResponse tokenResponse = validator.authenticate(token);
        OidcIdToken idToken = createOidcToken(token.getClientRegistration(), tokenResponse);
        OidcUser user = this.userService.loadUser(new OidcUserRequest(token.getClientRegistration(),
                tokenResponse.getAccessToken(), idToken, tokenResponse.getAdditionalParameters()));
        Collection<? extends GrantedAuthority> mappedAuthorities = this.authoritiesMapper
                .mapAuthorities(user.getAuthorities());
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
