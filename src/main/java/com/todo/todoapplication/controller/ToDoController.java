package com.todo.todoapplication.controller;

import com.todo.todoapplication.entity.ToDo;
import com.todo.todoapplication.service.ToDoService;
import com.todo.todoapplication.service.UserService;
import com.todo.todoapplication.utils.JwtUtils;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import jakarta.servlet.http.HttpServletRequest;
import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/api/todos")
public class ToDoController {

    private final ToDoService toDoService;
    private final JwtUtils jwtUtils;
    private final UserService userService;

    public ToDoController(ToDoService toDoService, JwtUtils jwtUtils, UserService userService) {
        this.toDoService = toDoService;
        this.jwtUtils = jwtUtils;
        this.userService = userService;
    }

    /**
     * 从请求头 Authorization: Bearer <token> 中解析出 userId
     */
    private Long extractUserId(HttpServletRequest request) {
        String authHeader = request.getHeader("Authorization");
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Missing or invalid Authorization header");
        }
        String token = authHeader.substring(7);
        String username = jwtUtils.getUsername(token);
        return userService.findByUsername(username).getId();
    }

    /**
     * 新增 ToDo
     * POST /api/todos
     * Body: { "title": "xxx", "dueDate": "2026-04-10", "priority": 1 }
     */
    @PostMapping
    public ResponseEntity<String> create(@RequestBody ToDo toDo, HttpServletRequest request) {
        toDo.setUserId(extractUserId(request));
        toDo.setCreatedAt(LocalDateTime.now());
        toDo.setIsCompleted(false);
        int rows = toDoService.insertTodo(toDo);
        return rows > 0
                ? ResponseEntity.status(HttpStatus.CREATED).body("Todo created successfully")
                : ResponseEntity.internalServerError().body("Failed to create todo");
    }

    /**
     * 查询当前用户的所有 ToDo
     * GET /api/todos?orderType=priority  (orderType 可选: priority / dueDate / createdAt)
     */
    @GetMapping
    public ResponseEntity<List<ToDo>> list(
            @RequestParam(defaultValue = "createdAt") String orderType,
            HttpServletRequest request) {
        Long userId = extractUserId(request);
        List<ToDo> todos = toDoService.selectTodosByUserId(userId, orderType);
        return ResponseEntity.ok(todos);
    }

    /**
     * 更新 ToDo（标题、截止日期、优先度、是否完成）
     * PUT /api/todos/{id}
     * Body: { "title": "xxx", "dueDate": "2026-04-10", "priority": 2, "isCompleted": true }
     */
    @PutMapping("/{id}")
    public ResponseEntity<String> update(@PathVariable Long id,
                                         @RequestBody ToDo toDo,
                                         HttpServletRequest request) {
        toDo.setId(id);
        toDo.setUserId(extractUserId(request));
        int rows = toDoService.updateTodo(toDo);
        return rows > 0
                ? ResponseEntity.ok("Todo updated successfully")
                : ResponseEntity.status(HttpStatus.NOT_FOUND).body("Todo not found or access denied");
    }

    /**
     * 删除 ToDo
     * DELETE /api/todos/{id}
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<String> delete(@PathVariable Long id, HttpServletRequest request) {
        Long userId = extractUserId(request);
        int rows = toDoService.deleteTodoById(id, userId);
        return rows > 0
                ? ResponseEntity.ok("Todo deleted successfully")
                : ResponseEntity.status(HttpStatus.NOT_FOUND).body("Todo not found or access denied");
    }
}
