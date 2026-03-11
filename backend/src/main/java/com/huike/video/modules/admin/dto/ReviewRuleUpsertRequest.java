package com.huike.video.modules.admin.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.Map;

/**
 * 新增/修改审核规则请求 DTO
 * 接口文档 模块四 2.2
 */
@Data
public class ReviewRuleUpsertRequest {

    /** 规则ID，更新时必填 */
    private Long ruleId;

    @NotBlank(message = "规则名称不能为空")
    private String ruleName;

    /** 类型: TEXT_SENSITIVE, IMAGE_PORN, COPYRIGHT 或 TEXT/IMAGE/VIDEO */
    @NotBlank(message = "规则类型不能为空")
    private String ruleType;

    /** 触发条件，如 {"keywords": ["词A"]} 或 {"threshold": 80} */
    @NotNull(message = "触发条件不能为空")
    private Map<String, Object> conditions;

    /** 执行动作，如 {"action": "REJECT"} */
    @NotNull(message = "执行动作不能为空")
    private Map<String, Object> actions;

    /** 优先级，默认 5 */
    private Integer priority = 5;
}
