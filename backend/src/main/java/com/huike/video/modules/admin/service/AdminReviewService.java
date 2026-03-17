package com.huike.video.modules.admin.service;

import com.huike.video.modules.admin.dto.ReviewDecisionRequest;
import com.huike.video.modules.admin.vo.ReviewDetailResponse;
import com.huike.video.modules.admin.vo.ReviewListResponse;

/**
 * 管理员审核任务服务
 * 接口文档 模块四 1.1-1.3
 */
public interface AdminReviewService {

    /**
     * 分页获取审核列表
     *
     * @param page       页码
     * @param size       每页条数（最大100）
     * @param status     状态筛选 PENDING/PASSED/REJECTED/RE_AUDIT
     * @param reviewerId 审核员ID，看自己的任务
     * @param contentType 内容类型 VIDEO/IMAGE
     */
    ReviewListResponse getReviewList(Integer page, Integer size, String status,
                                     String reviewerId, String contentType);

    /**
     * 获取审核详情
     */
    ReviewDetailResponse getReviewDetail(Long reviewId);

    /**
     * 提交审核决定（通过/驳回）
     */
    boolean submitDecision(Long reviewId, String adminId, ReviewDecisionRequest request);
}
