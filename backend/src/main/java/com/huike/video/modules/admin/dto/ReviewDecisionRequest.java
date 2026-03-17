package com.huike.video.modules.admin.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/**
 * 提交审核决定请求 DTO
 * 接口文档 模块四 1.3
 */
@Data
public class ReviewDecisionRequest {

    /** 决定结果: PASSED, REJECTED */
    @NotBlank(message = "决定结果不能为空")
    private String status;

    /** 驳回原因，当 status=REJECTED 时必填 */
    private String rejectReason;

    /** 修改建议，选填 */
    private String suggestions;
}
