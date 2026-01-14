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

/**
 * 学习结果
 * 表示学习任务执行后的结果
 *
 * @author Assistant Agent Team
 * @since 1.0.0
 */
public class LearningResult {

	/**
	 * 任务ID
	 */
	private String taskId;

	/**
	 * 是否成功
	 */
	private boolean success;

	/**
	 * 记录数量
	 */
	private int recordCount;

	/**
	 * 失败原因（如果失败）
	 */
	private String failureReason;

	/**
	 * 执行时长（毫秒）
	 */
	private long duration;

	/**
	 * 完成时间
	 */
	private Instant timestamp;

	public LearningResult() {
		this.timestamp = Instant.now();
	}

	public LearningResult(String taskId, boolean success) {
		this();
		this.taskId = taskId;
		this.success = success;
	}

	public String getTaskId() {
		return taskId;
	}

	public void setTaskId(String taskId) {
		this.taskId = taskId;
	}

	public boolean isSuccess() {
		return success;
	}

	public void setSuccess(boolean success) {
		this.success = success;
	}

	public int getRecordCount() {
		return recordCount;
	}

	public void setRecordCount(int recordCount) {
		this.recordCount = recordCount;
	}

	public String getFailureReason() {
		return failureReason;
	}

	public void setFailureReason(String failureReason) {
		this.failureReason = failureReason;
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

	public static Builder builder() {
		return new Builder();
	}

	public static class Builder {

		private final LearningResult result;

		public Builder() {
			this.result = new LearningResult();
		}

		public Builder taskId(String taskId) {
			result.taskId = taskId;
			return this;
		}

		public Builder success(boolean success) {
			result.success = success;
			return this;
		}

		public Builder recordCount(int recordCount) {
			result.recordCount = recordCount;
			return this;
		}

		public Builder failureReason(String failureReason) {
			result.failureReason = failureReason;
			return this;
		}

		public Builder duration(long duration) {
			result.duration = duration;
			return this;
		}

		public Builder timestamp(Instant timestamp) {
			result.timestamp = timestamp;
			return this;
		}

		public LearningResult build() {
			return result;
		}

	}

}

