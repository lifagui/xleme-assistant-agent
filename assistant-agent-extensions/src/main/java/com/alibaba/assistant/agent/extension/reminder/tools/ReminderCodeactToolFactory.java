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
package com.alibaba.assistant.agent.extension.reminder.tools;

import com.alibaba.assistant.agent.common.tools.CodeactTool;
import com.alibaba.assistant.agent.extension.reminder.config.ReminderProperties;
import com.alibaba.assistant.agent.extension.reminder.service.ReminderService;
import com.alibaba.assistant.agent.extension.trigger.manager.TriggerManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;

/**
 * Reminder CodeactTool 工厂类
 * <p>
 * 负责创建所有 Reminder 相关的 CodeactTool 实例
 * </p>
 *
 * @author Assistant Agent Team
 * @since 1.0.0
 */
public class ReminderCodeactToolFactory {

    private static final Logger log = LoggerFactory.getLogger(ReminderCodeactToolFactory.class);

    private final ReminderService reminderService;
    private final TriggerManager triggerManager;
    private final ReminderProperties properties;

    public ReminderCodeactToolFactory(ReminderService reminderService, TriggerManager triggerManager, ReminderProperties properties) {
        this.reminderService = reminderService;
        this.triggerManager = triggerManager;
        this.properties = properties;
    }

    /**
     * 创建所有 Reminder CodeactTool
     *
     * @return CodeactTool 列表
     */
    public List<CodeactTool> createTools() {
        List<CodeactTool> tools = new ArrayList<>();

        // 提醒管理工具
        tools.add(new ReminderCodeactTool(reminderService, triggerManager, properties));
        log.info("ReminderCodeactToolFactory#createTools - reason=创建ReminderCodeactTool成功");

        log.info("ReminderCodeactToolFactory#createTools - reason=提醒工具创建完成, count={}", tools.size());

        return tools;
    }
}
