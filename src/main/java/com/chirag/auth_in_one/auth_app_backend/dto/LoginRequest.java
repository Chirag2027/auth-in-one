package com.chirag.auth_in_one.auth_app_backend.dto;

public record LoginRequest(
        String email,
        String password
) {
}
