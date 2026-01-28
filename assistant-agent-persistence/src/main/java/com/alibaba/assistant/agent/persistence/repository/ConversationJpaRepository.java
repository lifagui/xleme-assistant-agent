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

import com.alibaba.assistant.agent.persistence.entity.ConversationEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * 会话 JPA Repository
 *
 * @author Assistant Agent Team
 * @since 1.0.0
 */
@Repository
public interface ConversationJpaRepository extends JpaRepository<ConversationEntity, String> {

    /**
     * 根据租户ID和用户ID查询会话列表
     */
    List<ConversationEntity> findByTenantIdAndUserId(String tenantId, String userId);

    /**
     * 根据租户ID和用户ID分页查询会话列表，按更新时间降序
     */
    Page<ConversationEntity> findByTenantIdAndUserIdOrderByUpdatedAtDesc(
            String tenantId, String userId, Pageable pageable);

    /**
     * 根据租户ID和会话ID查询
     */
    Optional<ConversationEntity> findByTenantIdAndId(String tenantId, String id);

    /**
     * 根据租户ID和用户ID及状态查询
     */
    List<ConversationEntity> findByTenantIdAndUserIdAndStatus(
            String tenantId, String userId, String status);

    /**
     * 根据租户ID和线程ID查询
     */
    Optional<ConversationEntity> findByTenantIdAndThreadId(String tenantId, String threadId);

    /**
     * 统计租户下用户的会话数量
     */
    @Query("SELECT COUNT(c) FROM ConversationEntity c WHERE c.tenantId = :tenantId AND c.userId = :userId")
    long countByTenantIdAndUserId(@Param("tenantId") String tenantId, @Param("userId") String userId);
}
