package com.chirag.auth_in_one.auth_app_backend.service;

import com.chirag.auth_in_one.auth_app_backend.dto.LoginRequest;
import com.chirag.auth_in_one.auth_app_backend.dto.RefreshTokenRequest;
import com.chirag.auth_in_one.auth_app_backend.dto.UserDto;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.security.core.Authentication;

import java.util.Optional;

// will keep things related to authentication
public interface IAuthService {

    UserDto registerUser(UserDto userDto);

    Authentication authenticate(LoginRequest loginRequest);

    Optional<String> readRefreshTokenFromRequest(RefreshTokenRequest body, HttpServletRequest request);
}
