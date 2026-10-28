package com.dy.lms.controller;

import java.util.List;

import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.dy.lms.common.ApiResponse;
import com.dy.lms.dto.AssignmentRequest;
import com.dy.lms.dto.AssignmentResponse;
import com.dy.lms.service.AssignmentService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/assignments")
@RequiredArgsConstructor
public class AssignmentController {
	private final AssignmentService assignmentService;
	
	@GetMapping
    public ApiResponse<List<AssignmentResponse>> list(@RequestParam(name = "courseIds", required = false) List<Long> courseIds) {
        return ApiResponse.ok(assignmentService.findAll(courseIds));
    }
	
	@PostMapping
	public ApiResponse<AssignmentResponse> create(@Valid @RequestBody AssignmentRequest req, Authentication auth) {
		return ApiResponse.ok(assignmentService.create(req, auth.getName(), isAdmin(auth)));
	}
	
	private boolean isAdmin(Authentication auth) {
        return auth.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));
    }
}
