package com.kaoyan.platform.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.kaoyan.platform.common.PageResult;
import com.kaoyan.platform.entity.Todo;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

public interface TodoService extends IService<Todo> {

    void addTodo(Long userId, String title, String description, Integer type, String targetDate,
                 String startDate, String endDate, String remindTime, Integer priority);

    PageResult<Todo> getTodoList(Long userId, Integer page, Integer size, Integer status, Integer type);

    void updateTodoStatus(Long id, Integer status);

    void deleteTodo(Long id);

    List<Todo> getOverdueTodos(Long userId);

    Map<String, Object> getPlanStatistics(Long userId);

    List<Todo> getTodosByDateRange(Long userId, LocalDate start, LocalDate end);

    Map<String, Object> getCalendarData(Long userId, LocalDate start, LocalDate end);
}
