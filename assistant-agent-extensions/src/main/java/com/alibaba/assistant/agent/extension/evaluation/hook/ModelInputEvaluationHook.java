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
import com.alibaba.assistant.agent.extension.evaluation.config.CodeactEvaluationResultAttacher;
import com.alibaba.assistant.agent.evaluation.EvaluationService;
import com.alibaba.assistant.agent.evaluation.model.EvaluationContext;
import com.alibaba.assistant.agent.evaluation.model.EvaluationSuite;
import com.alibaba.cloud.ai.graph.OverAllState;
import com.alibaba.cloud.ai.graph.RunnableConfig;
import com.alibaba.cloud.ai.graph.agent.hook.HookPosition;
import com.alibaba.cloud.ai.graph.agent.hook.HookPositions;
import com.alibaba.cloud.ai.graph.agent.hook.JumpTo;
import com.alibaba.cloud.ai.graph.agent.hook.ModelHook;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

/**
 * 模型输入评估 Hook
 * 统一处理 React 阶段和 CodeAct 阶段的模型输入增强与评估
 *
 * @author Assistant Agent Team
 */
@HookPositions(HookPosition.BEFORE_MODEL)
public class ModelInputEvaluationHook extends ModelHook {

	private static final Logger log = LoggerFactory.getLogger(ModelInputEvaluationHook.class);

	private final EvaluationService evaluationService;
	private final CodeactEvaluationContextFactory contextFactory;
	private final CodeactEvaluationResultAttacher resultAttacher;
	private final String phase; // "REACT" or "CODEACT"
	private final String suiteId;

	public ModelInputEvaluationHook(
			EvaluationService evaluationService,
			CodeactEvaluationContextFactory contextFactory,
			CodeactEvaluationResultAttacher resultAttacher,
			String phase,
			String suiteId) {
		this.evaluationService = evaluationService;
		this.contextFactory = contextFactory;
		this.resultAttacher = resultAttacher;
		this.phase = phase;
		this.suiteId = suiteId;
	}

	@Override
	public String getName() {
		return "ModelInputEvaluationHook-" + phase;
	}

	@Override
	public List<JumpTo> canJumpTo() {
		return List.of();
	}

	@Override
	public CompletableFuture<Map<String, Object>> beforeModel(OverAllState state, RunnableConfig config) {
		log.info("ModelInputEvaluationHook#beforeModel - reason=开始模型输入评估, phase={}", phase);

		try {
			// 构造评估上下文
			EvaluationContext context;
			if ("CODEACT".equals(phase)) {
				// CodeAct 阶段通常关注代码生成任务
				// 从 state 中提取代码生成相关参数
				String codeTaskDescription = extractCodeTaskDescription(state);
				String targetLanguage = extractTargetLanguage(state);
				Map<String, Object> environmentConstraints = extractEnvironmentConstraints(state);

				context = contextFactory.createCodeGenerationInputContext(state, codeTaskDescription, targetLanguage, environmentConstraints);
			} else {
				// React 阶段关注用户输入和对话历史
				context = contextFactory.createInputRoutingContext(state, config);
			}

			// 加载 Suite
			EvaluationSuite suite = evaluationService.loadSuite(suiteId);

			if (suite == null) {
				log.warn("ModelInputEvaluationHook#beforeModel - reason=未找到评估套件, suiteId={}", suiteId);
				return CompletableFuture.completedFuture(Map.of());
			}

			// 执行评估 (使用 evaluateAsync)
			return evaluationService.evaluateAsync(suite, context)
					.thenApply(result -> {
						log.info("ModelInputEvaluationHook#beforeModel - reason=评估完成, phase={}", phase);

						// 注入结果到 Messages (通过 resultAttacher)
						// 注意：这里我们需要确保 resultAttacher 能处理不同阶段的注入逻辑
						if ("CODEACT".equals(phase)) {
							return resultAttacher.attachCodeGenerationInputResult(state, result);
						} else {
							return resultAttacher.attachInputRoutingResult(state, result);
						}
					});

		} catch (Exception e) {
			log.error("ModelInputEvaluationHook#beforeModel - reason=评估失败", e);
			return CompletableFuture.completedFuture(Map.of());
		}
	}

	private String extractCodeTaskDescription(OverAllState state) {
		// 尝试从 state 中获取代码任务描述
		// 这取决于 CodeGeneratorSubAgent 如何传递参数
		// 假设它在 state 中有一个 "requirement" 或 "task" 字段
		return state.value("requirement", String.class).orElse("");
	}

	private String extractTargetLanguage(OverAllState state) {
		return state.value("language", String.class).orElse("python");
	}

	@SuppressWarnings("unchecked")
	private Map<String, Object> extractEnvironmentConstraints(OverAllState state) {
		return state.value("environmentConstraints", Map.class).orElse(Map.of());
	}
}
