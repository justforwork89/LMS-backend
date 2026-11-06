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
import com.dy.lms.domain.CourseFile;
import com.dy.lms.dto.CourseFileResponse;
import com.dy.lms.service.CourseFileService;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
// 강의 참고자료(첨부파일) 입구입니다.
public class CourseFileController {

    private final CourseFileService courseFileService;

    // 업로드 (form-data, key = files)
    @PostMapping(value = "/courses/{courseId}/files", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ApiResponse<List<CourseFileResponse>> upload(@PathVariable("courseId") Long courseId,
            @RequestPart("files") List<MultipartFile> files, Authentication auth) {
        return ApiResponse.ok(courseFileService.upload(courseId, files, auth.getName(), isAdmin(auth)));
    }

    // 목록
    @GetMapping("/courses/{courseId}/files")
    public ApiResponse<List<CourseFileResponse>> list(@PathVariable("courseId") Long courseId,
            Authentication auth) {
        return ApiResponse.ok(courseFileService.list(courseId, auth.getName(), isAdmin(auth)));
    }

    // 다운로드 (ApiResponse가 아니라 파일 내용 그대로)
    @GetMapping("/files/{fileId}/download")
    public ResponseEntity<Resource> download(@PathVariable("fileId") Long fileId, Authentication auth) {
        CourseFile file = courseFileService.getFileForDownload(fileId, auth.getName(), isAdmin(auth));
        Path path = courseFileService.resolvePath(file);

        // UUID가 아니라 원래 이름으로, 한글도 안 깨지게(UTF-8)
        ContentDisposition disposition = ContentDisposition.attachment()
                .filename(file.getOriginName(), StandardCharsets.UTF_8)
                .build();

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, disposition.toString())
                .contentType(MediaType.APPLICATION_OCTET_STREAM)   // 브라우저가 열지 않고 저장만
                .contentLength(file.getFileSize())
                .body(new FileSystemResource(path));                // 조금씩 읽어 보내서 메모리 안전
    }

    // 삭제
    @DeleteMapping("/files/{fileId}")
    public ApiResponse<Void> delete(@PathVariable("fileId") Long fileId, Authentication auth) {
        courseFileService.delete(fileId, auth.getName(), isAdmin(auth));
        return ApiResponse.ok();
    }

    private boolean isAdmin(Authentication auth) {
        return auth.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));
    }
}