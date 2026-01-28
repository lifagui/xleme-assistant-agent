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

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.chat.messages.AssistantMessage;
import org.springframework.ai.chat.messages.Message;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.alibaba.assistant.agent.common.context.LoginContext;
import com.alibaba.assistant.agent.extension.conversation.model.Conversation;
import com.alibaba.assistant.agent.extension.conversation.model.Message.ContentType;
import com.alibaba.assistant.agent.extension.conversation.model.Message.MessageRole;
import com.alibaba.assistant.agent.extension.conversation.service.AsyncMessagePersistenceService;
import com.alibaba.assistant.agent.extension.conversation.service.ConversationService;
import com.alibaba.cloud.ai.graph.CompiledGraph;
import com.alibaba.cloud.ai.graph.OverAllState;
import com.alibaba.cloud.ai.graph.RunnableConfig;
import com.alibaba.cloud.ai.graph.agent.ReactAgent;
import com.alibaba.cloud.ai.graph.streaming.StreamingOutput;
import com.fasterxml.jackson.annotation.JsonProperty;

import reactor.core.publisher.Flux;

/**
 * REST API Controller for Chat with Agent
 * 
 * <p>提供 HTTP API 供移动端 APP 调用智能体对话功能。
 * 
 * <h2>API 端点：</h2>
 * <ul>
 *   <li>POST /api/chat - 发送消息并获取回复</li>
 *   <li>POST /api/chat/stream - 发送消息并以 SSE 流式获取回复</li>
 *   <li>POST /api/chat/session - 创建新会话</li>
 *   <li>DELETE /api/chat/session/{sessionId} - 结束会话</li>
 * </ul>
 * 
 * @author Assistant Agent Team
 * @since 1.0.0
 */
@RestController
@RequestMapping("/api/chat")
public class ChatApiController {

    private static final Logger logger = LoggerFactory.getLogger(ChatApiController.class);

    private final ReactAgent agent;
    
    // 会话管理：sessionId -> threadId（用于多轮对话上下文保持）
    // 注意：这是内存缓存，用于快速查找；持久化数据在数据库中
    private final Map<String, String> sessionThreadMap = new ConcurrentHashMap<>();
    
    // 会话管理：sessionId -> conversationId（用于消息持久化）
    private final Map<String, String> sessionConversationMap = new ConcurrentHashMap<>();

    private final ConversationService conversationService;
    private final AsyncMessagePersistenceService messagePersistenceService;

    public ChatApiController(ReactAgent agent,
                            ConversationService conversationService,
                            AsyncMessagePersistenceService messagePersistenceService) {
        this.agent = agent;
        this.conversationService = conversationService;
        this.messagePersistenceService = messagePersistenceService;
        logger.info("ChatApiController 初始化完成, 持久化服务: {}", 
                conversationService != null ? "已启用" : "未启用");
    }

    // ==================== Request/Response DTOs ====================

    /**
     * 聊天请求
     */
    public record ChatRequest(
            @JsonProperty("message") String message,
            @JsonProperty("session_id") String sessionId
    ) {}

    /**
     * 聊天响应
     */
    public record ChatResponse(
            @JsonProperty("success") boolean success,
            @JsonProperty("message") String message,
            @JsonProperty("session_id") String sessionId,
            @JsonProperty("data") Object data,
            @JsonProperty("error") String error
    ) {
        public static ChatResponse success(String message, String sessionId, Object data) {
            return new ChatResponse(true, message, sessionId, data, null);
        }

        public static ChatResponse error(String error, String sessionId) {
            return new ChatResponse(false, null, sessionId, null, error);
        }
    }

    /**
     * 会话响应
     */
    public record SessionResponse(
            @JsonProperty("session_id") String sessionId,
            @JsonProperty("created") boolean created
    ) {}

    // ==================== API Endpoints ====================

    /**
     * 创建新会话
     * 
     * POST /api/chat/session
     */
    @PostMapping("/session")
    public ResponseEntity<SessionResponse> createSession() {
        String sessionId = UUID.randomUUID().toString();
        String threadId = UUID.randomUUID().toString();
        sessionThreadMap.put(sessionId, threadId);
        
        // 持久化会话到数据库
        if (conversationService != null) {
            try {
                String userId = LoginContext.getUserId();
                Conversation conversation = conversationService.createConversation(
                        userId != null ? userId : "anonymous",
                        threadId,
                        null  // 标题可以后续更新
                );
                sessionConversationMap.put(sessionId, conversation.getId());
                logger.info("创建新会话: sessionId={}, threadId={}, conversationId={}", 
                        sessionId, threadId, conversation.getId());
            } catch (Exception e) {
                logger.warn("持久化会话失败，继续使用内存会话: sessionId={}", sessionId, e);
            }
        } else {
            logger.info("创建新会话（内存模式）: sessionId={}, threadId={}", sessionId, threadId);
        }
        
        return ResponseEntity.ok(new SessionResponse(sessionId, true));
    }

    /**
     * 结束会话
     * 
     * DELETE /api/chat/session/{sessionId}
     */
    @DeleteMapping("/session/{sessionId}")
    public ResponseEntity<Map<String, Object>> deleteSession(@PathVariable String sessionId) {
        String removed = sessionThreadMap.remove(sessionId);
        Map<String, Object> response = new HashMap<>();
        response.put("session_id", sessionId);
        response.put("deleted", removed != null);
        
        logger.info("结束会话: sessionId={}, deleted={}", sessionId, removed != null);
        return ResponseEntity.ok(response);
    }

    /**
     * 发送消息并获取回复（同步）
     * 
     * POST /api/chat
     * Content-Type: application/json
     * 
     * Request Body:
     * {
     *   "message": "用户消息",
     *   "session_id": "会话ID（可选，不传则自动创建）"
     * }
     * 
     * Response:
     * {
     *   "success": true,
     *   "message": "智能体回复内容",
     *   "session_id": "会话ID",
     *   "data": { ... 额外数据 ... }
     * }
     */
    @PostMapping
    public ResponseEntity<ChatResponse> chat(@RequestBody ChatRequest request) {
        logger.info("收到聊天请求: message={}, sessionId={}", request.message(), request.sessionId());

        try {
            // 获取或创建会话
            String sessionId = request.sessionId();
            if (sessionId == null || sessionId.isBlank()) {
                sessionId = UUID.randomUUID().toString();
                sessionThreadMap.put(sessionId, UUID.randomUUID().toString());
            }
            String threadId = sessionThreadMap.computeIfAbsent(sessionId, k -> UUID.randomUUID().toString());
            
            // 确保会话已持久化
            String conversationId = ensureConversationPersisted(sessionId, threadId);

            // 异步保存用户消息
            saveMessageAsync(conversationId, MessageRole.USER, request.message());

            // 调用 Agent
            String reply = invokeAgent(request.message(), threadId);

            // 异步保存助手回复
            saveMessageAsync(conversationId, MessageRole.ASSISTANT, reply);

            logger.info("聊天响应: sessionId={}, replyLength={}", sessionId, reply.length());
            return ResponseEntity.ok(ChatResponse.success(reply, sessionId, null));

        } catch (Exception e) {
            logger.error("聊天处理失败", e);
            return ResponseEntity.ok(ChatResponse.error(e.getMessage(), request.sessionId()));
        }
    }

    /**
     * 发送消息并以 SSE 流式获取回复
     *
     * POST /api/chat/stream
     * Content-Type: application/json
     * Accept: text/event-stream
     *
     * Request Body:
     * {
     *   "message": "用户消息",
     *   "session_id": "会话ID（可选）"
     * }
     *
     * Response: Server-Sent Events 流
     * data: {"type": "content", "content": "部分回复..."}
     * data: {"type": "done", "session_id": "xxx"}
     */
    @PostMapping(value = "/stream", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flux<String> chatStream(@RequestBody ChatRequest request) {
        logger.info("收到流式聊天请求: message={}, sessionId={}", request.message(), request.sessionId());

        try {
            // 获取或创建会话
            String sessionId = request.sessionId();
            if (sessionId == null || sessionId.isBlank()) {
                sessionId = UUID.randomUUID().toString();
                sessionThreadMap.put(sessionId, UUID.randomUUID().toString());
            }
            String threadId = sessionThreadMap.computeIfAbsent(sessionId, k -> UUID.randomUUID().toString());
            final String finalSessionId = sessionId;
            
            // 确保会话已持久化
            String conversationId = ensureConversationPersisted(sessionId, threadId);
            final String finalConversationId = conversationId;

            // 处理特殊指令：[greeting] 转换为招呼语生成请求
            String message = request.message();
            String originalMessage = message;  // 保存原始消息用于持久化
            if ("[greeting]".equals(message)) {
                message = "你好呀，我是用户，刚打开app想和你聊聊天。请用小安旬的语气给我一句简短的招呼语吧，温暖亲切带点俏皮，2-3句话就好，直接说招呼语内容。";
            }
            
            // 异步保存用户消息
            saveMessageAsync(finalConversationId, MessageRole.USER, originalMessage);
            
            // 用于收集完整的助手回复
            StringBuilder fullReply = new StringBuilder();

            // 流式调用 Agent
            return invokeAgentStream(message, threadId)
                    .doOnNext(content -> {
                        logger.debug("流式内容准备发送: content={}", content);
                        fullReply.append(content);
                    })
                    .map(content -> formatSSE("content", content, null))
                    .doOnNext(sse -> logger.debug("SSE格式化完成: {}", sse.replace("\n", "\\n")))
                    .concatWith(Flux.just(formatSSE("done", null, finalSessionId)))
                    .doOnComplete(() -> {
                        logger.info("流式响应完成");
                        // 异步保存完整的助手回复
                        if (fullReply.length() > 0) {
                            saveMessageAsync(finalConversationId, MessageRole.ASSISTANT, fullReply.toString());
                        }
                    })
                    .onErrorResume(e -> {
                        logger.error("流式聊天处理失败", e);
                        return Flux.just(formatSSE("error", e.getMessage(), finalSessionId));
                    });

        } catch (Exception e) {
            logger.error("流式聊天初始化失败", e);
            return Flux.just(formatSSE("error", e.getMessage(), request.sessionId()));
        }
    }

    /**
     * 健康检查
     * 
     * GET /api/chat/health
     */
    @GetMapping("/health")
    public ResponseEntity<Map<String, Object>> health() {
        Map<String, Object> status = new HashMap<>();
        status.put("status", "UP");
        status.put("agent", agent != null ? "ready" : "not_initialized");
        status.put("active_sessions", sessionThreadMap.size());
        return ResponseEntity.ok(status);
    }

    // ==================== Private Methods ====================

    /**
     * 调用 Agent 获取回复（同步）
     */
    private String invokeAgent(String message, String threadId) throws Exception {
        // 构建输入状态
        OverAllState initialState = new OverAllState();
        initialState.input(Map.of("input", message));

        // 配置运行参数（支持多轮对话）
        RunnableConfig config = RunnableConfig.builder()
                .threadId(threadId)
                .build();

        // 获取编译后的图
        CompiledGraph compiledGraph = agent.getAndCompileGraph();

        // 使用 invoke 方法执行并获取最终状态
        Optional<OverAllState> result = compiledGraph.invoke(initialState, config);

        if (result.isPresent()) {
            OverAllState state = result.get();
            
            // 尝试从 agent_outcome 获取结果
            Optional<Object> outcome = state.value("agent_outcome");
            if (outcome.isPresent()) {
                return extractContent(outcome.get());
            }
            
            // 尝试从 messages 获取最后一条消息
            Optional<Object> messages = state.value("messages");
            if (messages.isPresent()) {
                return extractLastMessage(messages.get());
            }
            
            // 尝试从 output 获取
            Optional<Object> output = state.value("output");
            if (output.isPresent()) {
                return extractContent(output.get());
            }
        }

        return "抱歉，我暂时无法回答这个问题。";
    }

    /**
     * 调用 Agent 获取回复（真实流式）
     * 使用 Agent.stream() 方法实现真正的流式输出
     */
    private Flux<String> invokeAgentStream(String message, String threadId) {
        try {
            // 配置运行参数（支持多轮对话）
            RunnableConfig config = RunnableConfig.builder()
                    .threadId(threadId)
                    .build();

            // 使用 agent.stream() 进行真正的流式调用
            return agent.stream(message, config)
                    .doOnNext(output -> logger.debug("收到流式输出: type={}, class={}",
                            output.getClass().getSimpleName(), output.getClass().getName()))
                    .filter(output -> {
                        boolean isStreaming = output instanceof StreamingOutput;
                        if (!isStreaming) {
                            logger.debug("过滤非StreamingOutput: {}", output.getClass().getName());
                        }
                        return isStreaming;
                    })
                    .handle((output, sink) -> {
                        String content = extractStreamContent((StreamingOutput<?>) output);
                        logger.debug("提取内容: content={}", content);
                        if (content != null && !content.isBlank()) {
                            sink.next(content);
                        }
                    });
        } catch (Exception e) {
            logger.error("流式调用 Agent 失败", e);
            return Flux.error(e);
        }
    }

    /**
     * 从 StreamingOutput 中提取文本内容
     */
    private String extractStreamContent(StreamingOutput<?> streamingOutput) {
        // 优先从 message 获取内容
        Message message = streamingOutput.message();
        if (message instanceof AssistantMessage assistantMessage) {
            String text = assistantMessage.getText();
            if (text != null && !text.isBlank()) {
                return text;
            }
        }

        // 降级：尝试从 chunk 获取（已废弃但仍可用）
        @SuppressWarnings("deprecation")
        String chunk = streamingOutput.chunk();
        if (chunk != null && !chunk.isBlank()) {
            return chunk;
        }

        // 降级：尝试从原始数据获取
        Object originData = streamingOutput.getOriginData();
        if (originData instanceof String str && !str.isBlank()) {
            return str;
        }

        return null;
    }

    /**
     * 从 agent_outcome 提取内容
     */
    private String extractContent(Object outcome) {
        if (outcome == null) {
            return "";
        }
        // 尝试获取 output 或 content 字段
        if (outcome instanceof Map) {
            @SuppressWarnings("unchecked")
            Map<String, Object> map = (Map<String, Object>) outcome;
            Object output = map.get("output");
            if (output != null) {
                return output.toString();
            }
            Object content = map.get("content");
            if (content != null) {
                return content.toString();
            }
        }
        return outcome.toString();
    }

    /**
     * 从 messages 列表提取最后一条消息
     */
    private String extractLastMessage(Object messages) {
        if (messages instanceof java.util.List<?> list && !list.isEmpty()) {
            Object last = list.get(list.size() - 1);
            return extractContent(last);
        }
        return "";
    }

    /**
     * 格式化 SSE 数据
     * 注意：Spring WebFlux 的 TEXT_EVENT_STREAM_VALUE 会自动添加 "data:" 前缀
     * 所以这里只需要返回 JSON 内容，不需要手动添加 "data:" 前缀和换行符
     */
    private String formatSSE(String type, String content, String sessionId) {
        StringBuilder sb = new StringBuilder();
        sb.append("{\"type\":\"").append(type).append("\"");
        if (content != null) {
            sb.append(",\"content\":\"").append(escapeJson(content)).append("\"");
        }
        if (sessionId != null) {
            sb.append(",\"session_id\":\"").append(sessionId).append("\"");
        }
        sb.append("}");
        return sb.toString();
    }

    /**
     * 转义 JSON 字符串
     */
    private String escapeJson(String text) {
        if (text == null) return "";
        return text
                .replace("\\", "\\\\")
                .replace("\"", "\\\"")
                .replace("\n", "\\n")
                .replace("\r", "\\r")
                .replace("\t", "\\t");
    }

    /**
     * 确保会话已持久化
     * 
     * @param sessionId 会话ID
     * @param threadId 线程ID
     * @return 会话ID（conversationId），如果持久化服务不可用则返回 null
     */
    private String ensureConversationPersisted(String sessionId, String threadId) {
        if (conversationService == null) {
            return null;
        }
        
        // 检查是否已有持久化的会话
        String conversationId = sessionConversationMap.get(sessionId);
        if (conversationId != null) {
            return conversationId;
        }
        
        // 创建新的持久化会话
        try {
            String userId = LoginContext.getUserId();
            Conversation conversation = conversationService.createConversation(
                    userId != null ? userId : "anonymous",
                    threadId,
                    null
            );
            sessionConversationMap.put(sessionId, conversation.getId());
            return conversation.getId();
        } catch (Exception e) {
            logger.warn("创建持久化会话失败: sessionId={}", sessionId, e);
            return null;
        }
    }

    /**
     * 异步保存消息
     * 
     * @param conversationId 会话ID
     * @param role 消息角色
     * @param content 消息内容
     */
    private void saveMessageAsync(String conversationId, MessageRole role, String content) {
        if (messagePersistenceService == null || conversationId == null) {
            return;
        }
        
        try {
            String tenantId = LoginContext.getTenantId();
            com.alibaba.assistant.agent.extension.conversation.model.Message message = 
                    com.alibaba.assistant.agent.extension.conversation.model.Message.builder()
                    .id(UUID.randomUUID().toString())
                    .tenantId(tenantId)
                    .conversationId(conversationId)
                    .role(role)
                    .contentType(ContentType.TEXT)
                    .content(content)
                    .createdAt(LocalDateTime.now())
                    .build();
            
            messagePersistenceService.saveMessageAsync(message);
        } catch (Exception e) {
            logger.warn("异步保存消息失败: conversationId={}, role={}", conversationId, role, e);
        }
    }
}
