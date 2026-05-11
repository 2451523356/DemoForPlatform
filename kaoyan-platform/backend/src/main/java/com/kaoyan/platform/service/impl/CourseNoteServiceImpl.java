package com.kaoyan.platform.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.kaoyan.platform.entity.CourseNote;
import com.kaoyan.platform.mapper.CourseNoteMapper;
import com.kaoyan.platform.service.CourseNoteService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class CourseNoteServiceImpl extends ServiceImpl<CourseNoteMapper, CourseNote> implements CourseNoteService {

    @Override
    public List<CourseNote> getNotesByChapter(Long userId, Long chapterId) {
        LambdaQueryWrapper<CourseNote> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(CourseNote::getUserId, userId);
        wrapper.eq(CourseNote::getChapterId, chapterId);
        wrapper.orderByAsc(CourseNote::getVideoTime);
        return list(wrapper);
    }

    @Override
    public List<CourseNote> getNotesByCourse(Long userId, Long courseId) {
        LambdaQueryWrapper<CourseNote> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(CourseNote::getUserId, userId);
        wrapper.eq(CourseNote::getCourseId, courseId);
        wrapper.orderByDesc(CourseNote::getCreateTime);
        return list(wrapper);
    }

    @Override
    public void addNote(CourseNote note) {
        save(note);
    }

    @Override
    public void updateNote(Long id, CourseNote note) {
        note.setId(id);
        updateById(note);
    }

    @Override
    public void deleteNote(Long id) {
        removeById(id);
    }
}
