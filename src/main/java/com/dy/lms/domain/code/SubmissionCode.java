package com.dy.lms.domain.code;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum SubmissionCode {
	SUBMITTED("10"), GRADED("20");

	private final String value;
}
