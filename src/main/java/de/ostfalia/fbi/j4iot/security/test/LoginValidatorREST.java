package de.ostfalia.fbi.j4iot.security.test;

import org.springframework.http.*;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Collections;

public class LoginValidatorREST implements LoginValidator{
    private String name;
    private String password;

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
        RestTemplate restTemplate = new RestTemplate();

        // URL des Endpunkts
        String url = "http://localhost:8280/realms/thomseddon/protocol/openid-connect/token";

        // Request-Header
        LinkedMultiValueMap<String, String> body = new LinkedMultiValueMap<>();
        body.add("grant_type", "authorization_code");
        body.add("code", name);
        /*body.add("redirect_uri", "http://localhost:8080/iot" +
                OAuth2AuthorizationRequestRedirectFilter.DEFAULT_AUTHORIZATION_REQUEST_BASE_URI
                + "/keycloak");*/

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

        ResponseEntity<String> responseEntity = null;
        try {
            // Senden der POST-Anfrage und Empfangen der Antwort
            responseEntity = restTemplate.exchange(url, HttpMethod.POST, requestEntity, String.class);
        }
        catch(Exception ex){
            return false;
        }
        return responseEntity.getStatusCode().is2xxSuccessful();
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
