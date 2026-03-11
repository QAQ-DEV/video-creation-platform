package com.huike.video.modules.admin.vo;

import lombok.Data;

import java.util.Map;

/**
 * 审核规则项 VO
 * 接口文档 模块四 2.1
 */
@Data
public class ReviewRuleVO {

    private Long ruleId;
    private String ruleName;
    private String ruleType;
    private Integer priority;
    private String status;
    /** JSON 结构条件，接口以 Map 返回 */
    private Map<String, Object> conditions;
}
