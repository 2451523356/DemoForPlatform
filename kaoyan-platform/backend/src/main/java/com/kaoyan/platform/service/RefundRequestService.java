package com.kaoyan.platform.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.kaoyan.platform.common.PageResult;
import com.kaoyan.platform.entity.RefundRequest;

public interface RefundRequestService extends IService<RefundRequest> {

    PageResult<RefundRequest> getRefundList(Integer page, Integer size, Integer status);

    void createRefundRequest(Long userId, Long courseId, Long orderId, String reason);

    void approveRefund(Long id, String rejectReason);

    void rejectRefund(Long id, String rejectReason);
}
