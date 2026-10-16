package com.dy.lms.dto;

public record LoginResponse(String token, UserResponse user) {
}