package com.kaoyan.platform.service;

import com.kaoyan.platform.entity.DownloadHistory;
import java.util.List;

public interface DownloadHistoryService {
    void addDownloadHistory(DownloadHistory downloadHistory);
    List<DownloadHistory> getDownloadHistoryByUserId(Long userId);
    boolean hasDownloaded(Long userId, Long resourceId);
}
