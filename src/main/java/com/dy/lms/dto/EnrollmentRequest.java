package com.dy.lms.dto;

import jakarta.validation.constraints.NotNull;

// 수강신청/과정 변경 입력. 학생은 토큰에서 얻으므로 courseId만 받습니다.
public record EnrollmentRequest(
        @NotNull(message = "과정을 선택하세요.") Long courseId) {
}
