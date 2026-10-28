package com.dy.lms.dto;

import java.time.LocalDateTime;

import com.dy.lms.domain.Enrollment;

// 화면으로 내려보내는 수강신청 응답. Flutter Enrollment 모델과 대응합니다.
// TODO: Enrollment 도메인 클래스를 만들면 from(Enrollment e) 팩토리를 추가하세요.
public record EnrollmentResponse(
        Long id, String studentId, Long courseId,
        LocalDateTime appliedAt,
        String statusCode, String statusName) {
	
	public static EnrollmentResponse from(Enrollment e) {
	    return new EnrollmentResponse(  
	        e.getEnrollId(), e.getStudentId(), e.getCourseId(),
	        e.getAppliedAt(),
	        e.getStatusCd(), e.getStatusName());
	} 
}



