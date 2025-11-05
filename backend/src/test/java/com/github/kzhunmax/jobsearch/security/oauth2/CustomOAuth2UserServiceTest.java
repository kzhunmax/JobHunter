package com.github.kzhunmax.jobsearch.security.oauth2;

import com.github.kzhunmax.jobsearch.security.UserDetailsImpl;
import com.github.kzhunmax.jobsearch.shared.enums.AuthProvider;
import com.github.kzhunmax.jobsearch.shared.enums.Role;
import com.github.kzhunmax.jobsearch.user.model.User;
import com.github.kzhunmax.jobsearch.user.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.oauth2.client.registration.ClientRegistration;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.core.AuthorizationGrantType;
import org.springframework.security.oauth2.core.OAuth2AccessToken;
import org.springframework.security.oauth2.core.user.DefaultOAuth2User;
import org.springframework.security.oauth2.core.user.OAuth2User;

import java.time.Instant;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import static com.github.kzhunmax.jobsearch.util.TestDataFactory.*;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("CustomOAuth2UserService Unit Tests")
class CustomOAuth2UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private DefaultOAuth2UserService delegate;

    @InjectMocks
    private CustomOAuth2UserService customOAuth2UserService;

    private User testUser;
    private OAuth2UserRequest oAuth2UserRequest;
    private OAuth2User delegateUser;

    @BeforeEach
    void setUp() {
        testUser = createUser(TEST_ID, TEST_EMAIL);
        testUser.setProvider(AuthProvider.GOOGLE);

        ClientRegistration clientRegistration = ClientRegistration.withRegistrationId("google")
                .clientId("test-id")
                .clientSecret("test-secret")
                .authorizationGrantType(AuthorizationGrantType.AUTHORIZATION_CODE)
                .redirectUri("test-redirect-uri")
                .authorizationUri("test-auth-uri")
                .tokenUri("test-token-uri")
                .userInfoUri("test-user-info-uri")
                .userNameAttributeName("sub")
                .build();

        OAuth2AccessToken accessToken = new OAuth2AccessToken(
                OAuth2AccessToken.TokenType.BEARER,
                "test-access-token",
                Instant.now(),
                Instant.now().plusSeconds(JWT_EXPIRATION)
        );
        oAuth2UserRequest = new OAuth2UserRequest(clientRegistration, accessToken);

        delegateUser = new DefaultOAuth2User(
                Set.of(),
                Map.of("email", TEST_EMAIL, "name", "Test User", "sub", "12345"),
                "sub"
        );
    }

    @Test
    @DisplayName("should load existing user from database")
    void loadUser_shouldReturnExistingUser() {
        when(delegate.loadUser(oAuth2UserRequest)).thenReturn(delegateUser);
        when(userRepository.findByEmail(TEST_EMAIL)).thenReturn(Optional.of(testUser));

        OAuth2User result = customOAuth2UserService.loadUser(oAuth2UserRequest);

        assertThat(result).isInstanceOf(UserDetailsImpl.class);
        UserDetailsImpl userDetails = (UserDetailsImpl) result;
        assertThat(userDetails.getUsername()).isEqualTo(TEST_EMAIL);
        assertThat(userDetails.getUser().getProvider()).isEqualTo(AuthProvider.GOOGLE);
    }

    @Test
    @DisplayName("should register and return new user if not found")
    void loadUser_shouldRegisterNewUser() {
        when(delegate.loadUser(oAuth2UserRequest)).thenReturn(delegateUser);
        when(userRepository.findByEmail(TEST_EMAIL)).thenReturn(Optional.empty());
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> {
            User userToSave = invocation.getArgument(0);
            userToSave.setId(2L);
            return userToSave;
        });

        OAuth2User result = customOAuth2UserService.loadUser(oAuth2UserRequest);

        assertThat(result).isInstanceOf(UserDetailsImpl.class);
        UserDetailsImpl userDetails = (UserDetailsImpl) result;
        assertThat(userDetails.getUsername()).isEqualTo(TEST_EMAIL);
        verify(userRepository).save(argThat(user ->
                user.getEmail().equals(TEST_EMAIL) &&
                        user.getProvider() == AuthProvider.GOOGLE &&
                        user.isEmailVerified() &&
                        user.getRoles().contains(Role.ROLE_CANDIDATE)
        ));
    }
}