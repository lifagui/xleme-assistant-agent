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
package com.alibaba.assistant.agent.extension.reminder.service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.transaction.annotation.Transactional;

import com.alibaba.assistant.agent.common.context.UserContextHolder;
import com.alibaba.assistant.agent.extension.reminder.model.NotificationChannel;
import com.alibaba.assistant.agent.extension.reminder.model.Reminder;
import com.alibaba.assistant.agent.extension.reminder.model.ReminderLog;
import com.alibaba.assistant.agent.extension.reminder.model.ReminderType;
import com.alibaba.assistant.agent.persistence.entity.ReminderEntity;
import com.alibaba.assistant.agent.persistence.entity.ReminderLogEntity;
import com.alibaba.assistant.agent.persistence.repository.ReminderJpaRepository;
import com.alibaba.assistant.agent.persistence.repository.ReminderLogJpaRepository;

/**
 * 提醒服务
 * <p>
 * 注意：此类的 Bean 由 ReminderAutoConfiguration 创建，不使用 @Service 注解
 * </p>
 *
 * @author Assistant Agent Team
 * @since 1.0.0
 */
public class ReminderService {

    private static final Logger logger = LoggerFactory.getLogger(ReminderService.class);
    private static final String DEFAULT_TENANT_ID = "default";

    private final ReminderJpaRepository reminderRepository;
    private final ReminderLogJpaRepository reminderLogRepository;

    public ReminderService(ReminderJpaRepository reminderRepository,
                          ReminderLogJpaRepository reminderLogRepository) {
        this.reminderRepository = reminderRepository;
        this.reminderLogRepository = reminderLogRepository;
    }

    private String getTenantId() {
        // 使用 UserContextHolder 获取租户ID（自动从 LoginContext 或备用上下文获取）
        String tenantId = UserContextHolder.getTenantId();
        return (tenantId != null && !tenantId.isEmpty()) ? tenantId : DEFAULT_TENANT_ID;
    }

    /**
     * 创建提醒
     */
    @Transactional
    public Reminder createReminder(String id, String userId, String targetUserId, ReminderType type, 
                                   Map<String, Object> content, String triggerId) {
        String tenantId = getTenantId();
        
        ReminderEntity entity = ReminderEntity.builder()
                .id(id != null ? id : UUID.randomUUID().toString())
                .tenantId(tenantId)
                .userId(userId)
                .targetUserId(targetUserId != null ? targetUserId : userId)
                .triggerId(triggerId)
                .type(type.name())
                .content(content)
                .status(Reminder.ReminderStatus.ACTIVE.name())
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

        ReminderEntity saved = reminderRepository.save(entity);
        logger.info("创建提醒: id={}, tenantId={}, userId={}, type={}", 
                saved.getId(), tenantId, userId, type);
        
        return toReminder(saved);
    }

    /**
     * 根据ID获取提醒
     */
    public Optional<Reminder> getReminder(String id) {
        String tenantId = getTenantId();
        return reminderRepository.findByTenantIdAndId(tenantId, id)
                .map(this::toReminder);
    }

    /**
     * 根据触发器ID获取提醒
     */
    public Optional<Reminder> getReminderByTriggerId(String triggerId) {
        String tenantId = getTenantId();
        return reminderRepository.findByTenantIdAndTriggerId(tenantId, triggerId)
                .map(this::toReminder);
    }

    /**
     * 获取用户创建的提醒列表
     */
    public List<Reminder> getUserReminders(String userId) {
        String tenantId = getTenantId();
        return reminderRepository.findByTenantIdAndUserId(tenantId, userId)
                .stream()
                .map(this::toReminder)
                .collect(Collectors.toList());
    }

    /**
     * 获取用户创建的有效提醒列表
     */
    public List<Reminder> getActiveReminders(String userId) {
        String tenantId = getTenantId();
        return reminderRepository.findByTenantIdAndUserIdAndStatus(
                        tenantId, userId, Reminder.ReminderStatus.ACTIVE.name())
                .stream()
                .map(this::toReminder)
                .collect(Collectors.toList());
    }

    /**
     * 获取用户接收的提醒列表（目标用户）
     */
    public List<Reminder> getReceivedReminders(String targetUserId) {
        String tenantId = getTenantId();
        return reminderRepository.findByTenantIdAndTargetUserId(tenantId, targetUserId)
                .stream()
                .map(this::toReminder)
                .collect(Collectors.toList());
    }

    /**
     * 更新提醒内容
     */
    @Transactional
    public Optional<Reminder> updateContent(String id, Map<String, Object> content) {
        String tenantId = getTenantId();
        return reminderRepository.findByTenantIdAndId(tenantId, id)
                .map(entity -> {
                    entity.setContent(content);
                    entity.setUpdatedAt(LocalDateTime.now());
                    return toReminder(reminderRepository.save(entity));
                });
    }

    /**
     * 取消提醒
     */
    @Transactional
    public boolean cancelReminder(String id) {
        String tenantId = getTenantId();
        return reminderRepository.findByTenantIdAndId(tenantId, id)
                .map(entity -> {
                    entity.setStatus(Reminder.ReminderStatus.CANCELLED.name());
                    entity.setUpdatedAt(LocalDateTime.now());
                    reminderRepository.save(entity);
                    logger.info("取消提醒: id={}, tenantId={}", id, tenantId);
                    return true;
                })
                .orElse(false);
    }

    /**
     * 删除提醒（软删除）
     */
    @Transactional
    public boolean deleteReminder(String id) {
        String tenantId = getTenantId();
        return reminderRepository.findByTenantIdAndId(tenantId, id)
                .map(entity -> {
                    entity.setStatus(Reminder.ReminderStatus.DELETED.name());
                    entity.setUpdatedAt(LocalDateTime.now());
                    reminderRepository.save(entity);
                    logger.info("删除提醒: id={}, tenantId={}", id, tenantId);
                    return true;
                })
                .orElse(false);
    }

    /**
     * 创建提醒执行日志
     */
    @Transactional
    public ReminderLog createReminderLog(String reminderId, String triggerExecutionId,
                                         LocalDateTime scheduledTime, NotificationChannel channel) {
        String tenantId = getTenantId();
        
        ReminderLogEntity entity = ReminderLogEntity.builder()
                .id(UUID.randomUUID().toString())
                .tenantId(tenantId)
                .reminderId(reminderId)
                .triggerExecutionId(triggerExecutionId)
                .scheduledTime(scheduledTime)
                .channel(channel.name())
                .status(ReminderLog.ReminderLogStatus.PENDING.name())
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

        ReminderLogEntity saved = reminderLogRepository.save(entity);
        return toReminderLog(saved);
    }

    /**
     * 更新提醒执行日志状态
     */
    @Transactional
    public Optional<ReminderLog> updateReminderLogStatus(String logId, 
                                                         ReminderLog.ReminderLogStatus status,
                                                         String userFeedback) {
        return reminderLogRepository.findById(logId)
                .map(entity -> {
                    entity.setStatus(status.name());
                    entity.setActualTime(LocalDateTime.now());
                    if (userFeedback != null) {
                        entity.setUserFeedback(userFeedback);
                    }
                    entity.setUpdatedAt(LocalDateTime.now());
                    return toReminderLog(reminderLogRepository.save(entity));
                });
    }

    /**
     * 获取提醒的执行日志
     */
    public List<ReminderLog> getReminderLogs(String reminderId) {
        String tenantId = getTenantId();
        return reminderLogRepository.findByTenantIdAndReminderId(tenantId, reminderId)
                .stream()
                .map(this::toReminderLog)
                .collect(Collectors.toList());
    }

    /**
     * 标记提醒执行完成（针对一次性执行的提醒）
     */
    @Transactional
    public void completeReminder(String id) {
        String tenantId = getTenantId();
        reminderRepository.findByTenantIdAndId(tenantId, id).ifPresent(entity -> {
            entity.setStatus(Reminder.ReminderStatus.FINISHED.name());
            entity.setUpdatedAt(LocalDateTime.now());
            reminderRepository.save(entity);
            logger.info("提醒标记为已完成: id={}, tenantId={}", id, tenantId);
        });
    }

    /**
     * 实体转换为领域模型
     */
    private Reminder toReminder(ReminderEntity entity) {
        return Reminder.builder()
                .id(entity.getId())
                .tenantId(entity.getTenantId())
                .userId(entity.getUserId())
                .targetUserId(entity.getTargetUserId())
                .triggerId(entity.getTriggerId())
                .type(ReminderType.valueOf(entity.getType()))
                .content(entity.getContent())
                .status(Reminder.ReminderStatus.valueOf(entity.getStatus()))
                .createdAt(entity.getCreatedAt())
                .updatedAt(entity.getUpdatedAt())
                .build();
    }

    /**
     * 实体转换为领域模型
     */
    private ReminderLog toReminderLog(ReminderLogEntity entity) {
        return ReminderLog.builder()
                .id(entity.getId())
                .tenantId(entity.getTenantId())
                .reminderId(entity.getReminderId())
                .triggerExecutionId(entity.getTriggerExecutionId())
                .scheduledTime(entity.getScheduledTime())
                .actualTime(entity.getActualTime())
                .channel(NotificationChannel.valueOf(entity.getChannel()))
                .status(ReminderLog.ReminderLogStatus.valueOf(entity.getStatus()))
                .userFeedback(entity.getUserFeedback())
                .createdAt(entity.getCreatedAt())
                .updatedAt(entity.getUpdatedAt())
                .build();
    }
}
