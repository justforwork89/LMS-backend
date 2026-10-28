package com.dy.lms.dto;

import jakarta.validation.constraints.NotBlank;

// 승인/반려 처리 입력. ENROLL_STATUS 공통코드 값(10 신청, 20 승인, 30 반려).
public record EnrollmentStatusRequest(
        @NotBlank(message = "처리 상태를 지정하세요.") String statusCd) {
}
