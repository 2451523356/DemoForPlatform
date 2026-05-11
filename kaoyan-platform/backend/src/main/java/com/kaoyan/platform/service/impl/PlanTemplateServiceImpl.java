package com.kaoyan.platform.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.kaoyan.platform.common.BusinessException;
import com.kaoyan.platform.entity.PlanTemplate;
import com.kaoyan.platform.entity.Todo;
import com.kaoyan.platform.mapper.PlanTemplateMapper;
import com.kaoyan.platform.service.PlanTemplateService;
import com.kaoyan.platform.service.TodoService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class PlanTemplateServiceImpl extends ServiceImpl<PlanTemplateMapper, PlanTemplate> implements PlanTemplateService {

    private final TodoService todoService;

    private static final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public List<PlanTemplate> getList() {
        LambdaQueryWrapper<PlanTemplate> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(PlanTemplate::getStatus, 1);
        wrapper.orderByDesc(PlanTemplate::getUseCount).orderByDesc(PlanTemplate::getCreateTime);
        return list(wrapper);
    }

    @Override
    public PlanTemplate getDetail(Long id) {
        PlanTemplate template = getById(id);
        if (template == null) {
            throw new BusinessException(404, "备考计划模板不存在");
        }
        return template;
    }

    @Override
    public Map<String, Object> applyTemplate(Long id, Long userId) {
        PlanTemplate template = getById(id);
        if (template == null) {
            throw new BusinessException(404, "备考计划模板不存在");
        }

        String templateData = template.getTemplateData();
        if (templateData == null || templateData.isEmpty()) {
            throw new BusinessException("模板数据为空，无法应用");
        }

        List<TaskItem> tasks;
        try {
            Map<String, List<TaskItem>> dataMap = objectMapper.readValue(templateData,
                    new TypeReference<Map<String, List<TaskItem>>>() {});
            tasks = dataMap.get("tasks");
        } catch (Exception e) {
            throw new BusinessException("模板数据格式错误");
        }

        if (tasks == null || tasks.isEmpty()) {
            throw new BusinessException("模板中没有待办任务");
        }

        List<Long> createdTodoIds = new ArrayList<>();
        LocalDate today = LocalDate.now();

        for (TaskItem task : tasks) {
            Todo todo = new Todo();
            todo.setUserId(userId);
            todo.setTitle(task.getTitle());
            todo.setDescription(task.getSubject());
            todo.setType(1);
            todo.setStartDate(today);
            todo.setEndDate(today.plusDays(task.getDurationDays() != null ? task.getDurationDays() : 1));
            todo.setPriority(task.getPriority() != null ? task.getPriority() : 2);
            todo.setStatus(0);

            todoService.save(todo);
            createdTodoIds.add(todo.getId());
        }

        // 增加模板使用次数
        template.setUseCount((template.getUseCount() != null ? template.getUseCount() : 0) + 1);
        updateById(template);

        Map<String, Object> result = new HashMap<>();
        result.put("appliedCount", createdTodoIds.size());
        result.put("todoIds", createdTodoIds);
        return result;
    }

    /**
     * 模板任务项内部类，对应 template_data JSON 中 tasks 数组的单个元素
     */
    @lombok.Data
    private static class TaskItem {
        private String title;
        private String subject;
        private Integer durationDays;
        private Integer priority;
    }
}
