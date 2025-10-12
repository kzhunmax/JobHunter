package com.github.kzhunmax.jobsearch.service;

import com.github.kzhunmax.jobsearch.security.JwtService;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.ResponseCookie;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("CookieService Tests")
public class CookieServiceTest {

    @Mock
    private JwtService jwtService;

    @Mock
    private HttpServletResponse response;

    @InjectMocks
    private CookieService cookieService;

    @Test
    void createAuthCookies_shouldCreateSuccessfully() {
        when(jwtService.getJwtExpiration()).thenReturn(3600000L);
        when(jwtService.getRefreshExpiration()).thenReturn(7200000L);

        ResponseCookie[] cookies = cookieService.createAuthCookies("access", "refresh", jwtService);

        assertThat(cookies).hasSize(2);

        ResponseCookie accessCookie = cookies[0];
        assertThat(accessCookie.getName()).isEqualTo("access_token");
        assertThat(accessCookie.isHttpOnly()).isTrue();
        assertThat(accessCookie.isSecure()).isTrue();
        assertThat(accessCookie.getPath()).isEqualTo("/");
        assertThat(accessCookie.getMaxAge().getSeconds()).isEqualTo(3600);

        ResponseCookie refreshCookie = cookies[1];
        assertThat(refreshCookie.getName()).isEqualTo("refresh_token");
        assertThat(refreshCookie.getMaxAge().getSeconds()).isEqualTo(7200);
    }

    @Test
    void addAuthCookiesToResponse_shouldAddSuccessfully() {
        when(jwtService.getJwtExpiration()).thenReturn(3600000L);
        when(jwtService.getRefreshExpiration()).thenReturn(7200000L);

        cookieService.addAuthCookiesToResponse("access", "refresh", jwtService, response);
        verify(response, times(2)).addHeader(eq("Set-Cookie"), anyString());
    }
}
