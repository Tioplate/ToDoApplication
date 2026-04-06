package com.todo.todoapplication.entity;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;
import java.time.LocalDateTime;

@Data                  // 搞定 Getter/Setter/ToString
@Builder               // 搞定链式组装
@NoArgsConstructor     // 给 MyBatis 留的后门（必需）
@AllArgsConstructor    // 配合 @Builder 使用（必需）
public class User {
    private Long id;
    private String username;
    private String password;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
