package com.chirag.auth_in_one.auth_app_backend.service;

import com.chirag.auth_in_one.auth_app_backend.dto.UserDto;

// will keep things related to authentication
public interface IAuthService {

    UserDto registerUser(UserDto userDto);

}
