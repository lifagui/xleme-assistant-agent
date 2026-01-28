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

import com.alibaba.assistant.agent.persistence.entity.TriggerExecutionRecordEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 触发器执行记录 JPA Repository
 *
 * @author Assistant Agent Team
 * @since 1.0.0
 */
@Repository
public interface TriggerExecutionRecordJpaRepository extends JpaRepository<TriggerExecutionRecordEntity, String> {

    /**
     * 根据租户ID和触发器ID查询执行记录
     */
    List<TriggerExecutionRecordEntity> findByTenantIdAndTriggerId(String tenantId, String triggerId);

    /**
     * 根据租户ID和触发器ID分页查询，按计划时间降序
     */
    Page<TriggerExecutionRecordEntity> findByTenantIdAndTriggerIdOrderByScheduledTimeDesc(
            String tenantId, String triggerId, Pageable pageable);

    /**
     * 根据租户ID和状态查询
     */
    List<TriggerExecutionRecordEntity> findByTenantIdAndStatus(String tenantId, String status);

    /**
     * 根据租户ID、触发器ID和状态查询
     */
    List<TriggerExecutionRecordEntity> findByTenantIdAndTriggerIdAndStatus(
            String tenantId, String triggerId, String status);

    /**
     * 根据租户ID和时间范围查询
     */
    @Query("SELECT t FROM TriggerExecutionRecordEntity t WHERE t.tenantId = :tenantId " +
           "AND t.scheduledTime BETWEEN :startTime AND :endTime ORDER BY t.scheduledTime DESC")
    List<TriggerExecutionRecordEntity> findByTenantIdAndScheduledTimeBetween(
            @Param("tenantId") String tenantId,
            @Param("startTime") LocalDateTime startTime,
            @Param("endTime") LocalDateTime endTime);

    /**
     * 统计触发器的执行次数
     */
    @Query("SELECT COUNT(t) FROM TriggerExecutionRecordEntity t WHERE t.tenantId = :tenantId AND t.triggerId = :triggerId")
    long countByTenantIdAndTriggerId(
            @Param("tenantId") String tenantId, 
            @Param("triggerId") String triggerId);

    /**
     * 统计指定状态的执行记录数量
     */
    @Query("SELECT COUNT(t) FROM TriggerExecutionRecordEntity t WHERE t.tenantId = :tenantId AND t.triggerId = :triggerId AND t.status = :status")
    long countByTenantIdAndTriggerIdAndStatus(
            @Param("tenantId") String tenantId,
            @Param("triggerId") String triggerId,
            @Param("status") String status);
}
