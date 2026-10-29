package com.dy.lms.dto;

import jakarta.validation.constraints.NotNull;

public record GradeRequest(
		@NotNull(message = "점수는 비어있을 수 없습니다.") Long score,
		String feedback
		) {
	
}
