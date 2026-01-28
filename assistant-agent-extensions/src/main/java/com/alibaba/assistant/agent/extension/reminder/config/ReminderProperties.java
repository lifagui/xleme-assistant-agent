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
package com.alibaba.assistant.agent.extension.reminder.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * 提醒模块配置属性
 *
 * @author Assistant Agent Team
 * @since 1.0.0
 */
@Data
@ConfigurationProperties(prefix = "assistant.reminder")
public class ReminderProperties {

    /**
     * 是否启用提醒模块
     */
    private boolean enabled = true;

    /**
     * 内部 API 配置
     */
    private InternalApi internalApi = new InternalApi();

    @Data
    public static class InternalApi {
        /**
         * 基础地址
         */
        private String baseUrl = "http://localhost:8080";

        /**
         * 用户检查接口路径
         */
        private String userCheckPath = "/internal/user/check";

        /**
         * 短信发送接口路径
         */
        private String smsSendPath = "/internal/sms/send";

        /**
         * 访问 Token
         */
        private String token = "internal_secret_token_123";

        /**
         * 获取完整的用户检查 URL
         */
        public String getUserCheckUrl() {
            return baseUrl + userCheckPath;
        }

        /**
         * 获取完整的短信发送 URL
         */
        public String getSmsSendUrl() {
            return baseUrl + smsSendPath;
        }
    }
}
