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
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * 学习任务元数据
 * 包含任务的基本信息和元数据
 *
 * @author Assistant Agent Team
 * @since 1.0.0
 */
public class LearningTaskMetadata {

	/**
	 * 任务ID
	 */
	private String taskId;

	/**
	 * 学习类型（如：experience、pattern、error等）
	 */
	private String learningType;

	/**
	 * 触发来源
	 */
	private LearningTriggerSource triggerSource;

	/**
	 * 创建时间
	 */
	private Instant createdAt;

	/**
	 * 自定义数据
	 */
	private Map<String, Object> customData;

	public LearningTaskMetadata() {
		this.taskId = UUID.randomUUID().toString();
		this.createdAt = Instant.now();
		this.customData = new HashMap<>();
	}

	public LearningTaskMetadata(String learningType, LearningTriggerSource triggerSource) {
		this();
		this.learningType = learningType;
		this.triggerSource = triggerSource;
	}

	public String getTaskId() {
		return taskId;
	}

	public void setTaskId(String taskId) {
		this.taskId = taskId;
	}

	public String getLearningType() {
		return learningType;
	}

	public void setLearningType(String learningType) {
		this.learningType = learningType;
	}

	public LearningTriggerSource getTriggerSource() {
		return triggerSource;
	}

	public void setTriggerSource(LearningTriggerSource triggerSource) {
		this.triggerSource = triggerSource;
	}

	public Instant getCreatedAt() {
		return createdAt;
	}

	public void setCreatedAt(Instant createdAt) {
		this.createdAt = createdAt;
	}

	public Map<String, Object> getCustomData() {
		return customData;
	}

	public void setCustomData(Map<String, Object> customData) {
		this.customData = customData;
	}

	public static Builder builder() {
		return new Builder();
	}

	public static class Builder {

		private final LearningTaskMetadata metadata;

		public Builder() {
			this.metadata = new LearningTaskMetadata();
		}

		public Builder taskId(String taskId) {
			metadata.taskId = taskId;
			return this;
		}

		public Builder learningType(String learningType) {
			metadata.learningType = learningType;
			return this;
		}

		public Builder triggerSource(LearningTriggerSource triggerSource) {
			metadata.triggerSource = triggerSource;
			return this;
		}

		public Builder createdAt(Instant createdAt) {
			metadata.createdAt = createdAt;
			return this;
		}

		public Builder customData(Map<String, Object> customData) {
			metadata.customData = customData;
			return this;
		}

		public Builder customData(String key, Object value) {
			metadata.customData.put(key, value);
			return this;
		}

		public LearningTaskMetadata build() {
			return metadata;
		}

	}

}

