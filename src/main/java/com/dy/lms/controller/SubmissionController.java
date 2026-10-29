package com.dy.lms.controller;

import java.util.List;

import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.dy.lms.common.ApiResponse;
import com.dy.lms.dto.GradeRequest;
import com.dy.lms.dto.SubmissionRequest;
import com.dy.lms.dto.SubmissionResponse;
import com.dy.lms.dto.SubmissionUpdateRequest;
import com.dy.lms.service.SubmissionService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;


@RestController
@RequestMapping("/api/submissions")
@RequiredArgsConstructor
public class SubmissionController {
	private final SubmissionService submissionService;
	
	@GetMapping
	public ApiResponse<List<SubmissionResponse>> findAll(@RequestParam(name = "assignmentId", required = false) Long assignmentId, 
			@RequestParam(name = "studentId", required = false) String studentId, Authentication auth) {
		return ApiResponse.ok(submissionService.findAll(assignmentId, studentId, auth.getName(), isStudent(auth)));
	}
	
	@PostMapping
	public ApiResponse<SubmissionResponse> insert(@Valid @RequestBody SubmissionRequest req, Authentication auth) {
		return ApiResponse.ok(submissionService.insert(req.assignmentId(), auth.getName(), req.content(), req.fileName()));
	}
	
	@PutMapping("/{id}")
	public ApiResponse<SubmissionResponse> update(@PathVariable("id") Long id, @Valid @RequestBody SubmissionUpdateRequest req, Authentication auth) {
		return ApiResponse.ok(submissionService.update(id, req.content(), req.fileName(), auth.getName()));
	}
	
	@PatchMapping("/{id}")
	public ApiResponse<SubmissionResponse> grade(@PathVariable("id")  Long id, @Valid @RequestBody GradeRequest req, Authentication auth) {
		return ApiResponse.ok(submissionService.grade(id, req.score(), req.feedback(), auth.getName()));
	}
	
	private boolean isStudent(Authentication auth) {
		return auth.getAuthorities().stream().anyMatch(s -> s.getAuthority().equals("ROLE_STUDENT"));
	}
}

