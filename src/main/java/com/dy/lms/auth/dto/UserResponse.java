package com.dy.lms.auth.dto;

import com.dy.lms.user.User;

public record UserResponse(String id, String name, String role, String major, boolean active) {
    public static UserResponse from(User u) {
        return new UserResponse(u.getUserId(), u.getUserName(), u.getRole().name(), u.getMajor(), u.isActive());
    }
}