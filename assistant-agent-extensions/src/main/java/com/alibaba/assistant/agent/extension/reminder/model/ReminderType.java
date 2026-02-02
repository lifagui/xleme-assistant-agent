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

/**
 * 提醒类型枚举
 *
 * @author Assistant Agent Team
 * @since 1.0.0
 */
public enum ReminderType {

    /**
     * 喝水提醒
     */
    DRINK_WATER("喝水提醒"),

    /**
     * 吃药提醒
     */
    MEDICINE("吃药提醒"),

    /**
     * 久坐提醒
     */
    SEDENTARY("久坐提醒"),

    /**
     * 吃饭提醒
     */
    MEAL("吃饭提醒"),

    /**
     * 睡觉提醒
     */
    SLEEP("睡觉提醒"),

    /**
     * 起床提醒
     */
    WAKE_UP("起床提醒"),

    /**
     * 自定义提醒
     */
    CUSTOM("自定义提醒"),

    /**
     * 传话提醒（给别人设置的提醒）
     */
    RELAY("传话提醒");

    private final String description;

    ReminderType(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }
}
