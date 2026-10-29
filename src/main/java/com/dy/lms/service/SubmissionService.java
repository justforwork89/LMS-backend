package com.dy.lms.service;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.dy.lms.common.BusinessException;
import com.dy.lms.domain.Assignment;
import com.dy.lms.domain.Submission;
import com.dy.lms.dto.SubmissionResponse;
import com.dy.lms.mapper.SubmissionMapper;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class SubmissionService {
	private final SubmissionMapper submissionMapper;
	private final AssignmentService assignmentService;
	private final CourseService courseService;
	
	public List<SubmissionResponse> findAll(Long assignmentId, String studentId, String userId, boolean isStudent) {
		String targetId = isStudent ? userId : studentId;
		
		return submissionMapper.findAll(assignmentId, targetId).stream().map(SubmissionResponse::from).toList();
	}
	
	@Transactional
	public SubmissionResponse insert(Long assignmentId, String userId, 
			String content, String fileName) {
		Assignment assignment = assignmentService.findById(assignmentId);
		
		if(!submissionMapper.findAll(assignmentId, userId).isEmpty()) {
			throw new BusinessException(HttpStatus.CONFLICT, "이미 제출한 과제입니다. 수정하려면 수정 기능을 사용하세요.");
		}
		if(LocalDateTime.now().isAfter(assignment.getDueDate())) {
			throw new BusinessException(HttpStatus.BAD_REQUEST, "과제 마감일이 지났습니다.");
		}
		
		Submission s = new Submission();
		s.setAssignmentId(assignmentId);
		s.setStudentId(userId);
		s.setContent(content);
		s.setFileName(fileName);
		s.setSubmittedAt(LocalDateTime.now());
		
		submissionMapper.insert(s);
		
		return SubmissionResponse.from(submissionMapper.findById(s.getSubmissionId()));
	}
	
	@Transactional
	public SubmissionResponse update(Long submissionId, String content, 
			String fileName, String userId) {
		Submission submission = submissionMapper.findById(submissionId);
		
		if (submission == null) {
			throw new BusinessException(HttpStatus.NOT_FOUND, "과제를 찾을 수 없습니다.");
		}
		if (!submission.getStudentId().equals(userId)) {
		    throw new BusinessException(HttpStatus.FORBIDDEN, "본인 소유의 과제만 수정할 수 있습니다.");
		}
		Assignment assignment = assignmentService.findById(
				submission.getAssignmentId());
		if (submission.getScore() != null) {
			throw new BusinessException(HttpStatus.BAD_REQUEST, "채점 후인 과제는 수정 할 수 없습니다");
		}
		if(LocalDateTime.now().isAfter(assignment.getDueDate())) {
			throw new BusinessException(HttpStatus.BAD_REQUEST, "과제 마감일이 지났습니다.");
		}
		
		
		Submission s = new Submission();
		s.setSubmissionId(submissionId);
		s.setContent(content);
		s.setFileName(fileName);
		s.setSubmittedAt(LocalDateTime.now());
		
		submissionMapper.update(s);
		
		return SubmissionResponse.from(submissionMapper.findById(s.getSubmissionId()));
	}
	
	@Transactional
	public SubmissionResponse grade(Long submissionId, Long score, String feedback, String userId) {
		if (submissionMapper.findById(submissionId) == null) {
			throw new BusinessException(HttpStatus.NOT_FOUND, "과제를 찾을 수 없습니다.");
		}
		
		String instructorId = courseService.findById(assignmentService
				.findById(submissionMapper
						.findById(submissionId)
						.getAssignmentId())
				.getCourseId())
				.instructorId();
		
		if (!instructorId.equals(userId)) {
		    throw new BusinessException(HttpStatus.FORBIDDEN, "본인 소유의 과제만 채점할 수 있습니다.");
		}
		if (score < 0 || score > 100) {
			throw new BusinessException(HttpStatus.BAD_REQUEST, "점수는 0 ~ 100점 사이여야 합니다.");
		}
		
		Submission s = new Submission();
		s.setSubmissionId(submissionId);
		s.setScore(score);
		s.setFeedback(feedback);
		s.setStatusCd("20");
		
		submissionMapper.grade(s);
		
		return SubmissionResponse.from(submissionMapper.findById(s.getSubmissionId()));
	}
}
