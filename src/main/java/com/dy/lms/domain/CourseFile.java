package com.dy.lms.domain;

import java.time.LocalDateTime;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
// TB_COURSE_FILE 한 행을 담는 내부용 그릇입니다.
public class CourseFile {
    private Long fileId;
    private Long courseId;
    private String originName;
    private String saveName;
    private String filePath;
    private Long fileSize;
    private String contentType;
    private String uploaderId;
    private LocalDateTime regDt;
}