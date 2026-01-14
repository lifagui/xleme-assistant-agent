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
 * 学习触发上下文
 * 包含触发学习时的上下文信息
 *
 * @author Assistant Agent Team
 * @since 1.0.0
 */
public class LearningTriggerContext {

	/**
	 * 触发来源
	 */
	private LearningTriggerSource source;

	/**
	 * 学习上下文
	 */
	private LearningContext context;

	/**
	 * 触发时间
	 */
	private Instant triggerTime;

	/**
	 * 存储配置
	 */
	private LearningStorageConfig config;

	public LearningTriggerContext() {
		this.triggerTime = Instant.now();
	}

	public LearningTriggerContext(LearningTriggerSource source, LearningContext context) {
		this();
		this.source = source;
		this.context = context;
	}

	public LearningTriggerSource getSource() {
		return source;
	}

	public void setSource(LearningTriggerSource source) {
		this.source = source;
	}

	public LearningContext getContext() {
		return context;
	}

	public void setContext(LearningContext context) {
		this.context = context;
	}

	public Instant getTriggerTime() {
		return triggerTime;
	}

	public void setTriggerTime(Instant triggerTime) {
		this.triggerTime = triggerTime;
	}

	public LearningStorageConfig getConfig() {
		return config;
	}

	public void setConfig(LearningStorageConfig config) {
		this.config = config;
	}

	public static Builder builder() {
		return new Builder();
	}

	public static class Builder {

		private final LearningTriggerContext triggerContext;

		public Builder() {
			this.triggerContext = new LearningTriggerContext();
		}

		public Builder source(LearningTriggerSource source) {
			triggerContext.source = source;
			return this;
		}

		public Builder context(LearningContext context) {
			triggerContext.context = context;
			return this;
		}

		public Builder triggerTime(Instant triggerTime) {
			triggerContext.triggerTime = triggerTime;
			return this;
		}

		public Builder config(LearningStorageConfig config) {
			triggerContext.config = config;
			return this;
		}

		public LearningTriggerContext build() {
			return triggerContext;
		}

	}

}

