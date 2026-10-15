package com.dy.lms.user;

public enum Role {
	STUDENT("S"), INSTRUCTOR("T"), ADMIN("A");

	private final String code;

	Role(String code) {
		this.code = code;
	}

	public String getCode() {
		return code;
	}

	public static Role fromCode(String code) {
		for (Role r : values()) {
			if (r.code.equals(code)) return r;
		}
		throw new IllegalArgumentException("알 수 없는 역할 코드: " + code);
	}
}