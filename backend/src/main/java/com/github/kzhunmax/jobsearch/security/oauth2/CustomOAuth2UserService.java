package com.github.kzhunmax.jobsearch.security.oauth2;

import com.github.kzhunmax.jobsearch.model.Role;
import com.github.kzhunmax.jobsearch.model.User;
import com.github.kzhunmax.jobsearch.repository.UserRepository;
import com.github.kzhunmax.jobsearch.security.JwtService;
import com.github.kzhunmax.jobsearch.security.UserDetailsImpl;
import lombok.RequiredArgsConstructor;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Component;

import java.util.Set;

@Component
@RequiredArgsConstructor
public class CustomOAuth2UserService extends DefaultOAuth2UserService {

    private final UserRepository userRepository;
    private final JwtService jwtService;

    @Override
    public OAuth2User loadUser(OAuth2UserRequest request) throws OAuth2AuthenticationException {
        OAuth2User oAuth2User = super.loadUser(request);

        String email = oAuth2User.getAttribute("email");

        User user = userRepository.findByEmail(email)
                .orElseGet(() -> {
                    User newUser = User.builder()
                            .username(email)
                            .email(email)
                            .password("")
                            .roles(Set.of(Role.ROLE_CANDIDATE))
                            .build();
                    return userRepository.save(newUser);
                });
        UserDetailsImpl userDetails = new UserDetailsImpl(user);
        String accessToken = jwtService.generateToken(userDetails);
        String refreshToken = jwtService.generateRefreshToken(userDetails);

        return new CustomOAuth2User(oAuth2User, accessToken, refreshToken, email);
    }
}
