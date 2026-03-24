package com.chirag.auth_in_one.auth_app_backend.service;

import com.chirag.auth_in_one.auth_app_backend.dto.UserDto;

public interface IUser {

    UserDto createUser(UserDto userDto);

    UserDto getUserByEmail(String email);

    UserDto updateUser(UserDto userDto);

    UserDto deleteUser(UserDto userDto);

    Iterable<UserDto> getAllUsers();

}
