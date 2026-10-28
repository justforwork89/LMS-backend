package com.dy.lms.domain;

import java.time.LocalDateTime;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Setter
@Getter
@NoArgsConstructor
public class Enrollment {
	private Long enrollId;
	private String studentId;
	private Long courseId;
	private String statusCd;
	private String statusName;
	private LocalDateTime appliedAt;
}
