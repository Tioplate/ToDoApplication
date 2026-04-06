package com.todo.todoapplication.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ToDo {
    private Long id;
    private Long userId;     // 跨服务关联字段
    private String title;    // 对应：ToDo名
    private LocalDate dueDate; // 对应：期日

    /**
     * 优先度：0:低, 1:中, 2:高
     * 用于满足“优先度顺”排序需求
     */
    private Integer priority;

    /**
     * 是否完成
     * 对应：完了したタスクはグレーアウト
     */
    private Boolean isCompleted;

    private LocalDateTime createdAt;
}

