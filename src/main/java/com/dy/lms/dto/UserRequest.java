package com.dy.lms.dto;

import jakarta.validation.constraints.NotBlank;

public record UserRequest(
        @NotBlank(message = "아이디를 입력하세요.") String userId,
        String password,
        @NotBlank(message = "이름을 입력하세요.") String userName,
        @NotBlank(message = "역할을 선택하세요.") String role,
        String major,
        boolean active) {
}