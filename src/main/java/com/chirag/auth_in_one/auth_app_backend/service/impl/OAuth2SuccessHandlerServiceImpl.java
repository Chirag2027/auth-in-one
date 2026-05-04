package com.chirag.auth_in_one.auth_app_backend.service.impl;

import com.chirag.auth_in_one.auth_app_backend.entity.RefreshToken;
import com.chirag.auth_in_one.auth_app_backend.entity.User;
import com.chirag.auth_in_one.auth_app_backend.enums.Provider;
import com.chirag.auth_in_one.auth_app_backend.repository.RefreshTokenRepository;
import com.chirag.auth_in_one.auth_app_backend.repository.UserRepository;
import com.chirag.auth_in_one.auth_app_backend.utils.Constants;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.time.Instant;
import java.util.UUID;

@Component
@Slf4j
public class OAuth2SuccessHandlerServiceImpl implements AuthenticationSuccessHandler {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private JwtServiceImpl jwtServiceImpl;

    @Autowired
    private RefreshTokenRepository refreshTokenRepository;

    @Autowired
    private CookieServiceImpl cookieServiceImpl;

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response, Authentication authentication) throws IOException, ServletException {

        log.info("Successful Authentication");
        log.info(authentication.toString());

        OAuth2User oAuth2User = (OAuth2User) authentication.getPrincipal();

        // identify user
        String registrationId = "unknown";

        if (authentication instanceof OAuth2AuthenticationToken token) {
            registrationId = token.getAuthorizedClientRegistrationId();
        }

        log.info("Registration Id: {}", registrationId);
        log.info("User Name: {}", oAuth2User.getAttributes().toString());

        User user;

        switch (registrationId) {
            case Constants.GOOGLE -> {      // means provider is google
                String googleId = oAuth2User.getAttributes().getOrDefault("sub", "").toString();
                String email = oAuth2User.getAttributes().getOrDefault("email", "").toString();
                String name = oAuth2User.getAttributes().getOrDefault("name", "").toString();
                String picture = oAuth2User.getAttributes().getOrDefault("picture", "").toString();
                User newUser = User.builder()
                        .email(email)
                        .name(name)
                        .image(picture)
                        .enabled(Boolean.TRUE)
                        .provider(Provider.GOOGLE)
                        .providerId(googleId)
                        .build();

                user = userRepository.findByEmail(email).orElseGet(() -> userRepository.save(newUser));

            }

            // provider is github
            case Constants.GITHUB -> {
                String name = oAuth2User.getAttributes().getOrDefault("login", "").toString();
                String githubId = oAuth2User.getAttributes().getOrDefault("id", "").toString();
                String picture = oAuth2User.getAttributes().getOrDefault("avatar_url", "").toString();

                String email = (String) oAuth2User.getAttributes().get("email");
                if (email == null) {
                    email = name + "@gmail.com";
                }
                User newUser = User.builder()
                        .email(email)
                        .name(name)
                        .image(picture)
                        .enabled(Boolean.TRUE)
                        .provider(Provider.GITHUB)
                        .providerId(githubId)
                        .build();

                user = userRepository.findByEmail(email).orElseGet(() -> userRepository.save(newUser));


            }

            default -> {
                throw new RuntimeException("Invalid Provider");
            }

        }

        // refresh token generate to get new access token
        String jti = UUID.randomUUID().toString();
        RefreshToken refreshTokenOb = RefreshToken.builder()
                .jti(jti)
                .user(user)
                .revoked(Boolean.FALSE)
                .createdAt(Instant.now())
                .expiresAt(Instant.now().plusSeconds(jwtServiceImpl.getRefreshTokenTtlSeconds()))
                .build();

        refreshTokenRepository.save(refreshTokenOb);

        String accessToken = jwtServiceImpl.generateAccessToken(user);
        String refreshToken = jwtServiceImpl.generateRefreshToken(user, refreshTokenOb.getJti());

        cookieServiceImpl.attachRefreshCookie(response, refreshToken, (int) jwtServiceImpl.getRefreshTokenTtlSeconds());


        response.getWriter().write("Login successful");    // here will add frontend url where we have to redirect

    }

}
