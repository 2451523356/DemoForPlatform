package com.kaoyan.platform.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.kaoyan.platform.common.PageResult;
import com.kaoyan.platform.entity.LiveStream;
import com.kaoyan.platform.mapper.LiveStreamMapper;
import com.kaoyan.platform.service.LiveStreamService;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class LiveStreamServiceImpl extends ServiceImpl<LiveStreamMapper, LiveStream> implements LiveStreamService {

    @Override
    public PageResult<LiveStream> getLiveStreams(Integer page, Integer size, Integer status, Long courseId) {
        LambdaQueryWrapper<LiveStream> wrapper = new LambdaQueryWrapper<>();
        
        if (status != null) {
            wrapper.eq(LiveStream::getStatus, status);
        }
        
        if (courseId != null) {
            wrapper.eq(LiveStream::getCourseId, courseId);
        }
        
        wrapper.orderByDesc(LiveStream::getStartTime);
        
        Page<LiveStream> liveStreamPage = page(new Page<>(page, size), wrapper);
        
        return new PageResult<>(liveStreamPage.getTotal(), (long) page, (long) size, liveStreamPage.getRecords());
    }

    @Override
    public LiveStream getLiveStreamById(Long id) {
        return getById(id);
    }

    @Override
    public void startLiveStream(Long id) {
        LiveStream liveStream = getById(id);
        if (liveStream != null) {
            liveStream.setStatus(1); // 直播中
            liveStream.setStartTime(LocalDateTime.now());
            updateById(liveStream);
        }
    }

    @Override
    public void endLiveStream(Long id) {
        LiveStream liveStream = getById(id);
        if (liveStream != null) {
            liveStream.setStatus(2); // 已结束
            liveStream.setEndTime(LocalDateTime.now());
            updateById(liveStream);
        }
    }

    @Override
    public void updateViewerCount(Long id, Integer viewerCount) {
        LiveStream liveStream = getById(id);
        if (liveStream != null) {
            liveStream.setViewerCount(viewerCount);
            updateById(liveStream);
        }
    }

    @Override
    public List<LiveStream> getUpcomingLiveStreams() {
        LambdaQueryWrapper<LiveStream> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(LiveStream::getStatus, 0); // 未开始
        wrapper.gt(LiveStream::getStartTime, LocalDateTime.now());
        wrapper.orderByAsc(LiveStream::getStartTime);
        return list(wrapper);
    }

    @Override
    public List<LiveStream> getLiveStreamsByCourseId(Long courseId) {
        LambdaQueryWrapper<LiveStream> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(LiveStream::getCourseId, courseId);
        wrapper.orderByDesc(LiveStream::getStartTime);
        return list(wrapper);
    }
}
