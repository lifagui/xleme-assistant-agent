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

import com.alibaba.assistant.agent.persistence.entity.MessageEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * 消息 JPA Repository
 *
 * @author Assistant Agent Team
 * @since 1.0.0
 */
@Repository
public interface MessageJpaRepository extends JpaRepository<MessageEntity, String> {

    /**
     * 根据租户ID和会话ID查询消息列表，按创建时间升序
     */
    List<MessageEntity> findByTenantIdAndConversationIdOrderByCreatedAtAsc(
            String tenantId, String conversationId);

    /**
     * 根据租户ID和会话ID分页查询消息
     */
    Page<MessageEntity> findByTenantIdAndConversationIdOrderByCreatedAtDesc(
            String tenantId, String conversationId, Pageable pageable);

    /**
     * 根据租户ID和会话ID及角色查询消息
     */
    List<MessageEntity> findByTenantIdAndConversationIdAndRole(
            String tenantId, String conversationId, String role);

    /**
     * 统计会话中的消息数量
     */
    @Query("SELECT COUNT(m) FROM MessageEntity m WHERE m.tenantId = :tenantId AND m.conversationId = :conversationId")
    long countByTenantIdAndConversationId(
            @Param("tenantId") String tenantId, 
            @Param("conversationId") String conversationId);

    /**
     * 删除会话下的所有消息
     */
    void deleteByTenantIdAndConversationId(String tenantId, String conversationId);
}
