package com.dy.lms.controller;

import com.dy.lms.common.ApiResponse;
import com.dy.lms.dto.CourseResponse;
import com.dy.lms.dto.EnrollmentRequest;
import com.dy.lms.dto.EnrollmentResponse;
import com.dy.lms.dto.EnrollmentStatusRequest;
import com.dy.lms.service.EnrollmentService;

import jakarta.validation.Valid;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/enrollments")
@RequiredArgsConstructor

// 수강신청 URL 입구. 학생은 본인 신청만, 관리자는 전체를 다룹니다.
public class EnrollmentController {

    private final EnrollmentService enrollmentService;

    // 관리자: 전체, 그 외: 본인 신청 내역 (Flutter fetchAll / fetchByStudent)
    @GetMapping
    public ApiResponse<List<EnrollmentResponse>> list(Authentication auth) {
        return ApiResponse.ok(enrollmentService.findAll(auth.getName(), isAdmin(auth)));
    }

    // 수강신청 (Flutter enroll). 정원/중복/폐강 검증은 서비스에서.
    @PostMapping
    public ApiResponse<EnrollmentResponse> enroll(@Valid @RequestBody EnrollmentRequest req,
            Authentication auth) {
        return ApiResponse.ok(enrollmentService.insert(req.courseId(), auth.getName()));
    }

    // 과정 변경 (Flutter change)
    @PutMapping("/{id}")
    public ApiResponse<EnrollmentResponse> change(@PathVariable("id") Long id,
            @Valid @RequestBody EnrollmentRequest req, Authentication auth) {
        return ApiResponse.ok(
                enrollmentService.update(id, req.courseId(), auth.getName(), isAdmin(auth)));
    }

    // 신청 취소 (Flutter cancel)
    @DeleteMapping("/{id}")
    public ApiResponse<Void> cancel(@PathVariable("id") Long id, Authentication auth) {
        enrollmentService.cancel(id, auth.getName(), isAdmin(auth));
        return ApiResponse.ok();
    }

    // 승인/반려 (Flutter setStatus). 관리자 전용 검사는 서비스 또는 SecurityConfig에서.
    @PatchMapping("/{id}/status")
    public ApiResponse<EnrollmentResponse> setStatus(@PathVariable("id") Long id,
            @Valid @RequestBody EnrollmentStatusRequest req, Authentication auth) {
        return ApiResponse.ok(enrollmentService.setStatus(id, req.statusCd(), isAdmin(auth)));
    }

    private boolean isAdmin(Authentication auth) {
        return auth.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));
    }
}
