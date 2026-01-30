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
package com.alibaba.assistant.agent.start.controller;

import com.alibaba.assistant.agent.common.context.LoginContext;
import com.alibaba.assistant.agent.persistence.entity.ReminderEntity;
import com.alibaba.assistant.agent.persistence.repository.ReminderJpaRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * 回忆模块接口
 *
 * @author Assistant Agent Team
 * @since 1.0.0
 */
@RestController
@RequestMapping("/api/reminder")
public class ReminderApiController {

    private static final String DEFAULT_TENANT_ID = "default";
    private static final String TYPE_RELAY = "RELAY";
    private static final Set<String> SUPPORTED_FILTERS = Set.of("ALL", "REMINDER", "RELAY");

    private final ReminderJpaRepository reminderRepository;

    public ReminderApiController(ReminderJpaRepository reminderRepository) {
        this.reminderRepository = reminderRepository;
    }

    /**
     * 获取提醒统计数据（提醒数量、传话次数）
     */
    @GetMapping("/stats")
    public ResponseEntity<StatsResponse> getStats() {
        String userId = LoginContext.getUserId();
        if (!StringUtils.hasText(userId)) {
            return ResponseEntity.badRequest().build();
        }
        String tenantId = getTenantId();

        List<ReminderEntity> reminders = reminderRepository.findByTenantIdAndUserId(tenantId, userId);
        long relayCount = reminders.stream()
                .filter(this::isNotDeleted)
                .filter(reminder -> TYPE_RELAY.equals(reminder.getType()))
                .count();
        long reminderCount = reminders.stream()
                .filter(this::isNotDeleted)
                .filter(reminder -> !TYPE_RELAY.equals(reminder.getType()))
                .count();

        return ResponseEntity.ok(new StatsResponse(reminderCount, relayCount));
    }

    /**
     * 获取提醒时间轴数据
     */
    @GetMapping("/timeline")
    public ResponseEntity<TimelineResponse> getTimeline(
            @RequestParam(defaultValue = "ALL") String filter,
            @RequestParam(defaultValue = "50") int limit) {
        String userId = LoginContext.getUserId();
        if (!StringUtils.hasText(userId)) {
            return ResponseEntity.badRequest().build();
        }
        String tenantId = getTenantId();

        String normalizedFilter = normalizeFilter(filter);
        List<ReminderEntity> reminders = reminderRepository
                .findByTenantIdAndUserIdOrTargetUserId(tenantId, userId);

        List<TimelineItem> items = reminders.stream()
                .filter(Objects::nonNull)
                .filter(this::isNotDeleted)
                .filter(reminder -> matchFilter(normalizedFilter, reminder))
                .sorted(Comparator.comparing(ReminderEntity::getCreatedAt,
                        Comparator.nullsLast(Comparator.naturalOrder())).reversed())
                .limit(Math.max(1, limit))
                .map(reminder -> toTimelineItem(reminder, userId))
                .collect(Collectors.toList());

        return ResponseEntity.ok(new TimelineResponse(items));
    }

    private String getTenantId() {
        String tenantId = LoginContext.getTenantId();
        return StringUtils.hasText(tenantId) ? tenantId : DEFAULT_TENANT_ID;
    }

    private boolean isNotDeleted(ReminderEntity reminder) {
        return reminder == null || reminder.getStatus() == null
                || !"DELETED".equalsIgnoreCase(reminder.getStatus());
    }

    private String normalizeFilter(String filter) {
        if (!StringUtils.hasText(filter)) {
            return "ALL";
        }
        String normalized = filter.trim().toUpperCase();
        return SUPPORTED_FILTERS.contains(normalized) ? normalized : "ALL";
    }

    private boolean matchFilter(String filter, ReminderEntity reminder) {
        if ("ALL".equals(filter)) {
            return true;
        }
        if (reminder == null) {
            return false;
        }
        boolean isRelay = TYPE_RELAY.equals(reminder.getType());
        if ("RELAY".equals(filter)) {
            return isRelay;
        }
        return !isRelay;
    }

    private TimelineItem toTimelineItem(ReminderEntity reminder, String currentUserId) {
        String reminderType = reminder.getType();
        Map<String, Object> content = reminder.getContent();
        String text = content != null ? toStringSafely(content.get("text")) : null;
        String who = content != null ? toStringSafely(content.get("who")) : null;

        String direction = null;
        if (TYPE_RELAY.equals(reminderType)) {
            if (currentUserId != null
                    && currentUserId.equals(reminder.getTargetUserId())
                    && !currentUserId.equals(reminder.getUserId())) {
                direction = "RECEIVED";
            } else {
                direction = "SENT";
            }
        }

        String type = TYPE_RELAY.equals(reminderType) ? "RELAY" : "REMINDER";
        String title = buildTitle(reminderType, direction);
        String createdAt = reminder.getCreatedAt() != null ? reminder.getCreatedAt().toString() : null;

        return new TimelineItem(
                reminder.getId(),
                type,
                reminderType,
                direction,
                title,
                text,
                who,
                reminder.getStatus(),
                createdAt
        );
    }

    private String buildTitle(String reminderType, String direction) {
        if (!StringUtils.hasText(reminderType)) {
            return "提醒";
        }
        if (TYPE_RELAY.equals(reminderType)) {
            return "RECEIVED".equals(direction) ? "收到传话" : "传话提醒";
        }
        if ("DRINK_WATER".equals(reminderType)) {
            return "喝水提醒";
        }
        if ("MEDICINE".equals(reminderType)) {
            return "吃药提醒";
        }
        if ("SEDENTARY".equals(reminderType)) {
            return "久坐提醒";
        }
        if ("MEAL".equals(reminderType)) {
            return "吃饭提醒";
        }
        if ("SLEEP".equals(reminderType)) {
            return "睡觉提醒";
        }
        if ("CUSTOM".equals(reminderType)) {
            return "自定义提醒";
        }
        return "提醒";
    }

    private String toStringSafely(Object value) {
        return value != null ? value.toString() : null;
    }

    public record StatsResponse(long reminderCount, long relayCount) {
    }

    public record TimelineResponse(List<TimelineItem> items) {
    }

    public record TimelineItem(
            String id,
            String type,
            String subType,
            String direction,
            String title,
            String text,
            String who,
            String status,
            String createdAt
    ) {
    }
}
