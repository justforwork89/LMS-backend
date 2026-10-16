package com.dy.lms.service;

import com.dy.lms.common.BusinessException;
import com.dy.lms.domain.Course;
import com.dy.lms.dto.CommonCodeResponse;
import com.dy.lms.dto.CourseRequest;
import com.dy.lms.dto.CourseResponse;
import com.dy.lms.mapper.CourseMapper;

import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
// 업무 규칙(검증, 권한, 상태 규칙)을 처리합니다.
public class CourseService {

    private final CourseMapper courseMapper;
    private final UserService userService;
    private final CommonCodeService commonCodeService;

//    public List<CourseResponse> findAll() {
//        return courseMapper.findAll().stream().map(CourseResponse::from).toList();
//    }
    public List<CourseResponse> findAll(String keyword, String statusCd) {
        return courseMapper.findAll(keyword, statusCd).stream().map(CourseResponse::from).toList();
    }

    public CourseResponse findById(Long id) {
        return CourseResponse.from(getOrThrow(id));
    }

    @Transactional
    public CourseResponse create(CourseRequest req, String userId, boolean admin) {
        validate(req);
        String instructorId = userId;
        if (admin) {
            if (req.instructorId() == null || req.instructorId().isBlank()) {
                throw new BusinessException(HttpStatus.BAD_REQUEST, "담당 강사를 지정하세요.");
            }
            instructorId = req.instructorId();
        }
        userService.assertActiveInstructor(instructorId);

        Course c = toCourse(req);
        c.setInstructorId(instructorId);
        c.setStatusCd(req.statusCd() == null || req.statusCd().isBlank() ? "10" : req.statusCd());
        courseMapper.insert(c);                       // 생성된 courseId가 c에 채워짐
        return CourseResponse.from(courseMapper.findById(c.getCourseId()));
    }

    @Transactional
    public CourseResponse update(Long id, CourseRequest req, String userId, boolean admin) {
        validate(req);
        Course origin = getOrThrow(id);

        checkOwner(origin, userId, admin);
        if (!admin && !origin.getInstructorId().equals(userId)) {
            throw new BusinessException(HttpStatus.FORBIDDEN, "본인 강의만 수정할 수 있습니다.");
        }
        if (req.capacity() < origin.getEnrolledCnt()) {
            throw new BusinessException(HttpStatus.BAD_REQUEST,
                    "정원은 현재 수강 인원(" + origin.getEnrolledCnt() + "명)보다 작을 수 없습니다.");
        }
        Course c = toCourse(req);
        c.setCourseId(id);
        c.setStatusCd(req.statusCd() == null || req.statusCd().isBlank()
                ? origin.getStatusCd() : req.statusCd());
        courseMapper.update(c);
        return CourseResponse.from(courseMapper.findById(id));
    }

    @Transactional
    public void delete(Long id, String userId, boolean admin) {
        Course c = getOrThrow(id);
        checkOwner(c, userId, admin);
        if (c.getEnrolledCnt() > 0) {
            throw new BusinessException(HttpStatus.BAD_REQUEST, "수강 신청 이력이 있는 과정은 삭제할 수 없습니다.");
        }
        try {
            courseMapper.delete(id);
        } catch (DataIntegrityViolationException e) {
            throw new BusinessException(HttpStatus.CONFLICT, "수강 신청 또는 과제가 연결된 과정은 삭제할 수 없습니다.");
        }
    }
    
    private Course getOrThrow(Long id) {
        Course c = courseMapper.findById(id);
        if (c == null) {
            throw new BusinessException(HttpStatus.NOT_FOUND, "강의를 찾을 수 없습니다.");
        }
        return c;
    }
    
    private void checkOwner(Course c, String userId, boolean admin) {
        if (!admin && !c.getInstructorId().equals(userId)) {
            throw new BusinessException(HttpStatus.FORBIDDEN, "본인 강의만 수정/삭제할 수 있습니다.");
        }
    }
    private void validate(CourseRequest req) {
        if (req.endDate().isBefore(req.startDate())) {
            throw new BusinessException(HttpStatus.BAD_REQUEST, "종료일은 시작일보다 빠를 수 없습니다.");
        }
        if (req.statusCd() != null && !req.statusCd().isBlank()) {
            boolean ok = commonCodeService.findByGroup("COURSE_STATUS").stream()
                    .map(CommonCodeResponse::code)
                    .anyMatch(code -> code.equals(req.statusCd()));
            if (!ok) {
                throw new BusinessException(HttpStatus.BAD_REQUEST, "올바르지 않은 강의 상태입니다.");
            }
        }
    }

    private Course toCourse(CourseRequest req) {
        Course c = new Course();
        c.setCourseName(req.courseName());
        c.setDescription(req.description());
        c.setMajor(req.major() == null || req.major().isBlank() ? "공통" : req.major());
        c.setStartDate(req.startDate());
        c.setEndDate(req.endDate());
        c.setCapacity(req.capacity());
        return c;
    }
}