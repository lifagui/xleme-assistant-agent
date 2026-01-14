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
package com.alibaba.assistant.agent.autoconfigure.subagent;

import com.alibaba.cloud.ai.graph.KeyStrategy;
import com.alibaba.cloud.ai.graph.agent.BaseAgent;
import com.alibaba.cloud.ai.graph.agent.hook.Hook;
import com.alibaba.cloud.ai.graph.agent.interceptor.ModelInterceptor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

/**
 * CodeactSubAgent - Codeact子Agent抽象基类（参考ReactAgent的设计）
 *
 * <p>继承自BaseAgent，拥有完整的Agent能力（hooks、interceptors、状态管理、日志等）
 * <p>子类需要实现initGraph()定义具体的图结构
 *
 * @author Assistant Agent Team
 * @since 1.0.0
 */
public abstract class CodeactSubAgent extends BaseAgent {

	protected static final Logger logger = LoggerFactory.getLogger(CodeactSubAgent.class);

	protected List<? extends Hook> hooks;
	protected List<ModelInterceptor> modelInterceptors;

	protected CodeactSubAgent(
			String name,
			String description,
			List<? extends Hook> hooks,
			List<ModelInterceptor> modelInterceptors,
			String outputKey) {
		super(name,
			  description,
			  true,   // includeContents
			  false,  // returnReasoningContents（子Agent只返回结果）
			  outputKey,
			  KeyStrategy.REPLACE);

		this.hooks = hooks;
		this.modelInterceptors = modelInterceptors;

		logger.info("CodeactSubAgent#<init> 初始化: name={}", name);
	}

	/**
	 * Get hooks
	 */
	public List<? extends Hook> getHooks() {
		return hooks;
	}

	/**
	 * Get model interceptors
	 */
	public List<ModelInterceptor> getModelInterceptors() {
		return modelInterceptors;
	}
}

