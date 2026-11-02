package com.dy.lms.dto;

import com.dy.lms.domain.CommonCode;

public record CommonCodeAdminResponse(String groupCd, String code, String codeName, int sortOrder) {
	public static CommonCodeAdminResponse from(CommonCode c) {
        return new CommonCodeAdminResponse(c.getGroupCd(), c.getCode(), c.getCodeName(), c.getSortOrder());
    }
}
