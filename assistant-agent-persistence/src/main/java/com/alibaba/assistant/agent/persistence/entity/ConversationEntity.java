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
 * 会话实体
 *
 * @author Assistant Agent Team
 * @since 1.0.0
 */
@Data
@Entity
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "conversation")
public class ConversationEntity {

    @Id
    @Column(name = "id", length = 36)
    private String id;

    /**
     * 租户ID，区分不同业务方
     */
    @Column(name = "tenant_id", length = 64, nullable = false)
    private String tenantId;

    /**
     * 用户ID
     */
    @Column(name = "user_id", length = 64, nullable = false)
    private String userId;

    /**
     * Agent线程ID，用于多轮对话上下文保持
     */
    @Column(name = "thread_id", length = 36, nullable = false)
    private String threadId;

    /**
     * 会话标题
     */
    @Column(name = "title", length = 255)
    private String title;

    /**
     * 状态：ACTIVE/ARCHIVED/DELETED
     */
    @Column(name = "status", length = 20)
    private String status;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
}
