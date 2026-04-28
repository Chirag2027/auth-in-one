package com.chirag.auth_in_one.auth_app_backend.security;

import com.chirag.auth_in_one.auth_app_backend.entity.User;
import com.chirag.auth_in_one.auth_app_backend.exceptions.ResourceNotFoundException;
import com.chirag.auth_in_one.auth_app_backend.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class CustomUserDetailService implements UserDetailsService {

    private final UserRepository userRepository;

    @Override
    public UserDetails loadUserByUsername(String username) throws ResourceNotFoundException {

        // for our use case username is email
        return userRepository.findByEmail(username).orElseThrow(() -> new BadCredentialsException("User not found with the given emailId"));
    }

}
