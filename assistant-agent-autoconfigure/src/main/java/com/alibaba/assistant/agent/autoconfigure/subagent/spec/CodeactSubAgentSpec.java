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
package com.alibaba.assistant.agent.autoconfigure.subagent.spec;

import com.alibaba.assistant.agent.common.enums.Language;
import com.alibaba.assistant.agent.common.tools.CodeactTool;
import com.alibaba.cloud.ai.graph.agent.interceptor.ModelInterceptor;
import org.springframework.ai.chat.model.ChatModel;

import java.util.List;

/**
 * CodeactSubAgentSpec - 代码生成子Agent的配置规范（完全对标SubAgentSpec）
 *
 * <p>用于声明式配置Codeact子Agent，包括名称、描述、系统提示、模型、工具等。
 *
 * @author Assistant Agent Team
 * @since 1.0.0
 */
public class CodeactSubAgentSpec {

	private final String name;
	private final String description;
	private final String systemPrompt;
	private final ChatModel model;
	private final List<CodeactTool> codeactTools;
	private final List<ModelInterceptor> interceptors;
	private final boolean enableLoopingLog;
	private final Language language;
	private final boolean isCondition;

	private CodeactSubAgentSpec(Builder builder) {
		this.name = builder.name;
		this.description = builder.description;
		this.systemPrompt = builder.systemPrompt;
		this.model = builder.model;
		this.codeactTools = builder.codeactTools;
		this.interceptors = builder.interceptors;
		this.enableLoopingLog = builder.enableLoopingLog;
		this.language = builder.language;
		this.isCondition = builder.isCondition;
	}

	// Getters
	public String getName() {
		return name;
	}

	public String getDescription() {
		return description;
	}

	public String getSystemPrompt() {
		return systemPrompt;
	}

	public ChatModel getModel() {
		return model;
	}

	public List<CodeactTool> getCodeactTools() {
		return codeactTools;
	}

	public List<ModelInterceptor> getInterceptors() {
		return interceptors;
	}

	public boolean isEnableLoopingLog() {
		return enableLoopingLog;
	}

	public Language getLanguage() {
		return language;
	}

	public boolean isCondition() {
		return isCondition;
	}

	/**
	 * Create a new Builder
	 */
	public static Builder builder() {
		return new Builder();
	}

	/**
	 * Builder for CodeactSubAgentSpec（完全对标SubAgentSpec.Builder）
	 */
	public static class Builder {
		private String name;
		private String description;
		private String systemPrompt;
		private ChatModel model;
		private List<CodeactTool> codeactTools;
		private List<ModelInterceptor> interceptors;
		private boolean enableLoopingLog = false;
		private Language language;
		private boolean isCondition = false;

		public Builder name(String name) {
			this.name = name;
			return this;
		}

		public Builder description(String description) {
			this.description = description;
			return this;
		}

		public Builder systemPrompt(String systemPrompt) {
			this.systemPrompt = systemPrompt;
			return this;
		}

		public Builder model(ChatModel model) {
			this.model = model;
			return this;
		}

		public Builder codeactTools(List<CodeactTool> codeactTools) {
			this.codeactTools = codeactTools;
			return this;
		}

		public Builder interceptors(List<ModelInterceptor> interceptors) {
			this.interceptors = interceptors;
			return this;
		}

		public Builder enableLoopingLog(boolean enableLoopingLog) {
			this.enableLoopingLog = enableLoopingLog;
			return this;
		}

		public Builder language(Language language) {
			this.language = language;
			return this;
		}

		public Builder isCondition(boolean isCondition) {
			this.isCondition = isCondition;
			return this;
		}

		public CodeactSubAgentSpec build() {
			if (name == null || name.trim().isEmpty()) {
				throw new IllegalArgumentException("SubAgent name is required");
			}
			return new CodeactSubAgentSpec(this);
		}
	}
}

