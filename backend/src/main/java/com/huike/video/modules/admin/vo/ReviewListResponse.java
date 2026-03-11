package com.huike.video.modules.admin.vo;

import lombok.Data;

import java.util.List;

/**
 * 审核列表分页响应 VO
 */
@Data
public class ReviewListResponse {

    private Long total;
    private List<ReviewListItemVO> records;
}
