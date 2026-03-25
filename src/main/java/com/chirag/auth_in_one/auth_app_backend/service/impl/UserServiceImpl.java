package com.chirag.auth_in_one.auth_app_backend.service.impl;

import com.chirag.auth_in_one.auth_app_backend.dto.UserDto;
import com.chirag.auth_in_one.auth_app_backend.entity.User;
import com.chirag.auth_in_one.auth_app_backend.enums.Provider;
import com.chirag.auth_in_one.auth_app_backend.exceptions.ResourceNotFoundException;
import com.chirag.auth_in_one.auth_app_backend.helper.UserHelper;
import com.chirag.auth_in_one.auth_app_backend.repository.UserRepository;
import com.chirag.auth_in_one.auth_app_backend.service.IUser;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements IUser {

    private final UserRepository userRepository;
    private final ModelMapper modelMapper;

    @Override
    public UserDto createUser(UserDto userDto) {

        if (userDto.getEmail() == null || userDto.getEmail().isBlank()) {
            throw new IllegalArgumentException("Email is required for creating user");
        }

        if (userRepository.existsByEmail(userDto.getEmail())) {
            throw new IllegalArgumentException("Email already exists");
        }

        // dto -> entity conversion
        User user = modelMapper.map(userDto, User.class);
        user.setProvider(userDto.getProvider() != null ? userDto.getProvider() : Provider.LOCAL);
        // role assignment to new user for authorization

        User savedUser = userRepository.save(user);
        return modelMapper.map(savedUser, UserDto.class);
    }

    @Override
    public UserDto getUserByEmail(String email) {
        User user = userRepository
                .findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with email"));

        return modelMapper.map(user, UserDto.class);
    }

    @Override
    public UserDto updateUser(UserDto userDto, String userId) {
        UUID userUid = UserHelper.parseUUID(userId);
        // fetch old user
        User existingUser = userRepository.findById(userUid).orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + userId));
        // email id not update - unique hai
        if (userDto.getName() != null) existingUser.setName(userDto.getName());
        if (userDto.getImage() != null) existingUser.setImage(userDto.getImage());
        if (userDto.getProvider() != null) existingUser.setProvider(userDto.getProvider());
        if (userDto.getPassword() != null) existingUser.setPassword(userDto.getPassword());  // updation logic for pw will be updated
        existingUser.setEnabled(userDto.getEnabled());
        existingUser.setEmail(userDto.getEmail());
        existingUser.setUpdatedAt(Instant.now());
        User updatedUser = userRepository.save(existingUser);
        return modelMapper.map(updatedUser, UserDto.class);
    }

    @Override
    public void deleteUser(String userId) {
        UUID userUid = UserHelper.parseUUID(userId);
        userRepository.findById(userUid).orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + userId));
        userRepository.deleteById(userUid);
    }

    @Override
    public UserDto getUserById(String userId) {
        User user = userRepository.findById(UserHelper.parseUUID(userId)).orElseThrow(() -> new ResourceNotFoundException("User not found with userid: " + userId));
        return modelMapper.map(user, UserDto.class);
    }

    @Override
    public Iterable<UserDto> getAllUsers() {
        return userRepository
                .findAll()
                .stream()
                .map(user -> modelMapper.map(user, UserDto.class))
                .toList();
    }

}
