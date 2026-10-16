package com.dy.lms.mapper;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import com.dy.lms.domain.User;

@Mapper
public interface UserMapper {
    User findById(@Param("userId") String userId);
    
    java.util.List<User> findAll();
    int insert(User user);
    int update(User user);
}