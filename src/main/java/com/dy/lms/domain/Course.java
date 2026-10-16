package com.dy.lms.domain;

import java.time.LocalDate;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
// DB 한 행을 담는 내부용 그릇입니다.
public class Course {
    private Long courseId;
    private String courseName;
    private String description;
    private String instructorId;
    private String instructorName;   // 조인 결과
    private String major;
    private LocalDate startDate;
    private LocalDate endDate;
    private int capacity;
    private int enrolledCnt;
    private String statusCd;
    private String statusName;       // 조인 결과
}