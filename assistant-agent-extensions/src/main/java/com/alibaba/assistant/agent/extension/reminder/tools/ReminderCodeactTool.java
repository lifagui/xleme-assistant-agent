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

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.chat.model.ToolContext;
import org.springframework.ai.tool.definition.ToolDefinition;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.web.client.RestTemplate;

import com.alibaba.assistant.agent.common.context.LoginContext;
import com.alibaba.assistant.agent.common.context.UserContextHolder;
import com.alibaba.assistant.agent.common.enums.Language;
import com.alibaba.assistant.agent.common.tools.CodeExample;
import com.alibaba.assistant.agent.common.tools.CodeactTool;
import com.alibaba.assistant.agent.common.tools.CodeactToolMetadata;
import com.alibaba.assistant.agent.common.tools.DefaultCodeactToolMetadata;
import com.alibaba.assistant.agent.common.tools.definition.CodeactToolDefinition;
import com.alibaba.assistant.agent.common.tools.definition.DefaultCodeactToolDefinition;
import com.alibaba.assistant.agent.common.tools.definition.ParameterTree;
import com.alibaba.assistant.agent.extension.reminder.config.ReminderProperties;
import com.alibaba.assistant.agent.extension.reminder.model.NotificationChannel;
import com.alibaba.assistant.agent.extension.reminder.model.Reminder;
import com.alibaba.assistant.agent.extension.reminder.model.ReminderType;
import com.alibaba.assistant.agent.extension.reminder.service.ReminderService;
import com.alibaba.assistant.agent.extension.trigger.manager.TriggerManager;
import com.alibaba.assistant.agent.extension.trigger.model.ScheduleMode;
import com.alibaba.assistant.agent.extension.trigger.model.SourceType;
import com.alibaba.assistant.agent.extension.trigger.model.TriggerDefinition;
import com.alibaba.assistant.agent.extension.trigger.model.TriggerStatus;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * 提醒管理 CodeactTool
 * <p>
 * 提供创建、修改、删除、列出提醒的能力，供 Agent 调用
 * </p>
 *
 * @author Assistant Agent Team
 * @since 1.0.0
 */
public class ReminderCodeactTool implements CodeactTool {

    private static final Logger log = LoggerFactory.getLogger(ReminderCodeactTool.class);

    private final ReminderService reminderService;
    private final TriggerManager triggerManager;
    private final ReminderProperties properties;
    private final ObjectMapper objectMapper;
    private final ToolDefinition toolDefinition;
    private final CodeactToolDefinition codeactDefinition;
    private final CodeactToolMetadata codeactMetadata;
    private final RestTemplate restTemplate;

    public ReminderCodeactTool(ReminderService reminderService, TriggerManager triggerManager, ReminderProperties properties) {
        this.reminderService = reminderService;
        this.triggerManager = triggerManager;
        this.properties = properties;
        this.objectMapper = new ObjectMapper();
        this.toolDefinition = buildToolDefinition();
        this.codeactDefinition = buildCodeactDefinition();
        this.codeactMetadata = buildCodeactMetadata();
        this.restTemplate = new RestTemplate();
    }

    @Override
    public String call(String toolInput) {
        return call(toolInput, null);
    }

    @Override
    public String call(String toolInput, ToolContext toolContext) {
        log.debug("ReminderCodeactTool#call - input={}", toolInput);

        try {
            Map<String, Object> params = objectMapper.readValue(toolInput, 
                    new TypeReference<Map<String, Object>>() {});
            
            String action = (String) params.get("action");
            if (action == null) {
                return errorResult("缺少 action 参数");
            }

            return switch (action) {
                case "create" -> createReminder(params);
                case "update" -> updateReminder(params);
                case "cancel" -> cancelReminder(params);
                case "delete" -> deleteReminder(params);
                case "list" -> listReminders(params);
                case "get" -> getReminder(params);
                case "send_reminder" -> sendReminder(params, toolContext);
                default -> errorResult("不支持的操作: " + action);
            };

        } catch (Exception e) {
            log.error("ReminderCodeactTool#call - 执行失败", e);
            return errorResult("执行失败: " + e.getMessage());
        }
    }

    /**
     * 创建提醒
     */
    private String createReminder(Map<String, Object> params) {
        try {
            // 获取用户ID：优先从 LoginContext 获取，如果为空则从 UserContextHolder 获取
            String userId = UserContextHolder.getUserId();
            String tenantId = UserContextHolder.getTenantId();
            log.info("创建提醒 - userId={}, tenantId={}", userId, tenantId);
            
            String targetUserId = (String) params.getOrDefault("target_user_id", userId);
            String typeStr = (String) params.get("type");
            String text = (String) params.get("text");
            String scheduleMode = (String) params.get("schedule_mode");
            if(scheduleMode == null || scheduleMode.isEmpty()) {
                scheduleMode = "ONE_TIME";
            }
            String scheduleValue = (String) params.get("schedule_value");
            if(scheduleValue == null || scheduleValue.isEmpty()) {
                scheduleValue = "0";
            }

            if (typeStr == null || text == null) {
                return errorResult("缺少必要参数: type 和 text");
            }

            ReminderType type;
            try {
                type = ReminderType.valueOf(typeStr.toUpperCase());
            } catch (IllegalArgumentException e) {
                return errorResult("不支持的提醒类型: " + typeStr);
            }

            // 1. 预先生成提醒 ID
            String reminderId = UUID.randomUUID().toString();

            // 校验传话提醒必填参数
            if (type == ReminderType.RELAY) {
                String targetPhone = (String) params.get("target_phone");
                if (targetPhone == null || targetPhone.isEmpty()) {
                    return errorResult("传话提醒必须提供目标手机号");
                }
            }

            // 2. 创建并配置触发器
            TriggerDefinition trigger = new TriggerDefinition();
            trigger.setTriggerId(UUID.randomUUID().toString());
            trigger.setName("提醒: " + text);
            trigger.setDescription(type.getDescription());
            trigger.setSourceType(SourceType.USER);
            trigger.setSourceId(userId != null ? userId : "anonymous");
            trigger.setCreatedBy(userId);
            
            ScheduleMode parsedScheduleMode = parseScheduleMode(scheduleMode);
            trigger.setScheduleMode(parsedScheduleMode);
            
            // 对于 ONE_TIME 模式，将 scheduleValue 标准化为绝对时间戳（毫秒）
            String normalizedScheduleValue = normalizeScheduleValue(scheduleValue, parsedScheduleMode);
            trigger.setScheduleValue(normalizedScheduleValue);
            log.info("标准化 scheduleValue: 原值={}, 模式={}, 标准化后={}", 
                    scheduleValue, parsedScheduleMode, normalizedScheduleValue);
            
            trigger.setExecuteFunction("send_reminder");
            
            // 注入发送提醒的 Python 代码快照
            // 注意：固化 reminder_id 到代码中，这样触发器执行时不需要从上下文获取
            Map<String, String> codeSnapshot = new HashMap<>();
            codeSnapshot.put("send_reminder", String.format("""
                def send_reminder(**kwargs):
                    # 调用全局注入的 reminder 工具执行 send_reminder 操作，并固化提醒ID
                    return reminder(action='send_reminder', reminder_id='%s', **kwargs)
                """, reminderId));
            trigger.setFunctionCodeSnapshot(codeSnapshot);
            
            // 3. 订阅触发器（创建并激活）
            triggerManager.subscribe(trigger);

            // 4. 创建提醒内容
            Map<String, Object> content = new HashMap<>();
            
            // 对传话提醒进行参数长度校验和截断（短信模板限制各20字符）
            String finalText = text;
            if (type == ReminderType.RELAY && text != null && text.length() > 20) {
                finalText = text.substring(0, 20);
                log.warn("传话提醒 text 参数超过20字符，已截断: {} -> {}", text, finalText);
            }
            content.put("text", finalText);
            
            String targetPhone = (String) params.get("target_phone");
            if (targetPhone != null && !targetPhone.isEmpty()) {
                content.put("target_phone", targetPhone);
            }
            // 提取 who 参数 (提醒发起人称呼)
            String who = (String) params.get("who");
            if (who == null || who.isEmpty() || "匿名".equals(who)) {
                who = "匿名";
            }
            // 对传话提醒的 who 参数进行长度校验和截断
            if (type == ReminderType.RELAY && who.length() > 20) {
                who = who.substring(0, 20);
                log.warn("传话提醒 who 参数超过20字符，已截断: {} -> {}", params.get("who"), who);
            }
            content.put("who", who);

            @SuppressWarnings("unchecked")
            Map<String, Object> context = (Map<String, Object>) params.get("context");
            if (context != null) {
                content.put("context", context);
            }

            // 5. 创建并保存提醒实体
            Reminder reminder = reminderService.createReminder(
                    reminderId, // 使用预生成的ID
                    userId != null ? userId : "anonymous",
                    targetUserId,
                    type,
                    content,
                    trigger.getTriggerId()
            );

            log.info("创建提醒成功: id={}, type={}, text={}", reminder.getId(), type, text);

            Map<String, Object> result = new LinkedHashMap<>();
            result.put("success", true);
            result.put("message", "提醒创建成功");
            result.put("reminder_id", reminder.getId());
            result.put("trigger_id", trigger.getTriggerId());
            return objectMapper.writeValueAsString(result);

        } catch (Exception e) {
            log.error("创建提醒失败", e);
            return errorResult("创建提醒失败: " + e.getMessage());
        }
    }

    /**
     * 更新提醒
     */
    private String updateReminder(Map<String, Object> params) {
        try {
            String reminderId = (String) params.get("reminder_id");
            if (reminderId == null) {
                return errorResult("缺少 reminder_id 参数");
            }

            String text = (String) params.get("text");
            if (text == null) {
                return errorResult("缺少 text 参数");
            }

            Map<String, Object> content = new HashMap<>();
            content.put("text", text);
            @SuppressWarnings("unchecked")
            Map<String, Object> context = (Map<String, Object>) params.get("context");
            if (context != null) {
                content.put("context", context);
            }

            reminderService.updateContent(reminderId, content)
                    .orElseThrow(() -> new RuntimeException("提醒不存在: " + reminderId));

            log.info("更新提醒成功: id={}", reminderId);

            Map<String, Object> result = new LinkedHashMap<>();
            result.put("success", true);
            result.put("message", "提醒更新成功");
            result.put("reminder_id", reminderId);
            return objectMapper.writeValueAsString(result);

        } catch (Exception e) {
            log.error("更新提醒失败", e);
            return errorResult("更新提醒失败: " + e.getMessage());
        }
    }

    /**
     * 取消提醒
     */
    private String cancelReminder(Map<String, Object> params) {
        try {
            String reminderId = (String) params.get("reminder_id");
            if (reminderId == null) {
                return errorResult("缺少 reminder_id 参数");
            }

            // 取消提醒
            boolean cancelled = reminderService.cancelReminder(reminderId);
            if (!cancelled) {
                return errorResult("提醒不存在: " + reminderId);
            }

            // 取消关联的触发器
            reminderService.getReminder(reminderId).ifPresent(reminder -> {
                if (reminder.getTriggerId() != null) {
                    triggerManager.unsubscribe(reminder.getTriggerId());
                }
            });

            log.info("取消提醒成功: id={}", reminderId);

            Map<String, Object> result = new LinkedHashMap<>();
            result.put("success", true);
            result.put("message", "提醒已取消");
            result.put("reminder_id", reminderId);
            return objectMapper.writeValueAsString(result);

        } catch (Exception e) {
            log.error("取消提醒失败", e);
            return errorResult("取消提醒失败: " + e.getMessage());
        }
    }

    /**
     * 删除提醒
     */
    private String deleteReminder(Map<String, Object> params) {
        try {
            String reminderId = (String) params.get("reminder_id");
            if (reminderId == null) {
                return errorResult("缺少 reminder_id 参数");
            }

            // 先获取提醒以便取消触发器
            reminderService.getReminder(reminderId).ifPresent(reminder -> {
                if (reminder.getTriggerId() != null) {
                    triggerManager.unsubscribe(reminder.getTriggerId());
                }
            });

            // 删除提醒
            boolean deleted = reminderService.deleteReminder(reminderId);
            if (!deleted) {
                return errorResult("提醒不存在: " + reminderId);
            }

            log.info("删除提醒成功: id={}", reminderId);

            Map<String, Object> result = new LinkedHashMap<>();
            result.put("success", true);
            result.put("message", "提醒已删除");
            result.put("reminder_id", reminderId);
            return objectMapper.writeValueAsString(result);

        } catch (Exception e) {
            log.error("删除提醒失败", e);
            return errorResult("删除提醒失败: " + e.getMessage());
        }
    }

    /**
     * 列出提醒
     */
    private String listReminders(Map<String, Object> params) {
        try {
            String userId = LoginContext.getUserId();
            if (userId == null) {
                userId = (String) params.get("user_id");
            }

            List<Reminder> reminders;
            boolean activeOnly = Boolean.TRUE.equals(params.get("active_only"));
            
            if (activeOnly && userId != null) {
                reminders = reminderService.getActiveReminders(userId);
            } else if (userId != null) {
                reminders = reminderService.getUserReminders(userId);
            } else {
                return errorResult("缺少 user_id 参数");
            }

            List<Map<String, Object>> reminderList = reminders.stream()
                    .map(this::toReminderMap)
                    .collect(Collectors.toList());

            Map<String, Object> result = new LinkedHashMap<>();
            result.put("success", true);
            result.put("reminders", reminderList);
            result.put("count", reminderList.size());
            return objectMapper.writeValueAsString(result);

        } catch (Exception e) {
            log.error("列出提醒失败", e);
            return errorResult("列出提醒失败: " + e.getMessage());
        }
    }

    /**
     * 获取提醒详情
     */
    private String getReminder(Map<String, Object> params) {
        try {
            String reminderId = (String) params.get("reminder_id");
            if (reminderId == null) {
                return errorResult("缺少 reminder_id 参数");
            }

            Reminder reminder = reminderService.getReminder(reminderId)
                    .orElseThrow(() -> new RuntimeException("提醒不存在: " + reminderId));

            Map<String, Object> result = new LinkedHashMap<>();
            result.put("success", true);
            result.put("reminder", toReminderMap(reminder));
            return objectMapper.writeValueAsString(result);

        } catch (Exception e) {
            log.error("获取提醒详情失败", e);
            return errorResult("获取提醒详情失败: " + e.getMessage());
        }
    }

    /**
     * 执行提醒通知（由触发器异步调用）
     */
    private String sendReminder(Map<String, Object> params, ToolContext toolContext) {
        try {
            String reminderId = (String) params.get("reminder_id");
            if (reminderId == null) {
                return errorResult("缺少 reminder_id 参数");
            }

            log.info("接收到提醒通知请求: reminderId={}", reminderId);

            // 从工具上下文中获取触发器执行记录ID，实现日志关联
            String triggerExecutionId = null;
            if (toolContext != null && toolContext.getContext() != null) {
                triggerExecutionId = (String) toolContext.getContext().get("trigger_execution_id");
            }
            
            if (triggerExecutionId == null) {
                // 降级处理：如果没有上下文（如直接API调用），则生成新ID并打印警告
                triggerExecutionId = UUID.randomUUID().toString();
                log.warn("无法从 ToolContext 获取 trigger_execution_id，已降级生成随机ID: {}", triggerExecutionId);
            }

            // 1. 查找提醒实体
            Reminder reminder = reminderService.getReminder(reminderId)
                    .orElseThrow(() -> new RuntimeException("提醒不存在: " + reminderId));

            // 2. 检查状态
            if (reminder.getStatus() != Reminder.ReminderStatus.ACTIVE) {
                log.warn("提醒状态不是 ACTIVE，跳过通知发送: reminderId={}, status={}", 
                        reminderId, reminder.getStatus());
                return errorResult("提醒已取消或删除");
            }

            // 3. 执行发送通知逻辑
            NotificationChannel channel = NotificationChannel.IN_APP;

            // 获取发起人称呼
            String who = (String) reminder.getContent().getOrDefault("who", "匿名");
            log.info("执行提醒动作: type={}, reminderId={}, who={}, text={}", 
                    reminder.getType(), reminderId, who, reminder.getText());

            // 如果是传话提醒，检查目标用户是否是平台用户
            if (reminder.getType() == ReminderType.RELAY) {
                String targetPhone = (String) reminder.getContent().get("target_phone");
                if (targetPhone != null && !targetPhone.isEmpty()) {
                    boolean isPlatformUser = checkIsPlatformUser(targetPhone);
                    if (!isPlatformUser) {
                        channel = NotificationChannel.SMS;
                        log.info("传话目标用户非平台用户，切换至短信渠道: phone={}", targetPhone);
                        
                        // 执行短信发送逻辑
                        sendSmsNotice(targetPhone, reminder);
                    } else {
                        log.info("传话目标用户是平台用户，使用站内信渠道: phone={}", targetPhone);
                    }
                }
            }

            reminderService.createReminderLog(
                    reminderId,
                    triggerExecutionId,
                    LocalDateTime.now(), // 计划时间（即现在）
                    channel
            );

            // 4. 处理一次性执行的状态更新
            triggerManager.getDetail(reminder.getTriggerId()).ifPresent(trigger -> {
                if (trigger.getScheduleMode() == ScheduleMode.ONE_TIME) {
                    log.info("检测到一次性提醒执行完成，更新状态: reminderId={}, triggerId={}", 
                            reminderId, trigger.getTriggerId());
                    
                    // 更新提醒状态
                    reminderService.completeReminder(reminderId);
                    
                    // 更新触发器状态
                    triggerManager.updateStatus(trigger.getTriggerId(), 
                            TriggerStatus.FINISHED);
                }
            });

            log.info("提醒通知发送成功: reminderId={}, type={}, text={}, channel={}", 
                    reminderId, reminder.getType(), reminder.getText(), channel);

            Map<String, Object> result = new LinkedHashMap<>();
            result.put("success", true);
            result.put("message", "提醒通知已发送");
            result.put("reminder_id", reminderId);
            return objectMapper.writeValueAsString(result);

        } catch (Exception e) {
            log.error("执行提醒通知失败", e);
            return errorResult("执行提醒通知失败: " + e.getMessage());
        }
    }

    private Map<String, Object> toReminderMap(Reminder reminder) {
        Map<String, Object> map = new LinkedHashMap<>();
        map.put("id", reminder.getId());
        map.put("type", reminder.getType().name());
        map.put("type_desc", reminder.getType().getDescription());
        map.put("text", reminder.getText());
        map.put("status", reminder.getStatus().name());
        map.put("user_id", reminder.getUserId());
        map.put("target_user_id", reminder.getTargetUserId());
        map.put("trigger_id", reminder.getTriggerId());
        map.put("created_at", reminder.getCreatedAt() != null ? reminder.getCreatedAt().toString() : null);
        return map;
    }

    private ScheduleMode parseScheduleMode(String mode) {
        if (mode == null) {
            return ScheduleMode.FIXED_DELAY;
        }
        try {
            return ScheduleMode.valueOf(mode.toUpperCase());
        } catch (IllegalArgumentException e) {
            return ScheduleMode.FIXED_DELAY;
        }
    }

    /**
     * 标准化 scheduleValue
     * <p>
     * 对于 ONE_TIME 模式，将相对延迟（毫秒）转换为绝对时间戳（毫秒）
     * AI 应该按照工具定义传入毫秒数，此方法将其转换为绝对时间戳存储
     * </p>
     *
     * @param scheduleValue 原始调度值（延迟毫秒数）
     * @param scheduleMode 调度模式
     * @return 标准化后的调度值（绝对时间戳）
     */
    private String normalizeScheduleValue(String scheduleValue, ScheduleMode scheduleMode) {
        if (scheduleValue == null || scheduleValue.isEmpty()) {
            return scheduleValue;
        }
        
        // 只对 ONE_TIME 模式进行标准化（转换为绝对时间戳）
        if (scheduleMode != ScheduleMode.ONE_TIME) {
            return scheduleValue;
        }
        
        try {
            long numericValue = Long.parseLong(scheduleValue);
            
            // 判断是否已经是绝对时间戳（大于 2000000000000，约2033年）
            if (numericValue >= 2000000000000L) {
                // 已经是绝对时间戳，直接返回
                log.debug("scheduleValue 已是绝对时间戳: {}", numericValue);
                return scheduleValue;
            }
            
            // AI 传入的是延迟毫秒数，转换为绝对时间戳
            long absoluteTimestamp = System.currentTimeMillis() + numericValue;
            log.debug("scheduleValue 转换: 延迟{}ms -> 绝对时间戳{}", numericValue, absoluteTimestamp);
            return String.valueOf(absoluteTimestamp);
            
        } catch (NumberFormatException e) {
            // 不是数字，可能是 ISO-8601 格式，直接返回
            log.warn("scheduleValue 不是数字格式，直接返回: {}", scheduleValue);
            return scheduleValue;
        }
    }

    private String errorResult(String message) {
        try {
            Map<String, Object> result = new LinkedHashMap<>();
            result.put("success", false);
            result.put("message", message);
            return objectMapper.writeValueAsString(result);
        } catch (Exception e) {
            return "{\"success\":false,\"message\":\"" + message + "\"}";
        }
    }

    @Override
    public ToolDefinition getToolDefinition() {
        return toolDefinition;
    }

    @Override
    public CodeactToolDefinition getCodeactDefinition() {
        return codeactDefinition;
    }

    @Override
    public CodeactToolMetadata getCodeactMetadata() {
        return codeactMetadata;
    }

    /**
     * 内部接口响应 DTO
     */
    @lombok.Data
    public static class InternalWebResponse<T> {
        private String code;
        private String msg;
        private T data;
    }

    /**
     * 调用内部接口发送短信通知
     * 
     * 短信模板：小安旬AI助手在[笑了么]给您传达来自${who}的温馨提醒：您该${what}了。
     * 参数限制：who 和 what 各不超过20字符
     */
    private void sendSmsNotice(String phoneNumber, Reminder reminder) {
        try {
            String url = properties.getInternalApi().getSmsSendUrl();
            
            Map<String, String> requestBody = new HashMap<>();
            requestBody.put("phoneNumber", phoneNumber);
            
            // who 为提醒创建者，从内容中获取，默认降级为 "匿名"，确保不超过20字符
            String who = (String) reminder.getContent().getOrDefault("who", "匿名");
            if (who != null && who.length() > 20) {
                who = who.substring(0, 20);
                log.warn("短信 who 参数超过20字符，已截断");
            }
            requestBody.put("who", who); 
            
            // what 为提醒内容文本，确保不超过20字符
            String what = reminder.getText();
            if (what != null && what.length() > 20) {
                what = what.substring(0, 20);
                log.warn("短信 what 参数超过20字符，已截断");
            }
            requestBody.put("what", what);

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.set("X-Internal-Token", properties.getInternalApi().getToken());

            HttpEntity<Map<String, String>> entity = new HttpEntity<>(requestBody, headers);
            
            @SuppressWarnings("unchecked")
            InternalWebResponse<Void> response = restTemplate.postForObject(url, entity, InternalWebResponse.class);
            
            if (response != null && "SUCCESS".equals(response.getCode())) {
                log.info("内部接口发送短信成功: phone={}", phoneNumber);
            } else {
                log.warn("内部接口发送短信失败: phone={}, msg={}", phoneNumber, response != null ? response.getMsg() : "unknown");
            }
        } catch (Exception e) {
            log.error("调用内部接口发送短信失败: phone={}", phoneNumber, e);
        }
    }

    /**
     * 检查手机号是否是平台用户
     */
    private boolean checkIsPlatformUser(String phoneNumber) {
        try {
            String url = properties.getInternalApi().getUserCheckUrl();
            Map<String, String> requestBody = new HashMap<>();
            requestBody.put("appId", "default"); // 默认 appId
            requestBody.put("phoneNumber", phoneNumber);

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.set("X-Internal-Token", properties.getInternalApi().getToken());

            HttpEntity<Map<String, String>> entity = new HttpEntity<>(requestBody, headers);
            
            @SuppressWarnings("unchecked")
            InternalWebResponse<Boolean> response = restTemplate.postForObject(url, entity, InternalWebResponse.class);
            
            if (response != null && "SUCCESS".equals(response.getCode())) {
                return Boolean.TRUE.equals(response.getData());
            }
        } catch (Exception e) {
            log.error("调用内部接口检查用户失败: phone={}", phoneNumber, e);
        }
        return false;
    }

    private ToolDefinition buildToolDefinition() {
        String inputSchema = """
            {
                "type": "object",
                "properties": {
                    "action": {
                        "type": "string",
                        "description": "操作类型: create/update/cancel/delete/list/get/send_reminder (send_reminder 为内部触发专用)",
                        "enum": ["create", "update", "cancel", "delete", "list", "get", "send_reminder"]
                    },
                    "reminder_id": {
                        "type": "string",
                        "description": "提醒ID（update/cancel/delete/get 操作必填）"
                    },
                    "type": {
                        "type": "string",
                        "description": "提醒类型（create 操作必填）",
                        "enum": ["DRINK_WATER", "MEDICINE", "SEDENTARY", "MEAL", "SLEEP", "WAKE_UP", "CUSTOM", "RELAY"]
                    },
                    "text": {
                        "type": "string",
                        "description": "提醒内容文本（传话提醒时最多20个字符，对应短信模板中的${what}变量，如'按时吃药'、'早点睡觉'）"
                    },
                    "target_user_id": {
                        "type": "string",
                        "description": "目标用户ID（可选，不填则为当前用户）"
                    },
                    "target_phone": {
                        "type": "string",
                        "description": "目标手机号（传话提醒 RELAY 时必填）"
                    },
                    "who": {
                        "type": "string",
                        "description": "提醒发起人称呼（最多20个字符，对应短信模板中的${who}变量，如'妈妈'、'你的好朋友'，不填或设为'匿名'则显示为匿名）"
                    },
                    "schedule_mode": {
                        "type": "string",
                        "description": "调度模式，根据用户表述判断：\\n- ONE_TIME(一次性)：用于'明天'、'后天'、'今晚'、'下周X'、'X点'、'X分钟/小时后'等特定时间点\\n- CRON(定时重复)：仅用于明确说'每天'、'每周X'、'每隔X'等重复性提醒\\n- FIXED_DELAY(固定间隔循环)：用于'每隔X小时/分钟'的循环提醒\\n【重要】如果用户没有明确说'每天'或'每周'等重复词，默认使用 ONE_TIME",
                        "enum": ["CRON", "FIXED_DELAY", "FIXED_RATE", "ONE_TIME"]
                    },
                    "schedule_value": {
                        "type": "string",
                        "description": "调度值(字符串格式)：\\n- ONE_TIME模式：传从现在到目标时间的延迟毫秒数。如'明天早上8点'需计算距现在多少毫秒\\n- FIXED_DELAY/FIXED_RATE模式：传间隔毫秒数(如2小时=7200000)\\n- CRON模式：传cron表达式(如'0 0 8 * * ?'表示每天8点)"
                    },
                    "context": {
                        "type": "object",
                        "description": "提醒上下文信息"
                    },
                    "active_only": {
                        "type": "boolean",
                        "description": "仅列出有效的提醒（list 操作使用）"
                    }
                },
                "required": ["action"]
            }
            """;
        
        return ToolDefinition.builder()
                .name("reminder")
                .description("提醒管理工具，支持创建、更新、取消、删除、列出提醒。"
                        + "支持的提醒类型：喝水(DRINK_WATER)、吃药(MEDICINE)、久坐(SEDENTARY)、"
                        + "吃饭(MEAL)、睡觉(SLEEP)、起床(WAKE_UP)、自定义(CUSTOM)、传话(RELAY)。"
                        + "【调度模式选择规则】："
                        + "1. 一次性提醒(ONE_TIME)：'明天X点'、'后天'、'今晚'、'下周一'、'X分钟后'、'X点提醒我' - 只提醒一次的场景；"
                        + "2. 定时重复(CRON)：必须有'每天'、'每周X'等明确重复词才使用；"
                        + "3. 如果用户没说'每天'或'每周'，默认用 ONE_TIME")
                .inputSchema(inputSchema)
                .build();
    }

    private CodeactToolDefinition buildCodeactDefinition() {
        String inputSchema = toolDefinition.inputSchema();
        ParameterTree parameterTree = ParameterTree.builder().rawInputSchema(inputSchema).build();

        return DefaultCodeactToolDefinition.builder()
                .name("reminder")
                .description("提醒管理工具，支持创建、更新、取消、删除、列出提醒")
                .inputSchema(inputSchema)
                .parameterTree(parameterTree)
                .returnDescription("操作结果")
                .returnTypeHint("Dict[str, Any]")
                .build();
    }

    private CodeactToolMetadata buildCodeactMetadata() {
        return DefaultCodeactToolMetadata.builder()
                .addSupportedLanguage(Language.PYTHON)
                .addFewShot(new CodeExample("创建一次性起床提醒（明天早上8点）",
                        "# 用户说：明天早上八点提醒我起床\n"
                                + "# 分析：没有说'每天'，所以是一次性提醒，计算从现在到明天8点的毫秒数\n"
                                + "result = reminder(action='create', type='WAKE_UP', text='起床啦~', "
                                + "schedule_mode='ONE_TIME', schedule_value='43200000')",
                        "【一次性】明天X点、后天X点、今晚X点 都用 ONE_TIME，schedule_value传延迟毫秒数"))
                .addFewShot(new CodeExample("创建每天重复的起床提醒",
                        "# 用户说：每天早上八点提醒我起床\n"
                                + "# 分析：有'每天'，所以是重复提醒，用CRON\n"
                                + "result = reminder(action='create', type='WAKE_UP', text='起床啦~', "
                                + "schedule_mode='CRON', schedule_value='0 0 8 * * ?')",
                        "【重复】只有明确说'每天'、'每周X'才用 CRON"))
                .addFewShot(new CodeExample("创建一次性提醒（半小时后）",
                        "result = reminder(action='create', type='CUSTOM', text='上飞机', "
                                + "schedule_mode='ONE_TIME', schedule_value='1800000')",
                        "半小时=1800000毫秒，X分钟/小时后 用 ONE_TIME"))
                .addFewShot(new CodeExample("创建喝水提醒（每2小时循环）",
                        "# 用户说：每隔2小时提醒我喝水\n"
                                + "result = reminder(action='create', type='DRINK_WATER', text='该喝水啦~', "
                                + "schedule_mode='FIXED_DELAY', schedule_value='7200000')",
                        "'每隔X小时'用 FIXED_DELAY，2小时=7200000毫秒"))
                .addFewShot(new CodeExample("创建传话提醒（一次性）",
                        "result = reminder(action='create', type='RELAY', target_phone='13800138000', "
                                + "who='妈妈', text='按时吃药', schedule_mode='ONE_TIME', schedule_value='60000')",
                        "传话提醒通常是一次性的，1分钟=60000毫秒"))
                .addFewShot(new CodeExample("列出有效提醒",
                        "result = reminder(action='list', active_only=True)",
                        "列出当前用户所有有效的提醒"))
                .addFewShot(new CodeExample("取消提醒",
                        "result = reminder(action='cancel', reminder_id='xxx')",
                        "取消指定的提醒"))
                .displayName("提醒管理")
                .returnDirect(false)
                .build();
    }

}
