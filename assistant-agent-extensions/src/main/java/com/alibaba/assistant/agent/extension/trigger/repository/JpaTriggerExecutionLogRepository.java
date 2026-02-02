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
import com.alibaba.assistant.agent.extension.trigger.model.ExecutionStatus;
import com.alibaba.assistant.agent.extension.trigger.model.TriggerExecutionRecord;
import com.alibaba.assistant.agent.persistence.entity.TriggerExecutionRecordEntity;
import com.alibaba.assistant.agent.persistence.repository.TriggerExecutionRecordJpaRepository;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * 基于 JPA 的触发器执行记录存储实现
 *
 * @author Assistant Agent Team
 * @since 1.0.0
 */
public class JpaTriggerExecutionLogRepository implements TriggerExecutionLogRepository {

    private static final Logger logger = LoggerFactory.getLogger(JpaTriggerExecutionLogRepository.class);
    private static final String DEFAULT_TENANT_ID = "default";

    private final TriggerExecutionRecordJpaRepository jpaRepository;
    private final ObjectMapper objectMapper;

    public JpaTriggerExecutionLogRepository(TriggerExecutionRecordJpaRepository jpaRepository) {
        this.jpaRepository = jpaRepository;
        this.objectMapper = new ObjectMapper();
        logger.info("JpaTriggerExecutionLogRepository 初始化完成");
    }

    private String getTenantId() {
        // 使用 UserContextHolder 获取 tenantId（支持异步线程场景）
        String tenantId = UserContextHolder.getTenantId();
        return (tenantId != null && !tenantId.isEmpty()) ? tenantId : DEFAULT_TENANT_ID;
    }

    @Override
    @Transactional
    public void save(TriggerExecutionRecord record) {
        String tenantId = getTenantId();
        TriggerExecutionRecordEntity entity = toEntity(record, tenantId);
        jpaRepository.save(entity);
        logger.debug("保存触发器执行记录: executionId={}, triggerId={}, tenantId={}", 
                record.getExecutionId(), record.getTriggerId(), tenantId);
    }

    @Override
    public Optional<TriggerExecutionRecord> findById(String executionId) {
        String tenantId = getTenantId();
        return jpaRepository.findById(executionId)
                .filter(entity -> entity.getTenantId().equals(tenantId))
                .map(this::toRecord);
    }

    @Override
    @Transactional
    public void updateStatus(String executionId, ExecutionStatus status, String errorMessage,
                             Map<String, Object> outputSummary) {
        String tenantId = getTenantId();
        jpaRepository.findById(executionId).ifPresent(entity -> {
            if (entity.getTenantId().equals(tenantId)) {
                entity.setStatus(status.name());
                if (errorMessage != null) {
                    entity.setErrorMessage(errorMessage);
                }
                if (outputSummary != null) {
                    entity.setOutputSummary(toJson(outputSummary));
                }
                entity.setEndTime(LocalDateTime.now());
                entity.setUpdatedAt(LocalDateTime.now());
                jpaRepository.save(entity);
                logger.debug("更新触发器执行状态: executionId={}, status={}", executionId, status);
            }
        });
    }

    @Override
    public List<TriggerExecutionRecord> listByTrigger(String triggerId, int limit) {
        String tenantId = getTenantId();
        // 此处简化处理，实际可以使用 Pageable
        return jpaRepository.findByTenantIdAndTriggerId(tenantId, triggerId)
                .stream()
                .limit(limit)
                .map(this::toRecord)
                .collect(Collectors.toList());
    }

    @Override
    public List<TriggerExecutionRecord> findByTriggerId(String triggerId) {
        String tenantId = getTenantId();
        return jpaRepository.findByTenantIdAndTriggerId(tenantId, triggerId)
                .stream()
                .map(this::toRecord)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public void delete(String executionId) {
        String tenantId = getTenantId();
        jpaRepository.findById(executionId).ifPresent(entity -> {
            if (entity.getTenantId().equals(tenantId)) {
                jpaRepository.delete(entity);
                logger.debug("删除触发器执行记录: executionId={}, tenantId={}", executionId, tenantId);
            }
        });
    }

    // ==================== 转换方法 ====================

    private TriggerExecutionRecordEntity toEntity(TriggerExecutionRecord record, String tenantId) {
        return TriggerExecutionRecordEntity.builder()
                .executionId(record.getExecutionId())
                .tenantId(tenantId)
                .triggerId(record.getTriggerId())
                .scheduledTime(toLocalDateTime(record.getScheduledTime()))
                .startTime(toLocalDateTime(record.getStartTime()))
                .endTime(toLocalDateTime(record.getEndTime()))
                .status(record.getStatus().name())
                .errorMessage(record.getErrorMessage())
                .outputSummary(toJson(record.getOutputSummary()))
                .retryCount(record.getRetryCount())
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();
    }

    private TriggerExecutionRecord toRecord(TriggerExecutionRecordEntity entity) {
        TriggerExecutionRecord record = new TriggerExecutionRecord();
        record.setExecutionId(entity.getExecutionId());
        record.setTriggerId(entity.getTriggerId());
        record.setScheduledTime(toInstant(entity.getScheduledTime()));
        record.setStartTime(toInstant(entity.getStartTime()));
        record.setEndTime(toInstant(entity.getEndTime()));
        record.setStatus(ExecutionStatus.valueOf(entity.getStatus()));
        record.setErrorMessage(entity.getErrorMessage());
        record.setOutputSummary(fromJson(entity.getOutputSummary()));
        record.setRetryCount(entity.getRetryCount());
        return record;
    }

    private String toJson(Map<String, Object> map) {
        if (map == null || map.isEmpty()) {
            return null;
        }
        try {
            return objectMapper.writeValueAsString(map);
        } catch (Exception e) {
            logger.warn("JSON 序列化失败", e);
            return null;
        }
    }

    private Map<String, Object> fromJson(String json) {
        if (json == null || json.isEmpty()) {
            return new HashMap<>();
        }
        try {
            return objectMapper.readValue(json, new TypeReference<Map<String, Object>>() {});
        } catch (Exception e) {
            logger.warn("JSON 反序列化失败", e);
            return new HashMap<>();
        }
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
