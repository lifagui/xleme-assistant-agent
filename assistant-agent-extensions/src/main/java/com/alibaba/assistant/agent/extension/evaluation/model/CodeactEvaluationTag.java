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
package com.alibaba.assistant.agent.extension.evaluation.model;

/**
 * Codeact Evaluation 上下文字段标签常量
 * 定义在构造 EvaluationContext 时使用的统一 key
 *
 * @author Assistant Agent Team
 */
public final class CodeactEvaluationTag {

	// ===== Input 字段 =====

	/**
	 * 用户输入内容
	 */
	public static final String INPUT_USER_INPUT = "userInput";

	/**
	 * 会话历史
	 */
	public static final String INPUT_CONVERSATION_HISTORY = "conversationHistory";

	/**
	 * Agent 名称
	 */
	public static final String INPUT_AGENT_NAME = "agentName";

	/**
	 * 工具调用计划
	 */
	public static final String INPUT_TOOL_CALLS = "toolCalls";

	/**
	 * 代码生成任务描述
	 */
	public static final String INPUT_CODE_TASK_DESCRIPTION = "codeTaskDescription";

	/**
	 * 目标编程语言
	 */
	public static final String INPUT_TARGET_LANGUAGE = "targetLanguage";

	/**
	 * 环境约束信息
	 */
	public static final String INPUT_ENVIRONMENT_CONSTRAINTS = "environmentConstraints";

	/**
	 * 历史代码经验
	 */
	public static final String INPUT_CODE_EXPERIENCE = "codeExperience";

	// ===== ExecutionResult 字段 =====

	/**
	 * 知识检索命中结果
	 */
	public static final String EXEC_KNOWLEDGE_SEARCH_HITS = "knowledgeSearchHits";

	/**
	 * 模型输出内容
	 */
	public static final String EXEC_MODEL_OUTPUT = "modelOutput";

	/**
	 * 待执行代码片段
	 */
	public static final String EXEC_CODE_SNIPPET = "codeSnippet";

	/**
	 * 代码执行结果
	 */
	public static final String EXEC_CODE_EXEC_RESULT = "codeExecResult";

	/**
	 * 代码执行异常信息
	 */
	public static final String EXEC_CODE_EXEC_ERROR = "codeExecError";

	/**
	 * 代码执行运行时间（毫秒）
	 */
	public static final String EXEC_CODE_EXEC_DURATION_MS = "codeExecDurationMs";

	/**
	 * 工具执行轨迹
	 */
	public static final String EXEC_TOOL_EXECUTION_TRACE = "toolExecutionTrace";

	/**
	 * 完整对话轨迹
	 */
	public static final String EXEC_CONVERSATION_TRACE = "conversationTrace";

	// ===== OverAllState 写入字段（命名空间） =====

	/**
	 * 评估结果在 OverAllState 中的根路径
	 */
	public static final String STATE_EVALUATION_ROOT = "evaluation";

	/**
	 * 输入路由评估结果路径
	 */
	public static final String STATE_INPUT_ROUTING = "evaluation.inputRouting";

	/**
	 * 模型输出评估结果路径
	 */
	public static final String STATE_MODEL_OUTPUT = "evaluation.modelOutput";

	/**
	 * 代码生成输入评估结果路径
	 */
	public static final String STATE_CODE_GENERATION_INPUT = "evaluation.codeGenerationInput";

	/**
	 * 代码执行评估结果路径
	 */
	public static final String STATE_CODE_EXECUTION = "evaluation.codeExecution";

	/**
	 * 会话总结评估结果路径
	 */
	public static final String STATE_SESSION_SUMMARY = "evaluation.sessionSummary";

	private CodeactEvaluationTag() {
		// 工具类，禁止实例化
	}
}
