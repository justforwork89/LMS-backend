package com.dy.lms.service;

import com.dy.lms.common.BusinessException;
import com.dy.lms.common.security.JwtTokenProvider;
import com.dy.lms.domain.User;
import com.dy.lms.dto.LoginRequest;
import com.dy.lms.dto.LoginResponse;
import com.dy.lms.dto.UserResponse;
import com.dy.lms.mapper.UserMapper;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserMapper userMapper;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider jwtTokenProvider;

    public LoginResponse login(LoginRequest request) {
        User user = userMapper.findById(request.userId());
        if (user == null || !user.isActive()
                || !passwordEncoder.matches(request.password(), user.getPassword())) {
            throw new BusinessException(HttpStatus.UNAUTHORIZED, "아이디 또는 비밀번호가 올바르지 않습니다.");
        }
        String token = jwtTokenProvider.createToken(user.getUserId(), user.getRole());
        return new LoginResponse(token, UserResponse.from(user));
    }
}