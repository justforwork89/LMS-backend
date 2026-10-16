package com.dy.lms.controller;

import com.dy.lms.common.ApiResponse;
import com.dy.lms.dto.UserRequest;
import com.dy.lms.dto.UserResponse;
import com.dy.lms.service.UserService;

import jakarta.validation.Valid;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    @GetMapping
    public ApiResponse<List<UserResponse>> list() {
        return ApiResponse.ok(userService.findAll());
    }

    @PostMapping
    public ApiResponse<UserResponse> create(@Valid @RequestBody UserRequest req) {
        return ApiResponse.ok(userService.create(req));
    }

    @PutMapping("/{userId}")
    public ApiResponse<UserResponse> update(@PathVariable("userId") String userId,
            @Valid @RequestBody UserRequest req) {
        return ApiResponse.ok(userService.update(userId, req));
    }
}