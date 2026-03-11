package com.huike.video.modules.admin.controller;

import cn.dev33.satoken.stp.StpUtil;
import com.huike.video.common.result.Result;
import com.huike.video.modules.admin.dto.ReviewDecisionRequest;
import com.huike.video.modules.admin.dto.ReviewRuleStatusUpdateRequest;
import com.huike.video.modules.admin.dto.ReviewRuleUpsertRequest;
import com.huike.video.modules.admin.service.AdminReviewRuleService;
import com.huike.video.modules.admin.service.AdminReviewService;
import com.huike.video.modules.admin.vo.ReviewDetailResponse;
import com.huike.video.modules.admin.vo.ReviewListResponse;
import com.huike.video.modules.admin.vo.ReviewRuleListResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

/**
 * 内容审核与安全模块 - 管理员端
 * 接口文档 模块四 1.1-1.3, 2.1-2.3
 * 路径: /api/v1/admin/reviews, /api/v1/admin/review-rules
 */
@RestController
@RequiredArgsConstructor
public class AdminReviewController {

    private final AdminReviewService adminReviewService;
    private final AdminReviewRuleService adminReviewRuleService;

    // ---------- 审核任务管理 1.1-1.3 ----------

    /**
     * 1.1 获取审核列表 (管理员)
     * GET /api/v1/admin/reviews
     */
    @GetMapping("/api/v1/admin/reviews")
    public Result<ReviewListResponse> getReviewList(
            @RequestParam(value = "page", required = false) Integer page,
            @RequestParam(value = "size", required = false) Integer size,
            @RequestParam(value = "status", required = false) String status,
            @RequestParam(value = "reviewerId", required = false) String reviewerId,
            @RequestParam(value = "contentType", required = false) String contentType) {
        ReviewListResponse data = adminReviewService.getReviewList(page, size, status, reviewerId, contentType);
        return Result.success(data);
    }

    /**
     * 1.2 获取审核详情
     * GET /api/v1/admin/reviews/{reviewId}
     */
    @GetMapping("/api/v1/admin/reviews/{reviewId}")
    public Result<ReviewDetailResponse> getReviewDetail(@PathVariable Long reviewId) {
        ReviewDetailResponse data = adminReviewService.getReviewDetail(reviewId);
        return Result.success(data);
    }

    /**
     * 1.3 提交审核决定
     * PUT /api/v1/admin/reviews/{reviewId}/decision
     */
    @PutMapping("/api/v1/admin/reviews/{reviewId}/decision")
    public Result<Boolean> submitDecision(
            @PathVariable Long reviewId,
            @RequestBody @Validated ReviewDecisionRequest request) {
        String adminId = StpUtil.getLoginIdAsString();
        boolean success = adminReviewService.submitDecision(reviewId, adminId, request);
        return Result.success(success);
    }

    // ---------- 审核规则配置 2.1-2.3 ----------

    /**
     * 2.1 获取规则列表
     * GET /api/v1/admin/review-rules
     */
    @GetMapping("/api/v1/admin/review-rules")
    public Result<ReviewRuleListResponse> getReviewRules(
            @RequestParam(value = "ruleType", required = false) String ruleType) {
        ReviewRuleListResponse data = adminReviewRuleService.getRuleList(ruleType);
        return Result.success(data);
    }

    /**
     * 2.2 新增/修改规则
     * POST /api/v1/admin/review-rules
     */
    @PostMapping("/api/v1/admin/review-rules")
    public Result<Long> upsertReviewRule(@RequestBody @Validated ReviewRuleUpsertRequest request) {
        String adminId = StpUtil.getLoginIdAsString();
        Long ruleId = adminReviewRuleService.upsertRule(adminId, request);
        return Result.success(ruleId);
    }

    /**
     * 2.3 启用/禁用规则
     * PUT /api/v1/admin/review-rules/{ruleId}/status
     */
    @PutMapping("/api/v1/admin/review-rules/{ruleId}/status")
    public Result<Boolean> updateRuleStatus(
            @PathVariable Long ruleId,
            @RequestBody @Validated ReviewRuleStatusUpdateRequest request) {
        boolean success = adminReviewRuleService.updateRuleStatus(ruleId, request);
        return Result.success(success);
    }
}
