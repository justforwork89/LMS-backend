package com.dy.lms.common;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class ApiResponse<T> {

	private final boolean success;
	private final String message;
	private final T data;

	public static <T> ApiResponse<T> ok(T data) {
		return new ApiResponse<>(true, null, data);
	}

	public static ApiResponse<Void> ok() {
		return new ApiResponse<>(true, null, null);
	}

	public static ApiResponse<Void> fail(String message) {
		return new ApiResponse<>(false, message, null);
	}
}