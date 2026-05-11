package com.kaoyan.platform.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.kaoyan.platform.common.PageResult;
import com.kaoyan.platform.entity.Todo;
import com.kaoyan.platform.mapper.TodoMapper;
import com.kaoyan.platform.service.TodoService;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class TodoServiceImpl extends ServiceImpl<TodoMapper, Todo> implements TodoService {

    @Override
    public void addTodo(Long userId, String title, String description, Integer type, String targetDate,
                        String startDate, String endDate, String remindTime, Integer priority) {
        Todo todo = new Todo();
        todo.setUserId(userId);
        todo.setTitle(title);
        todo.setDescription(description);
        todo.setType(type);

        if (targetDate != null && !targetDate.isEmpty()) {
            todo.setTargetDate(LocalDate.parse(targetDate, DateTimeFormatter.ofPattern("yyyy-MM-dd")));
        }
        if (startDate != null && !startDate.isEmpty()) {
            todo.setStartDate(LocalDate.parse(startDate, DateTimeFormatter.ofPattern("yyyy-MM-dd")));
        }
        if (endDate != null && !endDate.isEmpty()) {
            todo.setEndDate(LocalDate.parse(endDate, DateTimeFormatter.ofPattern("yyyy-MM-dd")));
        }
        if (remindTime != null && !remindTime.isEmpty()) {
            todo.setRemindTime(LocalDateTime.parse(remindTime, DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
        }

        todo.setPriority(priority);
        todo.setStatus(0);

        save(todo);
    }

    @Override
    public PageResult<Todo> getTodoList(Long userId, Integer page, Integer size, Integer status, Integer type) {
        LambdaQueryWrapper<Todo> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Todo::getUserId, userId);

        if (status != null) {
            wrapper.eq(Todo::getStatus, status);
        }

        if (type != null) {
            wrapper.eq(Todo::getType, type);
        }

        wrapper.orderByDesc(Todo::getPriority).orderByDesc(Todo::getTargetDate);

        Page<Todo> todoPage = page(new Page<>(page, size), wrapper);

        return new PageResult<>(todoPage.getTotal(), (long) page, (long) size, todoPage.getRecords());
    }

    @Override
    public void updateTodoStatus(Long id, Integer status) {
        Todo todo = getById(id);
        if (todo != null) {
            todo.setStatus(status);
            updateById(todo);
        }
    }

    @Override
    public void deleteTodo(Long id) {
        removeById(id);
    }

    @Override
    public List<Todo> getOverdueTodos(Long userId) {
        LocalDate today = LocalDate.now();
        LambdaQueryWrapper<Todo> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Todo::getUserId, userId);
        wrapper.eq(Todo::getStatus, 0);
        wrapper.and(w -> w
                .lt(Todo::getEndDate, today)
                .or()
                .lt(Todo::getTargetDate, today)
        );
        return list(wrapper);
    }

    @Override
    public Map<String, Object> getPlanStatistics(Long userId) {
        Map<String, Object> statistics = new HashMap<>();

        LambdaQueryWrapper<Todo> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Todo::getUserId, userId);
        wrapper.eq(Todo::getType, 1);

        List<Todo> plans = list(wrapper);

        int totalPlans = plans.size();
        statistics.put("totalPlans", totalPlans);

        int completedPlans = 0;
        for (Todo plan : plans) {
            if (plan.getStatus() == 1) {
                completedPlans++;
            }
        }
        statistics.put("completedPlans", completedPlans);

        double completionRate = totalPlans > 0 ? (double) completedPlans / totalPlans * 100 : 0;
        statistics.put("completionRate", completionRate);

        List<Todo> inProgressPlans = new ArrayList<>();
        for (Todo plan : plans) {
            if (plan.getStatus() == 0) {
                inProgressPlans.add(plan);
            }
        }
        statistics.put("inProgressPlans", inProgressPlans);

        return statistics;
    }

    @Override
    public List<Todo> getTodosByDateRange(Long userId, LocalDate start, LocalDate end) {
        LambdaQueryWrapper<Todo> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Todo::getUserId, userId);
        wrapper.and(w -> w
                .between(Todo::getTargetDate, start, end)
                .or()
                .between(Todo::getStartDate, start, end)
                .or()
                .between(Todo::getEndDate, start, end)
                .or()
                .le(Todo::getStartDate, start)
                .ge(Todo::getEndDate, end)
        );
        return list(wrapper);
    }

    @Override
    public Map<String, Object> getCalendarData(Long userId, LocalDate start, LocalDate end) {
        Map<String, Object> result = new HashMap<>();

        List<Todo> todos = getTodosByDateRange(userId, start, end);

        Map<String, List<Map<String, Object>>> plansByDate = new HashMap<>();
        Map<String, List<Map<String, Object>>> todosByDate = new HashMap<>();

        for (Todo todo : todos) {
            Map<String, Object> item = new HashMap<>();
            item.put("id", todo.getId());
            item.put("title", todo.getTitle());
            item.put("priority", todo.getPriority());
            item.put("status", todo.getStatus());
            item.put("startDate", todo.getStartDate() != null ? todo.getStartDate().toString() : null);
            item.put("endDate", todo.getEndDate() != null ? todo.getEndDate().toString() : null);

            if (todo.getType() == 1 && todo.getStartDate() != null && todo.getEndDate() != null) {
                long totalDays = ChronoUnit.DAYS.between(todo.getStartDate(), todo.getEndDate());
                if (totalDays > 0) {
                    long elapsedDays = ChronoUnit.DAYS.between(todo.getStartDate(), LocalDate.now());
                    int progress = (int) Math.min(100, Math.max(0, elapsedDays * 100 / totalDays));
                    item.put("progress", progress);
                } else {
                    item.put("progress", 0);
                }
            }

            LocalDate date = todo.getTargetDate();
            if (date != null && !date.isBefore(start) && !date.isAfter(end)) {
                String key = date.toString();
                if (todo.getType() == 1) {
                    plansByDate.computeIfAbsent(key, k -> new ArrayList<>()).add(item);
                } else {
                    todosByDate.computeIfAbsent(key, k -> new ArrayList<>()).add(item);
                }
            }

            if (todo.getType() == 1 && todo.getStartDate() != null && todo.getEndDate() != null) {
                LocalDate cur = todo.getStartDate().isBefore(start) ? start : todo.getStartDate();
                LocalDate endLimit = todo.getEndDate().isAfter(end) ? end : todo.getEndDate();
                while (!cur.isAfter(endLimit)) {
                    String curKey = cur.toString();
                    if (date == null || !curKey.equals(date.toString())) {
                        plansByDate.computeIfAbsent(curKey, k -> new ArrayList<>()).add(item);
                    }
                    cur = cur.plusDays(1);
                }
            }
        }

        result.put("plansByDate", plansByDate);
        result.put("todosByDate", todosByDate);
        return result;
    }
}
