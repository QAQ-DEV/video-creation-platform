package com.huike.video.modules.security.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/**
 * 用户举报请求 DTO
 * 接口文档 模块四 3.1
 */
@Data
public class ReportCreateRequest {

    /** 被举报内容ID */
    @NotBlank(message = "被举报内容ID不能为空")
    private String targetId;

    /** 类型: VIDEO, COMMENT */
    @NotBlank(message = "举报类型不能为空")
    private String targetType;

    /** 举报类型: 涉黄/暴力/侵权/其他 */
    @NotBlank(message = "举报原因类型不能为空")
    private String reasonType;

    /** 详细描述，选填 */
    private String description;
}
