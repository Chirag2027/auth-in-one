package com.chirag.auth_in_one.auth_app_backend.service.impl;

import com.chirag.auth_in_one.auth_app_backend.dto.LoginRequest;
import com.chirag.auth_in_one.auth_app_backend.dto.RefreshTokenRequest;
import com.chirag.auth_in_one.auth_app_backend.dto.UserDto;
import com.chirag.auth_in_one.auth_app_backend.service.IAuthService;
import com.chirag.auth_in_one.auth_app_backend.service.IUser;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import lombok.AllArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.net.http.HttpHeaders;
import java.util.Arrays;
import java.util.Optional;

@Service
@AllArgsConstructor
public class AuthServiceImpl implements IAuthService {

    private final IUser userService;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final JwtServiceImpl jwtServiceImpl;
    private final CookieServiceImpl cookieServiceImpl;

    @Override
    public UserDto registerUser(UserDto userDto) {

        // for registration other logic or validations need to be written
        userDto.setPassword(passwordEncoder.encode(userDto.getPassword()));  // saving pw in encoded form
        return userService.createUser(userDto);
    }

    @Override
    public Authentication authenticate(LoginRequest loginRequest) {
        try {
            return authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(loginRequest.email(), loginRequest.password()));
        } catch (Exception e) {
            throw new BadCredentialsException("Invalid Credentials");
        }
    }

    // method to read refresh token either from cookie or from request body
    @Override
    public Optional<String> readRefreshTokenFromRequest(RefreshTokenRequest body, HttpServletRequest request) {
        // 1. prefer reading refresh token from cookie
        if (request.getCookies() != null) {

            // actual cookie
            Optional<String> fromCookie = Arrays.stream(request.getCookies())
                    .filter(cookie -> cookieServiceImpl.getRefreshTokenCookieName().equals(cookie.getName()))
                    .map(Cookie::getValue)
                    .filter(val -> !val.isBlank())
                    .findFirst();

            if (fromCookie.isPresent()) {
                return fromCookie;     // if cookie ke andar refresh token wali cookie aa gyi thi then return
            }

        }

        // 2. from body
        if (body != null && body.refreshToken() != null && !body.refreshToken().isBlank()) {
            return Optional.of(body.refreshToken());
        }

        // 3. custom header
        String refreshHeader =  request.getHeader("X-Refresh-Token");
        if (refreshHeader != null && !refreshHeader.isBlank()) {
            return Optional.of(refreshHeader.trim());
        }

        // 4. Authorization = Bearer <refresh token>
        String authHeader = request.getHeader("Authorization");
        if (authHeader != null && authHeader.replace("Bearer ", "").trim().startsWith("Bearer ")) {
            String candidate = authHeader.substring(7).trim();
            if (!candidate.isEmpty()) {
                try {
                    if (jwtServiceImpl.isRefreshToken(candidate)) {
                        return Optional.of(candidate);
                    }
                } catch (Exception _) {
                }
            }
        }

        return Optional.empty();

    }


}
