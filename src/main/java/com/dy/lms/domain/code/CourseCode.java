package com.dy.lms.domain.code;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum CourseCode {
	PREPARING("10"), RECRUITING("20"), ONGOING("30"), ENDED("40"), CLOSED("50");

	private final String value;
}
