package com.huike.video.modules.admin.vo;

import lombok.Data;

/**
 * 审核列表项 VO
 * 接口文档 模块四 1.1
 */
@Data
public class ReviewListItemVO {

    private Long reviewId;
    private String contentId;
    private String contentUrl;
    private String coverUrl;
    private String creatorName;
    private String submitTime;
    /** 机审建议(扩展) */
    private String machineCheckResult;
    private String status;
}
