package com.dy.lms.dto;

import jakarta.validation.constraints.NotBlank;

public record CommonCodeUpdateRequest(
		@NotBlank(message = "코드 이름은 비어 있을 수 없습니다.") String codeName,
	    Integer sortOrder) {

}
