package com.kaoyan.platform.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.kaoyan.platform.entity.DownloadHistory;
import com.kaoyan.platform.mapper.DownloadHistoryMapper;
import com.kaoyan.platform.service.DownloadHistoryService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
@RequiredArgsConstructor
public class DownloadHistoryServiceImpl extends ServiceImpl<DownloadHistoryMapper, DownloadHistory> implements DownloadHistoryService {
    
    private final DownloadHistoryMapper downloadHistoryMapper;
    
    @Override
    public void addDownloadHistory(DownloadHistory downloadHistory) {
        save(downloadHistory);
    }
    
    @Override
    public List<DownloadHistory> getDownloadHistoryByUserId(Long userId) {
        return baseMapper.selectList(new com.baomidou.mybatisplus.core.conditions.query.QueryWrapper<DownloadHistory>().eq("user_id", userId).eq("deleted", 0).orderByDesc("download_time"));
    }

    @Override
    public boolean hasDownloaded(Long userId, Long resourceId) {
        return baseMapper.selectCount(new com.baomidou.mybatisplus.core.conditions.query.QueryWrapper<DownloadHistory>()
                .eq("user_id", userId).eq("resource_id", resourceId).eq("deleted", 0)) > 0;
    }
}
