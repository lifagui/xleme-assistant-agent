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
 * 工具调用记录
 * 记录工具的调用信息，包括输入、输出、执行时间等
 *
 * @author Assistant Agent Team
 * @since 1.0.0
 */
public class ToolCallRecord {

	/**
	 * 工具名称
	 */
	private String toolName;

	/**
	 * 工具参数
	 */
	private Map<String, Object> arguments;

	/**
	 * 执行结果
	 */
	private Object result;

	/**
	 * 是否成功
	 */
	private boolean success;

	/**
	 * 执行时长（毫秒）
	 */
	private long duration;

	/**
	 * 时间戳
	 */
	private Instant timestamp;

	/**
	 * 错误信息（如果失败）
	 */
	private String errorMessage;

	public ToolCallRecord() {
		this.timestamp = Instant.now();
	}

	public ToolCallRecord(String toolName, Map<String, Object> arguments) {
		this();
		this.toolName = toolName;
		this.arguments = arguments;
	}

	public String getToolName() {
		return toolName;
	}

	public void setToolName(String toolName) {
		this.toolName = toolName;
	}

	public Map<String, Object> getArguments() {
		return arguments;
	}

	public void setArguments(Map<String, Object> arguments) {
		this.arguments = arguments;
	}

	public Object getResult() {
		return result;
	}

	public void setResult(Object result) {
		this.result = result;
	}

	public boolean isSuccess() {
		return success;
	}

	public void setSuccess(boolean success) {
		this.success = success;
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

	public String getErrorMessage() {
		return errorMessage;
	}

	public void setErrorMessage(String errorMessage) {
		this.errorMessage = errorMessage;
	}

	public static Builder builder() {
		return new Builder();
	}

	public static class Builder {

		private final ToolCallRecord record;

		public Builder() {
			this.record = new ToolCallRecord();
		}

		public Builder toolName(String toolName) {
			record.toolName = toolName;
			return this;
		}

		public Builder arguments(Map<String, Object> arguments) {
			record.arguments = arguments;
			return this;
		}

		public Builder result(Object result) {
			record.result = result;
			return this;
		}

		public Builder success(boolean success) {
			record.success = success;
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

		public Builder errorMessage(String errorMessage) {
			record.errorMessage = errorMessage;
			return this;
		}

		public ToolCallRecord build() {
			return record;
		}

	}

}

