package com.todo.todoapplication.mapper;

import com.todo.todoapplication.entity.ToDo;
import org.apache.ibatis.annotations.*;

import java.util.List;

@Mapper
public interface ToDoMapper {

    // 1. ToDo追加機能
    int insertTodo(ToDo todo);

    // 2. ToDo一覧機能 & 6. タスクの並び替え (期日順、優先度順)
    // orderType 约定传 "dueDate" 或 "priority"
    List<ToDo> selectTodosByUserId(@Param("userId") Long userId, @Param("orderType") String orderType);

    // 3. ToDoの編集機能 & 5. 完了/未完了 更新
    int updateTodo(ToDo todo);

    // 4. ToDoの削除機能
    // 务必带上 userId，防止越权删除别人的任务
    int deleteTodoById(@Param("id") Long id, @Param("userId") Long userId);
}

