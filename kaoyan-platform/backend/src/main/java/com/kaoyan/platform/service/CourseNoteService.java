package com.kaoyan.platform.service;

import com.kaoyan.platform.entity.CourseNote;

import java.util.List;

public interface CourseNoteService {

    List<CourseNote> getNotesByChapter(Long userId, Long chapterId);

    List<CourseNote> getNotesByCourse(Long userId, Long courseId);

    void addNote(CourseNote note);

    void updateNote(Long id, CourseNote note);

    void deleteNote(Long id);
}
