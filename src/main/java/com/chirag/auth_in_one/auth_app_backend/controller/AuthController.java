package com.chirag.auth_in_one.auth_app_backend.controller;

import com.chirag.auth_in_one.auth_app_backend.dto.LoginRequest;
import com.chirag.auth_in_one.auth_app_backend.dto.RefreshTokenRequest;
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
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.AllArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.Instant;
import java.util.Optional;
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

    // refresh and access token renew api
    // we will read token from cookie
    @PostMapping("/refresh")
    public ResponseEntity<TokenResponse> refreshToken(
           @RequestBody(required = false) RefreshTokenRequest body,
           HttpServletResponse response,
           HttpServletRequest request
    ) {

        String refreshToken = authService.readRefreshTokenFromRequest(body, request).orElseThrow(() -> new BadCredentialsException("Missing Refresh Token"));

        if (!jwtServiceImpl.isRefreshToken(refreshToken)) {
            throw new BadCredentialsException("Invalid Refresh Token Type");
        }

        String jti = jwtServiceImpl.getJti(refreshToken);
        UUID userId = jwtServiceImpl.getUserIdFromToken(refreshToken);

        RefreshToken storedRefreshToken = refreshTokenRepository.findByJti(jti).orElseThrow(() -> new BadCredentialsException("Refresh Token Not Recognized"));

        if (storedRefreshToken.isRevoked()) {
            throw new BadCredentialsException("Refresh Token is Revoked");
        }

        if (storedRefreshToken.getExpiresAt().isBefore(Instant.now())) {
            throw new BadCredentialsException("Refresh Token is Expired");
        }

        if (!storedRefreshToken.getUser().getId().equals(userId)) {
            throw new BadCredentialsException("Refresh Token does not match User");
        }

        // refresh token rotate
        storedRefreshToken.setRevoked(Boolean.TRUE);
        String newJti =  UUID.randomUUID().toString();
        storedRefreshToken.setReplacedByToken(newJti);
        refreshTokenRepository.save(storedRefreshToken);

        // new refresh token corresponding to this jti
        User user = storedRefreshToken.getUser();
        var newRefreshTokenObj = RefreshToken.builder()
                .jti(newJti)
                .user(user)
                .createdAt(Instant.now())
                .expiresAt(Instant.now().plusSeconds(jwtServiceImpl.getRefreshTokenTtlSeconds()))
                .revoked(Boolean.FALSE)
                .build();

        refreshTokenRepository.save(newRefreshTokenObj);
        String newAccessToken = jwtServiceImpl.generateAccessToken(user);
        String newRefreshToken = jwtServiceImpl.generateRefreshToken(user, newRefreshTokenObj.getJti());

        cookieServiceImpl.attachRefreshCookie(response, newRefreshToken, (int) jwtServiceImpl.getRefreshTokenTtlSeconds());
        cookieServiceImpl.addNoStoreHeaders(response);

        return ResponseEntity.ok(TokenResponse.of(newAccessToken, newRefreshToken, jwtServiceImpl.getAccessTokenTtlSeconds(), modelMapper.map(user, UserDto.class)));
    }

    @PostMapping("/logout")
    public ResponseEntity<?> logout(HttpServletRequest request, HttpServletResponse response) {
        authService.readRefreshTokenFromRequest(null, request).ifPresent(token -> {
            try {
                if (jwtServiceImpl.isRefreshToken(token)) {
                    String jti =  jwtServiceImpl.getJti(token);
                    refreshTokenRepository.findByJti(jti).ifPresent(rt -> {
                        rt.setRevoked(Boolean.TRUE);
                        refreshTokenRepository.save(rt);
                    });
                }
            } catch (Exception _) {}
        });

        // clear cookie as well along with revoking the token
        cookieServiceImpl.clearRefreshCookie(response);
        cookieServiceImpl.addNoStoreHeaders(response);
        SecurityContextHolder.clearContext();
        return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
    }

}
