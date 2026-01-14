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

import com.alibaba.cloud.ai.graph.agent.interceptor.ModelRequest;
import com.alibaba.assistant.agent.prompt.PromptBuilder;
import com.alibaba.assistant.agent.prompt.PromptContribution;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;
import java.util.Objects;

/**
 * 基于评估结果的PromptBuilder
 * 根据评估阶段的结果（如模糊/一般/清晰），生成相应的prompt指导
 *
 * <p>使用方式:
 * <ul>
 *   <li>REACT阶段: 当评估结果为"模糊"时，生成要求模型反问用户澄清意图的提示</li>
 *   <li>CODEACT阶段: 当评估结果为"模糊"时，生成使用reply工具反问用户的代码指导</li>
 * </ul>
 *
 * @author Assistant Agent Team
 * @since 1.0.0
 */
public class EvaluationBasedPromptBuilder implements PromptBuilder {

	private static final Logger log = LoggerFactory.getLogger(EvaluationBasedPromptBuilder.class);

	/**
	 * 阶段标识: REACT 或 CODEACT
	 */
	private final String phase;

	/**
	 * 优先级 (较低的值表示较早执行)
	 */
	private final int priority;

	/**
	 * 评估结果在context中的key
	 */
	private static final String EVALUATION_CONTEXT_KEY = "evaluation";

	/**
	 * 模糊度评估criterion名称
	 */
	private static final String FUZZY_CRITERION_NAME = "is_query_fuzzy";

	/**
	 * 路由策略criterion名称
	 */
	private static final String ROUTING_STRATEGY_CRITERION_NAME = "routing_strategy";

	/**
	 * 模糊度值常量
	 */
	public static final String FUZZY_VALUE = "模糊";
	public static final String GENERAL_VALUE = "一般";
	public static final String CLEAR_VALUE = "清晰";

	/**
	 * 路由策略值常量
	 */
	public static final String STRATEGY_CLARIFY = "CLARIFY";
	public static final String STRATEGY_KNOWLEDGE_ENHANCED = "KNOWLEDGE_ENHANCED";
	public static final String STRATEGY_DIRECT_ANSWER = "DIRECT_ANSWER";
	public static final String STRATEGY_FALLBACK = "FALLBACK";

	public EvaluationBasedPromptBuilder(String phase) {
		this(phase, 100); // 默认优先级100，确保在evaluation hook之后执行
	}

	public EvaluationBasedPromptBuilder(String phase, int priority) {
		this.phase = Objects.requireNonNull(phase, "phase must not be null");
		this.priority = priority;
	}

	@Override
	public boolean match(ModelRequest request) {
		// 检查context中是否包含评估结果
		Map<String, Object> context = request.getContext();
		if (context == null || context.isEmpty()) {
			log.debug("EvaluationBasedPromptBuilder#match - reason=无context, phase={}", phase);
			return false;
		}

		// 检查是否有评估结果
		Object evaluation = context.get(EVALUATION_CONTEXT_KEY);
		if (evaluation == null) {
			log.debug("EvaluationBasedPromptBuilder#match - reason=无评估结果, phase={}", phase);
			return false;
		}

		log.debug("EvaluationBasedPromptBuilder#match - reason=匹配成功, phase={}", phase);
		return true;
	}

	@Override
	public PromptContribution build(ModelRequest request) {
		log.info("EvaluationBasedPromptBuilder#build - reason=开始构建评估结果prompt, phase={}", phase);

		Map<String, Object> context = request.getContext();
		Object evaluationObj = context.get(EVALUATION_CONTEXT_KEY);

		if (!(evaluationObj instanceof Map)) {
			log.warn("EvaluationBasedPromptBuilder#build - reason=评估结果格式不正确, phase={}", phase);
			return PromptContribution.empty();
		}

		@SuppressWarnings("unchecked")
		Map<String, Object> evaluation = (Map<String, Object>) evaluationObj;

		// 提取模糊度评估结果
		String fuzzyValue = extractCriterionValue(evaluation, FUZZY_CRITERION_NAME);

		// 提取路由策略
		String routingStrategy = extractCriterionValue(evaluation, ROUTING_STRATEGY_CRITERION_NAME);

		// 根据阶段和评估结果生成对应的prompt
		String promptText = generatePromptForPhaseAndEvaluation(fuzzyValue, routingStrategy);

		if (promptText == null || promptText.isBlank()) {
			log.debug("EvaluationBasedPromptBuilder#build - reason=无需生成额外prompt, phase={}, fuzzy={}, strategy={}",
					phase, fuzzyValue, routingStrategy);
			return PromptContribution.empty();
		}

		log.info("EvaluationBasedPromptBuilder#build - reason=已生成评估结果prompt, phase={}, fuzzy={}, strategy={}",
				phase, fuzzyValue, routingStrategy);

		return PromptContribution.builder()
				.systemTextToAppend(promptText)
				.build();
	}

	@Override
	public int priority() {
		return priority;
	}

	/**
	 * 从评估结果中提取指定criterion的值
	 */
	@SuppressWarnings("unchecked")
	private String extractCriterionValue(Map<String, Object> evaluation, String criterionName) {
		// 尝试从 inputRouting 结果中获取
		Object inputRouting = evaluation.get("inputRouting");
		if (inputRouting instanceof Map) {
			Map<String, Object> inputRoutingMap = (Map<String, Object>) inputRouting;
			Object criteriaResults = inputRoutingMap.get("criteriaResults");
			if (criteriaResults instanceof Map) {
				Map<String, Object> criteriaMap = (Map<String, Object>) criteriaResults;
				Object criterionResult = criteriaMap.get(criterionName);
				if (criterionResult instanceof Map) {
					Map<String, Object> resultMap = (Map<String, Object>) criterionResult;
					Object value = resultMap.get("value");
					if (value != null) {
						return value.toString();
					}
				}
			}
		}

		// 尝试直接从evaluation根下获取
		Object criteriaResults = evaluation.get("criteriaResults");
		if (criteriaResults instanceof Map) {
			Map<String, Object> criteriaMap = (Map<String, Object>) criteriaResults;
			Object criterionResult = criteriaMap.get(criterionName);
			if (criterionResult instanceof Map) {
				Map<String, Object> resultMap = (Map<String, Object>) criterionResult;
				Object value = resultMap.get("value");
				if (value != null) {
					return value.toString();
				}
			}
		}

		return null;
	}

	/**
	 * 根据阶段和评估结果生成对应的prompt
	 */
	private String generatePromptForPhaseAndEvaluation(String fuzzyValue, String routingStrategy) {
		// 优先使用路由策略
		if (STRATEGY_CLARIFY.equals(routingStrategy) || FUZZY_VALUE.equals(fuzzyValue)) {
			return generateClarifyPrompt();
		}

		if (STRATEGY_KNOWLEDGE_ENHANCED.equals(routingStrategy)) {
			return generateKnowledgeEnhancedPrompt();
		}

		if (GENERAL_VALUE.equals(fuzzyValue)) {
			return generateGeneralPrompt();
		}

		// 清晰的输入不需要额外提示
		return null;
	}

	/**
	 * 生成模糊场景的澄清prompt
	 */
	private String generateClarifyPrompt() {
		if ("REACT".equalsIgnoreCase(phase)) {
			return """
					
					【重要：当前用户输入被评估为模糊】
					检测到用户输入意图不够清晰，请按以下方式处理：
					
					1. 不要直接执行任何操作
					2. 使用reply工具向用户提出澄清问题
					3. 在回复中：
					   - 礼貌地告知用户当前无法确定其具体需求
					   - 列出可能的理解方向（2-3个选项）
					   - 请求用户提供更多上下文或明确选择
					
					示例回复格式：
					"您好！我注意到您的问题可能有多种理解方式：
					1. [可能的理解1]
					2. [可能的理解2]
					请问您具体想要的是哪一种？或者可以提供更多详细信息吗？"
					""";
		} else { // CODEACT
			return """
					
					【重要：当前用户输入被评估为模糊】
					检测到用户输入意图不够清晰，请按以下方式生成代码：
					
					1. 直接生成使用reply工具反问用户的代码
					2. 不要尝试执行任何业务逻辑
					3. 代码应该：
					   - 调用reply工具
					   - 礼貌地询问用户具体需求
					   - 列出可能的选项供用户选择
					
					示例代码：
					```python
					def clarify_user_intent():
					    clarification_message = '''
					您好！我注意到您的问题可能有多种理解方式：
					1. [可能的理解1]
					2. [可能的理解2]
					请问您具体想要的是哪一种？或者可以提供更多详细信息吗？
					'''
					    reply(clarification_message)
					```
					""";
		}
	}

	/**
	 * 生成知识增强场景的prompt
	 */
	private String generateKnowledgeEnhancedPrompt() {
		if ("REACT".equalsIgnoreCase(phase)) {
			return """
					
					【知识增强模式】
					检测到当前问题可以通过知识库获取相关信息来回答，请：
					
					1. 使用search工具查询相关知识
					2. 基于检索到的知识组织回复
					3. 确保回复准确、有据可依
					""";
		} else { // CODEACT
			return """
					
					【知识增强模式】
					检测到当前问题可以通过知识库获取相关信息来回答，请在代码中：
					
					1. 使用search函数查询相关知识
					2. 处理和整合检索结果
					3. 基于知识生成准确的回复
					""";
		}
	}

	/**
	 * 生成一般场景的prompt（意图基本清晰但缺少细节）
	 */
	private String generateGeneralPrompt() {
		if ("REACT".equalsIgnoreCase(phase)) {
			return """
					
					【提示：用户输入意图基本清晰】
					用户的意图可以理解，但可能缺少一些具体细节。
					请尝试：
					1. 基于上下文合理推断缺失信息
					2. 如果无法推断，在回复中简要说明并提供帮助
					3. 保持友好和专业的语气
					""";
		} else { // CODEACT
			return """
					
					【提示：用户输入意图基本清晰】
					用户的意图可以理解，但可能缺少一些具体细节。
					请在代码中：
					1. 使用合理的默认值处理缺失信息
					2. 在执行过程中添加适当的异常处理
					3. 如果需要，在回复中说明使用了哪些假设
					""";
		}
	}

}

