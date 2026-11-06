package com.dy.lms.mapper;

import java.util.List;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import com.dy.lms.domain.CourseFile;

@Mapper
// 첨부파일 DB 조회/저장 메서드 목록입니다.
public interface CourseFileMapper {
    int insert(CourseFile file);                                       // 저장
    List<CourseFile> findByCourseId(@Param("courseId") Long courseId); // 강의별 목록
    CourseFile findById(@Param("fileId") Long fileId);                 // 1건
    int delete(@Param("fileId") Long fileId);                          // 1건 삭제
    int deleteByCourseId(@Param("courseId") Long courseId);            // 강의 통째 삭제
}