package com.dy.lms.mapper;

import java.util.List;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import com.dy.lms.domain.Enrollment;

@Mapper
public interface EnrollmentMapper {
	List<Enrollment> findAll(@Param("studentId") String studentId);
	
	Enrollment findById(@Param("enrollId") Long enrollId);
	Enrollment findByStudentAndCourse(@Param("studentId") String studentId,
            @Param("courseId") Long courseId);
	
	int insert(Enrollment enrollment);
	int delete(@Param("enrollId") Long enrollId);
	int updateStatus(@Param("enrollId") Long enrollId, @Param("statusCd") String statusCd);
	
	int increaseEnrolledCnt(@Param("courseId") Long courseId);
	int decreaseEnrolledCnt(@Param("courseId") Long courseId);
}


//@Param("enrollId") Long enrollId, @Param("studentId") String studentId, @Param("courseId") 