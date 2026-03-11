package com.huike.video.modules.admin.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.huike.video.common.exception.BusinessException;
import com.huike.video.modules.admin.dto.ReviewRuleStatusUpdateRequest;
import com.huike.video.modules.admin.dto.ReviewRuleUpsertRequest;
import com.huike.video.modules.admin.entity.ReviewRule;
import com.huike.video.modules.admin.mapper.ReviewRuleMapper;
import com.huike.video.modules.admin.service.AdminReviewRuleService;
import com.huike.video.modules.admin.vo.ReviewRuleListResponse;
import com.huike.video.modules.admin.vo.ReviewRuleVO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 规则状态: 1-启用, 2-禁用
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class AdminReviewRuleServiceImpl implements AdminReviewRuleService {

    private final ReviewRuleMapper reviewRuleMapper;
    private final ObjectMapper objectMapper;

    @Override
    public ReviewRuleListResponse getRuleList(String ruleType) {
        LambdaQueryWrapper<ReviewRule> q = Wrappers.lambdaQuery(ReviewRule.class)
                .orderByAsc(ReviewRule::getPriority)
                .orderByDesc(ReviewRule::getCreatedAt);
        if (StringUtils.hasText(ruleType)) {
            q.eq(ReviewRule::getRuleType, ruleType);
        }
        List<ReviewRule> list = reviewRuleMapper.selectList(q);
        List<ReviewRuleVO> voList = list.stream().map(this::toVO).collect(Collectors.toList());
        ReviewRuleListResponse resp = new ReviewRuleListResponse();
        resp.setList(voList);
        return resp;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long upsertRule(String adminId, ReviewRuleUpsertRequest request) {
        if (request.getPriority() == null) request.setPriority(5);
        String conditionsJson;
        String actionsJson;
        try {
            conditionsJson = objectMapper.writeValueAsString(request.getConditions());
            actionsJson = objectMapper.writeValueAsString(request.getActions());
        } catch (Exception e) {
            throw new BusinessException(40005, "conditions/actions 格式错误");
        }
        if (request.getRuleId() != null && request.getRuleId() > 0) {
            ReviewRule rule = reviewRuleMapper.selectById(request.getRuleId());
            if (rule == null) {
                throw new BusinessException(40002, "规则不存在");
            }
            rule.setRuleName(request.getRuleName());
            rule.setRuleType(request.getRuleType());
            rule.setConditions(conditionsJson);
            rule.setActions(actionsJson);
            rule.setPriority(request.getPriority());
            reviewRuleMapper.updateById(rule);
            return rule.getId();
        }
        ReviewRule rule = new ReviewRule();
        rule.setRuleName(request.getRuleName());
        rule.setRuleType(request.getRuleType());
        rule.setConditions(conditionsJson);
        rule.setActions(actionsJson);
        rule.setPriority(request.getPriority());
        rule.setStatus(1);
        rule.setCreatorId(adminId);
        reviewRuleMapper.insert(rule);
        return rule.getId();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean updateRuleStatus(Long ruleId, ReviewRuleStatusUpdateRequest request) {
        ReviewRule rule = reviewRuleMapper.selectById(ruleId);
        if (rule == null) {
            throw new BusinessException(40002, "规则不存在");
        }
        int code = "ENABLE".equalsIgnoreCase(request.getStatus()) ? 1 : 2;
        rule.setStatus(code);
        reviewRuleMapper.updateById(rule);
        return true;
    }

    private ReviewRuleVO toVO(ReviewRule r) {
        ReviewRuleVO vo = new ReviewRuleVO();
        vo.setRuleId(r.getId());
        vo.setRuleName(r.getRuleName());
        vo.setRuleType(r.getRuleType());
        vo.setPriority(r.getPriority() != null ? r.getPriority() : 5);
        vo.setStatus(r.getStatus() == 1 ? "ENABLE" : "DISABLE");
        if (StringUtils.hasText(r.getConditions())) {
            try {
                vo.setConditions(objectMapper.readValue(r.getConditions(), new TypeReference<Map<String, Object>>() {}));
            } catch (Exception e) {
                vo.setConditions(null);
            }
        }
        return vo;
    }
}
