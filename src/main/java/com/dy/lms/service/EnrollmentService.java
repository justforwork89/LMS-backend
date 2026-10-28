package com.dy.lms.service;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.dy.lms.common.BusinessException;
import com.dy.lms.domain.Enrollment;
import com.dy.lms.dto.CourseResponse;
import com.dy.lms.dto.EnrollmentResponse;
import com.dy.lms.mapper.EnrollmentMapper;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class EnrollmentService {
	private final EnrollmentMapper enrollmentMapper;
	private final CourseService courseService;
	
	public List<EnrollmentResponse> findAll(String userId, boolean admin) {
		String studentId = admin ? null : userId;
		return enrollmentMapper.findAll(studentId).stream().map(EnrollmentResponse::from).toList();
	}
	
	@Transactional
	public EnrollmentResponse insert(Long courseId, String studentId) {
		checkCourseAndReserve(courseId, studentId);
		Enrollment e = new Enrollment();
		e.setStudentId(studentId);
		e.setCourseId(courseId);
		e.setStatusCd("10");
		
		enrollmentMapper.insert(e);
		
		return EnrollmentResponse.from(enrollmentMapper.findById(e.getEnrollId()));
	}
	
	@Transactional
	public EnrollmentResponse update(Long enrollId, Long courseId, String userId, boolean isAdmin) {
		Enrollment origin = checkIdentity(enrollId, userId, isAdmin);
		String studentId = origin.getStudentId();
		checkCourseAndReserve(courseId, studentId);
		
		if(!origin.getStatusCd().equals("30")) {
			enrollmentMapper.decreaseEnrolledCnt(origin.getCourseId());
		}
		enrollmentMapper.delete(enrollId);
		
		Enrollment e = new Enrollment();
		
		e.setStudentId(studentId);
		e.setCourseId(courseId);
		e.setStatusCd("10");
		
		enrollmentMapper.insert(e);
		
		return EnrollmentResponse.from(enrollmentMapper.findById(e.getEnrollId()));
	}
	
	@Transactional
	public void cancel(Long enrollId, String userId, boolean isAdmin) {
		Enrollment origin = checkIdentity(enrollId, userId, isAdmin);
		if(!origin.getStatusCd().equals("30")) {
			enrollmentMapper.decreaseEnrolledCnt(origin.getCourseId());
		}
		enrollmentMapper.delete(enrollId);
	}
	
	@Transactional
	public EnrollmentResponse setStatus(Long enrollId, String statusCd, boolean isAdmin) {
		if (isAdmin == false) {
			throw new BusinessException(HttpStatus.FORBIDDEN, "관리자만 접근 할 수 있습니다.");
		}
		Enrollment origin = checkIdentity(enrollId, null, isAdmin);
		
		if (!statusCd.equals("20") && !statusCd.equals("30")) {
			throw new BusinessException(HttpStatus.BAD_REQUEST, "승인 또는 반려만 선택 할 수 있습니다.");
		}
		if (!origin.getStatusCd().equals("10")) {
			throw new BusinessException(HttpStatus.CONFLICT, "이미 처리된 신청입니다.");
		}
		if (statusCd.equals("30")) {
			enrollmentMapper.decreaseEnrolledCnt(origin.getCourseId());
		}
		
		enrollmentMapper.updateStatus(enrollId, statusCd);
		return EnrollmentResponse.from(enrollmentMapper.findById(enrollId));
	}
	
	private Enrollment checkIdentity(Long enrollId, String userId, boolean admin) {
		Enrollment origin = enrollmentMapper.findById(enrollId);
		if (origin == null) {
		    throw new BusinessException(HttpStatus.NOT_FOUND, "신청 내역을 찾을 수 없습니다.");
		}
		if (!admin && !origin.getStudentId().equals(userId)) {
		    throw new BusinessException(HttpStatus.FORBIDDEN, "본인의 신청만 변경할 수 있습니다.");
		}
		return origin;
    }
	
	private void checkCourseAndReserve(Long courseId, String studentId) {
		CourseResponse course = courseService.findById(courseId);
		
		if (List.of("40", "50").contains(course.statusCode())) {
			throw new BusinessException(HttpStatus.GONE, "이미 폐강 또는 종료된 강의입니다.");
		}
		if (enrollmentMapper.findByStudentAndCourse(studentId, courseId) != null) {
			throw new BusinessException(HttpStatus.CONFLICT, "이미 신청된 과목입니다.");
		}
		if (enrollmentMapper.increaseEnrolledCnt(courseId) == 0) {
			throw new BusinessException(HttpStatus.CONFLICT, "정원이 마감되었습니다.");
		}
	}
}
