package com.kaoyan.platform.vo;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class CourseProgressVO {
    private List<Long> completedChapters = new ArrayList<>();
    private Map<Long, Integer> chapterProgress = new HashMap<>();
    
    public List<Long> getCompletedChapters() {
        return completedChapters;
    }
    
    public void setCompletedChapters(List<Long> completedChapters) {
        this.completedChapters = completedChapters;
    }
    
    public Map<Long, Integer> getChapterProgress() {
        return chapterProgress;
    }
    
    public void setChapterProgress(Map<Long, Integer> chapterProgress) {
        this.chapterProgress = chapterProgress;
    }
}