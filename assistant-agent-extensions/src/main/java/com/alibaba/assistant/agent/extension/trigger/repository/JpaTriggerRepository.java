/*
 * Copyright 2024-2025 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.alibaba.assistant.agent.extension.trigger.repository;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.transaction.annotation.Transactional;

import com.alibaba.assistant.agent.common.context.UserContextHolder;
import com.alibaba.assistant.agent.extension.trigger.model.ScheduleMode;
import com.alibaba.assistant.agent.extension.trigger.model.SourceType;
import com.alibaba.assistant.agent.extension.trigger.model.TriggerDefinition;
import com.alibaba.assistant.agent.extension.trigger.model.TriggerStatus;
import com.alibaba.assistant.agent.persistence.entity.TriggerDefinitionEntity;
import com.alibaba.assistant.agent.persistence.repository.TriggerDefinitionJpaRepository;

/**
 * 基于 JPA 的触发器仓库实现
 * <p>
 * 实现 TriggerRepository 接口，内部使用 TriggerDefinitionJpaRepository 进行数据访问
 * </p>
 *
 * @author Assistant Agent Team
 * @since 1.0.0
 */
public class JpaTriggerRepository implements TriggerRepository {

    private static final Logger logger = LoggerFactory.getLogger(JpaTriggerRepository.class);

    private static final String EXTRA_KEY_EVENT_PROTOCOL = "eventProtocol";
    private static final String EXTRA_KEY_EVENT_KEY = "eventKey";
    private static final String EXTRA_KEY_REQUIRE_CONFIRMATION = "requireConfirmation";
    private static final String EXTRA_KEY_CONFIRM_CARD_TYPE = "confirmCardType";
    private static final String EXTRA_KEY_SESSION_SNAPSHOT_ID = "sessionSnapshotId";
    private static final String EXTRA_KEY_GRAPH_NAME = "graphName";
    private static final String EXTRA_KEY_AGENT_NAME = "agentName";
    private static final String EXTRA_KEY_METADATA = "metadata";
    private static final String EXTRA_KEY_MAX_RETRIES = "maxRetries";
    private static final String EXTRA_KEY_RETRY_DELAY = "retryDelay";

    private static final String DEFAULT_TENANT_ID = "default";

    private final TriggerDefinitionJpaRepository jpaRepository;

    public JpaTriggerRepository(TriggerDefinitionJpaRepository jpaRepository) {
        this.jpaRepository = jpaRepository;
        logger.info("JpaTriggerRepository 初始化完成");
    }

    private String getTenantId() {
        // 使用 UserContextHolder 获取 tenantId（支持异步线程场景）
        String tenantId = UserContextHolder.getTenantId();
        return (tenantId != null && !tenantId.isEmpty()) ? tenantId : DEFAULT_TENANT_ID;
    }

    @Override
    @Transactional
    public void save(TriggerDefinition definition) {
        String tenantId = getTenantId();
        TriggerDefinitionEntity entity = toEntity(definition, tenantId);
        jpaRepository.save(entity);
        logger.debug("保存触发器: triggerId={}, tenantId={}", definition.getTriggerId(), tenantId);
    }

    @Override
    public Optional<TriggerDefinition> findById(String triggerId) {
        String tenantId = getTenantId();
        return jpaRepository.findByTenantIdAndTriggerId(tenantId, triggerId)
                .map(this::toDefinition);
    }

    @Override
    public List<TriggerDefinition> findBySource(SourceType sourceType, String sourceId) {
        String tenantId = getTenantId();
        return jpaRepository.findByTenantIdAndSourceTypeAndSourceId(
                        tenantId, sourceType.name(), sourceId)
                .stream()
                .map(this::toDefinition)
                .collect(Collectors.toList());
    }

    @Override
    public List<TriggerDefinition> findByStatus(TriggerStatus status) {
        String tenantId = getTenantId();
        return jpaRepository.findByTenantIdAndStatus(tenantId, mapStatusToDb(status))
                .stream()
                .map(this::toDefinition)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public void updateStatus(String triggerId, TriggerStatus status) {
        String tenantId = getTenantId();
        int updated = jpaRepository.updateStatus(tenantId, triggerId, mapStatusToDb(status));
        if (updated > 0) {
            logger.debug("更新触发器状态: triggerId={}, status={}", triggerId, status);
        } else {
            logger.warn("更新触发器状态失败，未找到触发器: triggerId={}", triggerId);
        }
    }

    @Override
    @Transactional
    public void delete(String triggerId) {
        String tenantId = getTenantId();
        jpaRepository.deleteByTenantIdAndTriggerId(tenantId, triggerId);
        logger.debug("删除触发器: triggerId={}, tenantId={}", triggerId, tenantId);
    }

    @Override
    public List<TriggerDefinition> findAll() {
        String tenantId = getTenantId();
        return jpaRepository.findAllActiveByTenantId(tenantId)
                .stream()
                .map(this::toDefinition)
                .collect(Collectors.toList());
    }

    // ==================== 转换方法 ====================

    /**
     * 领域模型转换为实体
     */
    private TriggerDefinitionEntity toEntity(TriggerDefinition def, String tenantId) {
        // 构建额外参数（存储实体中没有的字段）
        Map<String, Object> parameters = new HashMap<>(def.getParameters() != null ? def.getParameters() : new HashMap<>());
        parameters.put(EXTRA_KEY_EVENT_PROTOCOL, def.getEventProtocol());
        parameters.put(EXTRA_KEY_EVENT_KEY, def.getEventKey());
        parameters.put(EXTRA_KEY_REQUIRE_CONFIRMATION, def.isRequireConfirmation());
        parameters.put(EXTRA_KEY_CONFIRM_CARD_TYPE, def.getConfirmCardType());
        parameters.put(EXTRA_KEY_SESSION_SNAPSHOT_ID, def.getSessionSnapshotId());
        parameters.put(EXTRA_KEY_GRAPH_NAME, def.getGraphName());
        parameters.put(EXTRA_KEY_AGENT_NAME, def.getAgentName());
        parameters.put(EXTRA_KEY_METADATA, def.getMetadata());
        parameters.put(EXTRA_KEY_MAX_RETRIES, def.getMaxRetries());
        parameters.put(EXTRA_KEY_RETRY_DELAY, def.getRetryDelay());

        return TriggerDefinitionEntity.builder()
                .triggerId(def.getTriggerId())
                .tenantId(tenantId != null ? tenantId : "default")
                .name(def.getName())
                .description(def.getDescription())
                .sourceType(def.getSourceType() != null ? def.getSourceType().name() : null)
                .sourceId(def.getSourceId())
                .scheduleMode(def.getScheduleMode() != null ? def.getScheduleMode().name() : null)
                .scheduleValue(def.getScheduleValue())
                .executeFunction(def.getExecuteFunction())
                .conditionFunction(def.getConditionFunction())
                .abandonFunction(def.getAbandonFunction())
                .functionCodeSnapshot(def.getFunctionCodeSnapshot())
                .parameters(parameters)
                .status(mapStatusToDb(def.getStatus()))
                .expireAt(toLocalDateTime(def.getExpireAt()))
                .createdBy(def.getCreatedBy())
                .createdAt(toLocalDateTime(def.getCreatedAt()))
                .updatedAt(toLocalDateTime(def.getUpdatedAt()))
                .build();
    }

    /**
     * 实体转换为领域模型
     */
    private TriggerDefinition toDefinition(TriggerDefinitionEntity entity) {
        TriggerDefinition def = new TriggerDefinition();
        def.setTriggerId(entity.getTriggerId());
        def.setName(entity.getName());
        def.setDescription(entity.getDescription());
        def.setSourceType(entity.getSourceType() != null ? SourceType.valueOf(entity.getSourceType()) : null);
        def.setSourceId(entity.getSourceId());
        def.setScheduleMode(entity.getScheduleMode() != null ? ScheduleMode.valueOf(entity.getScheduleMode()) : null);
        def.setScheduleValue(entity.getScheduleValue());
        def.setExecuteFunction(entity.getExecuteFunction());
        def.setConditionFunction(entity.getConditionFunction());
        def.setAbandonFunction(entity.getAbandonFunction());
        def.setFunctionCodeSnapshot(entity.getFunctionCodeSnapshot());
        def.setStatus(mapStatusFromDb(entity.getStatus()));
        def.setExpireAt(toInstant(entity.getExpireAt()));
        def.setCreatedBy(entity.getCreatedBy());
        def.setCreatedAt(toInstant(entity.getCreatedAt()));
        def.setUpdatedAt(toInstant(entity.getUpdatedAt()));

        // 从参数中恢复额外字段
        Map<String, Object> parameters = entity.getParameters();
        if (parameters != null) {
            def.setEventProtocol((String) parameters.get(EXTRA_KEY_EVENT_PROTOCOL));
            def.setEventKey((String) parameters.get(EXTRA_KEY_EVENT_KEY));
            def.setRequireConfirmation(Boolean.TRUE.equals(parameters.get(EXTRA_KEY_REQUIRE_CONFIRMATION)));
            def.setConfirmCardType((String) parameters.get(EXTRA_KEY_CONFIRM_CARD_TYPE));
            def.setSessionSnapshotId((String) parameters.get(EXTRA_KEY_SESSION_SNAPSHOT_ID));
            def.setGraphName((String) parameters.get(EXTRA_KEY_GRAPH_NAME));
            def.setAgentName((String) parameters.get(EXTRA_KEY_AGENT_NAME));
            
            @SuppressWarnings("unchecked")
            Map<String, Object> metadata = (Map<String, Object>) parameters.get(EXTRA_KEY_METADATA);
            def.setMetadata(metadata);
            
            Object maxRetries = parameters.get(EXTRA_KEY_MAX_RETRIES);
            if (maxRetries instanceof Number) {
                def.setMaxRetries(((Number) maxRetries).intValue());
            }
            
            Object retryDelay = parameters.get(EXTRA_KEY_RETRY_DELAY);
            if (retryDelay instanceof Number) {
                def.setRetryDelay(((Number) retryDelay).longValue());
            }
            
            // 设置业务参数（排除额外字段）
            Map<String, Object> bizParameters = new HashMap<>(parameters);
            bizParameters.remove(EXTRA_KEY_EVENT_PROTOCOL);
            bizParameters.remove(EXTRA_KEY_EVENT_KEY);
            bizParameters.remove(EXTRA_KEY_REQUIRE_CONFIRMATION);
            bizParameters.remove(EXTRA_KEY_CONFIRM_CARD_TYPE);
            bizParameters.remove(EXTRA_KEY_SESSION_SNAPSHOT_ID);
            bizParameters.remove(EXTRA_KEY_GRAPH_NAME);
            bizParameters.remove(EXTRA_KEY_AGENT_NAME);
            bizParameters.remove(EXTRA_KEY_METADATA);
            bizParameters.remove(EXTRA_KEY_MAX_RETRIES);
            bizParameters.remove(EXTRA_KEY_RETRY_DELAY);
            def.setParameters(bizParameters);
        }

        return def;
    }

    /**
     * 将 TriggerStatus 映射到数据库状态字符串
     * 数据库状态：ACTIVE/CANCELLED/DELETED/FINISHED
     */
    private String mapStatusToDb(TriggerStatus status) {
        if (status == null) {
            return "ACTIVE";
        }
        return switch (status) {
            case ACTIVE -> "ACTIVE";
            case PAUSED, PENDING_ACTIVATE -> "ACTIVE";  // 待激活和暂停都映射为 ACTIVE
            case CANCELED -> "CANCELLED";
            case EXPIRED -> "DELETED";
            case FINISHED -> "FINISHED";
        };
    }

    /**
     * 从数据库状态字符串映射到 TriggerStatus
     */
    private TriggerStatus mapStatusFromDb(String status) {
        if (status == null) {
            return TriggerStatus.PENDING_ACTIVATE;
        }
        return switch (status) {
            case "ACTIVE" -> TriggerStatus.ACTIVE;
            case "CANCELLED" -> TriggerStatus.CANCELED;
            case "DELETED" -> TriggerStatus.EXPIRED;
            case "FINISHED" -> TriggerStatus.FINISHED;
            default -> TriggerStatus.PENDING_ACTIVATE;
        };
    }

    private LocalDateTime toLocalDateTime(Instant instant) {
        if (instant == null) {
            return null;
        }
        return LocalDateTime.ofInstant(instant, ZoneId.systemDefault());
    }

    private Instant toInstant(LocalDateTime localDateTime) {
        if (localDateTime == null) {
            return null;
        }
        return localDateTime.atZone(ZoneId.systemDefault()).toInstant();
    }
}
