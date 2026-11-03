package com.dy.lms.domain.code;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum EnrollmentCode {
	ENROLL("10"), ACCEPT("20"), REJECTED("30");

	private final String value;
}
