package com.github.kzhunmax.jobsearch.service;

import com.github.kzhunmax.jobsearch.security.JwtService;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;
import org.springframework.http.ResponseCookie;
import org.springframework.stereotype.Service;

import static com.github.kzhunmax.jobsearch.constants.LoggingConstants.REQUEST_ID_MDC_KEY;

@Service
@Slf4j
@RequiredArgsConstructor
public class CookieService {

    public ResponseCookie[] createAuthCookies(String accessToken, String refreshToken, JwtService jwtService) {
        String requestId = MDC.get(REQUEST_ID_MDC_KEY);
        log.debug("Request [{}]: Creating auth cookies", requestId);
        ResponseCookie accessCookie = ResponseCookie.from("access_token", accessToken)
                .httpOnly(true)
                .secure(true)
                .path("/")
                .maxAge(jwtService.getJwtExpiration() / 1000)
                .sameSite("Strict")
                .build();

        ResponseCookie refreshCookie = ResponseCookie.from("refresh_token", refreshToken)
                .httpOnly(true)
                .secure(true)
                .path("/")
                .maxAge(jwtService.getRefreshExpiration() / 1000)
                .sameSite("Strict")
                .build();

        log.debug("Request [{}]: Auth cookies created successfully", requestId);
        return new ResponseCookie[]{accessCookie, refreshCookie};
    }

    public void addAuthCookiesToResponse(String accessToken, String refreshToken, JwtService jwtService, HttpServletResponse response) {
        String requestId = MDC.get(REQUEST_ID_MDC_KEY);
        log.debug("Request [{}]: Adding auth cookies to response", requestId);
        ResponseCookie[] cookies = createAuthCookies(accessToken, refreshToken, jwtService);
        response.addHeader("Set-Cookie", cookies[0].toString());
        response.addHeader("Set-Cookie", cookies[1].toString());
        log.debug("Request [{}]: Auth cookies added to response successfully", requestId);
    }
}
