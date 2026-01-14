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

package com.alibaba.assistant.agent.extension.learning.internal;

import com.alibaba.assistant.agent.extension.learning.model.LearningContext;
import com.alibaba.assistant.agent.extension.learning.model.LearningStorageConfig;
import com.alibaba.assistant.agent.extension.learning.model.LearningTask;
import com.alibaba.assistant.agent.extension.learning.model.LearningTaskMetadata;
import com.alibaba.assistant.agent.extension.learning.model.LearningTriggerSource;
import com.alibaba.assistant.agent.extension.learning.model.*;

/**
 * 默认学习任务实现
 * 提供学习任务的默认实现和Builder模式
 *
 * @author Assistant Agent Team
 * @since 1.0.0
 */
public class DefaultLearningTask implements LearningTask {

	private final String id;

	private final String learningType;

	private final LearningTriggerSource triggerSource;

	private final LearningContext context;

	private final LearningStorageConfig storageConfig;

	private final LearningTaskMetadata metadata;

	private DefaultLearningTask(Builder builder) {
		this.metadata = builder.metadata != null ? builder.metadata : new LearningTaskMetadata();
		this.id = this.metadata.getTaskId();
		this.learningType = builder.learningType;
		this.triggerSource = builder.triggerSource;
		this.context = builder.context;
		this.storageConfig = builder.storageConfig != null ? builder.storageConfig : new LearningStorageConfig();

		// Update metadata with task info
		this.metadata.setLearningType(learningType);
		this.metadata.setTriggerSource(triggerSource);
	}

	@Override
	public String getId() {
		return id;
	}

	@Override
	public String getLearningType() {
		return learningType;
	}

	@Override
	public LearningTriggerSource getTriggerSource() {
		return triggerSource;
	}

	@Override
	public LearningContext getContext() {
		return context;
	}

	@Override
	public LearningStorageConfig getStorageConfig() {
		return storageConfig;
	}

	@Override
	public LearningTaskMetadata getMetadata() {
		return metadata;
	}

	public static Builder builder() {
		return new Builder();
	}

	public static class Builder {

		private String learningType;

		private LearningTriggerSource triggerSource;

		private LearningContext context;

		private LearningStorageConfig storageConfig;

		private LearningTaskMetadata metadata;

		public Builder learningType(String learningType) {
			this.learningType = learningType;
			return this;
		}

		public Builder triggerSource(LearningTriggerSource triggerSource) {
			this.triggerSource = triggerSource;
			return this;
		}

		public Builder context(LearningContext context) {
			this.context = context;
			return this;
		}

		public Builder storageConfig(LearningStorageConfig storageConfig) {
			this.storageConfig = storageConfig;
			return this;
		}

		public Builder metadata(LearningTaskMetadata metadata) {
			this.metadata = metadata;
			return this;
		}

		public DefaultLearningTask build() {
			return new DefaultLearningTask(this);
		}

	}

}

