package com.huike.video.modules.admin.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.huike.video.common.exception.BusinessException;
import com.huike.video.common.service.ResourceService;
import com.huike.video.modules.admin.dto.ReviewDecisionRequest;
import com.huike.video.modules.admin.entity.ContentReview;
import com.huike.video.modules.admin.mapper.ContentReviewMapper;
import com.huike.video.modules.admin.service.AdminReviewService;
import com.huike.video.modules.admin.vo.*;
import com.huike.video.modules.community.entity.UserGeneratedContent;
import com.huike.video.modules.community.mapper.UserGeneratedContentMapper;
import com.huike.video.modules.user.entity.User;
import com.huike.video.modules.user.mapper.UserMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 审核状态: 1-待审核, 2-通过, 3-驳回, 4-复审
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class AdminReviewServiceImpl implements AdminReviewService {

    private static final DateTimeFormatter DF = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    private static final int MAX_PAGE_SIZE = 100;

    private final ContentReviewMapper contentReviewMapper;
    private final UserGeneratedContentMapper ugcMapper;
    private final UserMapper userMapper;
    private final ResourceService resourceService;

    @Override
    public ReviewListResponse getReviewList(Integer page, Integer size, String status,
                                            String reviewerId, String contentType) {
        if (page == null || page < 1) page = 1;
        if (size == null || size < 1) size = 20;
        if (size > MAX_PAGE_SIZE) size = MAX_PAGE_SIZE;

        Set<String> contentIdsByType = null;
        if (StringUtils.hasText(contentType)) {
            LambdaQueryWrapper<UserGeneratedContent> typeQ = Wrappers.lambdaQuery(UserGeneratedContent.class);
            if ("VIDEO".equalsIgnoreCase(contentType)) {
                typeQ.and(w -> w.like(UserGeneratedContent::getFormat, "mp4")
                        .or().like(UserGeneratedContent::getFormat, "webm")
                        .or().like(UserGeneratedContent::getFormat, "mov"));
            } else if ("IMAGE".equalsIgnoreCase(contentType)) {
                typeQ.and(w -> w.like(UserGeneratedContent::getFormat, "jpg")
                        .or().like(UserGeneratedContent::getFormat, "jpeg")
                        .or().like(UserGeneratedContent::getFormat, "png")
                        .or().like(UserGeneratedContent::getFormat, "gif"));
            }
            List<UserGeneratedContent> typeList = ugcMapper.selectList(typeQ);
            contentIdsByType = typeList.stream().map(UserGeneratedContent::getId).collect(Collectors.toSet());
            if (contentIdsByType.isEmpty()) {
                ReviewListResponse empty = new ReviewListResponse();
                empty.setTotal(0L);
                empty.setRecords(Collections.emptyList());
                return empty;
            }
        }

        LambdaQueryWrapper<ContentReview> q = Wrappers.lambdaQuery(ContentReview.class)
                .orderByDesc(ContentReview::getCreatedAt);
        if (StringUtils.hasText(status)) {
            q.eq(ContentReview::getReviewStatus, statusToCode(status));
        } else {
            q.eq(ContentReview::getReviewStatus, 1);
        }
        if (StringUtils.hasText(reviewerId)) {
            q.eq(ContentReview::getReviewerId, reviewerId);
        }
        if (contentIdsByType != null) {
            q.in(ContentReview::getContentId, contentIdsByType);
        }

        Page<ContentReview> p = contentReviewMapper.selectPage(new Page<>(page, size), q);
        List<ContentReview> list = p.getRecords();
        List<ReviewListItemVO> records = new ArrayList<>();
        Set<String> contentIds = list.stream().map(ContentReview::getContentId).collect(Collectors.toSet());
        Map<String, UserGeneratedContent> ugcMap = new HashMap<>();
        if (!contentIds.isEmpty()) {
            List<UserGeneratedContent> ugcList = ugcMapper.selectList(
                    Wrappers.lambdaQuery(UserGeneratedContent.class).in(UserGeneratedContent::getId, contentIds));
            for (UserGeneratedContent u : ugcList) ugcMap.put(u.getId(), u);
        }
        Set<String> creatorIds = ugcMap.values().stream().map(UserGeneratedContent::getCreatorId).collect(Collectors.toSet());
        Map<String, String> creatorNameMap = new HashMap<>();
        for (String cid : creatorIds) {
            User u = userMapper.selectById(cid);
            creatorNameMap.put(cid, u != null ? u.getUsername() : cid);
        }

        for (ContentReview r : list) {
            UserGeneratedContent ugc = ugcMap.get(r.getContentId());
            ReviewListItemVO vo = new ReviewListItemVO();
            vo.setReviewId(r.getId());
            vo.setContentId(r.getContentId());
            vo.setStatus(codeToStatus(r.getReviewStatus()));
            vo.setSubmitTime(r.getCreatedAt() != null ? r.getCreatedAt().format(DF) : null);
            vo.setMachineCheckResult(null);
            if (ugc != null) {
                vo.setContentUrl(resourceService.getUrl(ugc.getFilePath()));
                vo.setCoverUrl(StringUtils.hasText(ugc.getThumbnailPath()) ? resourceService.getUrl(ugc.getThumbnailPath()) : null);
                vo.setCreatorName(creatorNameMap.getOrDefault(ugc.getCreatorId(), ugc.getCreatorId()));
            }
            records.add(vo);
        }

        ReviewListResponse resp = new ReviewListResponse();
        resp.setTotal(p.getTotal());
        resp.setRecords(records);
        return resp;
    }

    @Override
    public ReviewDetailResponse getReviewDetail(Long reviewId) {
        ContentReview r = contentReviewMapper.selectById(reviewId);
        if (r == null) {
            throw new BusinessException(40002, "审核记录不存在");
        }
        ReviewDetailResponse resp = new ReviewDetailResponse();
        resp.setReviewId(r.getId());

        UserGeneratedContent ugc = ugcMapper.selectById(r.getContentId());
        if (ugc != null) {
            ReviewDetailResponse.ContentSummary content = new ReviewDetailResponse.ContentSummary();
            content.setUrl(resourceService.getUrl(ugc.getFilePath()));
            content.setTitle(ugc.getContentName());
            content.setDescription(ugc.getDescription());
            resp.setContent(content);
        }

        List<ReviewDetailResponse.ReviewHistoryItem> history = new ArrayList<>();
        if (r.getReviewTime() != null && (r.getReviewStatus() == 2 || r.getReviewStatus() == 3)) {
            ReviewDetailResponse.ReviewHistoryItem item = new ReviewDetailResponse.ReviewHistoryItem();
            item.setReviewTime(r.getReviewTime().format(DF));
            User reviewer = userMapper.selectById(r.getReviewerId());
            item.setReviewerName(reviewer != null ? reviewer.getUsername() : r.getReviewerId());
            item.setAction(r.getReviewStatus() == 2 ? "PASSED" : "REJECTED");
            item.setReason(r.getRejectReason());
            history.add(item);
        }
        resp.setHistory(history);
        return resp;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean submitDecision(Long reviewId, String adminId, ReviewDecisionRequest request) {
        ContentReview r = contentReviewMapper.selectById(reviewId);
        if (r == null) {
            throw new BusinessException(40002, "审核记录不存在");
        }
        if (r.getReviewStatus() != 1 && r.getReviewStatus() != 4) {
            throw new BusinessException(40003, "该记录已处理，无法重复提交");
        }
        String status = request.getStatus();
        if ("REJECTED".equals(status) && !StringUtils.hasText(request.getRejectReason())) {
            throw new BusinessException(40004, "驳回时请填写驳回原因");
        }
        int code = statusToCode(status);
        r.setReviewStatus(code);
        r.setReviewerId(adminId);
        r.setReviewTime(java.time.LocalDateTime.now());
        r.setRejectReason(request.getRejectReason());
        r.setSuggestions(request.getSuggestions());
        contentReviewMapper.updateById(r);
        return true;
    }

    private static int statusToCode(String status) {
        if (status == null) return 1;
        switch (status.toUpperCase()) {
            case "PENDING": return 1;
            case "PASSED": return 2;
            case "REJECTED": return 3;
            case "RE_AUDIT": return 4;
            default: return 1;
        }
    }

    private static String codeToStatus(int code) {
        switch (code) {
            case 1: return "PENDING";
            case 2: return "PASSED";
            case 3: return "REJECTED";
            case 4: return "RE_AUDIT";
            default: return "PENDING";
        }
    }
}
