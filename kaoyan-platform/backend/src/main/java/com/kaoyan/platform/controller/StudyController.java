package com.kaoyan.platform.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.kaoyan.platform.common.PageResult;
import com.kaoyan.platform.common.Result;
import com.kaoyan.platform.entity.StudyRecord;
import com.kaoyan.platform.entity.Todo;
import com.kaoyan.platform.entity.DownloadHistory;
import com.kaoyan.platform.entity.UserBehaviorLog;
import com.kaoyan.platform.entity.UserCourse;
import com.kaoyan.platform.service.StudyRecordService;
import com.kaoyan.platform.service.TodoService;
import com.kaoyan.platform.service.LearningTrajectoryService;
import com.kaoyan.platform.service.DownloadHistoryService;
import com.kaoyan.platform.service.UserBehaviorLogService;
import com.kaoyan.platform.service.CourseService;
import com.kaoyan.platform.utils.UserContext;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/study")
@RequiredArgsConstructor
public class StudyController {
    
    private final StudyRecordService studyRecordService;
    private final TodoService todoService;
    private final LearningTrajectoryService learningTrajectoryService;
    private final DownloadHistoryService downloadHistoryService;
    private final UserBehaviorLogService userBehaviorLogService;
    private final CourseService courseService;
    
    // 学习记录相关
    @PostMapping("/record")
    public Result<Void> addStudyRecord(
            @RequestParam Integer duration,
            @RequestParam String subject,
            @RequestParam String content,
            @RequestParam Integer type) {
        Long userId = UserContext.getUserId();
        studyRecordService.addStudyRecord(userId, duration, subject, content, type);
        return Result.success();
    }
    
    @GetMapping("/records")
    public Result<PageResult<StudyRecord>> getStudyRecords(
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer size) {
        Long userId = UserContext.getUserId();
        return Result.success(studyRecordService.getStudyRecords(userId, page, size));
    }
    
    @GetMapping("/records/range")
    public Result<List<StudyRecord>> getStudyRecordsByRange(
            @RequestParam String startDate,
            @RequestParam String endDate) {
        Long userId = UserContext.getUserId();
        LocalDate start = LocalDate.parse(startDate);
        LocalDate end = LocalDate.parse(endDate);
        return Result.success(studyRecordService.getStudyRecordsByDateRange(userId, start, end));
    }
    
    @GetMapping("/duration/{date}")
    public Result<Integer> getDurationByDate(@PathVariable String date) {
        Long userId = UserContext.getUserId();
        LocalDate studyDate = LocalDate.parse(date);
        return Result.success(studyRecordService.getTotalDurationByDate(userId, studyDate));
    }
    
    @GetMapping("/heatmap")
    public Result<java.util.Map<String, Integer>> getStudyHeatmap(
            @RequestParam String startDate,
            @RequestParam String endDate) {
        Long userId = UserContext.getUserId();
        LocalDate start = LocalDate.parse(startDate);
        LocalDate end = LocalDate.parse(endDate);
        java.util.Map<LocalDate, Integer> heatmapData = studyRecordService.getStudyDurationHeatmap(userId, start, end);
        // 转换为字符串键的Map，方便前端处理
        java.util.Map<String, Integer> result = new java.util.HashMap<>();
        for (java.util.Map.Entry<LocalDate, Integer> entry : heatmapData.entrySet()) {
            result.put(entry.getKey().toString(), entry.getValue());
        }
        return Result.success(result);
    }
    
    // 学习轨迹相关
    @GetMapping("/trajectory")
    public Result<PageResult<java.util.Map<String, Object>>> getLearningTrajectory(
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer size) {
        Long userId = UserContext.getUserId();
        return Result.success(learningTrajectoryService.getLearningTrajectory(userId, page, size));
    }
    
    @GetMapping("/trajectory/range")
    public Result<java.util.List<java.util.Map<String, Object>>> getLearningTrajectoryByRange(
            @RequestParam String startDate,
            @RequestParam String endDate) {
        Long userId = UserContext.getUserId();
        LocalDate start = LocalDate.parse(startDate);
        LocalDate end = LocalDate.parse(endDate);
        return Result.success(learningTrajectoryService.getLearningTrajectoryByDateRange(userId, start, end));
    }
    
    @GetMapping("/trajectory/summary")
    public Result<java.util.Map<String, Object>> getLearningSummary() {
        Long userId = UserContext.getUserId();
        return Result.success(learningTrajectoryService.getLearningSummary(userId));
    }
    
    // 待办事项相关
    @PostMapping("/todo")
    public Result<Void> addTodo(
            @RequestParam String title,
            @RequestParam(required = false) String description,
            @RequestParam Integer type,
            @RequestParam(required = false) String targetDate,
            @RequestParam(required = false) String startDate,
            @RequestParam(required = false) String endDate,
            @RequestParam(required = false) String remindTime,
            @RequestParam(defaultValue = "1") Integer priority) {
        Long userId = UserContext.getUserId();
        todoService.addTodo(userId, title, description, type, targetDate, startDate, endDate, remindTime, priority);
        return Result.success();
    }
    
    @GetMapping("/todos")
    public Result<PageResult<Todo>> getTodos(
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer size,
            @RequestParam(required = false) Integer status,
            @RequestParam(required = false) Integer type) {
        Long userId = UserContext.getUserId();
        return Result.success(todoService.getTodoList(userId, page, size, status, type));
    }
    
    @PutMapping("/todo/{id}/status")
    public Result<Void> updateTodoStatus(@PathVariable Long id, @RequestParam Integer status) {
        todoService.updateTodoStatus(id, status);
        return Result.success();
    }
    
    @DeleteMapping("/todo/{id}")
    public Result<Void> deleteTodo(@PathVariable Long id) {
        todoService.deleteTodo(id);
        return Result.success();
    }
    
    @GetMapping("/urgent-tasks")
    public Result<List<Map<String, Object>>> getUrgentTasks() {
        Long userId = UserContext.getUserId();
        List<Map<String, Object>> urgentTasks = new ArrayList<>();
        LocalDate today = LocalDate.now();

        // 获取所有待办和计划
        PageResult<Todo> todos = todoService.getTodoList(userId, 1, 200, 0, null);
        for (Todo todo : todos.getRecords()) {
            LocalDate deadline = null;
            if (todo.getEndDate() != null) deadline = todo.getEndDate();
            else if (todo.getTargetDate() != null) deadline = todo.getTargetDate();
            if (deadline == null) continue;

            long daysLeft = ChronoUnit.DAYS.between(today, deadline);
            if (daysLeft <= 3) {
                Map<String, Object> task = new HashMap<>();
                task.put("id", todo.getId());
                task.put("title", todo.getTitle());
                task.put("deadline", deadline.toString());
                task.put("type", todo.getType() == 1 ? "plan" : "todo");
                task.put("isOverdue", daysLeft < 0);
                task.put("daysLeft", (int) daysLeft);
                urgentTasks.add(task);
            }
        }
        return Result.success(urgentTasks);
    }

    @GetMapping("/todos/overdue")
    public Result<List<Todo>> getOverdueTodos() {
        Long userId = UserContext.getUserId();
        return Result.success(todoService.getOverdueTodos(userId));
    }
    
    @GetMapping("/plans/statistics")
    public Result<java.util.Map<String, Object>> getPlanStatistics() {
        Long userId = UserContext.getUserId();
        return Result.success(todoService.getPlanStatistics(userId));
    }

    @GetMapping("/calendar")
    public Result<Map<String, Object>> getCalendarData(
            @RequestParam String startDate,
            @RequestParam String endDate) {
        Long userId = UserContext.getUserId();
        LocalDate start = LocalDate.parse(startDate);
        LocalDate end = LocalDate.parse(endDate);

        Map<String, Object> calendarData = todoService.getCalendarData(userId, start, end);

        // 加入学习记录数据
        Map<String, Integer> studyRecords = new HashMap<>();
        java.util.Map<LocalDate, Integer> heatmapData = studyRecordService.getStudyDurationHeatmap(userId, start, end);
        for (java.util.Map.Entry<LocalDate, Integer> entry : heatmapData.entrySet()) {
            studyRecords.put(entry.getKey().toString(), entry.getValue());
        }
        calendarData.put("studyRecords", studyRecords);

        return Result.success(calendarData);
    }

    @GetMapping("/subject-distribution")
    public Result<List<Map<String, Object>>> getSubjectDistribution() {
        Long userId = UserContext.getUserId();
        // 全量聚合各科目学习时长
        Map<String, Integer> subjectDurations = new HashMap<>();
        List<StudyRecord> allRecords = studyRecordService.list(
            new LambdaQueryWrapper<StudyRecord>().eq(StudyRecord::getUserId, userId));
        for (StudyRecord r : allRecords) {
            String subject = r.getSubject() != null ? r.getSubject() : "其他";
            subjectDurations.merge(subject, r.getDuration() != null ? r.getDuration() : 0, Integer::sum);
        }
        // 按时长降序排列
        List<Map<String, Object>> result = new ArrayList<>();
        for (Map.Entry<String, Integer> entry : subjectDurations.entrySet()) {
            Map<String, Object> item = new HashMap<>();
            item.put("subject", entry.getKey());
            item.put("duration", entry.getValue());
            result.add(item);
        }
        result.sort((a, b) -> ((Integer) b.get("duration")).compareTo((Integer) a.get("duration")));
        return Result.success(result);
    }

    @GetMapping("/weekly-report")
    public Result<Map<String, Object>> getWeeklyReport(@RequestParam String weekStart) {
        Long userId = UserContext.getUserId();
        LocalDate start = LocalDate.parse(weekStart);
        LocalDate end = start.plusDays(6);
        Map<String, Object> report = new HashMap<>();

        // 本周总学习时长
        int weekTotal = studyRecordService.getTotalDurationByDateRange(userId, start, end);
        report.put("weekTotal", weekTotal);

        // 每日学习时长
        Map<String, Integer> dailyData = new HashMap<>();
        for (int i = 0; i < 7; i++) {
            LocalDate d = start.plusDays(i);
            int dur = studyRecordService.getTotalDurationByDate(userId, d);
            dailyData.put(d.toString(), dur);
        }
        report.put("dailyData", dailyData);

        // 本周科目分布
        List<StudyRecord> weekRecords = studyRecordService.getStudyRecordsByDateRange(userId, start, end);
        Map<String, Integer> subjectMap = new HashMap<>();
        for (StudyRecord r : weekRecords) {
            String s = r.getSubject() != null ? r.getSubject() : "其他";
            subjectMap.merge(s, r.getDuration() != null ? r.getDuration() : 0, Integer::sum);
        }
        report.put("subjectDistribution", subjectMap);

        // 本周完成章节数
        long completedChapters = weekRecords.stream()
            .filter(r -> r.getType() != null && r.getType() == 1)
            .count();
        report.put("completedChapters", (int) completedChapters);

        // 本周计划/待办完成数
        PageResult<Todo> todos = todoService.getTodoList(userId, 1, 200, null, null);
        long weekCompleted = todos.getRecords().stream()
            .filter(t -> t.getStatus() == 1 && t.getUpdateTime() != null
                && !t.getUpdateTime().toLocalDate().isBefore(start)
                && !t.getUpdateTime().toLocalDate().isAfter(end))
            .count();
        report.put("completedTasks", (int) weekCompleted);

        return Result.success(report);
    }

    @GetMapping("/stats")
    public Result<Map<String, Object>> getStudyStats() {
        Long userId = UserContext.getUserId();
        Map<String, Object> stats = new HashMap<>();
        
        // 总学习时长（最近30天）
        int totalStudyTime = studyRecordService.getTotalDurationByDateRange(userId, LocalDate.now().minusDays(30), LocalDate.now());
        stats.put("totalStudyTime", totalStudyTime);
        
        // 已完成课程数量（进度>=100的课程）
        List<Map<String, Object>> userCourses = courseService.getUserCourses(userId);
        long completedCount = 0;
        if (userCourses != null) {
            completedCount = userCourses.stream()
                .filter(c -> {
                    Object progress = c.get("progress");
                    if (progress instanceof Number) {
                        return ((Number) progress).intValue() >= 100;
                    }
                    return false;
                })
                .count();
        }
        stats.put("completedCourses", (int) completedCount);

        // 下载资源数量（最近30天）
        List<DownloadHistory> downloads = downloadHistoryService.getDownloadHistoryByUserId(userId);
        long recentDownloads = downloads.stream()
                .filter(h -> h.getDownloadTime() != null &&
                    h.getDownloadTime().toLocalDate().isAfter(LocalDate.now().minusDays(31)))
                .count();
        stats.put("downloadedResources", (int) recentDownloads);

        // 阅读资讯数量（最近30天）
        long recentNewsReads = userBehaviorLogService.count(new LambdaQueryWrapper<UserBehaviorLog>()
                .eq(UserBehaviorLog::getUserId, userId)
                .eq(UserBehaviorLog::getActionType, "view_news")
                .ge(UserBehaviorLog::getCreateTime, LocalDate.now().minusDays(30).atStartOfDay()));
        stats.put("readNews", (int) recentNewsReads);
        
        // 学习记录（最近7天）
        Map<String, Integer> studyRecords = new HashMap<>();
        for (int i = 6; i >= 0; i--) {
            LocalDate date = LocalDate.now().minusDays(i);
            int duration = studyRecordService.getTotalDurationByDate(userId, date);
            studyRecords.put(date.toString(), duration);
        }
        stats.put("studyRecords", studyRecords);
        
        // 最近学习记录
        List<StudyRecord> recentRecords = studyRecordService.getRecentRecords(userId, 10);
        stats.put("recentRecords", recentRecords);
        
        // 待办事项列表
        PageResult<Todo> todoPage = todoService.getTodoList(userId, 1, 50, null, 2);
        stats.put("todoList", todoPage.getRecords());

        // 学习计划列表 (附带进度计算)
        PageResult<Todo> planPage = todoService.getTodoList(userId, 1, 50, null, 1);
        List<Map<String, Object>> enrichedPlans = new ArrayList<>();
        for (Todo plan : planPage.getRecords()) {
            Map<String, Object> planMap = new HashMap<>();
            planMap.put("id", plan.getId());
            planMap.put("title", plan.getTitle());
            planMap.put("description", plan.getDescription());
            planMap.put("targetDate", plan.getTargetDate() != null ? plan.getTargetDate().toString() : null);
            planMap.put("startDate", plan.getStartDate() != null ? plan.getStartDate().toString() : null);
            planMap.put("endDate", plan.getEndDate() != null ? plan.getEndDate().toString() : null);
            planMap.put("priority", plan.getPriority());
            planMap.put("status", plan.getStatus());
            planMap.put("createTime", plan.getCreateTime());

            // 计算进度
            int progress = 0;
            if (plan.getStartDate() != null && plan.getEndDate() != null) {
                long totalDays = ChronoUnit.DAYS.between(plan.getStartDate(), plan.getEndDate());
                if (totalDays > 0) {
                    long elapsedDays = ChronoUnit.DAYS.between(plan.getStartDate(), LocalDate.now());
                    progress = (int) Math.min(100, Math.max(0, elapsedDays * 100 / totalDays));
                }
            }
            planMap.put("progress", plan.getStatus() == 1 ? 100 : progress);
            enrichedPlans.add(planMap);
        }
        stats.put("planList", enrichedPlans);

        // 学习轨迹数据（已读资讯、已下载资源、已学课程）
        PageResult<Map<String, Object>> trajectory = learningTrajectoryService.getLearningTrajectory(userId, 1, 20);
        List<Map<String, Object>> trajectoryList = trajectory.getRecords();
        List<Map<String, Object>> readNewsList = new ArrayList<>();
        List<Map<String, Object>> downloadedResourcesList = new ArrayList<>();
        List<Map<String, Object>> studiedCoursesList = new ArrayList<>();
        for (Map<String, Object> item : trajectoryList) {
            String type = (String) item.get("type");
            if ("read_news".equals(type)) {
                readNewsList.add(item);
            } else if ("download".equals(type)) {
                downloadedResourcesList.add(item);
            } else if ("study".equals(type)) {
                studiedCoursesList.add(item);
            }
        }
        stats.put("readNewsList", readNewsList);
        stats.put("downloadedResourcesList", downloadedResourcesList);
        stats.put("studiedCoursesList", studiedCoursesList);

        return Result.success(stats);
    }
}
