package com.huike.video.modules.admin.vo;

import lombok.Data;

import java.util.List;

/**
 * 审核规则列表响应 VO
 */
@Data
public class ReviewRuleListResponse {

    private List<ReviewRuleVO> list;
}
