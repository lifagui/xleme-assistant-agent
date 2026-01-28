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
package com.alibaba.assistant.agent.persistence.repository;

import com.alibaba.assistant.agent.persistence.entity.ReminderLogEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 提醒执行记录 JPA Repository
 *
 * @author Assistant Agent Team
 * @since 1.0.0
 */
@Repository
public interface ReminderLogJpaRepository extends JpaRepository<ReminderLogEntity, String> {

    /**
     * 根据租户ID和提醒ID查询执行记录
     */
    List<ReminderLogEntity> findByTenantIdAndReminderId(String tenantId, String reminderId);

    /**
     * 根据租户ID和提醒ID分页查询执行记录，按计划时间降序
     */
    Page<ReminderLogEntity> findByTenantIdAndReminderIdOrderByScheduledTimeDesc(
            String tenantId, String reminderId, Pageable pageable);

    /**
     * 根据租户ID和触发器执行ID查询
     */
    List<ReminderLogEntity> findByTenantIdAndTriggerExecutionId(
            String tenantId, String triggerExecutionId);

    /**
     * 根据租户ID和状态查询
     */
    List<ReminderLogEntity> findByTenantIdAndStatus(String tenantId, String status);

    /**
     * 根据租户ID和时间范围查询
     */
    @Query("SELECT r FROM ReminderLogEntity r WHERE r.tenantId = :tenantId " +
           "AND r.scheduledTime BETWEEN :startTime AND :endTime ORDER BY r.scheduledTime DESC")
    List<ReminderLogEntity> findByTenantIdAndScheduledTimeBetween(
            @Param("tenantId") String tenantId,
            @Param("startTime") LocalDateTime startTime,
            @Param("endTime") LocalDateTime endTime);

    /**
     * 统计提醒的执行次数
     */
    @Query("SELECT COUNT(r) FROM ReminderLogEntity r WHERE r.tenantId = :tenantId AND r.reminderId = :reminderId")
    long countByTenantIdAndReminderId(
            @Param("tenantId") String tenantId, 
            @Param("reminderId") String reminderId);
}
