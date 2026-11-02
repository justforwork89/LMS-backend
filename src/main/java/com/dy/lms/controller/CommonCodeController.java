package com.dy.lms.controller;

import java.util.List;

import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.dy.lms.common.ApiResponse;
import com.dy.lms.dto.CommonCodeAdminResponse;
import com.dy.lms.dto.CommonCodeRequest;
import com.dy.lms.dto.CommonCodeResponse;
import com.dy.lms.dto.CommonCodeUpdateRequest;
import com.dy.lms.service.CommonCodeService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/codes")
@RequiredArgsConstructor
public class CommonCodeController {

    private final CommonCodeService commonCodeService;
    
    @GetMapping
    public ApiResponse<List<CommonCodeAdminResponse>> listAll() {
    	return ApiResponse.ok(commonCodeService.findAll());
    }
    
    @GetMapping("/{groupCd}")
    public ApiResponse<List<CommonCodeResponse>> list(@PathVariable("groupCd") String groupCd) {
        return ApiResponse.ok(commonCodeService.findByGroup(groupCd));
    }
    
    @PostMapping
    public ApiResponse<CommonCodeAdminResponse> insert(@Valid @RequestBody CommonCodeRequest req) {
    	return ApiResponse.ok(commonCodeService.insert(req.groupCd(), req.code(), req.codeName(), req.sortOrder()));
    }
    
    @PutMapping("/{groupCd}/{code}")
    public ApiResponse<CommonCodeAdminResponse> update(@PathVariable("groupCd") String groupCd, @PathVariable("code") String code, @Valid @RequestBody CommonCodeUpdateRequest req) {
    	return ApiResponse.ok(commonCodeService.update(groupCd, code, req.codeName(), req.sortOrder()));
    }
    
    @DeleteMapping("/{groupCd}/{code}")
    public ApiResponse<Void> delete(@PathVariable("groupCd") String groupCd, @PathVariable("code") String code) {
    	commonCodeService.delete(groupCd, code);
    	return ApiResponse.ok();
    }
}