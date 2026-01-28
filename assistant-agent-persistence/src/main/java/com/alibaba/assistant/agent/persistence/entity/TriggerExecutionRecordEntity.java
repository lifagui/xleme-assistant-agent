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
package com.alibaba.assistant.agent.persistence.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 触发器执行记录实体
 *
 * @author Assistant Agent Team
 * @since 1.0.0
 */
@Data
@Entity
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "trigger_execution_record")
public class TriggerExecutionRecordEntity {

    @Id
    @Column(name = "execution_id", length = 64)
    private String executionId;

    /**
     * 租户ID
     */
    @Column(name = "tenant_id", length = 64, nullable = false)
    private String tenantId;

    /**
     * 触发器ID，逻辑关联 trigger_definition.trigger_id
     */
    @Column(name = "trigger_id", length = 64, nullable = false)
    private String triggerId;

    /**
     * 计划执行时间
     */
    @Column(name = "scheduled_time")
    private LocalDateTime scheduledTime;

    /**
     * 开始执行时间
     */
    @Column(name = "start_time")
    private LocalDateTime startTime;

    /**
     * 结束执行时间
     */
    @Column(name = "end_time")
    private LocalDateTime endTime;

    /**
     * 状态：PENDING/RUNNING/SUCCESS/FAILED/SKIPPED/TIMEOUT
     */
    @Column(name = "status", length = 20, nullable = false)
    private String status;

    /**
     * 错误信息
     */
    @Column(name = "error_message", columnDefinition = "TEXT")
    private String errorMessage;

    /**
     * 输出摘要
     */
    @Column(name = "output_summary", columnDefinition = "TEXT")
    private String outputSummary;

    /**
     * 重试次数
     */
    @Column(name = "retry_count")
    private Integer retryCount;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
}
