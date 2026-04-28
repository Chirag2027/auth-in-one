package com.chirag.auth_in_one.auth_app_backend.service.impl;

import com.chirag.auth_in_one.auth_app_backend.dto.LoginRequest;
import com.chirag.auth_in_one.auth_app_backend.dto.UserDto;
import com.chirag.auth_in_one.auth_app_backend.service.IAuthService;
import com.chirag.auth_in_one.auth_app_backend.service.IUser;
import lombok.AllArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@AllArgsConstructor
public class AuthServiceImpl implements IAuthService {

    private final IUser userService;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;

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


}
