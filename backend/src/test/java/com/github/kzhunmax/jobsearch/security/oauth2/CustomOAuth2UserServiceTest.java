package com.github.kzhunmax.jobsearch.security.oauth2;

import com.github.kzhunmax.jobsearch.model.Role;
import com.github.kzhunmax.jobsearch.model.User;
import com.github.kzhunmax.jobsearch.repository.UserRepository;
import com.github.kzhunmax.jobsearch.security.JwtService;
import com.github.kzhunmax.jobsearch.security.UserDetailsImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.oauth2.client.registration.ClientRegistration;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserService;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.OAuth2Error;
import org.springframework.security.oauth2.core.user.OAuth2User;

import java.util.Optional;

import static com.github.kzhunmax.jobsearch.util.TestDataFactory.*;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CustomOAuth2UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private JwtService jwtService;

    @Mock
    private OAuth2UserService<OAuth2UserRequest, OAuth2User> delegate;

    @Mock
    private OAuth2UserRequest oAuth2UserRequest;

    @Mock
    private OAuth2User delegateUser;

    @Mock
    private ClientRegistration clientRegistration;

    @InjectMocks
    private CustomOAuth2UserService customOAuth2UserService;

    private User testUser;

    @BeforeEach
    void setUp() {
        testUser = createUser(TEST_ID, TEST_USERNAME);

        when(oAuth2UserRequest.getClientRegistration()).thenReturn(clientRegistration);
        when(clientRegistration.getRegistrationId()).thenReturn("test-provider");
    }

    @Test
    void loadUser_withExistingUser_loadsAndReturnsCustomUser() {
        when(delegate.loadUser(oAuth2UserRequest)).thenReturn(delegateUser);
        when(delegateUser.getAttribute("email")).thenReturn(TEST_EMAIL);
        when(userRepository.findByEmail(TEST_EMAIL)).thenReturn(Optional.of(testUser));
        when(jwtService.generateToken(any(UserDetailsImpl.class))).thenReturn(ACCESS_TOKEN);
        when(jwtService.generateRefreshToken(any(UserDetailsImpl.class))).thenReturn(REFRESH_TOKEN);

        OAuth2User result = customOAuth2UserService.loadUser(oAuth2UserRequest);

        assertInstanceOf(CustomOAuth2User.class, result);
        CustomOAuth2User customUser = (CustomOAuth2User) result;
        assertEquals(ACCESS_TOKEN, customUser.accessToken());
        assertEquals(REFRESH_TOKEN, customUser.refreshToken());
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    void loadUser_withNewUser_createsSavesAndReturnsCustomUser() {
        when(delegate.loadUser(oAuth2UserRequest)).thenReturn(delegateUser);
        when(delegateUser.getAttribute("email")).thenReturn(TEST_EMAIL);
        when(userRepository.findByEmail(TEST_EMAIL)).thenReturn(Optional.empty());
        when(userRepository.save(any(User.class))).thenReturn(testUser);
        when(jwtService.generateToken(any(UserDetailsImpl.class))).thenReturn(ACCESS_TOKEN);
        when(jwtService.generateRefreshToken(any(UserDetailsImpl.class))).thenReturn(REFRESH_TOKEN);

        OAuth2User result = customOAuth2UserService.loadUser(oAuth2UserRequest);

        assertInstanceOf(CustomOAuth2User.class, result);
        verify(userRepository).save(argThat(user -> user.getEmail().equals(TEST_EMAIL) && user.getRoles().contains(Role.ROLE_CANDIDATE)));
    }

    @Test
    void loadUser_OAuth2Exception_propagatesException() {
        when(delegate.loadUser(oAuth2UserRequest))
                .thenThrow(new OAuth2AuthenticationException(new OAuth2Error("test_error")));

        assertThatThrownBy(() -> customOAuth2UserService.loadUser(oAuth2UserRequest))
                .isInstanceOf(OAuth2AuthenticationException.class);
    }
}