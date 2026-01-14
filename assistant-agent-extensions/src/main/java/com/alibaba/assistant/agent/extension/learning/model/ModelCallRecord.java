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

package com.alibaba.assistant.agent.extension.learning.model;

import java.time.Instant;
import java.util.Map;

/**
 * 模型调用记录
 * 记录模型调用的信息，包括输入、输出、token使用量等
 *
 * @author Assistant Agent Team
 * @since 1.0.0
 */
public class ModelCallRecord {

	/**
	 * 模型ID
	 */
	private String modelId;

	/**
	 * 提示词
	 */
	private String prompt;

	/**
	 * 模型响应
	 */
	private String response;

	/**
	 * Token使用量
	 */
	private TokenUsage tokenUsage;

	/**
	 * 执行时长（毫秒）
	 */
	private long duration;

	/**
	 * 时间戳
	 */
	private Instant timestamp;

	/**
	 * 是否成功
	 */
	private boolean success;

	/**
	 * 错误信息（如果失败）
	 */
	private String errorMessage;

	/**
	 * 自定义元数据
	 */
	private Map<String, Object> metadata;

	public ModelCallRecord() {
		this.timestamp = Instant.now();
	}

	public ModelCallRecord(String modelId, String prompt) {
		this();
		this.modelId = modelId;
		this.prompt = prompt;
	}

	public String getModelId() {
		return modelId;
	}

	public void setModelId(String modelId) {
		this.modelId = modelId;
	}

	public String getPrompt() {
		return prompt;
	}

	public void setPrompt(String prompt) {
		this.prompt = prompt;
	}

	public String getResponse() {
		return response;
	}

	public void setResponse(String response) {
		this.response = response;
	}

	public TokenUsage getTokenUsage() {
		return tokenUsage;
	}

	public void setTokenUsage(TokenUsage tokenUsage) {
		this.tokenUsage = tokenUsage;
	}

	public long getDuration() {
		return duration;
	}

	public void setDuration(long duration) {
		this.duration = duration;
	}

	public Instant getTimestamp() {
		return timestamp;
	}

	public void setTimestamp(Instant timestamp) {
		this.timestamp = timestamp;
	}

	public boolean isSuccess() {
		return success;
	}

	public void setSuccess(boolean success) {
		this.success = success;
	}

	public String getErrorMessage() {
		return errorMessage;
	}

	public void setErrorMessage(String errorMessage) {
		this.errorMessage = errorMessage;
	}

	public Map<String, Object> getMetadata() {
		return metadata;
	}

	public void setMetadata(Map<String, Object> metadata) {
		this.metadata = metadata;
	}

	/**
	 * Token使用量信息
	 */
	public static class TokenUsage {

		private int promptTokens;

		private int completionTokens;

		private int totalTokens;

		public TokenUsage() {
		}

		public TokenUsage(int promptTokens, int completionTokens, int totalTokens) {
			this.promptTokens = promptTokens;
			this.completionTokens = completionTokens;
			this.totalTokens = totalTokens;
		}

		public int getPromptTokens() {
			return promptTokens;
		}

		public void setPromptTokens(int promptTokens) {
			this.promptTokens = promptTokens;
		}

		public int getCompletionTokens() {
			return completionTokens;
		}

		public void setCompletionTokens(int completionTokens) {
			this.completionTokens = completionTokens;
		}

		public int getTotalTokens() {
			return totalTokens;
		}

		public void setTotalTokens(int totalTokens) {
			this.totalTokens = totalTokens;
		}

	}

	public static Builder builder() {
		return new Builder();
	}

	public static class Builder {

		private final ModelCallRecord record;

		public Builder() {
			this.record = new ModelCallRecord();
		}

		public Builder modelId(String modelId) {
			record.modelId = modelId;
			return this;
		}

		public Builder prompt(String prompt) {
			record.prompt = prompt;
			return this;
		}

		public Builder response(String response) {
			record.response = response;
			return this;
		}

		public Builder tokenUsage(TokenUsage tokenUsage) {
			record.tokenUsage = tokenUsage;
			return this;
		}

		public Builder duration(long duration) {
			record.duration = duration;
			return this;
		}

		public Builder timestamp(Instant timestamp) {
			record.timestamp = timestamp;
			return this;
		}

		public Builder success(boolean success) {
			record.success = success;
			return this;
		}

		public Builder errorMessage(String errorMessage) {
			record.errorMessage = errorMessage;
			return this;
		}

		public Builder metadata(Map<String, Object> metadata) {
			record.metadata = metadata;
			return this;
		}

		public ModelCallRecord build() {
			return record;
		}

	}

}

