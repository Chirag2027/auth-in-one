package com.chirag.auth_in_one.auth_app_backend.controller;

import com.chirag.auth_in_one.auth_app_backend.dto.LoginRequest;
import com.chirag.auth_in_one.auth_app_backend.dto.TokenResponse;
import com.chirag.auth_in_one.auth_app_backend.dto.UserDto;
import com.chirag.auth_in_one.auth_app_backend.entity.RefreshToken;
import com.chirag.auth_in_one.auth_app_backend.entity.User;
import com.chirag.auth_in_one.auth_app_backend.repository.RefreshTokenRepository;
import com.chirag.auth_in_one.auth_app_backend.repository.UserRepository;
import com.chirag.auth_in_one.auth_app_backend.service.IAuthService;
import com.chirag.auth_in_one.auth_app_backend.service.impl.CookieServiceImpl;
import com.chirag.auth_in_one.auth_app_backend.service.impl.JwtServiceImpl;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.http.HttpServletResponse;
import lombok.AllArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.Instant;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/auth")
@AllArgsConstructor
public class AuthController {

    private final IAuthService authService;
    private final UserRepository userRepository;
    private final JwtServiceImpl jwtServiceImpl;
    private final ModelMapper modelMapper;
    private final RefreshTokenRepository refreshTokenRepository;
    private final CookieServiceImpl cookieServiceImpl;

    @PostMapping("/login")
    public ResponseEntity<TokenResponse> login(@RequestBody LoginRequest loginRequest, HttpServletResponse response) {
        // authenticate the user
        Authentication authenticate = authService.authenticate(loginRequest);
        User user = userRepository.findByEmail(loginRequest.email()).orElseThrow(() -> new BadCredentialsException("Invalid Credentials"));

        // we got the user now generate token (access token)
        if (!user.isEnabled()) {
            throw new DisabledException("User is Disabled");
        }

        String jti = UUID.randomUUID().toString();
        var refreshTokenObj = RefreshToken.builder()
                .jti(jti)
                .user(user)
                .createdAt(Instant.now())
                .expiresAt(Instant.now().plusSeconds(jwtServiceImpl.getRefreshTokenTtlSeconds()))
                .revoked(Boolean.FALSE)
                .build();

        // save refresh token in DB
        refreshTokenRepository.save(refreshTokenObj);

        // access token generate
        String accessToken = jwtServiceImpl.generateAccessToken(user);
        // refresh token generate
        String refreshToken = jwtServiceImpl.generateRefreshToken(user, refreshTokenObj.getJti());

        // use cookie service - attach refresh token in cookie
        cookieServiceImpl.attachRefreshCookie(response, refreshToken, (int) jwtServiceImpl.getRefreshTokenTtlSeconds());
        cookieServiceImpl.addNoStoreHeaders(response);

        TokenResponse tokenResponse = TokenResponse.of(accessToken, refreshToken, jwtServiceImpl.getAccessTokenTtlSeconds(), modelMapper.map(user, UserDto.class));
        return ResponseEntity.ok(tokenResponse);
    }

    @PostMapping("/register")
    public ResponseEntity<UserDto> registerUser(@RequestBody UserDto userDto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(authService.registerUser(userDto));
    }

}
