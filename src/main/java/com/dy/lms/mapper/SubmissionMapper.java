package com.dy.lms.mapper;

import java.util.List;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import com.dy.lms.domain.Submission;

@Mapper
public interface SubmissionMapper {
	List<Submission> findAll(@Param("assignmentId") Long assignmentId, @Param("studentId") String studentId);
	Submission findById(@Param("submissionId") Long submissionId);
	
	int insert(Submission submission);
	int update(Submission submission);
	int grade(Submission submission);
}
