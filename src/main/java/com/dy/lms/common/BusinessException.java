package com.dy.lms.common;

import org.springframework.http.HttpStatus;

import lombok.Getter;

@Getter
public class BusinessException extends RuntimeException {

	private final HttpStatus status;

	public BusinessException(String message) {
		this(HttpStatus.BAD_REQUEST, message);
	}

	public BusinessException(HttpStatus status, String message) {
		super(message);
		this.status = status;
	}
}