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

import lombok.Data;

/**
 * 登录上下文，用于存储当前请求的租户ID和用户ID
 * <p>
 * 使用 ThreadLocal 存储，在 Filter 中设置，在请求结束时清理
 * </p>
 *
 * <p>使用示例：</p>
 * <pre>
 * // 在 Filter 中设置
 * LoginContext context = new LoginContext();
 * context.setTenantId(request.getHeader("X-Tenant-Id"));
 * context.setUserId(request.getHeader("X-User-Id"));
 * LoginContext.set(context);
 *
 * // 在业务代码中获取
 * String tenantId = LoginContext.getTenantId();
 * String userId = LoginContext.getUserId();
 *
 * // 请求结束时清理
 * LoginContext.clear();
 * </pre>
 *
 * @author Assistant Agent Team
 * @since 1.0.0
 */
@Data
public class LoginContext {

    private static final ThreadLocal<LoginContext> CONTEXT = new ThreadLocal<>();

    /**
     * 租户ID，来自 X-Tenant-Id 请求头
     */
    private String tenantId;

    /**
     * 用户ID，来自 X-User-Id 请求头
     */
    private String userId;

    /**
     * 设置当前线程的登录上下文
     *
     * @param context 登录上下文
     */
    public static void set(LoginContext context) {
        CONTEXT.set(context);
    }

    /**
     * 获取当前线程的登录上下文
     *
     * @return 登录上下文，如果未设置则返回 null
     */
    public static LoginContext get() {
        return CONTEXT.get();
    }

    /**
     * 获取当前租户ID
     *
     * @return 租户ID，如果未设置则返回 null
     */
    public static String getTenantId() {
        LoginContext ctx = CONTEXT.get();
        return ctx != null ? ctx.tenantId : null;
    }

    /**
     * 获取当前用户ID
     *
     * @return 用户ID，如果未设置则返回 null
     */
    public static String getUserId() {
        LoginContext ctx = CONTEXT.get();
        return ctx != null ? ctx.userId : null;
    }

    /**
     * 清理当前线程的登录上下文
     * <p>
     * 必须在请求结束时调用，防止内存泄漏
     * </p>
     */
    public static void clear() {
        CONTEXT.remove();
    }
}
