package com.dy.lms.dto;

import java.time.LocalDateTime;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record AssignmentRequest(
		@NotNull(message = "강의를 선택하세요.") Long courseId, 
		@NotBlank(message = "제목을 입력하세요.") String title, 
		String content, 
		@NotNull(message = "제출 마감 일시를 선택하세요.") LocalDateTime dueDate
		) {
}
