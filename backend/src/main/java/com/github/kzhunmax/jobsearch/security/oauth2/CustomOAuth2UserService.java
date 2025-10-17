package com.github.kzhunmax.jobsearch.security.oauth2;

import com.github.kzhunmax.jobsearch.model.Role;
import com.github.kzhunmax.jobsearch.model.User;
import com.github.kzhunmax.jobsearch.repository.UserRepository;
import com.github.kzhunmax.jobsearch.security.JwtService;
import com.github.kzhunmax.jobsearch.security.UserDetailsImpl;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserService;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Component;

import java.util.Set;

import static com.github.kzhunmax.jobsearch.constants.LoggingConstants.REQUEST_ID_MDC_KEY;

@Component
@RequiredArgsConstructor
@Slf4j
public class CustomOAuth2UserService implements OAuth2UserService<OAuth2UserRequest, OAuth2User> {

    private final UserRepository userRepository;
    private final JwtService jwtService;

    private final OAuth2UserService<OAuth2UserRequest, OAuth2User> delegate = new DefaultOAuth2UserService();

    @Override
    public OAuth2User loadUser(OAuth2UserRequest request) {
        String requestId = MDC.get(REQUEST_ID_MDC_KEY);
        log.info("Request [{}]: Loading OAuth2 user - registrationId={}", requestId, request.getClientRegistration().getRegistrationId());

        OAuth2User oAuth2User = delegate.loadUser(request);

        String email = oAuth2User.getAttribute("email");
        log.debug("Request [{}]: OAuth2 user loaded - email={}", requestId, email);
        User user = userRepository.findByEmail(email)
                .orElseGet(() -> {
                    log.info("Request [{}]: Creating new user for OAuth2 - email={}", requestId, email);
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
        log.info("Request [{}]: OAuth2 user processed successfully - email={}", requestId, email);
        return new CustomOAuth2User(oAuth2User, accessToken, refreshToken, email);
    }
}
