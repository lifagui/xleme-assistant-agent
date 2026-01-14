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

package com.alibaba.assistant.agent.extension.learning.interceptor;

import com.alibaba.assistant.agent.extension.learning.internal.DefaultLearningContext;
import com.alibaba.assistant.agent.extension.learning.internal.DefaultLearningTask;
import com.alibaba.assistant.agent.extension.learning.model.LearningContext;
import com.alibaba.assistant.agent.extension.learning.model.LearningResult;
import com.alibaba.assistant.agent.extension.learning.model.LearningTask;
import com.alibaba.assistant.agent.extension.learning.model.LearningTriggerContext;
import com.alibaba.assistant.agent.extension.learning.model.LearningTriggerSource;
import com.alibaba.assistant.agent.extension.learning.model.ToolCallRecord;
import com.alibaba.assistant.agent.extension.learning.model.*;
import com.alibaba.assistant.agent.extension.learning.spi.LearningExecutor;
import com.alibaba.assistant.agent.extension.learning.spi.LearningStrategy;
import com.alibaba.cloud.ai.graph.agent.interceptor.ToolCallHandler;
import com.alibaba.cloud.ai.graph.agent.interceptor.ToolCallRequest;
import com.alibaba.cloud.ai.graph.agent.interceptor.ToolCallResponse;
import com.alibaba.cloud.ai.graph.agent.interceptor.ToolInterceptor;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Instant;
import java.util.*;

/**
 * 工具调用学习拦截器
 * 拦截工具调用，收集工具使用信息并触发学习
 *
 * @author Assistant Agent Team
 * @since 1.0.0
 */
public class LearningToolInterceptor extends ToolInterceptor {

	private static final Logger log = LoggerFactory.getLogger(LearningToolInterceptor.class);

	private final LearningExecutor learningExecutor;

	private final LearningStrategy learningStrategy;

	private final String learningType;

	private final Set<String> includedTools;

	private final ObjectMapper objectMapper;

	public LearningToolInterceptor(LearningExecutor learningExecutor, LearningStrategy learningStrategy,
			String learningType, Set<String> includedTools) {
		this.learningExecutor = learningExecutor;
		this.learningStrategy = learningStrategy;
		this.learningType = learningType != null ? learningType : "tool_usage";
		this.includedTools = includedTools != null ? includedTools : new HashSet<>();
		this.objectMapper = new ObjectMapper();
	}

	@Override
	public ToolCallResponse interceptToolCall(ToolCallRequest request, ToolCallHandler handler) {
		// Check if this tool should be monitored for learning
		if (!includedTools.isEmpty() && !includedTools.contains(request.getToolName())) {
			// Not in included list, just pass through
			return handler.call(request);
		}

		Instant startTime = Instant.now();
		long startMillis = System.currentTimeMillis();

		log.debug("LearningToolInterceptor#interceptToolCall - reason=intercepting tool call, toolName={}",
				request.getToolName());

		// Execute the tool call
		ToolCallResponse response = null;
		Exception executionError = null;
		try {
			response = handler.call(request);
		}
		catch (Exception e) {
			executionError = e;
			log.error("LearningToolInterceptor#interceptToolCall - reason=tool call failed, toolName={}",
					request.getToolName(), e);
			throw e;
		}
		finally {
			// Always try to learn, even on failure
			try {
				performLearning(request, response, executionError, startTime, startMillis);
			}
			catch (Exception learningError) {
				// Learning failure should not affect tool execution
				log.error(
						"LearningToolInterceptor#interceptToolCall - reason=learning failed for tool call, toolName={}",
						request.getToolName(), learningError);
			}
		}

		return response;
	}

	@Override
	public String getName() {
		return "LearningToolInterceptor";
	}

	/**
	 * 执行学习过程
	 */
	private void performLearning(ToolCallRequest request, ToolCallResponse response, Exception error,
			Instant startTime, long startMillis) {

		try {
			long duration = System.currentTimeMillis() - startMillis;

			// Parse arguments from JSON string to Map
			Map<String, Object> arguments = new HashMap<>();
			try {
				if (request.getArguments() != null && !request.getArguments().isEmpty()) {
					@SuppressWarnings("unchecked")
					Map<String, Object> parsed = objectMapper.readValue(request.getArguments(), Map.class);
					arguments = parsed;
				}
			}
			catch (Exception e) {
				log.warn(
						"LearningToolInterceptor#performLearning - reason=failed to parse arguments, using empty map",
						e);
			}

			// Build tool call record
			ToolCallRecord.Builder recordBuilder = ToolCallRecord.builder()
				.toolName(request.getToolName())
				.arguments(arguments)
				.timestamp(startTime)
				.duration(duration);

			if (error == null && response != null) {
				recordBuilder.success(true).result(response.getResult());
			}
			else {
				recordBuilder.success(false).errorMessage(error != null ? error.getMessage() : "Unknown error");
			}

			ToolCallRecord toolRecord = recordBuilder.build();

			// Build learning context
			List<ToolCallRecord> toolRecords = new ArrayList<>();
			toolRecords.add(toolRecord);

			LearningContext context = DefaultLearningContext.builder()
				.toolCallRecords(toolRecords)
				.triggerSource(LearningTriggerSource.TOOL_INTERCEPTOR)
				.build();

			// Build trigger context
			LearningTriggerContext triggerContext = LearningTriggerContext.builder()
				.source(LearningTriggerSource.TOOL_INTERCEPTOR)
				.context(context)
				.build();

			// Check if should trigger learning
			if (!learningStrategy.shouldTriggerLearning(triggerContext)) {
				log.debug(
						"LearningToolInterceptor#performLearning - reason=strategy decided not to trigger learning, toolName={}",
						request.getToolName());
				return;
			}

			// Build learning task
			LearningTask task = DefaultLearningTask.builder()
				.learningType(learningType)
				.triggerSource(LearningTriggerSource.TOOL_INTERCEPTOR)
				.context(context)
				.build();

			// Execute learning (async or sync)
			if (learningStrategy.shouldExecuteAsync(task)) {
				log.debug(
						"LearningToolInterceptor#performLearning - reason=executing learning asynchronously, toolName={}, taskId={}",
						request.getToolName(), task.getId());
				learningExecutor.executeAsync(task).exceptionally(ex -> {
					log.error(
							"LearningToolInterceptor#performLearning - reason=async learning execution failed, toolName={}, taskId={}",
							request.getToolName(), task.getId(), ex);
					return null;
				});
			}
			else {
				log.debug(
						"LearningToolInterceptor#performLearning - reason=executing learning synchronously, toolName={}, taskId={}",
						request.getToolName(), task.getId());
				LearningResult result = learningExecutor.execute(task);
				if (!result.isSuccess()) {
					log.warn(
							"LearningToolInterceptor#performLearning - reason=learning execution failed, toolName={}, taskId={}, failureReason={}",
							request.getToolName(), task.getId(), result.getFailureReason());
				}
			}
		}
		catch (Exception e) {
			// Learning failure should not affect tool execution
			log.error(
					"LearningToolInterceptor#performLearning - reason=learning process failed, toolName={}",
					request.getToolName(), e);
		}
	}

}

