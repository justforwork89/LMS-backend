package com.dy.lms.service;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import com.dy.lms.domain.Enrollment;
import com.dy.lms.mapper.EnrollmentMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;
import org.springframework.web.multipart.MultipartFile;

import com.dy.lms.common.BusinessException;
import com.dy.lms.domain.Course;
import com.dy.lms.domain.CourseFile;
import com.dy.lms.dto.CourseFileResponse;
import com.dy.lms.mapper.CourseFileMapper;
import com.dy.lms.mapper.CourseMapper;
import com.dy.lms.service.FileStorageService.StoredFile;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
// 첨부파일 업무 규칙(검사, 권한, 디스크·DB 맞추기)을 처리합니다.
public class CourseFileService {

    private static final long MAX_TOTAL_BYTES = 500L * 1024 * 1024;   // 500MB

    private final CourseFileMapper courseFileMapper;
    private final CourseMapper courseMapper;
    private final FileStorageService storage;
    private final EnrollmentMapper enrollmentMapper;
    
    @Value("${app.file.max-files-per-upload:20}")
    private int maxFilesPerUpload;

    /** 업로드: 검사 → 디스크 저장 → DB 기록 */
    @Transactional
    public List<CourseFileResponse> upload(Long courseId, List<MultipartFile> files,
                                           String userId, boolean admin) {
        checkOwner(getCourse(courseId), userId, admin);

        List<MultipartFile> real = files == null ? List.of()
                : files.stream().filter(f -> !f.isEmpty()).toList();
        if (real.isEmpty()) {
            throw new BusinessException(HttpStatus.BAD_REQUEST, "올릴 파일을 선택하세요.");
        }
        if (real.size() > maxFilesPerUpload) {
            throw new BusinessException(HttpStatus.BAD_REQUEST,
                    "한 번에 " + maxFilesPerUpload + "개까지 올릴 수 있습니다.");
        }
        long total = real.stream().mapToLong(MultipartFile::getSize).sum();
        if (total > MAX_TOTAL_BYTES) {
            throw new BusinessException(HttpStatus.CONTENT_TOO_LARGE,
                    "한 번에 올릴 수 있는 용량은 500MB까지입니다.");
        }
        real.forEach(storage::validate);           // 1) 저장 전에 전부 검사

        List<StoredFile> written = new ArrayList<>();
        deleteWrittenIfRollback(written);          // 2) 실패하면 디스크 파일도 지우도록 예약

        List<CourseFileResponse> result = new ArrayList<>();
        for (MultipartFile f : real) {
            StoredFile s = storage.store(f, courseId);   // 3) 디스크에 저장
            written.add(s);

            CourseFile row = new CourseFile();           // 4) DB에 기록
            row.setCourseId(courseId);
            row.setOriginName(s.originName());
            row.setSaveName(s.saveName());
            row.setFilePath(s.relativeDir());
            row.setFileSize(s.size());
            row.setContentType(s.contentType());
            row.setUploaderId(userId);
            courseFileMapper.insert(row);                // 생성된 fileId가 row에 채워짐

            result.add(CourseFileResponse.from(courseFileMapper.findById(row.getFileId())));
        }
        return result;
    }

    /** 목록: 볼 수 있는 사람만 */
    public List<CourseFileResponse> list(Long courseId, String userId, boolean admin) {
        checkReadable(getCourse(courseId), userId, admin);
        return courseFileMapper.findByCourseId(courseId).stream()
                .map(CourseFileResponse::from)
                .toList();
    }

    /** 다운로드용 1건: 볼 수 있는 사람인지까지 확인 */
    public CourseFile getFileForDownload(Long fileId, String userId, boolean admin) {
        CourseFile file = getFile(fileId);
        checkReadable(getCourse(file.getCourseId()), userId, admin);
        return file;
    }

    private static final String ENROLL_APPROVED = "20";   // 공통코드 ENROLL_STATUS '승인'

    /** 관리자, 담당 강사, 수강 승인된 학생만 볼 수 있다 */
    private void checkReadable(Course c, String userId, boolean admin) {
        if (admin || c.getInstructorId().equals(userId)) {
            return;
        }
        Enrollment e = enrollmentMapper.findByStudentAndCourse(userId, c.getCourseId());
        if (e == null || !ENROLL_APPROVED.equals(e.getStatusCd())) {
            throw new BusinessException(HttpStatus.FORBIDDEN, "수강 승인된 강의의 자료만 볼 수 있습니다.");
        }
    }
    
    /** 1건 */
    public CourseFile getFile(Long fileId) {
        CourseFile file = courseFileMapper.findById(fileId);
        if (file == null) {
            throw new BusinessException(HttpStatus.NOT_FOUND, "파일 정보를 찾을 수 없습니다.");
        }
        return file;
    }

    /** 다운로드용: 실제 파일이 디스크에 있는지까지 확인 */
    public Path resolvePath(CourseFile file) {
        Path path = storage.load(file.getFilePath(), file.getSaveName());
        if (!Files.exists(path)) {
            throw new BusinessException(HttpStatus.NOT_FOUND, "서버에 파일이 없습니다.");
        }
        return path;
    }

    /** 삭제: DB 먼저, 성공하면 실제 파일 */
    @Transactional
    public void delete(Long fileId, String userId, boolean admin) {
        CourseFile file = getFile(fileId);
        checkOwner(getCourse(file.getCourseId()), userId, admin);
        courseFileMapper.delete(fileId);
        deleteFilesAfterCommit(List.of(file));
    }

    /** 강의 삭제 전에 CourseService가 부른다 */
    @Transactional
    public void deleteAllByCourse(Long courseId) {
        List<CourseFile> rows = courseFileMapper.findByCourseId(courseId);
        courseFileMapper.deleteByCourseId(courseId);
        deleteFilesAfterCommit(rows);
    }

    private Course getCourse(Long courseId) {
        Course c = courseMapper.findById(courseId);
        if (c == null) {
            throw new BusinessException(HttpStatus.NOT_FOUND, "강의를 찾을 수 없습니다.");
        }
        return c;
    }

    /** 관리자가 아니면 본인 강의만 (CourseService.checkOwner와 같은 규칙) */
    private void checkOwner(Course c, String userId, boolean admin) {
        if (!admin && !c.getInstructorId().equals(userId)) {
            throw new BusinessException(HttpStatus.FORBIDDEN, "본인 강의의 자료만 올리거나 삭제할 수 있습니다.");
        }
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
    private void deleteFilesAfterCommit(List<CourseFile> rows) {
        TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
            @Override
            public void afterCommit() {
                rows.forEach(r -> storage.delete(r.getFilePath(), r.getSaveName()));
            }
        });
    }
}