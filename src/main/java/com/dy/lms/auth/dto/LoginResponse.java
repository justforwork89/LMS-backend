package com.dy.lms.auth.dto;

public record LoginResponse(String token, UserResponse user) {
}