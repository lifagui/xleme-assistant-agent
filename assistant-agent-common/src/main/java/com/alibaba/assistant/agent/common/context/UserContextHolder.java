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
package com.alibaba.assistant.agent.common.context;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 用户上下文持有器
 * <p>
 * 用于在异步/跨线程场景中传递用户上下文信息。
 * 当 LoginContext（基于 ThreadLocal）在异步线程中不可用时，
 * 可以从此类获取备用的用户上下文。
 * </p>
 * <p>
 * 支持多用户并发场景：使用 sessionId 作为 key 隔离不同用户的上下文。
 * </p>
 *
 * @author Assistant Agent Team
 * @since 1.0.0
 */
public class UserContextHolder {

    private static final Logger logger = LoggerFactory.getLogger(UserContextHolder.class);

    /**
     * 用户上下文信息
     */
    public record UserContext(String userId, String tenantId, long timestamp) {
        public UserContext(String userId, String tenantId) {
            this(userId, tenantId, System.currentTimeMillis());
        }
    }

    /**
     * 按 sessionId 存储用户上下文（支持多用户并发）
     */
    private static final Map<String, UserContext> SESSION_CONTEXT_MAP = new ConcurrentHashMap<>();

    /**
     * 当前线程关联的 sessionId（用于在工具执行时查找对应的用户上下文）
     */
    private static final ThreadLocal<String> CURRENT_SESSION_ID = new ThreadLocal<>();

    /**
     * 设置当前会话的用户上下文
     *
     * @param sessionId 会话ID
     * @param userId 用户ID
     * @param tenantId 租户ID
     */
    public static void setContext(String sessionId, String userId, String tenantId) {
        if (sessionId != null && (userId != null || tenantId != null)) {
            SESSION_CONTEXT_MAP.put(sessionId, new UserContext(userId, tenantId));
            CURRENT_SESSION_ID.set(sessionId);
            logger.debug("设置用户上下文: sessionId={}, userId={}, tenantId={}", sessionId, userId, tenantId);
        }
    }

    /**
     * 设置当前线程关联的 sessionId
     * <p>
     * 在开始处理请求时调用，以便后续工具执行时能找到对应的用户上下文
     * </p>
     *
     * @param sessionId 会话ID
     */
    public static void setCurrentSessionId(String sessionId) {
        CURRENT_SESSION_ID.set(sessionId);
    }

    /**
     * 获取当前线程关联的 sessionId
     *
     * @return sessionId，如果未设置则返回 null
     */
    public static String getCurrentSessionId() {
        return CURRENT_SESSION_ID.get();
    }

    /**
     * 根据 sessionId 获取用户上下文
     *
     * @param sessionId 会话ID
     * @return 用户上下文，如果不存在则返回 null
     */
    public static UserContext getContext(String sessionId) {
        return sessionId != null ? SESSION_CONTEXT_MAP.get(sessionId) : null;
    }

    /**
     * 获取当前会话的用户上下文
     * <p>
     * 优先使用当前线程关联的 sessionId 查找，如果没有则返回最近活跃的上下文
     * </p>
     *
     * @return 用户上下文，如果未设置则返回 null
     */
    public static UserContext getContext() {
        // 优先使用当前线程关联的 sessionId
        String sessionId = CURRENT_SESSION_ID.get();
        if (sessionId != null) {
            UserContext ctx = SESSION_CONTEXT_MAP.get(sessionId);
            if (ctx != null) {
                return ctx;
            }
        }
        // 兜底：返回最近活跃的上下文（按时间戳排序）
        return SESSION_CONTEXT_MAP.values().stream()
                .max((a, b) -> Long.compare(a.timestamp(), b.timestamp()))
                .orElse(null);
    }

    /**
     * 获取用户ID
     * <p>
     * 优先从 LoginContext 获取，如果为空则从备用上下文获取
     * </p>
     *
     * @return 用户ID，如果都为空则返回 null
     */
    public static String getUserId() {
        // 优先从 LoginContext 获取
        String userId = LoginContext.getUserId();
        if (userId != null && !userId.isEmpty()) {
            return userId;
        }
        // 从备用上下文获取
        UserContext ctx = getContext();
        if (ctx != null && ctx.userId() != null && !ctx.userId().isEmpty()) {
            logger.debug("从备用上下文获取 userId: {}", ctx.userId());
            return ctx.userId();
        }
        return null;
    }

    /**
     * 获取租户ID
     * <p>
     * 优先从 LoginContext 获取，如果为空则从备用上下文获取
     * </p>
     *
     * @return 租户ID，如果都为空则返回 null
     */
    public static String getTenantId() {
        // 优先从 LoginContext 获取
        String tenantId = LoginContext.getTenantId();
        if (tenantId != null && !tenantId.isEmpty()) {
            return tenantId;
        }
        // 从备用上下文获取
        UserContext ctx = getContext();
        if (ctx != null && ctx.tenantId() != null && !ctx.tenantId().isEmpty()) {
            logger.debug("从备用上下文获取 tenantId: {}", ctx.tenantId());
            return ctx.tenantId();
        }
        return null;
    }

    /**
     * 清除指定会话的用户上下文
     *
     * @param sessionId 会话ID
     */
    public static void clearSession(String sessionId) {
        if (sessionId != null) {
            SESSION_CONTEXT_MAP.remove(sessionId);
            logger.debug("清除会话上下文: sessionId={}", sessionId);
        }
    }

    /**
     * 清除当前线程关联的 sessionId
     */
    public static void clearCurrentSessionId() {
        CURRENT_SESSION_ID.remove();
    }

    /**
     * 清理过期的会话上下文（超过30分钟未活跃的会话）
     */
    public static void cleanupExpiredSessions() {
        long expirationTime = System.currentTimeMillis() - 30 * 60 * 1000; // 30分钟
        SESSION_CONTEXT_MAP.entrySet().removeIf(entry -> entry.getValue().timestamp() < expirationTime);
        logger.debug("清理过期会话，剩余会话数: {}", SESSION_CONTEXT_MAP.size());
    }
}
