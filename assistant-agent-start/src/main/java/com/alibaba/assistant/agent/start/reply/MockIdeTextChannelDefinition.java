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
package com.alibaba.assistant.agent.start.reply;

import com.alibaba.assistant.agent.extension.reply.model.ChannelExecutionContext;
import com.alibaba.assistant.agent.extension.reply.model.ParameterSchema;
import com.alibaba.assistant.agent.extension.reply.model.ReplyResult;
import com.alibaba.assistant.agent.extension.reply.spi.ReplyChannelDefinition;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.Map;

/**
 * Mock IDE文本渠道定义
 * 向IDE发送纯文本消息（用于演示和测试）
 *
 * @author Assistant Agent Team
 */
@Component
public class MockIdeTextChannelDefinition implements ReplyChannelDefinition {

    private static final Logger log = LoggerFactory.getLogger(MockIdeTextChannelDefinition.class);

    @Override
    public String getChannelCode() {
        return "IDE_TEXT";
    }

    @Override
    public String getDescription() {
        return "Send plain text message to IDE";
    }

    @Override
    public ParameterSchema getSupportedParameters() {
        return ParameterSchema.builder()
                .parameter("text", ParameterSchema.ParameterType.STRING, true, "Message text")
                .build();
    }

    @Override
    public ReplyResult execute(ChannelExecutionContext context, Map<String, Object> params) {
        try {
            String text = (String) params.get("text");

            if (text == null || text.trim().isEmpty()) {
                log.warn("MockIdeTextChannelDefinition#execute - reason=text is empty, toolName={}", context.getToolName());
                return ReplyResult.failure("Text message is empty");
            }

            // TODO: 实际的IDE文本输出逻辑
            // 当前只是记录日志作为示例
            log.info("MockIdeTextChannelDefinition#execute - reason=text sent, toolName={}, text={}", context.getToolName(), text);

            ReplyResult result = ReplyResult.success("Text displayed in IDE");
            result.putMetadata("text", text);
            result.putMetadata("toolName", context.getToolName());

            return result;
        } catch (Exception e) {
            log.error("MockIdeTextChannelDefinition#execute - reason=execution failed, toolName={}, error={}",
                    context.getToolName(), e.getMessage(), e);
            return ReplyResult.failure("Failed to send text: " + e.getMessage());
        }
    }
}

