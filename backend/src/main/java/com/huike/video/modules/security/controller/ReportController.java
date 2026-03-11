package com.huike.video.modules.security.controller;

import cn.dev33.satoken.stp.StpUtil;
import com.huike.video.common.result.Result;
import com.huike.video.modules.security.dto.ReportCreateRequest;
import com.huike.video.modules.security.service.ReportService;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

/**
 * 违规举报服务 - 用户端
 * 接口文档 模块四 3.1
 * 路径: /api/v1/reports
 */
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/reports")
public class ReportController {

    private final ReportService reportService;

    /**
     * 3.1 提交举报 (用户端)
     * POST /api/v1/reports
     */
    @PostMapping
    public Result<Boolean> createReport(@RequestBody @Validated ReportCreateRequest request) {
        String reporterId = StpUtil.getLoginIdAsString();
        boolean success = reportService.createReport(reporterId, request);
        return Result.success(success);
    }
}
