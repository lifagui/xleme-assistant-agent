package com.alibaba.assistant.agent.extension.experience.internal;

import com.alibaba.assistant.agent.extension.experience.model.Experience;
import com.alibaba.assistant.agent.extension.experience.model.ExperienceQuery;
import com.alibaba.assistant.agent.extension.experience.model.ExperienceQueryContext;
import com.alibaba.assistant.agent.extension.experience.model.ExperienceScope;
import com.alibaba.assistant.agent.extension.experience.model.*;
import com.alibaba.assistant.agent.extension.experience.spi.ExperienceProvider;
import com.alibaba.assistant.agent.extension.experience.spi.ExperienceRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import java.util.*;
import java.util.stream.Collectors;

/**
 * 基于内存的经验提供者实现
 * 从InMemoryExperienceRepository读取并过滤经验数据
 *
 * @author Assistant Agent Team
 */
public class InMemoryExperienceProvider implements ExperienceProvider {

    private static final Logger log = LoggerFactory.getLogger(InMemoryExperienceProvider.class);

    private final ExperienceRepository experienceRepository;

    public InMemoryExperienceProvider(ExperienceRepository experienceRepository) {
        this.experienceRepository = experienceRepository;
    }

    @Override
    public List<Experience> query(ExperienceQuery query, ExperienceQueryContext context) {
        log.debug("InMemoryExperienceProvider#query - reason=start querying experiences type={}, limit={}",
                query != null ? query.getType() : null, query != null ? query.getLimit() : 0);

        if (query == null) {
            log.warn("InMemoryExperienceProvider#query - reason=query is null, return empty list");
            return new ArrayList<>();
        }

        List<Experience> candidates = new ArrayList<>();

        // 根据scope优先级查询
        List<ExperienceScope> scopes = determinePriorityScopes(query, context);

        for (ExperienceScope scope : scopes) {
            String ownerId = getOwnerIdForScope(scope, context);
            String projectId = getProjectIdForScope(scope, context);

            List<Experience> scopedExperiences = experienceRepository.findByTypeAndScope(
                    query.getType(), scope, ownerId, projectId);

            candidates.addAll(scopedExperiences);

            log.debug("InMemoryExperienceProvider#query - reason=found {} experiences for scope={}",
                    scopedExperiences.size(), scope);
        }

        // 应用过滤条件
        List<Experience> filtered = applyFilters(candidates, query, context);

        // 排序和限制数量
        List<Experience> results = applySortingAndLimit(filtered, query);

        log.info("InMemoryExperienceProvider#query - reason=query completed, found {} experiences after filtering",
                results.size());

        return results;
    }

    /**
     * 根据查询条件和上下文确定scope优先级
     */
    private List<ExperienceScope> determinePriorityScopes(ExperienceQuery query, ExperienceQueryContext context) {
        if (!CollectionUtils.isEmpty(query.getScopes())) {
            return query.getScopes();
        }

        // 默认优先级: USER+PROJECT -> USER -> TEAM+PROJECT -> TEAM -> PROJECT -> GLOBAL
        List<ExperienceScope> scopes = new ArrayList<>();

        if (context != null && StringUtils.hasText(context.getUserId())) {
            if (StringUtils.hasText(context.getProjectId())) {
                // USER + PROJECT 优先级最高，这里通过多次查询实现
                scopes.add(ExperienceScope.USER);
            }
            scopes.add(ExperienceScope.USER);
            scopes.add(ExperienceScope.TEAM);
        }

        if (context != null && StringUtils.hasText(context.getProjectId())) {
            scopes.add(ExperienceScope.PROJECT);
        }

        scopes.add(ExperienceScope.GLOBAL);

        return scopes.stream().distinct().collect(Collectors.toList());
    }

    /**
     * 根据scope确定查询用的ownerId
     */
    private String getOwnerIdForScope(ExperienceScope scope, ExperienceQueryContext context) {
        if (context == null) {
            return null;
        }

        return switch (scope) {
            case USER -> context.getUserId();
            case TEAM ->
                // TODO: 从context中获取团队ID，这里暂时使用userId的前缀
                    StringUtils.hasText(context.getUserId()) ?
                            context.getUserId().split("@")[0] : null;
            default -> null;
        };
    }

    /**
     * 根据scope确定查询用的projectId
     */
    private String getProjectIdForScope(ExperienceScope scope, ExperienceQueryContext context) {
        if (context == null) {
            return null;
        }

        if (scope == ExperienceScope.PROJECT || scope == ExperienceScope.USER || scope == ExperienceScope.TEAM) {
            return context.getProjectId();
        }

        return null;
    }

    /**
     * 应用过滤条件
     */
    private List<Experience> applyFilters(List<Experience> experiences, ExperienceQuery query, ExperienceQueryContext context) {
        return experiences.stream()
                .filter(experience -> matchesLanguage(experience, query, context))
                .filter(experience -> matchesTags(experience, query))
                .filter(experience -> matchesText(experience, query))
                .distinct() // 去重，可能同一经验在不同scope下被找到
                .collect(Collectors.toList());
    }

    /**
     * 语言匹配检查
     */
    private boolean matchesLanguage(Experience experience, ExperienceQuery query, ExperienceQueryContext context) {
        String queryLanguage = query.getLanguage();
        if (!StringUtils.hasText(queryLanguage) && context != null) {
            queryLanguage = context.getLanguage();
        }

        if (!StringUtils.hasText(queryLanguage)) {
            return true; // 没有语言限制
        }

        String experienceLanguage = experience.getLanguage();
        if (!StringUtils.hasText(experienceLanguage)) {
            return true; // 经验没有语言限制
        }

        return queryLanguage.equalsIgnoreCase(experienceLanguage);
    }

    /**
     * 标签匹配检查
     */
    private boolean matchesTags(Experience experience, ExperienceQuery query) {
        Set<String> queryTags = query.getTags();
        if (CollectionUtils.isEmpty(queryTags)) {
            return true; // 没有标签限制
        }

        Set<String> experienceTags = experience.getTags();
        if (CollectionUtils.isEmpty(experienceTags)) {
            return false; // 经验没有标签，但查询有标签要求
        }

        // 检查是否有任何交集
        return queryTags.stream().anyMatch(experienceTags::contains);
    }

    /**
     * 文本匹配检查 (基于子串匹配数量)
     */
    private boolean matchesText(Experience experience, ExperienceQuery query) {
        String queryText = query.getText();
        if (!StringUtils.hasText(queryText)) {
            return true; // 没有文本限制
        }

        String content = experience.getContent();
        if (!StringUtils.hasText(content)) {
            return false;
        }

        // 只要有任意子串匹配，就认为匹配（由排序决定相关性）
        return calculateMatchScore(content, queryText) > 0;
    }

    /**
     * 计算匹配分数：queryText的所有子串在content中出现的次数总和
     */
    private int calculateMatchScore(String content, String queryText) {
        if (!StringUtils.hasText(content) || !StringUtils.hasText(queryText)) {
            return 0;
        }

        String lowerContent = content.toLowerCase();
        String lowerQuery = queryText.toLowerCase();
        int n = lowerQuery.length();

        // 如果查询词过短，直接全匹配
        if (n < 2) {
            return lowerContent.contains(lowerQuery) ? 1 : 0;
        }

        int score = 0;
        // 生成所有长度>=2的子串
        for (int len = 2; len <= n; len++) {
            for (int i = 0; i <= n - len; i++) {
                String sub = lowerQuery.substring(i, i + len);
                if (lowerContent.contains(sub)) {
                    score++;
                }
            }
        }
        return score;
    }

    /**
     * 应用排序和数量限制
     */
    private List<Experience> applySortingAndLimit(List<Experience> experiences, ExperienceQuery query) {
        Comparator<Experience> comparator = getComparator(query);

        return experiences.stream()
                .sorted(comparator)
                .limit(query.getLimit())
                .collect(Collectors.toList());
    }

    /**
     * 获取排序比较器
     */
    private Comparator<Experience> getComparator(ExperienceQuery query) {
        // 如果有文本查询，优先按匹配数量排序
        if (StringUtils.hasText(query.getText())) {
            String queryText = query.getText();
            return (e1, e2) -> {
                int score1 = calculateMatchScore(e1.getContent(), queryText);
                int score2 = calculateMatchScore(e2.getContent(), queryText);

                // 降序排列
                return Integer.compare(score2, score1);
            };
        }

        return switch (query.getOrderBy()) {
            case CREATED_AT -> (e1, e2) -> e2.getCreatedAt().compareTo(e1.getCreatedAt());
            case UPDATED_AT -> (e1, e2) -> e2.getUpdatedAt().compareTo(e1.getUpdatedAt());
            case SCORE ->
                // TODO: 实现基于置信度的评分排序
                    (e1, e2) -> {
                        Double score1 = e1.getMetadata().getConfidence();
                        Double score2 = e2.getMetadata().getConfidence();
                        if (score1 == null) score1 = 0.5;
                        if (score2 == null) score2 = 0.5;
                        return score2.compareTo(score1);
                    };
        };
    }
}
