package com.todo.todoapplication.service;

import com.todo.todoapplication.entity.ToDo;
import com.todo.todoapplication.mapper.ToDoMapper;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ToDoService {

    private final ToDoMapper toDoMapper;

    public ToDoService(ToDoMapper toDoMapper) {
        this.toDoMapper = toDoMapper;
    }

    public int insertTodo(ToDo toDo) {
        return toDoMapper.insertTodo(toDo);
    }

    public int deleteTodoById(Long id, Long userId) {
        return toDoMapper.deleteTodoById(id, userId);
    }

    public List<ToDo> selectTodosByUserId(Long userId, String orderType) {
        return toDoMapper.selectTodosByUserId(userId, orderType);
    }

    public int updateTodo(ToDo toDo) {
        return toDoMapper.updateTodo(toDo);
    }
}

