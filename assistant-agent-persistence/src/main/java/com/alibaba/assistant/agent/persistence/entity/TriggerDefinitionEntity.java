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

import java.time.LocalDateTime;
import java.util.Map;

import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 触发器定义实体
 *
 * @author Assistant Agent Team
 * @since 1.0.0
 */
@Data
@Entity
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "trigger_definition")
public class TriggerDefinitionEntity {

    @Id
    @Column(name = "trigger_id", length = 64)
    private String triggerId;

    /**
     * 租户ID
     */
    @Column(name = "tenant_id", length = 64, nullable = false)
    private String tenantId;

    /**
     * 触发器名称
     */
    @Column(name = "name", length = 100, nullable = false)
    private String name;

    /**
     * 触发器描述
     */
    @Column(name = "description", length = 500)
    private String description;

    /**
     * 来源类型：USER/GROUP/GLOBAL
     */
    @Column(name = "source_type", length = 20, nullable = false)
    private String sourceType;

    /**
     * 来源ID
     */
    @Column(name = "source_id", length = 64, nullable = false)
    private String sourceId;

    /**
     * 调度模式：CRON/FIXED_DELAY/FIXED_RATE/ONE_TIME/TRIGGER
     */
    @Column(name = "schedule_mode", length = 20, nullable = false)
    private String scheduleMode;

    /**
     * 调度值（cron表达式或间隔值）
     */
    @Column(name = "schedule_value", length = 100)
    private String scheduleValue;

    /**
     * 执行函数名
     */
    @Column(name = "execute_function", length = 100, nullable = false)
    private String executeFunction;

    /**
     * 条件函数名
     */
    @Column(name = "condition_function", length = 100)
    private String conditionFunction;

    /**
     * 放弃条件函数名
     */
    @Column(name = "abandon_function", length = 100)
    private String abandonFunction;

    /**
     * 函数代码快照
     */
    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "function_code_snapshot", columnDefinition = "jsonb")
    private Map<String, String> functionCodeSnapshot;

    /**
     * 执行参数
     */
    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "parameters", columnDefinition = "jsonb")
    private Map<String, Object> parameters;

    /**
     * 状态：ACTIVE/CANCELLED/DELETED/FINISHED
     * ACTIVE：活动状态
     * CANCELLED：已取消
     * DELETED：已删除
     * FINISHED：已完成
     */
    @Column(name = "status", length = 20)
    private String status;

    /**
     * 过期时间
     */
    @Column(name = "expire_at")
    private LocalDateTime expireAt;

    /**
     * 创建者
     */
    @Column(name = "created_by", length = 64)
    private String createdBy;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
}
