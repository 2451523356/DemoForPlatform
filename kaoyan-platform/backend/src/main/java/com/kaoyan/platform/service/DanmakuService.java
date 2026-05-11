package com.kaoyan.platform.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.kaoyan.platform.common.PageResult;
import com.kaoyan.platform.entity.Danmaku;

import java.util.List;

public interface DanmakuService extends IService<Danmaku> {

    void addDanmaku(Long liveStreamId, Long userId, String content, Integer type, String color, Integer position);

    PageResult<Danmaku> getDanmakus(Long liveStreamId, Integer page, Integer size);

    List<Danmaku> getRecentDanmakus(Long liveStreamId, Integer limit);

    Integer getDanmakuCount(Long liveStreamId);
}
