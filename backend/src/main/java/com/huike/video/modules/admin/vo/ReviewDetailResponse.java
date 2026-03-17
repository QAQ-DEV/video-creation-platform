package com.huike.video.modules.admin.vo;

import lombok.Data;

import java.util.List;

/**
 * 审核详情响应 VO
 * 接口文档 模块四 1.2
 */
@Data
public class ReviewDetailResponse {

    private Long reviewId;
    private ContentSummary content;
    private List<ReviewHistoryItem> history;

    @Data
    public static class ContentSummary {
        private String url;
        private String title;
        private String description;
    }

    @Data
    public static class ReviewHistoryItem {
        private String reviewTime;
        private String reviewerName;
        private String action;
        private String reason;
    }
}
