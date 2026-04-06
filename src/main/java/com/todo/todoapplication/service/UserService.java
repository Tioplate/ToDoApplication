package com.todo.todoapplication.service;

import com.todo.todoapplication.entity.LoginUser;
import com.todo.todoapplication.entity.User;
import com.todo.todoapplication.mapper.UserMapper;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
public class UserService implements UserDetailsService {

    private final UserMapper userMapper;
    private final PasswordEncoder passwordEncoder;

    public UserService(UserMapper userMapper, PasswordEncoder passwordEncoder) {
        this.userMapper = userMapper;
        this.passwordEncoder = passwordEncoder;
    }

    /**
     * 用户注册
     * 注册前检查用户名是否已存在，并记录创建时间
     *
     * @param user 用户信息
     * @return 注册成功返回 true，用户名已存在返回 false
     */
    public boolean register(User user) {
        // 检查用户名是否已被占用
        if (userMapper.findByUsername(user.getUsername()) != null) {
            throw new RuntimeException("Username already exists: " + user.getUsername());
        }
        user.setPassword(passwordEncoder.encode(user.getPassword()));
        user.setCreatedAt(LocalDateTime.now());
        user.setUpdatedAt(LocalDateTime.now());
        return userMapper.register(user) > 0;
    }

    /**
     * 根据用户名查询用户
     *
     * @param username 用户名
     * @return 用户对象，不存在则返回 null
     */
    public User findByUsername(String username) {
        return userMapper.findByUsername(username);
    }

    /**
     * 根据 ID 查询用户
     *
     * @param id 用户 ID
     * @return 用户对象，不存在则返回 null
     */
    public User findById(Long id) {
        return userMapper.findById(id);
    }

    /**
     * 更新用户信息
     * 自动刷新 updatedAt 时间
     *
     * @param user 用户信息（需包含 id）
     * @return 更新成功返回 true
     */
    public boolean update(User user) {
        user.setUpdatedAt(LocalDateTime.now());
        return userMapper.update(user) > 0;
    }

    /**
     * 根据旧用户名直接通过单次 SQL 更新用户信息
     *
     * @param currentUsername 旧的当前用户名
     * @param user 包含需要更新的字段（如新的 username, password）
     * @return 更新成功返回 true
     */
    public boolean updateByUsername(String currentUsername, User user) {
        return userMapper.updateByUsername(currentUsername, user) > 0;
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        User user = userMapper.findByUsername(username);
        if (user == null) {
            throw new UsernameNotFoundException("User not found with username: " + username);
        }
        // 这里简单地将 User 转换为 Spring Security 的 UserDetails 对象
        return new LoginUser(user);
    }
}
