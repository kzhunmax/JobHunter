package com.github.kzhunmax.jobsearch.security.oauth2;

import com.github.kzhunmax.jobsearch.exception.OAuth2AuthenticationProcessingException;
import com.github.kzhunmax.jobsearch.model.AuthProvider;
import com.github.kzhunmax.jobsearch.model.Role;
import com.github.kzhunmax.jobsearch.model.User;
import com.github.kzhunmax.jobsearch.repository.UserRepository;
import com.github.kzhunmax.jobsearch.security.UserDetailsImpl;
import com.github.kzhunmax.jobsearch.security.oauth2.user.OAuth2UserInfo;
import com.github.kzhunmax.jobsearch.security.oauth2.user.OAuth2UserInfoFactory;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;
import org.springframework.security.authentication.InternalAuthenticationServiceException;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.Optional;
import java.util.Set;

import static com.github.kzhunmax.jobsearch.constants.LoggingConstants.REQUEST_ID_MDC_KEY;

@Component
@RequiredArgsConstructor
@Slf4j
public class CustomOAuth2UserService extends DefaultOAuth2UserService {

    private final UserRepository userRepository;

    @Override
    @Transactional
    public OAuth2User loadUser(OAuth2UserRequest request) throws OAuth2AuthenticationException {
        String requestId = MDC.get(REQUEST_ID_MDC_KEY);
        String registrationId = request.getClientRegistration().getRegistrationId();
        log.info("Request [{}]: Loading OAuth2 user - registrationId={}", requestId, registrationId);

        OAuth2User oAuth2User = super.loadUser(request);

        try {
            return processOAuth2User(request, oAuth2User);
        } catch (Exception e) {
            throw new InternalAuthenticationServiceException(e.getMessage(), e.getCause());
        }
    }

    private OAuth2User processOAuth2User(OAuth2UserRequest request, OAuth2User oAuth2User) {
        String registrationId = request.getClientRegistration().getRegistrationId();
        OAuth2UserInfo userInfo = OAuth2UserInfoFactory.getOAuth2UserInfo(registrationId, oAuth2User.getAttributes());

        if (!StringUtils.hasText(userInfo.getEmail())) {
            throw new OAuth2AuthenticationProcessingException("Email not found from OAuth2 provider");
        }
        String email = userInfo.getEmail();
        String requestId = MDC.get(REQUEST_ID_MDC_KEY);
        log.debug("Request [{}]: OAuth2 user loaded - email={}", requestId, email);

        Optional<User> optionalUser = userRepository.findByEmail(email);
        User user;
        if (optionalUser.isPresent()) {
            user = optionalUser.get();
            if (!user.getProvider().name().equalsIgnoreCase(registrationId)) {
                throw new OAuth2AuthenticationProcessingException("Looks like you're signed up with " +
                        user.getProvider() + " account. Please use it to login.");
            }
            log.info("Request [{}]: Found existing user for OAuth2 - email={}", requestId, email);
//            user = updateExistingUser(user, userInfo); TODO: Implement
        } else {
            log.info("Request [{}]: Creating new user for OAuth2 - email={}", requestId, email);
            user = registerNewUser(request, userInfo);
        }

        return new UserDetailsImpl(user, oAuth2User.getAttributes());
    }

    private User registerNewUser(OAuth2UserRequest request, OAuth2UserInfo userInfo) {
        User newUser = User.builder()
                .email(userInfo.getEmail())
                .password("")
                .provider(AuthProvider.valueOf(request.getClientRegistration().getRegistrationId().toUpperCase()))
                .roles(Set.of(Role.ROLE_CANDIDATE))
                .build();
        return userRepository.save(newUser);
    }

    // TODO: Implement UserProfile and ability to updateExistingUser details like Name, PictureUrl
//    private User updateExistingUser(UserProfile existingUser, OAuth2UserInfo userInfo) {
//        existingUser.setName(userInfo.getName());
//        existingUser.setImageUrl(userInfo.getImageUrl());
//        return userProfileRepository.save(existingUser);
//    }
}
