package com.huike.video.modules.security.service.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.huike.video.common.exception.BusinessException;
import com.huike.video.modules.admin.entity.ContentReview;
import com.huike.video.modules.admin.mapper.ContentReviewMapper;
import com.huike.video.modules.security.dto.ReportCreateRequest;
import com.huike.video.modules.security.service.ReportService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.Map;

/**
 * 举报生成待审核记录：插入 content_reviews，content_id=被举报ID，
 * reviewer_id 暂存举报人ID（待管理员处理后会更新为审核员ID），
 * suggestions 存 JSON：report=true, reasonType, description
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ReportServiceImpl implements ReportService {

    private final ContentReviewMapper contentReviewMapper;
    private final ObjectMapper objectMapper;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean createReport(String reporterId, ReportCreateRequest request) {
        Map<String, Object> reportMeta = new HashMap<>();
        reportMeta.put("report", true);
        reportMeta.put("reporterId", reporterId);
        reportMeta.put("reasonType", request.getReasonType());
        reportMeta.put("targetType", request.getTargetType());
        if (request.getDescription() != null) {
            reportMeta.put("description", request.getDescription());
        }
        String suggestionsJson;
        try {
            suggestionsJson = objectMapper.writeValueAsString(reportMeta);
        } catch (Exception e) {
            throw new BusinessException(50001, "举报信息序列化失败");
        }
        ContentReview r = new ContentReview();
        r.setContentId(request.getTargetId());
        r.setReviewerId(reporterId);
        r.setReviewStatus(1);
        r.setSuggestions(suggestionsJson);
        r.setVersion(1);
        contentReviewMapper.insert(r);
        log.info("User {} reported content {} type {} reason {}", reporterId, request.getTargetId(), request.getTargetType(), request.getReasonType());
        return true;
    }
}
