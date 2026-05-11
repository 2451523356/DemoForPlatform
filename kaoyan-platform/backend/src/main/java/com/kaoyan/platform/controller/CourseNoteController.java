package com.kaoyan.platform.controller;

import com.kaoyan.platform.common.Result;
import com.kaoyan.platform.entity.CourseNote;
import com.kaoyan.platform.service.CourseNoteService;
import com.kaoyan.platform.utils.UserContext;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/course-note")
@RequiredArgsConstructor
public class CourseNoteController {

    private final CourseNoteService courseNoteService;

    @GetMapping("/list")
    public Result<List<CourseNote>> listNotes(@RequestParam(required = false) Long chapterId,
                                              @RequestParam(required = false) Long courseId) {
        Long userId = UserContext.getUserId();
        if (userId == null) {
            return Result.error(401, "用户未登录");
        }
        List<CourseNote> notes;
        if (chapterId != null) {
            notes = courseNoteService.getNotesByChapter(userId, chapterId);
        } else if (courseId != null) {
            notes = courseNoteService.getNotesByCourse(userId, courseId);
        } else {
            return Result.error(400, "请提供 chapterId 或 courseId");
        }
        return Result.success(notes);
    }

    @PostMapping("/add")
    public Result<Void> addNote(@RequestBody CourseNote note) {
        Long userId = UserContext.getUserId();
        if (userId == null) {
            return Result.error(401, "用户未登录");
        }
        note.setUserId(userId);
        courseNoteService.addNote(note);
        return Result.success();
    }

    @PutMapping("/{id}")
    public Result<Void> updateNote(@PathVariable Long id, @RequestBody CourseNote note) {
        Long userId = UserContext.getUserId();
        if (userId == null) {
            return Result.error(401, "用户未登录");
        }
        note.setUserId(userId);
        courseNoteService.updateNote(id, note);
        return Result.success();
    }

    @DeleteMapping("/{id}")
    public Result<Void> deleteNote(@PathVariable Long id) {
        Long userId = UserContext.getUserId();
        if (userId == null) {
            return Result.error(401, "用户未登录");
        }
        courseNoteService.deleteNote(id);
        return Result.success();
    }
}
