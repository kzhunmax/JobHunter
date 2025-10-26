package com.github.kzhunmax.jobsearch.security.oauth2.user;

import com.github.kzhunmax.jobsearch.exception.OAuth2AuthenticationProcessingException;
import com.github.kzhunmax.jobsearch.shared.enums.AuthProvider;

import java.util.Map;

public class OAuth2UserInfoFactory {
    public static OAuth2UserInfo getOAuth2UserInfo(String registrationId, Map<String, Object> attributes) {
        if (AuthProvider.GOOGLE.name().equalsIgnoreCase(registrationId)) {
            return new GoogleOAuth2UserInfo(attributes);
        } else if (AuthProvider.GITHUB.name().equalsIgnoreCase(registrationId)) {
            return new GitHubOAuth2UserInfo(attributes);
        } else {
            throw new OAuth2AuthenticationProcessingException("Login with " + registrationId + " is not supported");
        }
    }
}
