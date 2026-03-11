package com.huike.video.modules.admin.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/**
 * 启用/禁用审核规则请求 DTO
 * 接口文档 模块四 2.3
 */
@Data
public class ReviewRuleStatusUpdateRequest {

    /** ENABLE / DISABLE */
    @NotBlank(message = "状态不能为空")
    private String status;
}
