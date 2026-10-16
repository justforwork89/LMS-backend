package com.dy.lms.controller;

import com.dy.lms.common.ApiResponse;
import com.dy.lms.dto.CourseRequest;
import com.dy.lms.dto.CourseResponse;
import com.dy.lms.service.CourseService;

import jakarta.validation.Valid;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/courses")
@RequiredArgsConstructor

// 주소(URL)별 입구. 요청을 받아 서비스에 넘기고 응답을 감쌉니다.
public class CourseController {

    private final CourseService courseService;

   
//    @GetMapping
//	public ApiResponse<List<CourseResponse>> list(
//			@RequestParam(required = false) String keyword,
//			@RequestParam(required = false) String status) {
//		return ApiResponse.ok(courseService.findAll(keyword, status));
//	}
//    
    @GetMapping
    public ApiResponse<List<CourseResponse>> list(
            @RequestParam(name = "keyword", required = false) String keyword,
            @RequestParam(name = "status", required = false) String status) {
        return ApiResponse.ok(courseService.findAll(keyword, status));
    }
    
    @GetMapping("/{id}")
    public ApiResponse<CourseResponse> detail(@PathVariable("id") Long id) {
        return ApiResponse.ok(courseService.findById(id));
    }

    @PostMapping
    public ApiResponse<CourseResponse> create(@Valid @RequestBody CourseRequest req,
            Authentication auth) {
        return ApiResponse.ok(courseService.create(req, auth.getName(), isAdmin(auth)));
    }

    @PutMapping("/{id}")
    public ApiResponse<CourseResponse> update(@PathVariable("id") Long id,
            @Valid @RequestBody CourseRequest req, Authentication auth) {
        return ApiResponse.ok(courseService.update(id, req, auth.getName(), isAdmin(auth)));
    }
    
    @DeleteMapping("/{id}")
    public ApiResponse<Void> delete(@PathVariable("id") Long id, Authentication auth) {
        courseService.delete(id, auth.getName(), isAdmin(auth));
        return ApiResponse.ok();
    }
    private boolean isAdmin(Authentication auth) {
        return auth.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));
    }
}