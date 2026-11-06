package com.dy.lms.common;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.servlet.resource.NoResourceFoundException;
import org.springframework.web.multipart.MaxUploadSizeExceededException;
import org.springframework.web.multipart.MultipartException;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

	@ExceptionHandler(BusinessException.class)
	public ResponseEntity<ApiResponse<Void>> handleBusiness(BusinessException e) {
		return ResponseEntity.status(e.getStatus()).body(ApiResponse.fail(e.getMessage()));
	}

	@ExceptionHandler(MethodArgumentNotValidException.class)
	public ResponseEntity<ApiResponse<Void>> handleValidation(MethodArgumentNotValidException e) {
		String message = e.getBindingResult().getFieldErrors().stream()
				.findFirst()
				.map(f -> f.getDefaultMessage())
				.orElse("요청 값이 올바르지 않습니다.");
		return ResponseEntity.badRequest().body(ApiResponse.fail(message));
	}

	@ExceptionHandler(Exception.class)
	public ResponseEntity<ApiResponse<Void>> handleException(Exception e) {
		log.error("처리되지 않은 예외", e);
		return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
				.body(ApiResponse.fail("서버 오류가 발생했습니다."));
	}
	@ExceptionHandler(NoResourceFoundException.class)
	public ResponseEntity<ApiResponse<Void>> handleNotFound(NoResourceFoundException e) {
		return ResponseEntity.status(HttpStatus.NOT_FOUND)
				.body(ApiResponse.fail("요청한 주소를 찾을 수 없습니다."));
	}

	@ExceptionHandler(HttpRequestMethodNotSupportedException.class)
	public ResponseEntity<ApiResponse<Void>> handleMethodNotAllowed(HttpRequestMethodNotSupportedException e) {
		return ResponseEntity.status(HttpStatus.METHOD_NOT_ALLOWED)
				.body(ApiResponse.fail("지원하지 않는 요청 방식입니다."));
	}

	@ExceptionHandler(HttpMessageNotReadableException.class)
	public ResponseEntity<ApiResponse<Void>> handleUnreadable(HttpMessageNotReadableException e) {
		return ResponseEntity.badRequest()
				.body(ApiResponse.fail("요청 본문(JSON) 형식이 올바르지 않습니다."));
	}
	@ExceptionHandler(MaxUploadSizeExceededException.class)
	public ResponseEntity<ApiResponse<Void>> handleMaxUpload(MaxUploadSizeExceededException e) {
		return ResponseEntity.status(HttpStatus.CONTENT_TOO_LARGE)
				.body(ApiResponse.fail("한 번에 올릴 수 있는 용량은 500MB까지입니다."));
	}

	@ExceptionHandler(MultipartException.class)
	public ResponseEntity<ApiResponse<Void>> handleMultipart(MultipartException e) {
		return ResponseEntity.badRequest()
				.body(ApiResponse.fail("파일이 너무 많거나 업로드 요청이 올바르지 않습니다."));
	}
}