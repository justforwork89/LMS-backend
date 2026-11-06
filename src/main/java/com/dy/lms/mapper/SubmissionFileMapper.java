package com.dy.lms.mapper;
import java.util.List;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import com.dy.lms.domain.SubmissionFile;
@Mapper
// 과제 제출 첨부파일 DB 조회/저장 메서드 목록입니다.
public interface SubmissionFileMapper {
    int insert(SubmissionFile file);                                                 // 저장
    List<SubmissionFile> findBySubmissionId(@Param("submissionId") Long submissionId); // 제출별 목록
    SubmissionFile findById(@Param("fileId") Long fileId);                           // 1건
    int delete(@Param("fileId") Long fileId);                                        // 1건 삭제
}