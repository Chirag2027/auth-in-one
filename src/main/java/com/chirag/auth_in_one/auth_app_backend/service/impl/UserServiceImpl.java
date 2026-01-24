package com.chirag.auth_in_one.auth_app_backend.service.impl;

import com.chirag.auth_in_one.auth_app_backend.dto.UserDto;
import com.chirag.auth_in_one.auth_app_backend.service.IUser;
import org.springframework.stereotype.Service;

@Service
public class UserServiceImpl implements IUser {
    @Override
    public UserDto createUser(UserDto userDto) {
        return null;
    }

    @Override
    public UserDto updateUser(UserDto userDto) {
        return null;
    }

    @Override
    public UserDto deleteUser(UserDto userDto) {
        return null;
    }

    @Override
    public Iterable<UserDto> getAllUsers() {
        return null;
    }

}
