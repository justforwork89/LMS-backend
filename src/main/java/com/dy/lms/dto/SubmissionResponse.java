package com.dy.lms.dto;

import java.time.LocalDateTime;

import com.dy.lms.domain.Submission;

public record SubmissionResponse(Long id, Long assignmentId, String studentId,
		String content, String fileName, Long score, String feedback, LocalDateTime submittedAt, String statusCd, String studentName) {
	
	public static SubmissionResponse from(Submission s) {
		return new SubmissionResponse(
				s.getSubmissionId(), s.getAssignmentId(), 
				s.getStudentId(), s.getContent(), s.getFileName(),s.getScore(), s.getFeedback(), s.getSubmittedAt(), s.getStatusCd(), s.getStudentName());
	}
}
