package com.dy.lms.mapper;

import java.util.List;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import com.dy.lms.domain.CommonCode;

@Mapper
public interface CommonCodeMapper {
    List<CommonCode> findByGroup(@Param("groupCd") String groupCd);
    List<CommonCode> findAll();
    CommonCode findByKey(@Param("groupCd") String groupCd, @Param("code") String code);
    
    int insert(CommonCode commonCode);
    int update(CommonCode commonCode);
    int changeUseYn(@Param("useYn") String useYn, @Param("groupCd") String groupCd, @Param("code") String code);
}