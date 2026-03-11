package com.huike.video.modules.security.service;

import com.huike.video.modules.security.dto.ReportCreateRequest;

/**
 * 用户举报服务
 * 接口文档 模块四 3.1 - 举报信息将生成一条待审核记录
 */
public interface ReportService {

    /**
     * 提交举报，生成待审核记录（写入 content_reviews）
     *
     * @param reporterId 举报人用户ID
     * @param request    举报内容
     * @return 是否成功
     */
    boolean createReport(String reporterId, ReportCreateRequest request);
}
