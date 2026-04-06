package com.todo.todoapplication.controller;

import com.todo.todoapplication.entity.User;
import com.todo.todoapplication.service.UserService;
import com.todo.todoapplication.utils.JwtUtils;
import com.todo.todoapplication.utils.RedisUtils;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import jakarta.servlet.http.HttpServletRequest;

import java.util.Map;
import java.util.concurrent.TimeUnit;

@RestController
@RequestMapping("/api/users")
public class UserController {

    private final UserService userService;
    private final JwtUtils jwtUtils;
    private final PasswordEncoder passwordEncoder;
    private final RedisUtils redisUtils;

    public UserController(UserService userService, JwtUtils jwtUtils, PasswordEncoder passwordEncoder, RedisUtils redisUtils) {
        this.userService = userService;
        this.jwtUtils = jwtUtils;
        this.passwordEncoder = passwordEncoder;
        this.redisUtils = redisUtils;
    }

    /**
     * 注册接口
     * POST /api/users/register
     * Body: { "username": "xxx", "password": "xxx" }
     */
    @PostMapping("/register")
    public ResponseEntity<String> register(@RequestBody User user) {
        try {
            userService.register(user);
            return ResponseEntity.ok("User registered successfully");
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    /**
     * 登录接口
     * POST /api/users/login
     * Body: { "username": "xxx", "password": "xxx" }
     * 返回: { "token": "eyJ..." }
     */
    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody User loginRequest) {
        User user = userService.findByUsername(loginRequest.getUsername());
        if (user == null) {
            return ResponseEntity.status(401).body("Invalid username or password");
        }
        if (!passwordEncoder.matches(loginRequest.getPassword(), user.getPassword())) {
            return ResponseEntity.status(401).body("Invalid username or password");
        }
        // 登录成功，生成 JWT Token
        String token = jwtUtils.generateToken(user.getUsername());
        String redisKey = "auth:token:" + token;
        // 存入 username 而不是整个 User 对象，避免实体类未实现 Serializable 导致存入 Redis 失败
        redisUtils.set(redisKey, user.getUsername(), 1, TimeUnit.HOURS);
        return ResponseEntity.ok(Map.of("token", token));
    }

    /**
     * 登出接口
     * POST /api/users/logout
     */
    @PostMapping("/logout")
    public ResponseEntity<String> logout(HttpServletRequest request) {
        String authHeader = request.getHeader("Authorization");
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            String token = authHeader.substring(7);
            redisUtils.del("auth:token:" + token);
        }
        return ResponseEntity.ok("Logged out successfully");
    }

    /**
     * 检查用户名是否已存在 (专门用于注册前的校验)
     * GET /api/users/check?username=xxx
     * 返回: true (存在) / false (不存在)
     */
    @GetMapping("/check")
    public ResponseEntity<Boolean> checkUsername(@RequestParam String username) {
        boolean exists = (userService.findByUsername(username) != null);
        return ResponseEntity.ok(exists);
    }

    /**
     * 根据用户名查询用户
     * GET /api/users/{username}
     */
    @GetMapping("/{username}")
    public ResponseEntity<User> getByUsername(@PathVariable String username) {
        User user = userService.findByUsername(username);
        return user != null ? ResponseEntity.ok(user) : ResponseEntity.notFound().build();
    }

    /**
     * 根据 ID 查询用户
     * GET /api/users/id/{id}
     */
    @GetMapping("/id/{id}")
    public ResponseEntity<User> getById(@PathVariable Long id) {
        User user = userService.findById(id);
        return user != null ? ResponseEntity.ok(user) : ResponseEntity.notFound().build();
    }

    /**
     * 更新用户信息
     * PUT /api/users/{id}
     * Body: { "username": "xxx", ... }
     */
    @PutMapping("/{id}")
    public ResponseEntity<String> update(@PathVariable Long id, @RequestBody User user) {
        user.setId(id);
        boolean success = userService.update(user);
        return success ? ResponseEntity.ok("User updated successfully") : ResponseEntity.badRequest().body("Failed to update user");
    }

    /**
     * 根据旧用户名更新用户信息（新增方法，不修改原代码）
     * PUT /api/users/updateByUsername/{currentUsername}
     * Body: { "username": "xxx", "password": "xxx" }
     */
    @PutMapping("/updateByUsername/{currentUsername}")
    public ResponseEntity<String> updateByUsername(@PathVariable String currentUsername, @RequestBody User user) {
        // 1. 如果提供了新密码，需要进行加密
        if (user.getPassword() != null && !user.getPassword().isEmpty()) {
            user.setPassword(passwordEncoder.encode(user.getPassword()));
        }

        // 2. 直接调用 Service 层新方法进行单次 SQL 更新
        boolean success = userService.updateByUsername(currentUsername, user);

        return success ? ResponseEntity.ok("User updated successfully")
                : ResponseEntity.badRequest().body("Failed to update user or user not found");
    }
}
