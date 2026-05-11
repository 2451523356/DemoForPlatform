package com.kaoyan.platform.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.kaoyan.platform.common.PageResult;
import com.kaoyan.platform.entity.Danmaku;
import com.kaoyan.platform.mapper.DanmakuMapper;
import com.kaoyan.platform.service.DanmakuService;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class DanmakuServiceImpl extends ServiceImpl<DanmakuMapper, Danmaku> implements DanmakuService {

    @Override
    public void addDanmaku(Long liveStreamId, Long userId, String content, Integer type, String color, Integer position) {
        Danmaku danmaku = new Danmaku();
        danmaku.setLiveStreamId(liveStreamId);
        danmaku.setUserId(userId);
        danmaku.setContent(content);
        danmaku.setType(type);
        danmaku.setColor(color);
        danmaku.setPosition(position);
        danmaku.setSendTime(LocalDateTime.now());
        save(danmaku);
    }

    @Override
    public PageResult<Danmaku> getDanmakus(Long liveStreamId, Integer page, Integer size) {
        LambdaQueryWrapper<Danmaku> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Danmaku::getLiveStreamId, liveStreamId);
        wrapper.orderByAsc(Danmaku::getSendTime);
        
        Page<Danmaku> danmakuPage = page(new Page<>(page, size), wrapper);
        
        return new PageResult<>(danmakuPage.getTotal(), (long) page, (long) size, danmakuPage.getRecords());
    }

    @Override
    public List<Danmaku> getRecentDanmakus(Long liveStreamId, Integer limit) {
        LambdaQueryWrapper<Danmaku> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Danmaku::getLiveStreamId, liveStreamId);
        wrapper.orderByDesc(Danmaku::getSendTime);
        wrapper.last("LIMIT " + limit);
        return list(wrapper);
    }

    @Override
    public Integer getDanmakuCount(Long liveStreamId) {
        LambdaQueryWrapper<Danmaku> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Danmaku::getLiveStreamId, liveStreamId);
        return Math.toIntExact(count(wrapper));
    }
}
