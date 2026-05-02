package com.chirag.auth_in_one.auth_app_backend.service.impl;

import com.chirag.auth_in_one.auth_app_backend.entity.User;
import com.chirag.auth_in_one.auth_app_backend.enums.Provider;
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

@Component
@Slf4j
public class OAuth2SuccessHandlerServiceImpl implements AuthenticationSuccessHandler {

    @Autowired
    private UserRepository userRepository;

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
                user = User.builder()
                        .email(email)
                        .name(name)
                        .image(picture)
                        .provider(Provider.GOOGLE)
                        .build();

                userRepository.findByEmail(email).ifPresentOrElse(u -> {
                    log.info("User with email {} already exists", email);
                }, () -> {
                    log.info("User with email {} does not exist, hence saving in DB", email);
                    userRepository.save(user);
                });
            }

            default -> {
                throw new RuntimeException("Invalid Provider");
            }

        }



        response.getWriter().write("Login successful");

    }

}
