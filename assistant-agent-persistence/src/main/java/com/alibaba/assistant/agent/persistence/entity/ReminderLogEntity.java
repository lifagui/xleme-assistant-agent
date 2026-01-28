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
 * 提醒执行记录实体
 *
 * @author Assistant Agent Team
 * @since 1.0.0
 */
@Data
@Entity
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "reminder_log")
public class ReminderLogEntity {

    @Id
    @Column(name = "id", length = 64)
    private String id;

    /**
     * 租户ID
     */
    @Column(name = "tenant_id", length = 64, nullable = false)
    private String tenantId;

    /**
     * 提醒ID，逻辑关联 reminder.id
     */
    @Column(name = "reminder_id", length = 64, nullable = false)
    private String reminderId;

    /**
     * 触发器执行ID，逻辑关联 trigger_execution_record.execution_id
     */
    @Column(name = "trigger_execution_id", length = 64)
    private String triggerExecutionId;

    /**
     * 计划执行时间
     */
    @Column(name = "scheduled_time", nullable = false)
    private LocalDateTime scheduledTime;

    /**
     * 实际执行时间
     */
    @Column(name = "actual_time")
    private LocalDateTime actualTime;

    /**
     * 通知渠道：SMS/IN_APP
     */
    @Column(name = "channel", length = 20, nullable = false)
    private String channel;

    /**
     * 状态：PENDING/SENT/DELIVERED/COMPLETED/SKIPPED/SNOOZED/FAILED
     */
    @Column(name = "status", length = 20, nullable = false)
    private String status;

    /**
     * 用户反馈
     */
    @Column(name = "user_feedback", columnDefinition = "TEXT")
    private String userFeedback;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
}
