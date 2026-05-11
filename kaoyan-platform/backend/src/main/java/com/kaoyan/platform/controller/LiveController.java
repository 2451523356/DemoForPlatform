package com.kaoyan.platform.controller;

import com.kaoyan.platform.common.PageResult;
import com.kaoyan.platform.common.Result;
import com.kaoyan.platform.entity.Danmaku;
import com.kaoyan.platform.entity.LiveStream;
import com.kaoyan.platform.service.DanmakuService;
import com.kaoyan.platform.service.LiveStreamService;
import com.kaoyan.platform.utils.UserContext;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/live")
@RequiredArgsConstructor
public class LiveController {

    private final LiveStreamService liveStreamService;
    private final DanmakuService danmakuService;

    // 直播管理相关
    @GetMapping("/list")
    public Result<PageResult<LiveStream>> getLiveStreams(
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer size,
            @RequestParam(required = false) Integer status,
            @RequestParam(required = false) Long courseId) {
        return Result.success(liveStreamService.getLiveStreams(page, size, status, courseId));
    }

    @GetMapping("/detail/{id}")
    public Result<LiveStream> getLiveStreamDetail(@PathVariable Long id) {
        return Result.success(liveStreamService.getLiveStreamById(id));
    }

    @PostMapping("/start/{id}")
    public Result<Void> startLiveStream(@PathVariable Long id) {
        liveStreamService.startLiveStream(id);
        return Result.success();
    }

    @PostMapping("/end/{id}")
    public Result<Void> endLiveStream(@PathVariable Long id) {
        liveStreamService.endLiveStream(id);
        return Result.success();
    }

    @PutMapping("/viewer-count/{id}")
    public Result<Void> updateViewerCount(@PathVariable Long id, @RequestParam Integer viewerCount) {
        liveStreamService.updateViewerCount(id, viewerCount);
        return Result.success();
    }

    @GetMapping("/upcoming")
    public Result<List<LiveStream>> getUpcomingLiveStreams() {
        return Result.success(liveStreamService.getUpcomingLiveStreams());
    }

    @GetMapping("/course/{courseId}")
    public Result<List<LiveStream>> getLiveStreamsByCourse(@PathVariable Long courseId) {
        return Result.success(liveStreamService.getLiveStreamsByCourseId(courseId));
    }

    // 弹幕相关
    @PostMapping("/danmaku")
    public Result<Void> addDanmaku(
            @RequestParam Long liveStreamId,
            @RequestParam String content,
            @RequestParam(defaultValue = "0") Integer type,
            @RequestParam(defaultValue = "#ffffff") String color,
            @RequestParam(defaultValue = "0") Integer position) {
        Long userId = UserContext.getUserId();
        if (userId == null) {
            return Result.error(401, "用户未登录");
        }
        danmakuService.addDanmaku(liveStreamId, userId, content, type, color, position);
        return Result.success();
    }

    @GetMapping("/danmaku/{liveStreamId}")
    public Result<PageResult<Danmaku>> getDanmakus(
            @PathVariable Long liveStreamId,
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "50") Integer size) {
        return Result.success(danmakuService.getDanmakus(liveStreamId, page, size));
    }

    @GetMapping("/danmaku/{liveStreamId}/recent")
    public Result<List<Danmaku>> getRecentDanmakus(
            @PathVariable Long liveStreamId,
            @RequestParam(defaultValue = "20") Integer limit) {
        return Result.success(danmakuService.getRecentDanmakus(liveStreamId, limit));
    }

    @GetMapping("/danmaku/{liveStreamId}/count")
    public Result<Integer> getDanmakuCount(@PathVariable Long liveStreamId) {
        return Result.success(danmakuService.getDanmakuCount(liveStreamId));
    }
}
