package com.dy.lms.controller;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.util.List;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import com.dy.lms.common.ApiResponse;
import com.dy.lms.domain.SubmissionFile;
import com.dy.lms.dto.SubmissionFileResponse;
import com.dy.lms.service.SubmissionFileService;
import lombok.RequiredArgsConstructor;
@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
// 과제 제출 첨부파일 입구입니다.
public class SubmissionFileController {
    private final SubmissionFileService submissionFileService;
    // 업로드 (form-data, key = files) - 제출한 학생 본인만
    @PostMapping(value = "/submissions/{submissionId}/files", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ApiResponse<List<SubmissionFileResponse>> upload(@PathVariable("submissionId") Long submissionId,
            @RequestPart("files") List<MultipartFile> files, Authentication auth) {
        return ApiResponse.ok(submissionFileService.upload(submissionId, files, auth.getName()));
    }
    // 목록 - 학생 본인, 담당 강사, 관리자
    @GetMapping("/submissions/{submissionId}/files")
    public ApiResponse<List<SubmissionFileResponse>> list(@PathVariable("submissionId") Long submissionId,
            Authentication auth) {
        return ApiResponse.ok(submissionFileService.list(submissionId, auth.getName(), isAdmin(auth)));
    }
    // 다운로드 (ApiResponse가 아니라 파일 내용 그대로)
    @GetMapping("/submission-files/{fileId}/download")
    public ResponseEntity<Resource> download(@PathVariable("fileId") Long fileId, Authentication auth) {
        SubmissionFile file = submissionFileService.getFileForDownload(fileId, auth.getName(), isAdmin(auth));
        Path path = submissionFileService.resolvePath(file);
        ContentDisposition disposition = ContentDisposition.attachment()
                .filename(file.getOriginName(), StandardCharsets.UTF_8)
                .build();
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, disposition.toString())
                .contentType(MediaType.APPLICATION_OCTET_STREAM)
                .contentLength(file.getFileSize())
                .body(new FileSystemResource(path));
    }
    // 삭제 - 제출한 학생 본인만
    @DeleteMapping("/submission-files/{fileId}")
    public ApiResponse<Void> delete(@PathVariable("fileId") Long fileId, Authentication auth) {
        submissionFileService.delete(fileId, auth.getName());
        return ApiResponse.ok();
    }
    private boolean isAdmin(Authentication auth) {
        return auth.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));
    }
}