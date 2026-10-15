package com.dy.lms.user;

import com.dy.lms.auth.dto.UserResponse;
import com.dy.lms.common.BusinessException;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserMapper userMapper;
    private final PasswordEncoder passwordEncoder;

    public List<UserResponse> findAll() {
        return userMapper.findAll().stream().map(UserResponse::from).toList();
    }

    @Transactional
    public UserResponse create(UserRequest req) {
        if (req.password() == null || req.password().isBlank()) {
            throw new BusinessException(HttpStatus.BAD_REQUEST, "비밀번호를 입력하세요.");
        }
        if (userMapper.findById(req.userId()) != null) {
            throw new BusinessException(HttpStatus.CONFLICT, "이미 사용 중인 아이디입니다.");
        }
        User user = toUser(req);
        user.setPassword(passwordEncoder.encode(req.password()));
        userMapper.insert(user);
        return UserResponse.from(userMapper.findById(req.userId()));
    }

    @Transactional
    public UserResponse update(String userId, UserRequest req) {
        if (userMapper.findById(userId) == null) {
            throw new BusinessException(HttpStatus.NOT_FOUND, "사용자를 찾을 수 없습니다.");
        }
        User user = toUser(req);
        user.setUserId(userId);
        user.setPassword(req.password() == null || req.password().isBlank()
                ? null : passwordEncoder.encode(req.password()));
        userMapper.update(user);
        return UserResponse.from(userMapper.findById(userId));
    }

    private User toUser(UserRequest req) {
        Role role;
        try {
            role = Role.valueOf(req.role());
        } catch (IllegalArgumentException e) {
            throw new BusinessException(HttpStatus.BAD_REQUEST, "올바르지 않은 역할입니다.");
        }
        User user = new User();
        user.setUserId(req.userId());
        user.setUserName(req.userName());
        user.setRoleCd(role.getCode());
        user.setMajor(req.major());
        user.setUseYn(req.active() ? "Y" : "N");
        return user;
    }
}