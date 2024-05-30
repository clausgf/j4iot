package de.ostfalia.fbi.j4iot.security.provider;

import org.springframework.security.oauth2.client.authentication.OAuth2LoginAuthenticationToken;
import org.springframework.security.oauth2.core.endpoint.OAuth2AccessTokenResponse;

public interface OAuth2AuthenticationValidator {
    OAuth2AccessTokenResponse authenticate(OAuth2LoginAuthenticationToken token);
}
