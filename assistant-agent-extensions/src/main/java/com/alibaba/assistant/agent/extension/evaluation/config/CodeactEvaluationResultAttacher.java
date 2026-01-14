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
package com.alibaba.assistant.agent.extension.evaluation.config;

import com.alibaba.assistant.agent.extension.evaluation.model.CodeactEvaluationTag;
import com.alibaba.assistant.agent.evaluation.model.EvaluationResult;
import com.alibaba.cloud.ai.graph.OverAllState;
import com.alibaba.assistant.agent.evaluation.model.CriterionResult;
import com.alibaba.assistant.agent.evaluation.model.EvaluationContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.chat.messages.AssistantMessage;
import org.springframework.ai.chat.messages.Message;
import org.springframework.ai.chat.messages.ToolResponseMessage;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Codeact Evaluation 结果附加器
 * 负责将 EvaluationResult 安全地写回 OverAllState
 *
 * @author Assistant Agent Team
 */
public class CodeactEvaluationResultAttacher {

	private static final Logger log = LoggerFactory.getLogger(CodeactEvaluationResultAttacher.class);
	private static final String EVALUATION_TOOL_NAME = "__evaluation_injection__";

	/**
	 * 将输入路由评估结果写入 OverAllState
	 *
	 * @param state Agent 状态
	 * @param result 评估结果
	 * @return 更新的状态 Map
	 */
	public Map<String, Object> attachInputRoutingResult(OverAllState state, EvaluationResult result) {
		log.debug("CodeactEvaluationResultAttacher#attachInputRoutingResult - reason=写入输入路由评估结果");
		return attachResult(state, CodeactEvaluationTag.STATE_INPUT_ROUTING, result);
	}

	/**
	 * 将模型输出评估结果写入 OverAllState
	 *
	 * @param state Agent 状态
	 * @param result 评估结果
	 * @return 更新的状态 Map
	 */
	public Map<String, Object> attachModelOutputResult(OverAllState state, EvaluationResult result) {
		log.debug("CodeactEvaluationResultAttacher#attachModelOutputResult - reason=写入模型输出评估结果");
		return attachResult(state, CodeactEvaluationTag.STATE_MODEL_OUTPUT, result);
	}

	/**
	 * 将代码生成输入评估结果写入 OverAllState（新增）
	 *
	 * @param state Agent 状态
	 * @param result 评估结果
	 * @return 更新的状态 Map
	 */
	public Map<String, Object> attachCodeGenerationInputResult(OverAllState state, EvaluationResult result) {
		log.debug("CodeactEvaluationResultAttacher#attachCodeGenerationInputResult - reason=写入代码生成输入评估结果");
		return attachResult(state, CodeactEvaluationTag.STATE_CODE_GENERATION_INPUT, result);
	}

	/**
	 * 将代码执行评估结果写入 OverAllState
	 *
	 * @param state Agent 状态
	 * @param result 评估结果
	 * @return 更新的状态 Map
	 */
	public Map<String, Object> attachCodeExecutionResult(OverAllState state, EvaluationResult result) {
		log.debug("CodeactEvaluationResultAttacher#attachCodeExecutionResult - reason=写入代码执行评估结果");
		return attachResult(state, CodeactEvaluationTag.STATE_CODE_EXECUTION, result);
	}

	/**
	 * 将会话总结评估结果写入 OverAllState
	 *
	 * @param state Agent 状态
	 * @param result 评估结果
	 * @return 更新的状态 Map
	 */
	public Map<String, Object> attachSessionSummaryResult(OverAllState state, EvaluationResult result) {
		log.debug("CodeactEvaluationResultAttacher#attachSessionSummaryResult - reason=写入会话总结评估结果");
		return attachResult(state, CodeactEvaluationTag.STATE_SESSION_SUMMARY, result);
	}

	/**
	 * 将评估结果注入到上下文（兼容旧接口）
	 *
	 * @param context 评估上下文
	 * @param result 评估结果
	 */
	public void attachToContext(EvaluationContext context, EvaluationResult result) {
		// 此方法主要用于兼容 ModelInputEvaluationHook 中的调用
		// 实际上我们应该直接操作 state，但 ModelHook 的返回值会被合并到 state 中
		// 所以这里我们不需要做太多事情，主要的写入逻辑在 attachResult 中完成并返回 Map
		log.debug("CodeactEvaluationResultAttacher#attachToContext - reason=兼容性调用，实际写入由 attachResult 完成");
	}

	/**
	 * 通用的结果附加方法
	 *
	 * @param state Agent 状态
	 * @param statePath 状态路径
	 * @param result 评估结果
	 * @return 更新的状态 Map
	 */
	@SuppressWarnings("unchecked")
	private Map<String, Object> attachResult(OverAllState state, String statePath, EvaluationResult result) {
		Map<String, Object> updates = new HashMap<>();

		try {
			// 转换为可序列化的 Map
			Map<String, Object> resultMap = convertToMap(result);

			// 确保 evaluation 根节点存在
			Optional<Object> evalRootOpt = state.value(CodeactEvaluationTag.STATE_EVALUATION_ROOT);
			Object evalRoot = evalRootOpt.orElse(null);
			Map<String, Object> evaluationMap;

			if (evalRoot instanceof Map) {
				evaluationMap = new HashMap<>((Map<String, Object>) evalRoot);
			} else {
				evaluationMap = new HashMap<>();
			}

			// 提取路径最后一段作为 key
			String[] pathParts = statePath.split("\\.");
			String key = pathParts[pathParts.length - 1];

			// 写入结果
			evaluationMap.put(key, resultMap);

			// 更新到 state
			updates.put(CodeactEvaluationTag.STATE_EVALUATION_ROOT, evaluationMap);

			// 核心修改：将评估结果注入到 messages 中
			appendEvaluationResultToMessages(state, updates, result);

			log.info("CodeactEvaluationResultAttacher#attachResult - reason=评估结果已写入, path={}, suiteId={}",
					statePath, result.getSuiteId());

		} catch (Exception e) {
			log.error("CodeactEvaluationResultAttacher#attachResult - reason=写入评估结果失败, path=" + statePath, e);
		}

		return updates;
	}

	/**
	 * 将评估结果追加到 Messages 中
	 */
	private void appendEvaluationResultToMessages(OverAllState state, Map<String, Object> updates, EvaluationResult result) {
		log.info("CodeactEvaluationResultAttacher#appendEvaluationResultToMessages - reason=开始处理评估结果注入");

		// 获取当前的 messages
		List<Message> currentMessages = new ArrayList<>();
		Optional<List<Message>> messagesOpt = state.value("messages");
		if (messagesOpt.isPresent()) {
			currentMessages.addAll(messagesOpt.get());
		}

		// 1. 解析历史中已存在的 ref-id 集合
		Map<String, String> existingEvaluations = new HashMap<>();
		for (Message msg : currentMessages) {
			if (msg instanceof ToolResponseMessage toolMsg) {
				for (ToolResponseMessage.ToolResponse resp : toolMsg.getResponses()) {
					if (EVALUATION_TOOL_NAME.equals(resp.name())) {
						parseExistingEvaluations(resp.responseData(), existingEvaluations);
						break;
					}
				}
			}
		}

		log.info("CodeactEvaluationResultAttacher#appendEvaluationResultToMessages - reason=已解析历史评估结果, 历史条目数={}", existingEvaluations.size());

		// 2. 筛选出需要新增的 criterion（每个 ref-id 对应一个独立的 evaluation 标签，基于 suite-id + criterion + ref-id 去重）
		Map<String, String> newEvaluations = new HashMap<>();
		String suiteId = result.getSuiteId();

		if (result.getCriteriaResults() != null) {
			for (Map.Entry<String, CriterionResult> entry : result.getCriteriaResults().entrySet()) {
				String criterionName = entry.getKey();
				CriterionResult criterionResult = entry.getValue();

				Map<String, Object> metadata = criterionResult.getMetadata();
				log.debug("CodeactEvaluationResultAttacher#appendEvaluationResultToMessages - reason=检查criterion metadata, criterion={}, metadata={}",
						criterionName, metadata != null ? metadata.keySet() : "null");

				// 检查是否为空结果，如果 is_empty=true 则跳过该 criterion
				if (metadata != null && Boolean.TRUE.equals(metadata.get("is_empty"))) {
					log.debug("CodeactEvaluationResultAttacher#appendEvaluationResultToMessages - reason=跳过空结果的criterion, criterion={}", criterionName);
					continue;
				}

				// 检查是否有 ref_entries（每个经验的独立条目）
				if (metadata != null && metadata.containsKey("ref_entries")) {
					Object entriesObj = metadata.get("ref_entries");
					if (entriesObj instanceof List) {
						@SuppressWarnings("unchecked")
						List<Map<String, String>> refEntries = (List<Map<String, String>>) entriesObj;
						log.debug("CodeactEvaluationResultAttacher#appendEvaluationResultToMessages - reason=找到ref_entries, criterion={}, count={}",
								criterionName, refEntries.size());

						for (Map<String, String> refEntry : refEntries) {
							String refId = refEntry.get("ref_id");
							String content = refEntry.get("content");

							if (refId == null || refId.isEmpty()) {
								continue;
							}

							// 使用 suite-id + criterion + ref-id 作为去重 key
							String dedupeKey = suiteId + "|" + criterionName + "|" + refId;

							if (existingEvaluations.containsKey(dedupeKey)) {
								log.debug("CodeactEvaluationResultAttacher#appendEvaluationResultToMessages - reason=跳过已存在的ref-id, criterion={}, refId={}",
										criterionName, refId);
								continue;
							}

							// 存储格式: key -> content
							newEvaluations.put(dedupeKey, content);
							log.debug("CodeactEvaluationResultAttacher#appendEvaluationResultToMessages - reason=添加新ref-id, criterion={}, refId={}",
									criterionName, refId);
						}
					}
				} else {
					// 没有 ref_entries，使用整体内容（如 enhanced_user_input）
					String content = String.valueOf(criterionResult.getValue());
					String dedupeKey = suiteId + "|" + criterionName + "|";

					if (existingEvaluations.containsKey(dedupeKey)) {
						log.debug("CodeactEvaluationResultAttacher#appendEvaluationResultToMessages - reason=跳过已存在的无ref-id criterion, criterion={}",
								criterionName);
						continue;
					}

					newEvaluations.put(dedupeKey, content);
					log.debug("CodeactEvaluationResultAttacher#appendEvaluationResultToMessages - reason=添加无ref-id的criterion, criterion={}", criterionName);
				}
			}
		}

		if (newEvaluations.isEmpty()) {
			log.info("CodeactEvaluationResultAttacher#appendEvaluationResultToMessages - reason=没有新的评估内容需要注入");
			return;
		}

		log.info("CodeactEvaluationResultAttacher#appendEvaluationResultToMessages - reason=筛选出新评估条目, 新条目数={}", newEvaluations.size());

		// 3. 仅用新的 evaluations 构建 XML 内容（不包含历史）
		String newEvaluationText = rebuildEvaluationXml(newEvaluations);

		// 4. 创建增量的消息对（只包含这一对新消息）
		String toolCallId = "eval_" + UUID.randomUUID().toString().substring(0, 8);

		// 4.1 创建 AssistantMessage with ToolCall
		AssistantMessage.ToolCall toolCall = new AssistantMessage.ToolCall(toolCallId, "function", EVALUATION_TOOL_NAME, "{}");
		AssistantMessage assistantMsg = AssistantMessage.builder().content("").toolCalls(List.of(toolCall)).build();

		// 4.2 创建 ToolResponseMessage with ToolResponse
		ToolResponseMessage.ToolResponse toolResponse = new ToolResponseMessage.ToolResponse(toolCallId, EVALUATION_TOOL_NAME, newEvaluationText);
		ToolResponseMessage toolMsg = ToolResponseMessage.builder().responses(List.of(toolResponse)).build();

		// 5. 将增量消息对放入 updates（只包含新的这两条消息）
		List<Message> incrementalMessages = new ArrayList<>();
		incrementalMessages.add(assistantMsg);
		incrementalMessages.add(toolMsg);

		updates.put("messages", incrementalMessages);

		log.info("CodeactEvaluationResultAttacher#appendEvaluationResultToMessages - reason=已创建增量评估消息对, 新消息数={}", incrementalMessages.size());
	}

	private void parseExistingEvaluations(String xml, Map<String, String> map) {
		// 简单的正则解析，假设格式比较规范
		// <evaluations suite-id="..."> ... </evaluations>
		Pattern suitePattern = Pattern.compile("<evaluations suite-id=\"([^\"]+)\">([\\s\\S]*?)</evaluations>");
		Matcher suiteMatcher = suitePattern.matcher(xml);

		while (suiteMatcher.find()) {
			String suiteId = suiteMatcher.group(1);
			String content = suiteMatcher.group(2);

			Pattern evalPattern = Pattern.compile("<evaluation criterion=\"([^\"]+)\" ref-id=\"([^\"]*)\">([\\s\\S]*?)</evaluation>");
			Matcher evalMatcher = evalPattern.matcher(content);

			while (evalMatcher.find()) {
				String criterion = evalMatcher.group(1);
				String refId = evalMatcher.group(2);
				String value = evalMatcher.group(3).trim();

				// 使用 suite-id + criterion + ref-id 作为去重 key
				String key = suiteId + "|" + criterion + "|" + (refId == null ? "" : refId);
				map.put(key, value);
			}
		}
	}

	private String rebuildEvaluationXml(Map<String, String> map) {
		// 按 suiteId 分组
		Map<String, List<String>> suiteGroups = new HashMap<>();

		for (Map.Entry<String, String> entry : map.entrySet()) {
			String key = entry.getKey();
			String content = entry.getValue();
			String[] parts = key.split("\\|", 3);
			if (parts.length < 3) continue;

			String suiteId = parts[0];
			String criterion = parts[1];
			String refId = parts[2]; // 可能为空字符串

			suiteGroups.computeIfAbsent(suiteId, k -> new ArrayList<>()).add(
					String.format("  <evaluation criterion=\"%s\" ref-id=\"%s\">\n%s\n  </evaluation>", criterion, refId, content)
			);
		}

		StringBuilder sb = new StringBuilder();
		for (Map.Entry<String, List<String>> entry : suiteGroups.entrySet()) {
			sb.append("<evaluations suite-id=\"").append(entry.getKey()).append("\">\n");
			for (String eval : entry.getValue()) {
				sb.append(eval).append("\n");
			}
			sb.append("</evaluations>\n");
		}
		return sb.toString();
	}

	/**
	 * 将 EvaluationResult 转换为可序列化的 Map
	 *
	 * @param result 评估结果
	 * @return Map 表示
	 */
	private Map<String, Object> convertToMap(EvaluationResult result) {
		Map<String, Object> map = new HashMap<>();

		map.put("suiteId", result.getSuiteId());
		map.put("suiteName", result.getSuiteName());
		map.put("criteriaResults", result.getCriteriaResults());
		map.put("statistics", result.getStatistics());
		map.put("startTimeMillis", result.getStartTimeMillis());
		map.put("endTimeMillis", result.getEndTimeMillis());

		return map;
	}

	/**
	 * 从评估结果中提取特定标志位（便于后续 Hook/Interceptor 使用）
	 *
	 * @param result 评估结果
	 * @param criterionName 指标名称
	 * @return 指标值（如果存在）
	 */
	public Object extractCriterionValue(EvaluationResult result, String criterionName) {
		if (result == null || result.getCriteriaResults() == null) {
			return null;
		}

		var criterionResult = result.getCriteriaResults().get(criterionName);
		if (criterionResult != null) {
			return criterionResult.getValue();
		}

		return null;
	}
}

