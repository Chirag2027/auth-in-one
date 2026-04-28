package com.chirag.auth_in_one.auth_app_backend.service;

import com.chirag.auth_in_one.auth_app_backend.dto.LoginRequest;
import com.chirag.auth_in_one.auth_app_backend.dto.UserDto;
import org.springframework.security.core.Authentication;

// will keep things related to authentication
public interface IAuthService {

    UserDto registerUser(UserDto userDto);

    Authentication authenticate(LoginRequest loginRequest);
}
