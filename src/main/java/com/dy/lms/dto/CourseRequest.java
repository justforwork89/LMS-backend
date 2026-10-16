package com.dy.lms.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDate;

// 화면에서 들어오는 입력(JSON)을 담고 검증합니다.
public record CourseRequest(
        @NotBlank(message = "강의명을 입력하세요.") String courseName,
        String description,
        String instructorId,          // 관리자만 지정 가능, 강사는 무시됨
        String major,
        @NotNull(message = "시작일을 입력하세요.") LocalDate startDate,
        @NotNull(message = "종료일을 입력하세요.") LocalDate endDate,
        @Min(value = 1, message = "정원은 1명 이상이어야 합니다.") int capacity,
        String statusCd) {
}