package com.dy.lms.controller;

import com.dy.lms.common.ApiResponse;
import com.dy.lms.dto.CommonCodeResponse;
import com.dy.lms.service.CommonCodeService;

import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/codes")
@RequiredArgsConstructor
public class CommonCodeController {

    private final CommonCodeService commonCodeService;

    @GetMapping("/{groupCd}")
    public ApiResponse<List<CommonCodeResponse>> list(@PathVariable("groupCd") String groupCd) {
        return ApiResponse.ok(commonCodeService.findByGroup(groupCd));
    }
}