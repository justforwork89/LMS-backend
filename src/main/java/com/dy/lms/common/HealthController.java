package com.dy.lms.common;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/health")
public class HealthController {

	@GetMapping
	public ApiResponse<String> health() {
		return ApiResponse.ok("OK");
	}

	@GetMapping("/error")
	public ApiResponse<Void> error() {
		throw new BusinessException("테스트용 업무 예외입니다.");
	}
}