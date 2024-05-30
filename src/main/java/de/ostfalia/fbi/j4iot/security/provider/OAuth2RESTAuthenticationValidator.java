package de.ostfalia.fbi.j4iot.security.provider;

import org.springframework.http.*;
import org.springframework.security.oauth2.client.authentication.OAuth2LoginAuthenticationToken;
import org.springframework.security.oauth2.core.AuthorizationGrantType;
import org.springframework.security.oauth2.core.OAuth2AccessToken;
import org.springframework.security.oauth2.core.endpoint.OAuth2AccessTokenResponse;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.web.client.RestTemplate;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.*;

public class OAuth2RESTAuthenticationValidator implements OAuth2AuthenticationValidator {

    @Override
    public OAuth2AccessTokenResponse authenticate(OAuth2LoginAuthenticationToken token) {
        RestTemplate restTemplate = new RestTemplate();

        // URL des Endpunkts
        final String url = token.getClientRegistration().getProviderDetails().getTokenUri();

        // Request-Header
        HttpHeaders headers = new HttpHeaders();
        headers.setAccept(Collections.singletonList(MediaType.APPLICATION_JSON_UTF8));
        headers.setContentType(MediaType.valueOf(MediaType.APPLICATION_FORM_URLENCODED_VALUE + ";charset=UTF-8"));
        headers.setBasicAuth(encodeClientCredential(token.getClientRegistration().getClientId()),
                encodeClientCredential(token.getClientRegistration().getClientSecret()));

        //Request-Body
        LinkedMultiValueMap<String, String> body = new LinkedMultiValueMap<>();
        body.add("grant_type", AuthorizationGrantType.AUTHORIZATION_CODE.getValue());
        body.add("code", token.getAuthorizationExchange().getAuthorizationResponse().getCode());
        body.add("redirect_uri", token.getAuthorizationExchange().getAuthorizationRequest().getRedirectUri());

        OAuth2AccessTokenResponse response = null;
        try {
            // Senden der POST-Anfrage und Empfangen der Antwort
            ResponseEntity<String> responseEntity = restTemplate.exchange(url, HttpMethod.POST, new HttpEntity<>(body, headers), String.class);

            String str = responseEntity.getBody().replace("\"", "");
            str = str.substring(1, str.length()-1);
            Map<String, String> bodyKeyValues = new HashMap<>();
            for(String pair : str.split(",")){
                String[] keyValue = pair.split(":");
                bodyKeyValues.put(keyValue[0], keyValue[1]);
            }
            Map<String, Object> additionalParameters = new HashMap<>();
            additionalParameters.put("refresh_expires_in", Long.valueOf(bodyKeyValues.get("refresh_expires_in")));
            additionalParameters.put("not-before-policy", Long.valueOf(bodyKeyValues.get("not-before-policy")));
            additionalParameters.put("id_token", bodyKeyValues.get("id_token"));
            additionalParameters.put("session_state", bodyKeyValues.get("session_state"));
            response = OAuth2AccessTokenResponse
                    .withToken(bodyKeyValues.get("access_token"))
                    .expiresIn(Long.valueOf(bodyKeyValues.get("expires_in")))
                    .refreshToken(bodyKeyValues.get("refresh_token"))
                    .scopes(new HashSet<>(Arrays.asList(bodyKeyValues.get("scope").split(" "))))
                    .tokenType(OAuth2AccessToken.TokenType.BEARER)
                    .additionalParameters(additionalParameters)
                    .build();
        }
        catch(Exception ex){
            return null;
        }
        return response;
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
