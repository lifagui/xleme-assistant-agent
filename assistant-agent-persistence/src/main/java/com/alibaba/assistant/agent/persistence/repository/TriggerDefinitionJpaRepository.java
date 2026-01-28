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

import com.alibaba.assistant.agent.persistence.entity.TriggerDefinitionEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * 触发器定义 JPA Repository
 *
 * @author Assistant Agent Team
 * @since 1.0.0
 */
@Repository
public interface TriggerDefinitionJpaRepository extends JpaRepository<TriggerDefinitionEntity, String> {

    /**
     * 根据租户ID和触发器ID查询
     */
    Optional<TriggerDefinitionEntity> findByTenantIdAndTriggerId(String tenantId, String triggerId);

    /**
     * 根据租户ID和来源查询
     */
    List<TriggerDefinitionEntity> findByTenantIdAndSourceTypeAndSourceId(
            String tenantId, String sourceType, String sourceId);

    /**
     * 根据租户ID和状态查询
     */
    List<TriggerDefinitionEntity> findByTenantIdAndStatus(String tenantId, String status);

    /**
     * 根据租户ID和创建者查询
     */
    List<TriggerDefinitionEntity> findByTenantIdAndCreatedBy(String tenantId, String createdBy);

    /**
     * 查询所有有效的触发器
     */
    @Query("SELECT t FROM TriggerDefinitionEntity t WHERE t.tenantId = :tenantId AND t.status = 'ACTIVE'")
    List<TriggerDefinitionEntity> findAllActiveByTenantId(@Param("tenantId") String tenantId);

    /**
     * 更新触发器状态
     */
    @Modifying
    @Query("UPDATE TriggerDefinitionEntity t SET t.status = :status, t.updatedAt = CURRENT_TIMESTAMP " +
           "WHERE t.tenantId = :tenantId AND t.triggerId = :triggerId")
    int updateStatus(
            @Param("tenantId") String tenantId,
            @Param("triggerId") String triggerId,
            @Param("status") String status);

    /**
     * 根据租户ID删除触发器
     */
    void deleteByTenantIdAndTriggerId(String tenantId, String triggerId);
}
