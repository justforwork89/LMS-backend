package com.dy.lms.dto;

import java.time.LocalDate;

import com.dy.lms.domain.Course;


// 화면으로 내려보내는 응답 내용을 담습니다.
public record CourseResponse(
        Long id, String name, String description,
        String instructorId, String instructorName, String major,
        LocalDate startDate, LocalDate endDate,
        int capacity, int enrolledCount,
        String statusCode, String statusName) {

    public static CourseResponse from(Course c) {
        return new CourseResponse(
                c.getCourseId(), c.getCourseName(), c.getDescription(),
                c.getInstructorId(), c.getInstructorName(), c.getMajor(),
                c.getStartDate(), c.getEndDate(),
                c.getCapacity(), c.getEnrolledCnt(),
                c.getStatusCd(), c.getStatusName());
    }
}