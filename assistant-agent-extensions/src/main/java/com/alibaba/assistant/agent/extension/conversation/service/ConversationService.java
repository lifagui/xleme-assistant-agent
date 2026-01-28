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
package com.alibaba.assistant.agent.extension.conversation.service;

import com.alibaba.assistant.agent.common.context.LoginContext;
import com.alibaba.assistant.agent.extension.conversation.model.Conversation;
import com.alibaba.assistant.agent.extension.conversation.model.Message;
import com.alibaba.assistant.agent.persistence.entity.ConversationEntity;
import com.alibaba.assistant.agent.persistence.entity.MessageEntity;
import com.alibaba.assistant.agent.persistence.repository.ConversationJpaRepository;
import com.alibaba.assistant.agent.persistence.repository.MessageJpaRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * 会话服务
 *
 * @author Assistant Agent Team
 * @since 1.0.0
 */
@Service
public class ConversationService {

    private static final Logger logger = LoggerFactory.getLogger(ConversationService.class);

    private final ConversationJpaRepository conversationRepository;
    private final MessageJpaRepository messageRepository;

    public ConversationService(ConversationJpaRepository conversationRepository,
                               MessageJpaRepository messageRepository) {
        this.conversationRepository = conversationRepository;
        this.messageRepository = messageRepository;
    }

    private static final String DEFAULT_TENANT_ID = "default";
    private static final String DEFAULT_USER_ID = "anonymous";

    /**
     * 创建会话
     */
    @Transactional
    public Conversation createConversation(String userId, String threadId, String title) {
        String tenantId = LoginContext.getTenantId();
        // 当 tenantId 为 null 时使用默认值
        if (tenantId == null || tenantId.isEmpty()) {
            tenantId = DEFAULT_TENANT_ID;
        }
        // 当 userId 为 null 时使用默认值
        if (userId == null || userId.isEmpty()) {
            userId = LoginContext.getUserId();
            if (userId == null || userId.isEmpty()) {
                userId = DEFAULT_USER_ID;
            }
        }
        
        ConversationEntity entity = ConversationEntity.builder()
                .id(UUID.randomUUID().toString())
                .tenantId(tenantId)
                .userId(userId)
                .threadId(threadId)
                .title(title)
                .status(Conversation.ConversationStatus.ACTIVE.name())
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

        ConversationEntity saved = conversationRepository.save(entity);
        logger.info("创建会话: id={}, tenantId={}, userId={}", saved.getId(), tenantId, userId);
        
        return toConversation(saved);
    }

    /**
     * 根据ID获取会话
     */
    public Optional<Conversation> getConversation(String id) {
        String tenantId = LoginContext.getTenantId();
        return conversationRepository.findByTenantIdAndId(tenantId, id)
                .map(this::toConversation);
    }

    /**
     * 根据线程ID获取会话
     */
    public Optional<Conversation> getConversationByThreadId(String threadId) {
        String tenantId = LoginContext.getTenantId();
        return conversationRepository.findByTenantIdAndThreadId(tenantId, threadId)
                .map(this::toConversation);
    }

    /**
     * 获取用户的会话列表
     */
    public List<Conversation> getUserConversations(String userId, int page, int size) {
        String tenantId = LoginContext.getTenantId();
        Page<ConversationEntity> pageResult = conversationRepository
                .findByTenantIdAndUserIdOrderByUpdatedAtDesc(tenantId, userId, PageRequest.of(page, size));
        return pageResult.getContent().stream()
                .map(this::toConversation)
                .collect(Collectors.toList());
    }

    /**
     * 更新会话标题
     */
    @Transactional
    public Optional<Conversation> updateTitle(String id, String title) {
        String tenantId = LoginContext.getTenantId();
        return conversationRepository.findByTenantIdAndId(tenantId, id)
                .map(entity -> {
                    entity.setTitle(title);
                    entity.setUpdatedAt(LocalDateTime.now());
                    return toConversation(conversationRepository.save(entity));
                });
    }

    /**
     * 删除会话（软删除）
     */
    @Transactional
    public boolean deleteConversation(String id) {
        String tenantId = LoginContext.getTenantId();
        return conversationRepository.findByTenantIdAndId(tenantId, id)
                .map(entity -> {
                    entity.setStatus(Conversation.ConversationStatus.DELETED.name());
                    entity.setUpdatedAt(LocalDateTime.now());
                    conversationRepository.save(entity);
                    logger.info("删除会话: id={}, tenantId={}", id, tenantId);
                    return true;
                })
                .orElse(false);
    }

    /**
     * 获取会话的消息列表
     */
    public List<Message> getMessages(String conversationId) {
        String tenantId = LoginContext.getTenantId();
        return messageRepository.findByTenantIdAndConversationIdOrderByCreatedAtAsc(tenantId, conversationId)
                .stream()
                .map(this::toMessage)
                .collect(Collectors.toList());
    }

    /**
     * 实体转换为领域模型
     */
    private Conversation toConversation(ConversationEntity entity) {
        return Conversation.builder()
                .id(entity.getId())
                .tenantId(entity.getTenantId())
                .userId(entity.getUserId())
                .threadId(entity.getThreadId())
                .title(entity.getTitle())
                .status(Conversation.ConversationStatus.valueOf(entity.getStatus()))
                .createdAt(entity.getCreatedAt())
                .updatedAt(entity.getUpdatedAt())
                .build();
    }

    /**
     * 实体转换为领域模型
     */
    private Message toMessage(MessageEntity entity) {
        return Message.builder()
                .id(entity.getId())
                .tenantId(entity.getTenantId())
                .conversationId(entity.getConversationId())
                .role(Message.MessageRole.valueOf(entity.getRole()))
                .contentType(entity.getContentType() != null ? 
                        Message.ContentType.valueOf(entity.getContentType()) : Message.ContentType.TEXT)
                .content(entity.getContent())
                .metadata(entity.getMetadata())
                .createdAt(entity.getCreatedAt())
                .updatedAt(entity.getUpdatedAt())
                .build();
    }
}
