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

import com.alibaba.cloud.ai.graph.agent.interceptor.ModelCallHandler;
import com.alibaba.cloud.ai.graph.agent.interceptor.ModelInterceptor;
import com.alibaba.cloud.ai.graph.agent.interceptor.ModelRequest;
import com.alibaba.cloud.ai.graph.agent.interceptor.ModelResponse;
import com.alibaba.assistant.agent.prompt.PromptContribution;
import com.alibaba.assistant.agent.prompt.PromptManager;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.chat.messages.Message;
import org.springframework.ai.chat.messages.SystemMessage;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * 评估结果感知的Prompt注入拦截器
 *
 * <p>该拦截器结合了评估结果与PromptBuilder，在模型调用前：
 * <ol>
 *   <li>从ModelRequest的context中读取评估结果</li>
 *   <li>将评估结果传递给PromptManager进行prompt组装</li>
 *   <li>将组装后的prompt注入到ModelRequest中</li>
 * </ol>
 *
 * @author Assistant Agent Team
 * @since 1.0.0
 */
public class EvaluationAwarePromptInterceptor extends ModelInterceptor {

	private static final Logger log = LoggerFactory.getLogger(EvaluationAwarePromptInterceptor.class);

	private final PromptManager promptManager;
	private final SystemTextMergeMode systemTextMergeMode;

	/**
	 * 评估结果在context中的key
	 */
	private static final String EVALUATION_CONTEXT_KEY = "evaluation";

	public EvaluationAwarePromptInterceptor(PromptManager promptManager) {
		this(promptManager, SystemTextMergeMode.APPEND);
	}

	public EvaluationAwarePromptInterceptor(PromptManager promptManager, SystemTextMergeMode systemTextMergeMode) {
		this.promptManager = Objects.requireNonNull(promptManager, "promptManager must not be null");
		this.systemTextMergeMode = Objects.requireNonNull(systemTextMergeMode, "systemTextMergeMode must not be null");
	}

	@Override
	public ModelResponse interceptModel(ModelRequest request, ModelCallHandler handler) {
		log.debug("EvaluationAwarePromptInterceptor#interceptModel - reason=开始处理");

		// 检查context中是否有评估结果
		Map<String, Object> context = request.getContext();
		if (context == null || !context.containsKey(EVALUATION_CONTEXT_KEY)) {
			log.debug("EvaluationAwarePromptInterceptor#interceptModel - reason=无评估结果，跳过prompt注入");
			return handler.call(request);
		}

		// 使用PromptManager组装prompt
		PromptContribution contribution = promptManager.assemble(request);
		if (contribution == null || contribution.isEmpty()) {
			log.debug("EvaluationAwarePromptInterceptor#interceptModel - reason=无prompt贡献，跳过注入");
			return handler.call(request);
		}

		// 合并SystemMessage
		SystemMessage mergedSystem = mergeSystemMessage(request.getSystemMessage(), contribution);

		// 合并Messages
		List<Message> mergedMessages = mergeMessages(request.getMessages(), contribution);

		// 构建更新后的request
		ModelRequest updated = ModelRequest.builder(request)
				.systemMessage(mergedSystem)
				.messages(mergedMessages)
				.build();

		log.info("EvaluationAwarePromptInterceptor#interceptModel - reason=已注入评估结果prompt");

		return handler.call(updated);
	}

	@Override
	public String getName() {
		return "EvaluationAwarePromptInjection";
	}

	/**
	 * 合并SystemMessage
	 */
	private SystemMessage mergeSystemMessage(SystemMessage original, PromptContribution contribution) {
		String prepend = contribution.systemTextToPrepend();
		String append = contribution.systemTextToAppend();

		String originalText = original != null ? original.getText() : null;
		String merged = originalText;

		if (systemTextMergeMode == SystemTextMergeMode.PREPEND) {
			merged = join(prepend, merged);
			merged = join(merged, append);
		} else {
			merged = join(merged, prepend);
			merged = join(merged, append);
		}

		if (merged == null || merged.isBlank()) {
			return null;
		}
		return new SystemMessage(merged);
	}

	/**
	 * 合并Messages
	 */
	private static List<Message> mergeMessages(List<Message> original, PromptContribution contribution) {
		List<Message> out = new ArrayList<>();
		if (contribution.messagesToPrepend() != null && !contribution.messagesToPrepend().isEmpty()) {
			out.addAll(contribution.messagesToPrepend());
		}
		if (original != null && !original.isEmpty()) {
			out.addAll(original);
		}
		if (contribution.messagesToAppend() != null && !contribution.messagesToAppend().isEmpty()) {
			out.addAll(contribution.messagesToAppend());
		}
		return out;
	}

	/**
	 * 拼接字符串
	 */
	private static String join(String a, String b) {
		if (a == null || a.isBlank()) {
			return b;
		}
		if (b == null || b.isBlank()) {
			return a;
		}
		return a + "\n\n" + b;
	}

	/**
	 * SystemText合并模式
	 */
	public enum SystemTextMergeMode {
		PREPEND, APPEND
	}

}

