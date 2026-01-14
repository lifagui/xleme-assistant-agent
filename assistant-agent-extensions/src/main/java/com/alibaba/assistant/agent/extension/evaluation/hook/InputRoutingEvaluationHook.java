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
package com.alibaba.assistant.agent.extension.evaluation.hook;

import com.alibaba.assistant.agent.extension.evaluation.config.CodeactEvaluationContextFactory;
import com.alibaba.assistant.agent.extension.evaluation.config.CodeactEvaluationProperties;
import com.alibaba.assistant.agent.extension.evaluation.config.CodeactEvaluationResultAttacher;
import com.alibaba.assistant.agent.evaluation.EvaluationService;
import com.alibaba.assistant.agent.evaluation.model.EvaluationContext;
import com.alibaba.assistant.agent.evaluation.model.EvaluationResult;
import com.alibaba.assistant.agent.evaluation.model.EvaluationSuite;
import com.alibaba.cloud.ai.graph.OverAllState;
import com.alibaba.cloud.ai.graph.RunnableConfig;
import com.alibaba.cloud.ai.graph.agent.hook.AgentHook;
import com.alibaba.cloud.ai.graph.agent.hook.HookPosition;
import com.alibaba.cloud.ai.graph.agent.hook.HookPositions;
import com.alibaba.cloud.ai.graph.agent.hook.JumpTo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.chat.messages.AssistantMessage;
import org.springframework.ai.chat.messages.Message;
import org.springframework.ai.chat.messages.ToolResponseMessage;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

/**
 * 输入路由评估 Hook
 * 在 BEFORE_AGENT 阶段评估用户输入清晰度与路由策略
 *
 * @author Assistant Agent Team
 */
@HookPositions(HookPosition.BEFORE_AGENT)
public class InputRoutingEvaluationHook extends AgentHook {

	private static final Logger log = LoggerFactory.getLogger(InputRoutingEvaluationHook.class);

	private final EvaluationService evaluationService;
	private final CodeactEvaluationContextFactory contextFactory;
	private final CodeactEvaluationResultAttacher resultAttacher;
	private final CodeactEvaluationProperties properties;

	public InputRoutingEvaluationHook(
			EvaluationService evaluationService,
			CodeactEvaluationContextFactory contextFactory,
			CodeactEvaluationResultAttacher resultAttacher,
			CodeactEvaluationProperties properties) {
		this.evaluationService = evaluationService;
		this.contextFactory = contextFactory;
		this.resultAttacher = resultAttacher;
		this.properties = properties;
	}

	@Override
	public String getName() {
		return "InputRoutingEvaluationHook";
	}

	@Override
	public List<JumpTo> canJumpTo() {
		return List.of();
	}

	@Override
	public CompletableFuture<Map<String, Object>> beforeAgent(OverAllState state, RunnableConfig config) {
		log.info("InputRoutingEvaluationHook#beforeAgent - reason=开始输入路由评估");

		try {
			// 检查是否启用
			if (!properties.getInputRouting().isEnabled()) {
				log.debug("InputRoutingEvaluationHook#beforeAgent - reason=输入路由评估未启用，跳过");
				return CompletableFuture.completedFuture(Map.of());
			}

			// 构造评估上下文
			EvaluationContext context = contextFactory.createInputRoutingContext(state, config);

			// 加载或获取 Suite
			String suiteId = properties.getInputRouting().getSuiteId();
			EvaluationSuite suite = evaluationService.loadSuite(suiteId);

			if (suite == null) {
				log.warn("InputRoutingEvaluationHook#beforeAgent - reason=未找到输入路由评估套件, suiteId={}", suiteId);
				return CompletableFuture.completedFuture(Map.of());
			}

			// 执行评估
			EvaluationResult result;
			if (properties.isAsync()) {
				result = evaluationService.evaluateAsync(suite, context)
						.get(properties.getTimeoutMs(), TimeUnit.MILLISECONDS);
			} else {
				result = evaluationService.evaluate(suite, context);
			}

			log.info("InputRoutingEvaluationHook#beforeAgent - reason=输入路由评估完成, suiteId={}, statistics={}",
					result.getSuiteId(), result.getStatistics());

			// 写入状态
			Map<String, Object> updates = resultAttacher.attachInputRoutingResult(state, result);

			// 🔥 核心：将评估结果注入到 messages 中
			Map<String, Object> messageUpdates = injectEvaluationResultToMessages(state, result);

			// 合并 updates
			if (messageUpdates != null && !messageUpdates.isEmpty()) {
				// 注意：这里需要小心处理，因为 updates 是不可变的 Map.of 创建的
				// 实际上 resultAttacher 返回的 updates 可能只包含 evaluation 相关的字段
				// 而 messageUpdates 包含 messages 字段
				// 我们需要返回一个新的 Map 包含所有更新
				java.util.HashMap<String, Object> allUpdates = new java.util.HashMap<>(updates);
				allUpdates.putAll(messageUpdates);
				return CompletableFuture.completedFuture(allUpdates);
			}

			return CompletableFuture.completedFuture(updates);

		} catch (Exception e) {
			log.error("InputRoutingEvaluationHook#beforeAgent - reason=输入路由评估失败", e);
			return CompletableFuture.completedFuture(Map.of());
		}
	}

	/**
	 * 将评估结果注入到 messages 中
	 * 使用 AssistantMessage + ToolResponseMessage 配对方式
	 */
	@SuppressWarnings("unchecked")
	private Map<String, Object> injectEvaluationResultToMessages(OverAllState state, EvaluationResult result) {
		log.info("InputRoutingEvaluationHook#injectEvaluationResultToMessages - reason=开始注入评估结果到messages");

		try {
			Optional<Object> messagesOpt = state.value("messages");
			if (messagesOpt.isEmpty()) {
				log.warn("InputRoutingEvaluationHook#injectEvaluationResultToMessages - reason=state中没有messages，跳过");
				return Map.of();
			}

			List<Message> messages = (List<Message>) messagesOpt.get();

			// 检查是否已经注入过评估结果（避免重复注入）
			for (Message msg : messages) {
				if (msg instanceof ToolResponseMessage toolMsg) {
					for (ToolResponseMessage.ToolResponse response : toolMsg.getResponses()) {
						if ("input_routing_evaluation_injection".equals(response.name())) {
							log.info("InputRoutingEvaluationHook#injectEvaluationResultToMessages - reason=检测到已注入评估结果，跳过重复注入");
							return Map.of();
						}
					}
				}
			}

			// 构建评估结果内容
			String evaluationContent = buildEvaluationContent(result);

			// 🔥 构造 AssistantMessage + ToolResponseMessage 配对
			String toolCallId = "eval_input_" + UUID.randomUUID().toString().substring(0, 8);

			// 1. AssistantMessage with toolCall
			AssistantMessage assistantMessage = AssistantMessage.builder()
					.toolCalls(List.of(
							new AssistantMessage.ToolCall(
									toolCallId,
									"function",
									"input_routing_evaluation_injection",
									"{}"  // 空参数
							)
					))
					.build();

			// 2. ToolResponseMessage with response
			ToolResponseMessage.ToolResponse toolResponse = new ToolResponseMessage.ToolResponse(
					toolCallId,
					"input_routing_evaluation_injection",
					evaluationContent
			);

			ToolResponseMessage toolResponseMessage = ToolResponseMessage.builder()
					.responses(List.of(toolResponse))
					.build();

			log.info("InputRoutingEvaluationHook#injectEvaluationResultToMessages - reason=已注入评估结果到messages");

			// 🔥 返回配对的两条消息，框架会自动追加到现有 messages
			return Map.of("messages", List.of(assistantMessage, toolResponseMessage));

		} catch (Exception e) {
			log.error("InputRoutingEvaluationHook#injectEvaluationResultToMessages - reason=注入评估结果失败", e);
			return Map.of();
		}
	}

	/**
	 * 构建评估结果内容
	 */
	private String buildEvaluationContent(EvaluationResult result) {
		StringBuilder content = new StringBuilder();
		content.append("=== 输入路由评估结果 ===\n\n");

		if (result.getCriteriaResults() != null) {
			result.getCriteriaResults().forEach((key, criterionResult) -> {
				content.append("🔍 ").append(key).append(": ");
				if (criterionResult.getValue() != null) {
					content.append(criterionResult.getValue());
				} else {
					content.append("N/A");
				}
				content.append("\n");
			});
		}

		return content.toString();
	}
}
