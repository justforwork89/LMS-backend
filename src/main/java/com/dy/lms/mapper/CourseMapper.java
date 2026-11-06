package com.dy.lms.mapper;

import java.util.List;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import com.dy.lms.domain.Course;

@Mapper
// DB 조회/저장 메서드의 목록(인터페이스)입니다.
public interface CourseMapper {
    //List<Course> findAll();
    List<Course> findAll(@Param("keyword") String keyword, @Param("statusCd") String statusCd,
                         @Param("hidePreparing") boolean hidePreparing,
                         @Param("instructorId") String instructorId);
    Course findById(@Param("courseId") Long courseId);
    int insert(Course course);
    int update(Course course);
    int delete(@Param("courseId") Long courseId);
    int syncStatusByDate();     // 날짜에 맞춰 모집예정·모집중·모집마감·진행중·종료로 변경
}