package com.dy.lms.domain;

import java.time.LocalDateTime;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class Assignment {
	private Long assignmentId;
	private Long courseId;
	private String title;
	private String content;
	private LocalDateTime dueDate;
}
