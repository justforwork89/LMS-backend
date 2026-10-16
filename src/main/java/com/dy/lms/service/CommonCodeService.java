package com.dy.lms.service;

import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import com.dy.lms.dto.CommonCodeResponse;
import com.dy.lms.mapper.CommonCodeMapper;

@Service
@RequiredArgsConstructor
public class CommonCodeService {

    private final CommonCodeMapper commonCodeMapper;

    public List<CommonCodeResponse> findByGroup(String groupCd) {
        return commonCodeMapper.findByGroup(groupCd).stream()
                .map(CommonCodeResponse::from)
                .toList();
    }
}