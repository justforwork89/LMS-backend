package com.dy.lms.service;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.dy.lms.common.BusinessException;
import com.dy.lms.domain.Assignment;
import com.dy.lms.dto.AssignmentRequest;
import com.dy.lms.dto.AssignmentResponse;
import com.dy.lms.dto.CourseResponse;
import com.dy.lms.mapper.AssignmentMapper;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class AssignmentService {
	private final CourseService courseService;
	private final AssignmentMapper assignmentMapper;


	public List<AssignmentResponse> findAll(List<Long> courseIds) {
		if(courseIds != null && courseIds.isEmpty()) {
			return Collections.emptyList();
		}
		
        return assignmentMapper.findAll(courseIds).stream().map(AssignmentResponse::from).toList();
    }
	
	public Assignment findById(Long assignmentId) {
		Assignment a = assignmentMapper.findById(assignmentId);
		if (a == null) {
			throw new BusinessException(HttpStatus.NOT_FOUND, "존재하지 않는 과제입니다.");
		}
		
		return a;
	}
	
	@Transactional
	public AssignmentResponse create(AssignmentRequest req, String userId, boolean isAdmin) {
		Long courseId = req.courseId();
		
		checkOwner(courseId, userId, isAdmin);
		
		if(req.dueDate().isBefore(LocalDateTime.now())) {
			throw new BusinessException(HttpStatus.BAD_REQUEST, "마감일은 현재 이후여야 합니다.");
		}
		
		Assignment a = new Assignment();
		a.setCourseId(courseId);
		a.setTitle(req.title());
		a.setContent(req.content());
		a.setDueDate(req.dueDate());
		
		assignmentMapper.insert(a);
		
		return AssignmentResponse.from(assignmentMapper.findById(a.getAssignmentId()));
	}
	
	private void checkOwner(Long courseId, String userId, boolean isAdmin) {
        if (!isAdmin && !courseService.findById(courseId).instructorId().equals(userId)) {
            throw new BusinessException(HttpStatus.FORBIDDEN, "본인 강의에만 과제를 추가할 수 있습니다.");
        }
    }
}
