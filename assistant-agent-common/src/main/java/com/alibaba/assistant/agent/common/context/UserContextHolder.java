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
 * 注意：这是一个简化方案，适用于单用户或低并发场景。
 * 对于高并发多用户场景，建议使用更复杂的上下文传递机制（如 Reactor Context）。
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
    public record UserContext(String userId, String tenantId) {}

    /**
     * 当前活跃的用户上下文（volatile 保证可见性）
     */
    private static volatile UserContext currentUserContext = null;

    /**
     * 设置当前用户上下文
     *
     * @param userId 用户ID
     * @param tenantId 租户ID
     */
    public static void setContext(String userId, String tenantId) {
        if (userId != null || tenantId != null) {
            currentUserContext = new UserContext(userId, tenantId);
            logger.debug("设置用户上下文: userId={}, tenantId={}", userId, tenantId);
        }
    }

    /**
     * 获取当前用户上下文
     *
     * @return 用户上下文，如果未设置则返回 null
     */
    public static UserContext getContext() {
        return currentUserContext;
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
        UserContext ctx = currentUserContext;
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
        UserContext ctx = currentUserContext;
        if (ctx != null && ctx.tenantId() != null && !ctx.tenantId().isEmpty()) {
            logger.debug("从备用上下文获取 tenantId: {}", ctx.tenantId());
            return ctx.tenantId();
        }
        return null;
    }

    /**
     * 清除当前用户上下文
     */
    public static void clear() {
        currentUserContext = null;
        logger.debug("清除用户上下文");
    }
}
