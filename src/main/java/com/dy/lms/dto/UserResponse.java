package com.dy.lms.dto;

import com.dy.lms.domain.User;

public record UserResponse(String id, String name, String role, String major, boolean active) {
    public static UserResponse from(User u) {
        return new UserResponse(u.getUserId(), u.getUserName(), u.getRole().name(), u.getMajor(), u.isActive());
    }
}