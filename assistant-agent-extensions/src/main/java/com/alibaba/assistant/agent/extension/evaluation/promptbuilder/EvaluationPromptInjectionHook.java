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
package com.alibaba.assistant.agent.extension.evaluation.promptbuilder;

import com.alibaba.cloud.ai.graph.OverAllState;
import com.alibaba.cloud.ai.graph.RunnableConfig;
import com.alibaba.cloud.ai.graph.agent.hook.HookPosition;
import com.alibaba.cloud.ai.graph.agent.hook.HookPositions;
import com.alibaba.cloud.ai.graph.agent.hook.JumpTo;
import com.alibaba.cloud.ai.graph.agent.hook.ModelHook;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.chat.messages.AssistantMessage;
import org.springframework.ai.chat.messages.Message;
import org.springframework.ai.chat.messages.ToolResponseMessage;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * 评估结果 Prompt 注入 Hook
 *
 * <p>该 Hook 在评估完成后执行，根据评估结果调用注册的 {@link EvaluationPromptGuidanceProvider}
 * 生成相应的 prompt 指导并注入到 messages 中。
 *
 * <p>工作流程：
 * <ol>
 *   <li>从 messages 中查找最近一条评估结果（__evaluation_injection__工具的响应）</li>
 *   <li>解析评估结果 XML，提取各 criterion 的值</li>
 *   <li>调用所有匹配当前阶段的 {@link EvaluationPromptGuidanceProvider} 生成 prompt 指导</li>
 *   <li>将 prompt 作为 tool response 消息注入到 messages 中</li>
 * </ol>
 *
 * <p>这是 evaluation 和 prompt builder 对接的**机制层**，具体的 prompt 内容由
 * {@link EvaluationPromptGuidanceProvider} 实现类提供。
 *
 * @author Assistant Agent Team
 * @since 1.0.0
 */
@HookPositions(HookPosition.BEFORE_MODEL)
public class EvaluationPromptInjectionHook extends ModelHook {

    private static final Logger log = LoggerFactory.getLogger(EvaluationPromptInjectionHook.class);

    /**
     * 阶段标识: REACT 或 CODEACT
     */
    private final String phase;

    /**
     * 注册的 prompt 指导提供者列表
     */
    private final List<EvaluationPromptGuidanceProvider> guidanceProviders;

    /**
     * 评估注入工具名称（由 CodeactEvaluationResultAttacher 使用）
     */
    private static final String EVALUATION_TOOL_NAME = "__evaluation_injection__";

    /**
     * Prompt 指导注入工具名称
     */
    private static final String PROMPT_INJECTION_TOOL_NAME = "__evaluation_prompt_guidance__";

    /**
     * 用于解析 evaluation XML 的正则表达式
     */
    private static final Pattern EVALUATION_PATTERN = Pattern.compile(
            "<evaluation criterion=\"([^\"]+)\" ref-id=\"[^\"]*\">([\\s\\S]*?)</evaluation>");

    /**
     * 构造函数
     *
     * @param phase 阶段标识
     */
    public EvaluationPromptInjectionHook(String phase) {
        this(phase, List.of());
    }

    /**
     * 构造函数
     *
     * @param phase 阶段标识
     * @param guidanceProviders prompt 指导提供者列表
     */
    public EvaluationPromptInjectionHook(String phase, List<EvaluationPromptGuidanceProvider> guidanceProviders) {
        this.phase = phase;
        // 按优先级排序
        this.guidanceProviders = guidanceProviders.stream()
                .filter(p -> phase.equalsIgnoreCase(p.getPhase()))
                .sorted(Comparator.comparingInt(EvaluationPromptGuidanceProvider::getPriority))
                .collect(Collectors.toList());
        log.info("EvaluationPromptInjectionHook#<init> - reason=初始化, phase={}, providerCount={}",
                phase, this.guidanceProviders.size());
    }

    @Override
    public String getName() {
        return "EvaluationPromptInjectionHook-" + phase;
    }

    @Override
    public List<JumpTo> canJumpTo() {
        return List.of();
    }

    @Override
    public CompletableFuture<Map<String, Object>> beforeModel(OverAllState state, RunnableConfig config) {
        log.info("EvaluationPromptInjectionHook#beforeModel - reason=开始处理评估结果prompt注入, phase={}", phase);

        try {
            // 1. 从 messages 中查找最近的评估结果
            Optional<List<Message>> messagesOpt = state.value("messages");
            if (messagesOpt.isEmpty()) {
                log.debug("EvaluationPromptInjectionHook#beforeModel - reason=messages为空，跳过prompt注入, phase={}", phase);
                return CompletableFuture.completedFuture(Map.of());
            }

            List<Message> messages = messagesOpt.get();

            // 2. 从后往前查找最近的评估结果 ToolResponseMessage
            String evaluationXml = findLatestEvaluationResult(messages);
            if (evaluationXml == null || evaluationXml.isBlank()) {
                log.debug("EvaluationPromptInjectionHook#beforeModel - reason=未找到评估结果消息，跳过prompt注入, phase={}", phase);
                return CompletableFuture.completedFuture(Map.of());
            }

            log.debug("EvaluationPromptInjectionHook#beforeModel - reason=找到评估结果XML, phase={}, xmlLength={}",
                    phase, evaluationXml.length());

            // 3. 解析评估结果 XML，提取 criterion 值
            Map<String, String> evaluationResults = parseEvaluationXml(evaluationXml);

            log.debug("EvaluationPromptInjectionHook#beforeModel - reason=解析评估结果, phase={}, results={}",
                    phase, evaluationResults);

            // 4. 调用所有 provider 生成 prompt 指导
            String promptGuidance = generatePromptGuidance(evaluationResults);

            if (promptGuidance == null || promptGuidance.isBlank()) {
                log.debug("EvaluationPromptInjectionHook#beforeModel - reason=评估结果不需要额外prompt指导, phase={}", phase);
                return CompletableFuture.completedFuture(Map.of());
            }

            // 5. 构建注入消息
            Map<String, Object> updates = new HashMap<>();
            injectPromptGuidanceToMessages(updates, promptGuidance);

            log.info("EvaluationPromptInjectionHook#beforeModel - reason=已注入评估结果prompt指导, phase={}, guidanceLength={}",
                    phase, promptGuidance.length());

            return CompletableFuture.completedFuture(updates);

        } catch (Exception e) {
            log.error("EvaluationPromptInjectionHook#beforeModel - reason=注入评估结果prompt失败", e);
            return CompletableFuture.completedFuture(Map.of());
        }
    }

    /**
     * 从 messages 列表中查找最近的评估结果
     */
    private String findLatestEvaluationResult(List<Message> messages) {
        for (int i = messages.size() - 1; i >= 0; i--) {
            Message msg = messages.get(i);
            if (msg instanceof ToolResponseMessage toolMsg) {
                for (ToolResponseMessage.ToolResponse resp : toolMsg.getResponses()) {
                    if (EVALUATION_TOOL_NAME.equals(resp.name())) {
                        log.debug("EvaluationPromptInjectionHook#findLatestEvaluationResult - reason=找到评估结果, index={}", i);
                        return resp.responseData();
                    }
                }
            }
        }
        return null;
    }

    /**
     * 解析评估结果 XML，提取各 criterion 的值
     */
    private Map<String, String> parseEvaluationXml(String xml) {
        Map<String, String> result = new HashMap<>();

        Matcher matcher = EVALUATION_PATTERN.matcher(xml);
        while (matcher.find()) {
            String criterionName = matcher.group(1);
            String value = matcher.group(2).trim();
            result.put(criterionName, value);
            log.debug("EvaluationPromptInjectionHook#parseEvaluationXml - reason=解析到criterion, name={}, valueLength={}",
                    criterionName, value.length());
        }

        return result;
    }

    /**
     * 调用所有匹配的 provider 生成 prompt 指导
     */
    private String generatePromptGuidance(Map<String, String> evaluationResults) {
        if (guidanceProviders.isEmpty()) {
            log.debug("EvaluationPromptInjectionHook#generatePromptGuidance - reason=没有注册的GuidanceProvider");
            return null;
        }

        StringBuilder sb = new StringBuilder();

        for (EvaluationPromptGuidanceProvider provider : guidanceProviders) {
            try {
                if (!provider.shouldHandle(evaluationResults)) {
                    continue;
                }

                String guidance = provider.generateGuidance(evaluationResults);
                if (guidance != null && !guidance.isBlank()) {
                    if (sb.length() > 0) {
                        sb.append("\n\n");
                    }
                    sb.append(guidance);
                    log.debug("EvaluationPromptInjectionHook#generatePromptGuidance - reason=Provider生成了指导, provider={}, guidanceLength={}",
                            provider.getClass().getSimpleName(), guidance.length());
                }
            } catch (Exception e) {
                log.error("EvaluationPromptInjectionHook#generatePromptGuidance - reason=Provider执行失败, provider={}",
                        provider.getClass().getSimpleName(), e);
            }
        }

        return sb.length() > 0 ? sb.toString() : null;
    }

    /**
     * 将 prompt 指导注入到 messages 中
     */
    private void injectPromptGuidanceToMessages(Map<String, Object> updates, String guidance) {
        String toolCallId = "prompt_" + UUID.randomUUID().toString().substring(0, 8);

        // 创建 AssistantMessage with ToolCall
        AssistantMessage.ToolCall toolCall = new AssistantMessage.ToolCall(
                toolCallId, "function", PROMPT_INJECTION_TOOL_NAME, "{}");
        AssistantMessage assistantMsg = AssistantMessage.builder().content("").toolCalls(List.of(toolCall)).build();

        // 创建 ToolResponseMessage with ToolResponse
        ToolResponseMessage.ToolResponse toolResponse = new ToolResponseMessage.ToolResponse(
                toolCallId, PROMPT_INJECTION_TOOL_NAME, guidance);
        ToolResponseMessage toolMsg = ToolResponseMessage.builder().responses(List.of(toolResponse)).build();

        // 将消息对放入 updates
        List<Message> incrementalMessages = new ArrayList<>();
        incrementalMessages.add(assistantMsg);
        incrementalMessages.add(toolMsg);

        updates.put("messages", incrementalMessages);

        log.debug("EvaluationPromptInjectionHook#injectPromptGuidanceToMessages - reason=已创建prompt指导消息对");
    }
}

