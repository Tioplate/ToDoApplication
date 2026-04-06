package com.todo.todoapplication.mapper;

import com.todo.todoapplication.entity.User;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface UserMapper {
    int register(User user);
    User findByUsername(String username);
    User findById(Long id);
    int update(User user);
    int updateByUsername(@Param("currentUsername") String currentUsername, @Param("user") User user);
}
