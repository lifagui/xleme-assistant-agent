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
package com.alibaba.assistant.agent.start.filter;

import com.alibaba.assistant.agent.common.context.LoginContext;
import jakarta.servlet.Filter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.io.IOException;

/**
 * 登录上下文过滤器
 * <p>
 * 从 HTTP 请求头中提取租户ID和用户ID，设置到 ThreadLocal 上下文中
 * </p>
 *
 * <p>请求头：</p>
 * <ul>
 *   <li>X-Tenant-Id - 租户ID</li>
 *   <li>X-User-Id - 用户ID</li>
 * </ul>
 *
 * @author Assistant Agent Team
 * @since 1.0.0
 */
@Component
@Order(Ordered.HIGHEST_PRECEDENCE)
public class LoginContextFilter implements Filter {

    private static final Logger logger = LoggerFactory.getLogger(LoginContextFilter.class);

    private static final String HEADER_TENANT_ID = "X-Tenant-Id";
    private static final String HEADER_USER_ID = "X-User-Id";

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {
        
        HttpServletRequest httpRequest = (HttpServletRequest) request;
        
        // 从请求头获取租户ID和用户ID（非强制要求）
        String tenantId = httpRequest.getHeader(HEADER_TENANT_ID);
        String userId = httpRequest.getHeader(HEADER_USER_ID);
        
        // 创建并设置登录上下文
        LoginContext context = new LoginContext();
        context.setTenantId(tenantId);
        context.setUserId(userId);
        LoginContext.set(context);
        
        if (logger.isDebugEnabled()) {
            logger.debug("设置登录上下文: tenantId={}, userId={}, uri={}", 
                    tenantId, userId, httpRequest.getRequestURI());
        }
        
        try {
            chain.doFilter(request, response);
        } finally {
            // 请求结束时清理上下文，防止内存泄漏
            LoginContext.clear();
        }
    }
}
