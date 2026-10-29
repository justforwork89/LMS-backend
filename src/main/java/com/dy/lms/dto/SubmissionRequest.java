package com.dy.lms.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record SubmissionRequest(
		@NotNull(message = "과제 ID는 필수입니다.") Long assignmentId, 
		@NotBlank(message = "제출 본문은 비어있을 수 없습니다.") String content,
		String fileName
		) {
	
}
