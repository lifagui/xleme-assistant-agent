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

import com.alibaba.assistant.agent.extension.conversation.model.Message;
import com.alibaba.assistant.agent.persistence.entity.MessageEntity;
import com.alibaba.assistant.agent.persistence.repository.MessageJpaRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

/**
 * 异步消息持久化服务
 * <p>
 * 消息保存采用异步方式，不阻塞主对话流程
 * </p>
 *
 * @author Assistant Agent Team
 * @since 1.0.0
 */
@Service
public class AsyncMessagePersistenceService {

    private static final Logger logger = LoggerFactory.getLogger(AsyncMessagePersistenceService.class);

    private final MessageJpaRepository messageRepository;

    public AsyncMessagePersistenceService(MessageJpaRepository messageRepository) {
        this.messageRepository = messageRepository;
    }

    /**
     * 异步保存单条消息
     *
     * @param message 消息
     * @return CompletableFuture
     */
    @Async("messagePersistenceExecutor")
    public CompletableFuture<Void> saveMessageAsync(Message message) {
        try {
            MessageEntity entity = toEntity(message);
            messageRepository.save(entity);
            logger.debug("异步保存消息成功: id={}, conversationId={}", entity.getId(), entity.getConversationId());
        } catch (Exception e) {
            logger.error("异步保存消息失败: conversationId={}", message.getConversationId(), e);
        }
        return CompletableFuture.completedFuture(null);
    }

    /**
     * 异步批量保存消息
     *
     * @param messages 消息列表
     * @return CompletableFuture
     */
    @Async("messagePersistenceExecutor")
    public CompletableFuture<Void> batchSaveMessagesAsync(List<Message> messages) {
        try {
            List<MessageEntity> entities = messages.stream()
                    .map(this::toEntity)
                    .collect(Collectors.toList());
            messageRepository.saveAll(entities);
            logger.debug("异步批量保存消息成功: count={}", entities.size());
        } catch (Exception e) {
            logger.error("异步批量保存消息失败: count={}", messages.size(), e);
        }
        return CompletableFuture.completedFuture(null);
    }

    /**
     * 同步保存消息（用于需要确保持久化完成的场景）
     *
     * @param message 消息
     * @return 保存后的消息
     */
    public Message saveMessage(Message message) {
        MessageEntity entity = toEntity(message);
        MessageEntity saved = messageRepository.save(entity);
        logger.debug("同步保存消息成功: id={}, conversationId={}", saved.getId(), saved.getConversationId());
        return toMessage(saved);
    }

    private static final String DEFAULT_TENANT_ID = "default";

    /**
     * 领域模型转换为实体
     */
    private MessageEntity toEntity(Message message) {
        String tenantId = message.getTenantId();
        if (tenantId == null || tenantId.isEmpty()) {
            tenantId = DEFAULT_TENANT_ID;
        }
        
        return MessageEntity.builder()
                .id(message.getId() != null ? message.getId() : UUID.randomUUID().toString())
                .tenantId(tenantId)
                .conversationId(message.getConversationId())
                .role(message.getRole().name())
                .contentType(message.getContentType() != null ? message.getContentType().name() : "TEXT")
                .content(message.getContent())
                .metadata(message.getMetadata())
                .createdAt(message.getCreatedAt() != null ? message.getCreatedAt() : LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
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
