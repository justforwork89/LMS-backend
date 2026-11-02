package com.dy.lms.service;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.dy.lms.common.BusinessException;
import com.dy.lms.domain.CommonCode;
import com.dy.lms.dto.CommonCodeAdminResponse;
import com.dy.lms.dto.CommonCodeResponse;
import com.dy.lms.mapper.CommonCodeMapper;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class CommonCodeService {

	private final CommonCodeMapper commonCodeMapper;

	public List<CommonCodeAdminResponse> findAll() {
		return commonCodeMapper.findAll().stream().map(CommonCodeAdminResponse::from).toList();
	}

	public List<CommonCodeResponse> findByGroup(String groupCd) {
		return commonCodeMapper.findByGroup(groupCd).stream().map(CommonCodeResponse::from).toList();
	}

	@Transactional
	public CommonCodeAdminResponse insert(String groupCd, String code, String codeName, Integer sortOrder) {
		CommonCode existing = commonCodeMapper.findByKey(groupCd, code);

		if (existing != null && "Y".equals(existing.getUseYn())) {
			throw new BusinessException(HttpStatus.CONFLICT, "이미 등록된 코드입니다.");
		}
		CommonCode c = new CommonCode();
		c.setGroupCd(groupCd);
		c.setCode(code);
		c.setCodeName(codeName);
		c.setSortOrder(sortOrder == null ? 0 : sortOrder);
		if (existing != null) {
			commonCodeMapper.update(c);
			commonCodeMapper.changeUseYn("Y", groupCd, code);

			return CommonCodeAdminResponse.from(c);
		}
		commonCodeMapper.insert(c);
		return CommonCodeAdminResponse.from(c);
	}
	
	@Transactional
	public CommonCodeAdminResponse update(String groupCd, String code, String codeName, Integer sortOrder) {
		CommonCode existing = commonCodeMapper.findByKey(groupCd, code);
		if (existing == null || !"Y".equals(existing.getUseYn())) {
		    throw new BusinessException(HttpStatus.NOT_FOUND, "해당 코드를 찾을 수 없습니다.");
		}
		
		CommonCode c = new CommonCode();
		c.setGroupCd(groupCd);
		c.setCode(code);
		c.setCodeName(codeName);
		c.setSortOrder(sortOrder == null ? existing.getSortOrder() : sortOrder);
		commonCodeMapper.update(c);
		return CommonCodeAdminResponse.from(c);
	}
	
	@Transactional
	public void delete(String groupCd, String code) {
		CommonCode existing = commonCodeMapper.findByKey(groupCd, code);
		if (existing == null || !"Y".equals(existing.getUseYn())) {
		    throw new BusinessException(HttpStatus.NOT_FOUND, "해당 코드를 찾을 수 없습니다.");
		}
		
		commonCodeMapper.changeUseYn("N", groupCd, code);
	}
}