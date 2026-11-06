package com.dy.lms.service;

import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import com.dy.lms.mapper.CourseMapper;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
@RequiredArgsConstructor
// 모집·교육 기간에 맞춰 강의 상태를 자동으로 바꿉니다. (모집예정 → 모집중 → 모집마감 → 진행중 → 종료)
public class CourseStatusScheduler {

    private final CourseMapper courseMapper;

    /** 서버가 켜질 때 한 번 (꺼져 있던 동안 지난 날짜를 따라잡기) */
    @EventListener(ApplicationReadyEvent.class)
    public void onStartup() {
        run();
    }

    /** 매일 0시 1분 (한국 시간) */
    @Scheduled(cron = "0 1 0 * * *", zone = "Asia/Seoul")
    public void run() {
        int changed = courseMapper.syncStatusByDate();
        if (changed > 0) {
            log.info("강의 상태 자동 변경: {}건", changed);
        }
    }
}
