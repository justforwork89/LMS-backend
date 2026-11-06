package com.dy.lms.dto;
import java.time.LocalDateTime;
import com.dy.lms.domain.SubmissionFile;
// 화면으로 내려보내는 과제 제출 첨부파일 정보입니다.
public record SubmissionFileResponse(
        Long id,
        Long submissionId,
        String name,
        Long size,
        String contentType,
        LocalDateTime regDt) {
    public static SubmissionFileResponse from(SubmissionFile f) {
        return new SubmissionFileResponse(
                f.getFileId(), f.getSubmissionId(), f.getOriginName(),
                f.getFileSize(), f.getContentType(), f.getRegDt());
    }
}