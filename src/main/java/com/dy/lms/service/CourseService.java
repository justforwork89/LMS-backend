package com.dy.lms.service;

import java.util.List;
import java.util.Set;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.dy.lms.common.BusinessException;
import com.dy.lms.domain.Course;
import com.dy.lms.domain.code.CourseCode;
import com.dy.lms.dto.CommonCodeResponse;
import com.dy.lms.dto.CourseRequest;
import com.dy.lms.dto.CourseResponse;
import com.dy.lms.mapper.CourseMapper;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
// 업무 규칙(검증, 권한, 상태 규칙)을 처리합니다.
public class CourseService {

    /** 전공 미지정 시 기본값 (공통코드 MAJOR의 '공통'). */
    private static final String DEFAULT_MAJOR = "40";

    /** 날짜에 따라 자동으로 바뀌는 상태: 모집예정·모집중·모집마감·진행중·종료 */
    private static final Set<String> AUTO_STATUS = Set.of(
            CourseCode.SCHEDULED.getValue(), CourseCode.RECRUITING.getValue(),
            CourseCode.RECRUIT_CLOSED.getValue(), CourseCode.ONGOING.getValue(),
            CourseCode.ENDED.getValue());

    private final CourseMapper courseMapper;
    private final UserService userService;
    private final CommonCodeService commonCodeService; 
    private final CourseFileService courseFileService;

//    public List<CourseResponse> findAll() {
//        return courseMapper.findAll().stream().map(CourseResponse::from).toList();
//    }
    /** mine: 강사면 본인 아이디(본인 과정만), 관리자·학생이면 null(전체) */
    public List<CourseResponse> findAll(String keyword, String statusCd, boolean student, String mine) {
        return courseMapper.findAll(keyword, statusCd, student, mine).stream().map(CourseResponse::from).toList();
    }

    public CourseResponse findById(Long id) {
        return CourseResponse.from(getOrThrow(id));
    }

    /** 화면 조회용: 학생에게는 승인 전(준비중) 강의를 없는 것처럼 보여준다. */
    public CourseResponse findByIdFor(Long id, boolean student, String mine) {
        Course c = getOrThrow(id);
        if (student && CourseCode.PREPARING.getValue().equals(c.getStatusCd())) {
            throw new BusinessException(HttpStatus.NOT_FOUND, "강의를 찾을 수 없습니다.");
        }
        // 강사는 남의 과정을 주소로 직접 열어도 없는 것처럼
        if (mine != null && !mine.equals(c.getInstructorId())) {
            throw new BusinessException(HttpStatus.NOT_FOUND, "강의를 찾을 수 없습니다.");
        }
        return CourseResponse.from(c);
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
        c.setStatusCd(statusForCreate(req.statusCd(), admin));
        courseMapper.insert(c);                       // 생성된 courseId가 c에 채워짐
        courseMapper.syncStatusByDate();              // 승인된 강의면 날짜에 맞는 상태로
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
        c.setStatusCd(statusForUpdate(origin, req.statusCd(), admin));
        courseMapper.update(c);
        courseMapper.syncStatusByDate();              // 날짜를 바꿨으면 상태도 다시 맞춤
        return CourseResponse.from(courseMapper.findById(id));
    }

    @Transactional
    public void delete(Long id, String userId, boolean admin) {
        Course c = getOrThrow(id);
        checkOwner(c, userId, admin);
        if (c.getEnrolledCnt() > 0) {
            throw new BusinessException(HttpStatus.BAD_REQUEST, "수강 신청 이력이 있는 과정은 삭제할 수 없습니다.");
        } 
        courseFileService.deleteAllByCourse(id); 
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
    
    /** 등록 시 상태: 강사는 무조건 준비중(승인대기), 관리자는 준비중·모집중만 */
    private String statusForCreate(String requested, boolean admin) {
        String preparing = CourseCode.PREPARING.getValue();
        if (!admin || requested == null || requested.isBlank()) {
            return preparing;
        }
        if (requested.equals(preparing) || requested.equals(CourseCode.RECRUITING.getValue())) {
            return requested;
        }
        throw new BusinessException(HttpStatus.BAD_REQUEST, "등록할 때는 준비중 또는 모집중만 선택할 수 있습니다.");
    }

    /** 수정 시 상태: 강사는 못 바꾸고, 관리자는 승인(준비중→모집중)과 폐강만.
     *  승인 뒤 실제 상태(모집예정·모집중·모집마감 등)는 syncStatusByDate가 날짜로 정한다. */
    private String statusForUpdate(Course origin, String requested, boolean admin) {
        String now = origin.getStatusCd();
        if (requested == null || requested.isBlank() || requested.equals(now)
                || (AUTO_STATUS.contains(now) && AUTO_STATUS.contains(requested))) {
            return now;   // 자동 상태끼리는 '안 바꿈'으로 본다
        }
        if (!admin) {
            throw new BusinessException(HttpStatus.FORBIDDEN, "강의 상태는 관리자만 바꿀 수 있습니다.");
        }
        boolean approve = now.equals(CourseCode.PREPARING.getValue())
                && requested.equals(CourseCode.RECRUITING.getValue());
        boolean cancel = requested.equals(CourseCode.CLOSED.getValue())
                && !now.equals(CourseCode.ENDED.getValue());
        if (approve || cancel) {
            return requested;
        }
        throw new BusinessException(HttpStatus.BAD_REQUEST,
                "모집예정·모집중·모집마감·진행중·종료는 날짜에 따라 자동으로 바뀝니다.");
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
        if (req.recruitEndDate().isBefore(req.recruitStartDate())) {
            throw new BusinessException(HttpStatus.BAD_REQUEST, "모집 종료일은 모집 시작일보다 빠를 수 없습니다.");
        }
        if (req.recruitEndDate().isAfter(req.endDate())) {
            throw new BusinessException(HttpStatus.BAD_REQUEST, "모집 종료일은 교육 종료일보다 늦을 수 없습니다.");
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
        c.setMajor(req.major() == null || req.major().isBlank() ? DEFAULT_MAJOR : req.major());
        c.setRecruitStartDate(req.recruitStartDate());
        c.setRecruitEndDate(req.recruitEndDate());
        c.setStartDate(req.startDate());
        c.setEndDate(req.endDate());
        c.setCapacity(req.capacity());
        return c;
    }
}