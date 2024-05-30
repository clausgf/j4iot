package de.ostfalia.fbi.j4iot.security.test;

import de.ostfalia.fbi.j4iot.data.service.UserService;
import org.springframework.http.*;
import org.springframework.http.converter.FormHttpMessageConverter;
import org.springframework.security.oauth2.client.http.OAuth2ErrorResponseErrorHandler;
import org.springframework.security.oauth2.client.oidc.authentication.OidcIdTokenDecoderFactory;
import org.springframework.security.oauth2.client.registration.ClientRegistration;
import org.springframework.security.oauth2.core.*;
import org.springframework.security.oauth2.core.endpoint.OAuth2AccessTokenResponse;
import org.springframework.security.oauth2.core.http.converter.OAuth2AccessTokenResponseHttpMessageConverter;
import org.springframework.security.oauth2.core.oidc.OidcIdToken;
import org.springframework.security.oauth2.core.oidc.endpoint.OidcParameterNames;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtDecoderFactory;
import org.springframework.security.oauth2.jwt.JwtException;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.web.client.RestTemplate;

import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Collections;
import java.util.Map;

public class LoginValidatorAuth implements LoginValidator {

    private UserService userService;
    private ClientRegistration clientRegistration;

    private String name;
    private String password;

    public LoginValidatorAuth(UserService userService){
        this.userService = userService;
        clientRegistration = clientRegistration();
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public String getPassword() {
        return password;
    }

    @Override
    public boolean loginIsValid(String name, String password) {
        this.name = name;
        this.password = password;

        try {

            LinkedMultiValueMap<String, String> body = new LinkedMultiValueMap<>();
            body.add("grant_type", "authorization_code");
            body.add("code", name);
            body.add("redirect_uri", "http://localhost:8080/iot/ui/login");

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

            OidcIdToken idToken = createOidcToken(clientRegistration, tokenResponse);
            this.name = idToken.getClaimAsString("preferred_username");
            int a = 0;
            a++;
            /*OidcUser oidcUser = this.userService.loadUser(new OidcUserRequest(clientRegistration,
                    tokenResponse.getAccessToken(), idToken, tokenResponse.getAdditionalParameters()));*/

        }
        catch(Exception ex){
            return false;
        }
        return true;
    }


    private ClientRegistration clientRegistration() {
        return ClientRegistration.withRegistrationId("keycloak")
                .clientId("j4iot")
                .clientSecret("OW9zETMvGJ4h3HtQLm0sgQpUEHHlBZBE")
                .clientAuthenticationMethod(ClientAuthenticationMethod.CLIENT_SECRET_BASIC)
                .authorizationGrantType(AuthorizationGrantType.AUTHORIZATION_CODE)
                .redirectUri("http://localhost:8080/iot/ui/login")
                .authorizationUri("http://localhost:8280/realms/thomseddon/protocol/openid-connect/auth")
                .tokenUri("http://localhost:8280/realms/thomseddon/protocol/openid-connect/token")
                .userInfoUri("http://localhost:8280/realms/thomseddon/protocol/openid-connect/userinfo")
                .jwkSetUri("http://localhost:8280/realms/thomseddon/protocol/openid-connect/certs")
                .userNameAttributeName("preferred_username")
                .scope("openid")
                .build();
    }

    private JwtDecoderFactory<ClientRegistration> jwtDecoderFactory = new OidcIdTokenDecoderFactory();

    private static final String INVALID_ID_TOKEN_ERROR_CODE = "invalid_id_token";

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

    private static String encodeClientCredential(String clientCredential) {
        try {
            return URLEncoder.encode(clientCredential, StandardCharsets.UTF_8.toString());
        }
        catch (UnsupportedEncodingException ex) {
            // Will not happen since UTF-8 is a standard charset
            throw new IllegalArgumentException(ex);
        }
    }
}
