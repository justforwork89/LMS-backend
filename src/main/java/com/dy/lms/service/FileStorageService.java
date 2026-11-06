package com.dy.lms.service;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import com.dy.lms.common.BusinessException;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
// 디스크에 파일을 넣고, 찾고, 지웁니다. (DB는 CourseFileService 담당)
public class FileStorageService {

    private static final DateTimeFormatter YEAR_MONTH = DateTimeFormatter.ofPattern("yyyy/MM");

    private final Path root;                     // D:/lms-files
    private final Set<String> allowedExtensions; // pdf, pptx, ...

    // yml 값을 읽어서 채운다
    public FileStorageService(@Value("${app.file.upload-dir}") String uploadDir,
                              @Value("${app.file.allowed-extensions}") List<String> allowed) {
        this.root = Paths.get(uploadDir).toAbsolutePath().normalize();
        this.allowedExtensions = allowed.stream()
                .map(s -> s.trim().toLowerCase(Locale.ROOT))
                .collect(Collectors.toSet());
    }

    /** 저장 결과 영수증 (이 값을 DB에 적는다) */
    public record StoredFile(String originName, String saveName, String relativeDir,
                             long size, String contentType) {}

    /** ① 확장자 검사 */
    public void validate(MultipartFile file) {
        String ext = StringUtils.getFilenameExtension(originName(file));
        if (ext == null || !allowedExtensions.contains(ext.toLowerCase(Locale.ROOT))) {
            throw new BusinessException("올릴 수 없는 파일 형식입니다: " + originName(file));
        }
    }

    /** ② 저장: D:/lms-files/course/{강의ID}/{연}/{월}/{UUID}.{확장자} */
    public StoredFile store(MultipartFile file, Long courseId) {
        String originName = originName(file);
        String ext = StringUtils.getFilenameExtension(originName).toLowerCase(Locale.ROOT);
        String saveName = UUID.randomUUID() + "." + ext;
        String relativeDir = "course/" + courseId + "/" + LocalDate.now().format(YEAR_MONTH);

        Path dir = safeResolve(relativeDir);
        try {
            Files.createDirectories(dir);           // 폴더가 없으면 만든다
            file.transferTo(dir.resolve(saveName)); // 파일을 옮겨 놓는다
        } catch (IOException e) {
            throw new BusinessException("파일을 저장하지 못했습니다: " + originName);
        }
        return new StoredFile(originName, saveName, relativeDir, file.getSize(), file.getContentType());
    }

    /** ③ 찾기: DB의 폴더 + 저장 이름 → 실제 위치 */
    public Path load(String relativeDir, String saveName) {
        return safeResolve(relativeDir + "/" + saveName);
    }

    /** ④ 지우기 (실패해도 멈추지 않고 로그만 남김) */
    public void delete(String relativeDir, String saveName) {
        try {
            Files.deleteIfExists(load(relativeDir, saveName));
        } catch (Exception e) {
            log.warn("파일 삭제 실패: {}/{}", relativeDir, saveName, e);
        }
    }

    private static final int MAX_NAME_LENGTH = 80;   // 한글 80자 = 240바이트 < DB 255바이트

    /** C:\a\보고서.pdf → 보고서.pdf, 제어 문자 제거, 너무 길면 확장자는 살리고 자름 */
    private String originName(MultipartFile file) {
        String name = StringUtils.getFilename(StringUtils.cleanPath(String.valueOf(file.getOriginalFilename())));
        if (name == null || name.isBlank()) {
            return "file";
        }
        name = name.replaceAll("\\p{Cntrl}", "_");
        if (name.length() > MAX_NAME_LENGTH) {
            String ext = StringUtils.getFilenameExtension(name);
            String base = StringUtils.stripFilenameExtension(name);
            int keep = MAX_NAME_LENGTH - (ext == null ? 0 : ext.length() + 1);
            name = base.substring(0, Math.max(1, keep)) + (ext == null ? "" : "." + ext);
        }
        return name;
    }
    /** 합친 경로가 D:/lms-files 안쪽인지 확인 (../../ 장난 막기) */
    private Path safeResolve(String relative) {
        Path resolved = root.resolve(relative).normalize();
        if (!resolved.startsWith(root)) {
            throw new BusinessException("잘못된 파일 경로입니다.");
        }
        return resolved;
    }
}