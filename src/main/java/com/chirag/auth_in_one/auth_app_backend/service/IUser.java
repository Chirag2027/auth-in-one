package com.chirag.auth_in_one.auth_app_backend.service;

import com.chirag.auth_in_one.auth_app_backend.dto.UserDto;

public interface IUser {

    UserDto createUser(UserDto userDto);

    UserDto getUserByEmail(String email);

    UserDto updateUser(UserDto userDto, String userId);

    void deleteUser(String userID);

    UserDto getUserById(String userId);

    Iterable<UserDto> getAllUsers();

}
