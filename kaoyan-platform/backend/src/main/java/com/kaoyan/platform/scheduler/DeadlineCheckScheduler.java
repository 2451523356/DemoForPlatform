package com.kaoyan.platform.scheduler;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.kaoyan.platform.entity.Notification;
import com.kaoyan.platform.entity.Todo;
import com.kaoyan.platform.service.NotificationPushService;
import com.kaoyan.platform.service.NotificationService;
import com.kaoyan.platform.service.TodoService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class DeadlineCheckScheduler {

    private final TodoService todoService;
    private final NotificationPushService notificationPushService;
    private final NotificationService notificationService;

    @Scheduled(fixedRate = 300000)
    public void checkDeadlines() {
        log.debug("Running deadline check...");
        try {
            // 查找所有未完成的 todos
            List<Todo> activeTodos = todoService.list(new LambdaQueryWrapper<Todo>()
                    .eq(Todo::getStatus, 0));

            LocalDate today = LocalDate.now();
            LocalDate tomorrow = today.plusDays(1);

            for (Todo todo : activeTodos) {
                LocalDate deadline = getEffectiveDeadline(todo);
                if (deadline == null) {
                    continue;
                }

                boolean isOverdue = deadline.isBefore(today);
                boolean isDueSoon = deadline.equals(today) || deadline.equals(tomorrow);

                if (!isOverdue && !isDueSoon) {
                    continue;
                }

                // 检查是否已经为此 todo 创建过同类提醒
                if (alreadyNotified(todo.getId())) {
                    continue;
                }

                notificationPushService.pushDeadlineReminder(
                        todo.getUserId(),
                        todo.getId(),
                        todo.getTitle(),
                        isOverdue
                );

                log.info("Deadline reminder sent for todo {} (user={}, overdue={})",
                        todo.getId(), todo.getUserId(), isOverdue);
            }
        } catch (Exception e) {
            log.error("Error during deadline check", e);
        }
    }

    private LocalDate getEffectiveDeadline(Todo todo) {
        if (todo.getEndDate() != null) {
            return todo.getEndDate();
        }
        return todo.getTargetDate();
    }

    private boolean alreadyNotified(Long todoId) {
        // 仅检查今天是否已提醒过，允许每天重新提醒
        long count = notificationService.count(new LambdaQueryWrapper<Notification>()
                .eq(Notification::getType, "deadline")
                .eq(Notification::getRelatedId, todoId)
                .ge(Notification::getCreateTime, LocalDate.now().atStartOfDay()));
        return count > 0;
    }
}
