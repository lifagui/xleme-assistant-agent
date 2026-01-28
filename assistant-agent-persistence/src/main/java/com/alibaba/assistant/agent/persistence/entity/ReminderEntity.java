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
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.LocalDateTime;
import java.util.Map;

/**
 * 提醒实体
 *
 * @author Assistant Agent Team
 * @since 1.0.0
 */
@Data
@Entity
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "reminder")
public class ReminderEntity {

    @Id
    @Column(name = "id", length = 64)
    private String id;

    /**
     * 租户ID
     */
    @Column(name = "tenant_id", length = 64, nullable = false)
    private String tenantId;

    /**
     * 创建者用户ID
     */
    @Column(name = "user_id", length = 64, nullable = false)
    private String userId;

    /**
     * 目标用户ID（支持给别人设置提醒/传话筒功能）
     */
    @Column(name = "target_user_id", length = 64, nullable = false)
    private String targetUserId;

    /**
     * 触发器ID，逻辑关联 trigger_definition.trigger_id
     */
    @Column(name = "trigger_id", length = 64)
    private String triggerId;

    /**
     * 提醒类型：DRINK_WATER/MEDICINE/SEDENTARY/MEAL/SLEEP/CUSTOM/RELAY
     */
    @Column(name = "type", length = 30, nullable = false)
    private String type;

    /**
     * 提醒内容及上下文信息，JSONB类型
     * 例如：{"text":"记得喝水","context":{...}}
     */
    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "content", columnDefinition = "jsonb", nullable = false)
    private Map<String, Object> content;

    /**
     * 状态：ACTIVE/CANCELLED/DELETED
     */
    @Column(name = "status", length = 20)
    private String status;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
}
