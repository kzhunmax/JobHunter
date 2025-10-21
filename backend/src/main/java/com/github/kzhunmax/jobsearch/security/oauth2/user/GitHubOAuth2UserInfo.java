package com.github.kzhunmax.jobsearch.security.oauth2.user;

import java.util.Map;

public class GitHubOAuth2UserInfo extends OAuth2UserInfo {

    public GitHubOAuth2UserInfo(Map<String, Object> attributes) {
        super(attributes);
    }

    @Override
    public String getId() {
        return ((Long) attributes.get("id")).toString();
    }

    @Override
    public String getName() {
        return (String) attributes.get("name");
    }

    @Override
    public String getEmail() {
        String email = (String) attributes.get("email");
        if (email == null) {
            return attributes.get("login") + "@github.local";
        }
        return email;
    }

    @Override
    public String getImageUrl() {
        return (String) attributes.get("avatar_url");
    }
}
