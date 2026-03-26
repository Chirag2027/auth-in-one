package com.chirag.auth_in_one.auth_app_backend.service.impl;

import com.chirag.auth_in_one.auth_app_backend.dto.UserDto;
import com.chirag.auth_in_one.auth_app_backend.service.IAuthService;
import com.chirag.auth_in_one.auth_app_backend.service.IUser;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@AllArgsConstructor
public class AuthServiceImpl implements IAuthService {

    private final IUser userService;

    @Override
    public UserDto registerUser(UserDto userDto) {

        // for registration other logic or validations need to be written

        return userService.createUser(userDto);
    }


}
