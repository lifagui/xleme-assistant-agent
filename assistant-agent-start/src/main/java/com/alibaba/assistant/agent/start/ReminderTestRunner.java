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
package com.alibaba.assistant.agent.start;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

import com.alibaba.assistant.agent.extension.reminder.model.Reminder;
import com.alibaba.assistant.agent.extension.reminder.model.ReminderType;
import com.alibaba.assistant.agent.extension.reminder.service.ReminderService;
import com.alibaba.assistant.agent.extension.trigger.manager.TriggerManager;
import com.alibaba.assistant.agent.extension.trigger.model.ScheduleMode;
import com.alibaba.assistant.agent.extension.trigger.model.SourceType;
import com.alibaba.assistant.agent.extension.trigger.model.TriggerDefinition;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * 测试提醒功能的 CommandLineRunner
 * 
 * 启动参数: --spring.main.test-reminder=true
 * 或者: mvn spring-boot:run -Dspring-boot.run.arguments="--spring.main.test-reminder=true"
 * 
 * @author Assistant Agent Team
 * @since 1.0.0
 */
@Slf4j
@Component
@RequiredArgsConstructor
@ConditionalOnProperty(name = "spring.main.test-reminder", havingValue = "true")
public class ReminderTestRunner implements CommandLineRunner {

    private final TriggerManager triggerManager;
    private final ReminderService reminderService;

    @Override
    public void run(String... args) throws Exception {
        log.info("====================================");
        log.info("开始测试提醒功能...");
        log.info("====================================");
        
        // 检查参数是否包含 test-reminder
        boolean isTestMode = Arrays.stream(args).anyMatch(arg -> 
            arg.contains("test-reminder") || arg.equals("test-reminder"));
        
        if (!isTestMode) {
            log.info("非测试模式，跳过测试");
            return;
        }
        
        try {
            // 生成一个测试用的提醒ID
            String reminderId = java.util.UUID.randomUUID().toString();
            
            // 1. 创建触发器（包含提醒ID）
            String triggerId = createTestTrigger(reminderId);
            log.info("✅ 触发器创建成功: {}", triggerId);
            
            // 2. 创建提醒（关联触发器）
            Reminder reminder = createTestReminder(triggerId, reminderId);
            log.info("✅ 提醒创建成功: {}", reminder.getId());
            
            // 3. 等待触发器执行 (10秒后执行)
            log.info("⏰ 等待触发器执行 (10秒后)...");
            log.info("====================================");
            log.info("✅ 测试初始化完成！触发器将在10秒后自动执行。");
            log.info("📝 可以查看数据库中的记录：");
            log.info("   - trigger_definition 表 (triggerId={})", triggerId);
            log.info("   - trigger_execution_record 表");
            log.info("   - reminder 表 (reminderId={})", reminderId);
            log.info("   - reminder_log 表");
            log.info("====================================");
            
        } catch (Exception e) {
            log.error("❌ 测试失败", e);
            throw e;
        }
    }

    private String createTestTrigger(String reminderId) {
        // 创建触发器定义 - 一次性触发器，10秒后执行
        TriggerDefinition trigger = new TriggerDefinition();
        trigger.setName("喝水提醒触发器");
        trigger.setDescription("测试：提醒用户喝水");
        trigger.setSourceType(SourceType.USER);
        trigger.setSourceId("test-user");
        trigger.setScheduleMode(ScheduleMode.ONE_TIME);
        trigger.setScheduleValue("10"); // 10秒后执行
        trigger.setExecuteFunction("send_reminder");
        // 使用正确的Python函数定义格式
        String functionCode = String.format("""
def send_reminder():
    return reminder(action='send_reminder', reminder_id='%s')
""", reminderId);
        trigger.setFunctionCodeSnapshot(Map.of("send_reminder", functionCode));
        trigger.setRequireConfirmation(true);

        return triggerManager.subscribe(trigger);
    }

    private Reminder createTestReminder(String triggerId, String reminderId) {
        // 创建提醒内容
        Map<String, Object> content = new HashMap<>();
        content.put("text", "该喝水了，保持身体健康！");
        content.put("context", Map.of("source", "test-runner"));

        // 创建提醒 - 使用正确的方法签名
        // createReminder(String id, String userId, String targetUserId, ReminderType type, 
        //                Map<String, Object> content, String triggerId)
        return reminderService.createReminder(
                reminderId,  // 使用预生成的reminderId
                "test-user",
                "test-user", 
                ReminderType.DRINK_WATER,
                content,
                triggerId
        );
    }
}
