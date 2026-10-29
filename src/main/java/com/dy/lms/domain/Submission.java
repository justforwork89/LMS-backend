package com.dy.lms.domain;

import java.time.LocalDateTime;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Setter
@Getter
@NoArgsConstructor
public class Submission {
	private Long submissionId;
	private Long assignmentId;
	private String studentId;
	private String content;
	private String fileName;
	private Long score;
	private String feedback;
	private LocalDateTime submittedAt;
	private String statusCd;
}
