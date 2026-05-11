package com.kaoyan.platform.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.kaoyan.platform.common.PageResult;
import com.kaoyan.platform.entity.LiveStream;

import java.util.List;

public interface LiveStreamService extends IService<LiveStream> {

    PageResult<LiveStream> getLiveStreams(Integer page, Integer size, Integer status, Long courseId);

    LiveStream getLiveStreamById(Long id);

    void startLiveStream(Long id);

    void endLiveStream(Long id);

    void updateViewerCount(Long id, Integer viewerCount);

    List<LiveStream> getUpcomingLiveStreams();

    List<LiveStream> getLiveStreamsByCourseId(Long courseId);
}
