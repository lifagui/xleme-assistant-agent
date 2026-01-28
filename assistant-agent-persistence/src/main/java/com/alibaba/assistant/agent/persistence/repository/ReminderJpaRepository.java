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

import com.alibaba.assistant.agent.persistence.entity.ReminderEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * 提醒 JPA Repository
 *
 * @author Assistant Agent Team
 * @since 1.0.0
 */
@Repository
public interface ReminderJpaRepository extends JpaRepository<ReminderEntity, String> {

    /**
     * 根据租户ID和创建者用户ID查询提醒列表
     */
    List<ReminderEntity> findByTenantIdAndUserId(String tenantId, String userId);

    /**
     * 根据租户ID和目标用户ID查询提醒列表
     */
    List<ReminderEntity> findByTenantIdAndTargetUserId(String tenantId, String targetUserId);

    /**
     * 根据租户ID和触发器ID查询提醒
     */
    Optional<ReminderEntity> findByTenantIdAndTriggerId(String tenantId, String triggerId);

    /**
     * 根据租户ID和提醒ID查询
     */
    Optional<ReminderEntity> findByTenantIdAndId(String tenantId, String id);

    /**
     * 根据租户ID、用户ID和状态查询
     */
    List<ReminderEntity> findByTenantIdAndUserIdAndStatus(
            String tenantId, String userId, String status);

    /**
     * 根据租户ID、目标用户ID和状态查询
     */
    List<ReminderEntity> findByTenantIdAndTargetUserIdAndStatus(
            String tenantId, String targetUserId, String status);

    /**
     * 根据租户ID、用户ID和类型查询
     */
    List<ReminderEntity> findByTenantIdAndUserIdAndType(
            String tenantId, String userId, String type);

    /**
     * 统计用户创建的有效提醒数量
     */
    @Query("SELECT COUNT(r) FROM ReminderEntity r WHERE r.tenantId = :tenantId AND r.userId = :userId AND r.status = 'ACTIVE'")
    long countActiveByTenantIdAndUserId(
            @Param("tenantId") String tenantId, 
            @Param("userId") String userId);
}
