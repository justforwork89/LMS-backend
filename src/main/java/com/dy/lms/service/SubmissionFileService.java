package com.dy.lms.service;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;
import org.springframework.web.multipart.MultipartFile;
import com.dy.lms.common.BusinessException;
import com.dy.lms.domain.Assignment;
import com.dy.lms.domain.Submission;
import com.dy.lms.domain.SubmissionFile;
import com.dy.lms.dto.SubmissionFileResponse;
import com.dy.lms.mapper.SubmissionFileMapper;
import com.dy.lms.mapper.SubmissionMapper;
import com.dy.lms.service.FileStorageService.StoredFile;
import lombok.RequiredArgsConstructor;
@Service
@RequiredArgsConstructor
// 과제 제출 첨부파일 업무 규칙(권한, 개수·용량 제한, 디스크·DB 맞추기)을 처리합니다.
public class SubmissionFileService {
    private final SubmissionFileMapper submissionFileMapper;
    private final SubmissionMapper submissionMapper;
    private final AssignmentService assignmentService;
    private final CourseService courseService;
    private final FileStorageService storage;
    @Value("${app.file.submission.max-files:5}")
    private int maxFiles;            // 제출 1건에 최대 개수
    @Value("${app.file.submission.max-total-mb:50}")
    private long maxTotalMb;         // 제출 1건 전체 용량(MB)
    /** 업로드: 권한 → 개수·용량(이미 올린 것 포함) → 확장자 → 디스크 저장 → DB 기록 */
    @Transactional
    public List<SubmissionFileResponse> upload(Long submissionId, List<MultipartFile> files, String userId) {
        Submission submission = getSubmission(submissionId);
        checkEditable(submission, userId);
        List<MultipartFile> real = files == null ? List.of()
                : files.stream().filter(f -> !f.isEmpty()).toList();
        if (real.isEmpty()) {
            throw new BusinessException(HttpStatus.BAD_REQUEST, "올릴 파일을 선택하세요.");
        }
        List<SubmissionFile> already = submissionFileMapper.findBySubmissionId(submissionId);
        if (already.size() + real.size() > maxFiles) {
            throw new BusinessException(HttpStatus.BAD_REQUEST,
                    "과제 1건에 파일은 " + maxFiles + "개까지 붙일 수 있습니다. (지금 " + already.size() + "개)");
        }
        long total = already.stream().mapToLong(SubmissionFile::getFileSize).sum()
                + real.stream().mapToLong(MultipartFile::getSize).sum();
        if (total > maxTotalMb * 1024 * 1024) {
            throw new BusinessException(HttpStatus.CONTENT_TOO_LARGE,
                    "과제 1건의 파일은 합쳐서 " + maxTotalMb + "MB까지입니다.");
        }
        real.forEach(storage::validate);           // 1) 저장 전에 전부 검사
        List<StoredFile> written = new ArrayList<>();
        deleteWrittenIfRollback(written);          // 2) 실패하면 디스크 파일도 지우도록 예약
        List<SubmissionFileResponse> result = new ArrayList<>();
        for (MultipartFile f : real) {
            StoredFile s = storage.store(f, "submission/" + submissionId);   // 3) 디스크에 저장
            written.add(s);
            SubmissionFile row = new SubmissionFile();                        // 4) DB에 기록
            row.setSubmissionId(submissionId);
            row.setOriginName(s.originName());
            row.setSaveName(s.saveName());
            row.setFilePath(s.relativeDir());
            row.setFileSize(s.size());
            row.setContentType(s.contentType());
            submissionFileMapper.insert(row);
            result.add(SubmissionFileResponse.from(submissionFileMapper.findById(row.getFileId())));
        }
        return result;
    }
    /** 목록: 볼 수 있는 사람만 */
    public List<SubmissionFileResponse> list(Long submissionId, String userId, boolean admin) {
        checkReadable(getSubmission(submissionId), userId, admin);
        return submissionFileMapper.findBySubmissionId(submissionId).stream()
                .map(SubmissionFileResponse::from)
                .toList();
    }
    /** 다운로드용 1건: 볼 수 있는 사람인지까지 확인 */
    public SubmissionFile getFileForDownload(Long fileId, String userId, boolean admin) {
        SubmissionFile file = getFile(fileId);
        checkReadable(getSubmission(file.getSubmissionId()), userId, admin);
        return file;
    }
    /** 다운로드용: 실제 파일이 디스크에 있는지까지 확인 */
    public Path resolvePath(SubmissionFile file) {
        Path path = storage.load(file.getFilePath(), file.getSaveName());
        if (!Files.exists(path)) {
            throw new BusinessException(HttpStatus.NOT_FOUND, "서버에 파일이 없습니다.");
        }
        return path;
    }
    /** 삭제: 올린 학생 본인만, DB 먼저 지우고 성공하면 실제 파일 */
    @Transactional
    public void delete(Long fileId, String userId) {
        SubmissionFile file = getFile(fileId);
        checkEditable(getSubmission(file.getSubmissionId()), userId);
        submissionFileMapper.delete(fileId);
        deleteFilesAfterCommit(List.of(file));
    }
    /** 올리기·지우기: 제출한 학생 본인 + 채점 전 + 마감 전 */
    private void checkEditable(Submission s, String userId) {
        if (!s.getStudentId().equals(userId)) {
            throw new BusinessException(HttpStatus.FORBIDDEN, "본인 과제에만 파일을 올리거나 지울 수 있습니다.");
        }
        if (s.getScore() != null) {
            throw new BusinessException(HttpStatus.BAD_REQUEST, "채점이 끝난 과제는 파일을 바꿀 수 없습니다.");
        }
        Assignment a = assignmentService.findById(s.getAssignmentId());
        if (LocalDateTime.now().isAfter(a.getDueDate())) {
            throw new BusinessException(HttpStatus.BAD_REQUEST, "과제 마감일이 지났습니다.");
        }
    }
    /** 보기·내려받기: 제출한 학생 본인, 그 강의 담당 강사, 관리자 */
    private void checkReadable(Submission s, String userId, boolean admin) {
        if (admin || s.getStudentId().equals(userId)) {
            return;
        }
        Assignment a = assignmentService.findById(s.getAssignmentId());
        String instructorId = courseService.findById(a.getCourseId()).instructorId();
        if (!instructorId.equals(userId)) {
            throw new BusinessException(HttpStatus.FORBIDDEN, "이 과제의 제출 파일을 볼 수 없습니다.");
        }
    }
    private Submission getSubmission(Long submissionId) {
        Submission s = submissionMapper.findById(submissionId);
        if (s == null) {
            throw new BusinessException(HttpStatus.NOT_FOUND, "제출 내역을 찾을 수 없습니다.");
        }
        return s;
    }
    private SubmissionFile getFile(Long fileId) {
        SubmissionFile file = submissionFileMapper.findById(fileId);
        if (file == null) {
            throw new BusinessException(HttpStatus.NOT_FOUND, "파일 정보를 찾을 수 없습니다.");
        }
        return file;
    }
    /** 예약: 일이 실패로 끝나면 저장해 둔 파일을 지운다 */
    private void deleteWrittenIfRollback(List<StoredFile> written) {
        TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
            @Override
            public void afterCompletion(int status) {
                if (status != STATUS_COMMITTED) {
                    written.forEach(s -> storage.delete(s.relativeDir(), s.saveName()));
                }
            }
        });
    }
    /** 예약: 일이 성공으로 끝나면 실제 파일을 지운다 */
    private void deleteFilesAfterCommit(List<SubmissionFile> rows) {
        TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
            @Override
            public void afterCommit() {
                rows.forEach(r -> storage.delete(r.getFilePath(), r.getSaveName()));
            }
        });
    }
}