package com.chirag.auth_in_one.auth_app_backend.service.impl;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.Getter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.stereotype.Service;

@Service
@Getter
public class CookieServiceImpl {

    private final String refreshTokenCookieName;
    private final boolean cookieSecure;
    private final boolean cookieHttpOnly;
    private final String cookieDomain;
    private final String cookieSameSite;


    public CookieServiceImpl(
            @Value("${security.jwt.refresh-token-cookie-name}") String refreshTokenCookieName,
            @Value("${security.jwt.cookie-secure}") boolean cookieSecure,
            @Value("${security.jwt.cookie-http-only}") boolean cookieHttpOnly,
            @Value("${security.jwt.cookie-domain}") String cookieDomain,
            @Value("${security.jwt.cookie-same-site}") String cookieSameSite) {
        this.refreshTokenCookieName = refreshTokenCookieName;
        this.cookieSecure = cookieSecure;
        this.cookieHttpOnly = cookieHttpOnly;
        this.cookieDomain = cookieDomain;
        this.cookieSameSite = cookieSameSite;
    }

    // method to attach cookie to response
    public void attachRefreshCookie(HttpServletResponse response, String value, int maxAge) {
        var responseCookieBuilder = ResponseCookie.from(refreshTokenCookieName, value)
                .httpOnly(cookieHttpOnly)
                .secure(cookieSecure)
                .path("/")   // for all apis
                .maxAge(maxAge)
                .sameSite(cookieSameSite);

        if (cookieDomain != null && !cookieDomain.isBlank()){
            responseCookieBuilder.domain(cookieDomain);
        }

        ResponseCookie responseCookie = responseCookieBuilder.build();
        // adding cookie in response
        response.addHeader(HttpHeaders.SET_COOKIE, responseCookie.toString());

    }

    // method to clear the refresh cookie - mainly when logout is done by any user
    public void clearRefreshCookie(HttpServletResponse response) {
        ResponseCookie.ResponseCookieBuilder builder = ResponseCookie.from(refreshTokenCookieName, "")
                .maxAge(0)
                .sameSite(cookieSameSite)
                .httpOnly(cookieHttpOnly)
                .secure(cookieSecure)
                .path("/");

        if (cookieDomain != null && !cookieDomain.isBlank()){
            builder.domain(cookieDomain);
        }

        ResponseCookie responseCookie = builder.build();
        // adding cookie in response
        response.addHeader(HttpHeaders.SET_COOKIE, responseCookie.toString());

    }

    public void addNoStoreHeaders(HttpServletResponse response) {
        response.setHeader(HttpHeaders.CACHE_CONTROL, "no-store");
        response.setHeader("Pragma", "no-cache");
    }

}
