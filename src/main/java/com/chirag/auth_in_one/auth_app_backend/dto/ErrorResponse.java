package com.chirag.auth_in_one.auth_app_backend.dto;

import org.springframework.http.HttpStatus;

public record ErrorResponse(String message, HttpStatus status) {

}
