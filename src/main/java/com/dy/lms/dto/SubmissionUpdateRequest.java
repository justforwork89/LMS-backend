package com.dy.lms.dto;

import jakarta.validation.constraints.NotBlank;

public record SubmissionUpdateRequest(
	    @NotBlank(message = "제출 본문은 비어있을 수 없습니다.") String content,
	    String fileName
	) {}