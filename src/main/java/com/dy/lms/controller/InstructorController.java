package com.dy.lms.controller;

import com.dy.lms.common.ApiResponse;
import com.dy.lms.dto.InstructorResponse;
import com.dy.lms.service.UserService;

import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/instructors")
@RequiredArgsConstructor
public class InstructorController {

    private final UserService userService;

    @GetMapping
    public ApiResponse<List<InstructorResponse>> list() {
        return ApiResponse.ok(userService.findActiveInstructors());
    }
}