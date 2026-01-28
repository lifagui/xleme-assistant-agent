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
package com.alibaba.assistant.agent.extension.reminder.model;

import java.time.LocalDateTime;
import java.util.Map;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 提醒领域模型
 *
 * @author Assistant Agent Team
 * @since 1.0.0
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Reminder {

    /**
     * 提醒ID
     */
    private String id;

    /**
     * 租户ID
     */
    private String tenantId;

    /**
     * 创建者用户ID
     */
    private String userId;

    /**
     * 目标用户ID（支持给别人设置提醒）
     */
    private String targetUserId;

    /**
     * 关联的触发器ID
     */
    private String triggerId;

    /**
     * 提醒类型
     */
    private ReminderType type;

    /**
     * 提醒内容及上下文信息
     * <p>
     * 例如：{"text":"记得喝水","context":{...}}
     * </p>
     */
    private Map<String, Object> content;

    /**
     * 状态
     */
    private ReminderStatus status;

    /**
     * 创建时间
     */
    private LocalDateTime createdAt;

    /**
     * 更新时间
     */
    private LocalDateTime updatedAt;

    /**
     * 提醒状态枚举
     */
    public enum ReminderStatus {
        ACTIVE,
        CANCELLED,
        DELETED,
        FINISHED
    }

    /**
     * 获取提醒文本内容
     */
    public String getText() {
        if (content == null) {
            return null;
        }
        Object text = content.get("text");
        return text != null ? text.toString() : null;
    }
}
