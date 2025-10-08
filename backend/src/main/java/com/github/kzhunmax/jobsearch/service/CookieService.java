package com.github.kzhunmax.jobsearch.service;

import com.github.kzhunmax.jobsearch.security.JwtService;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseCookie;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class CookieService {

    public ResponseCookie[] createAuthCookies(String accessToken, String refreshToken, JwtService jwtService) {
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

        return new ResponseCookie[]{accessCookie, refreshCookie};
    }

    public void addAuthCookiesToResponse(String accessToken, String refreshToken, JwtService jwtService, HttpServletResponse response) {
        ResponseCookie[] cookies = createAuthCookies(accessToken, refreshToken, jwtService);
        response.addHeader("Set-Cookie", cookies[0].toString());
        response.addHeader("Set-Cookie", cookies[1].toString());
    }
}
