package com.huike.video.modules.admin.service;

import com.huike.video.modules.admin.dto.ReviewRuleStatusUpdateRequest;
import com.huike.video.modules.admin.dto.ReviewRuleUpsertRequest;
import com.huike.video.modules.admin.vo.ReviewRuleListResponse;

/**
 * 管理员审核规则配置服务
 * 接口文档 模块四 2.1-2.3
 */
public interface AdminReviewRuleService {

    /**
     * 获取规则列表，可选按 ruleType 筛选
     */
    ReviewRuleListResponse getRuleList(String ruleType);

    /**
     * 新增或修改规则，返回 ruleId
     */
    Long upsertRule(String adminId, ReviewRuleUpsertRequest request);

    /**
     * 启用/禁用规则
     */
    boolean updateRuleStatus(Long ruleId, ReviewRuleStatusUpdateRequest request);
}
