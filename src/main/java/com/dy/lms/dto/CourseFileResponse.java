package com.dy.lms.dto;

import java.time.LocalDateTime;

import com.dy.lms.domain.CourseFile;

// 화면으로 내려보내는 첨부파일 정보입니다.
public record CourseFileResponse(
        Long id,
        Long courseId,
        String name,
        Long size,
        String contentType,
        String uploaderId,
        LocalDateTime regDt) {

    public static CourseFileResponse from(CourseFile f) {
        return new CourseFileResponse(
                f.getFileId(), f.getCourseId(), f.getOriginName(),
                f.getFileSize(), f.getContentType(), f.getUploaderId(), f.getRegDt());
    }
}