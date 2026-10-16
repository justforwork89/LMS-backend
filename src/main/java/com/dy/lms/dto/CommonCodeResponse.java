package com.dy.lms.dto;

import com.dy.lms.domain.CommonCode;

public record CommonCodeResponse(String code, String name) {
    public static CommonCodeResponse from(CommonCode c) {
        return new CommonCodeResponse(c.getCode(), c.getCodeName());
    }
}