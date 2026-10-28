package com.dy.lms.dto;

import java.time.LocalDateTime;

import com.dy.lms.domain.Assignment;

public record AssignmentResponse(Long id, Long courseId, String title, 
		String content, LocalDateTime dueDate) {
	public static AssignmentResponse from(Assignment a) {
		return new AssignmentResponse(
				a.getAssignmentId(), a.getCourseId(), a.getTitle(),
				a.getContent(), a.getDueDate());
	}
}