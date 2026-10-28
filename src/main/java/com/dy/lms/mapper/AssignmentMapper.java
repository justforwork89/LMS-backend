package com.dy.lms.mapper;

import java.util.List;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import com.dy.lms.domain.Assignment;

@Mapper
public interface AssignmentMapper {
	List<Assignment> findAll(@Param("courseIds") List<Long> courseIds);
	
	Assignment findById(@Param("assignmentId") Long assignmentId);
	
	int insert(Assignment assignment);
}
