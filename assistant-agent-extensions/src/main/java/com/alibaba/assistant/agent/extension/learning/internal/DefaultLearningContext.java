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
import com.alibaba.assistant.agent.extension.learning.model.LearningTriggerSource;
import com.alibaba.assistant.agent.extension.learning.model.ModelCallRecord;
import com.alibaba.assistant.agent.extension.learning.model.ToolCallRecord;
import com.alibaba.assistant.agent.extension.learning.model.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 默认学习上下文实现
 * 提供学习上下文的默认实现和Builder模式
 *
 * @author Assistant Agent Team
 * @since 1.0.0
 */
public class DefaultLearningContext implements LearningContext {

	private final Object overAllState;

	private final List<Object> conversationHistory;

	private final List<ToolCallRecord> toolCallRecords;

	private final List<ModelCallRecord> modelCallRecords;

	private final Map<String, Object> customData;

	private final LearningTriggerSource triggerSource;

	private DefaultLearningContext(Builder builder) {
		this.overAllState = builder.overAllState;
		this.conversationHistory = builder.conversationHistory != null ? builder.conversationHistory
				: new ArrayList<>();
		this.toolCallRecords = builder.toolCallRecords != null ? builder.toolCallRecords : new ArrayList<>();
		this.modelCallRecords = builder.modelCallRecords != null ? builder.modelCallRecords : new ArrayList<>();
		this.customData = builder.customData != null ? builder.customData : new HashMap<>();
		this.triggerSource = builder.triggerSource;
	}

	@Override
	public Object getOverAllState() {
		return overAllState;
	}

	@Override
	public List<Object> getConversationHistory() {
		return conversationHistory;
	}

	@Override
	public List<ToolCallRecord> getToolCallRecords() {
		return toolCallRecords;
	}

	@Override
	public List<ModelCallRecord> getModelCallRecords() {
		return modelCallRecords;
	}

	@Override
	public Map<String, Object> getCustomData() {
		return customData;
	}

	@Override
	public LearningTriggerSource getTriggerSource() {
		return triggerSource;
	}

	public static Builder builder() {
		return new Builder();
	}

	public static class Builder {

		private Object overAllState;

		private List<Object> conversationHistory;

		private List<ToolCallRecord> toolCallRecords;

		private List<ModelCallRecord> modelCallRecords;

		private Map<String, Object> customData;

		private LearningTriggerSource triggerSource;

		public Builder overAllState(Object overAllState) {
			this.overAllState = overAllState;
			return this;
		}

		public Builder conversationHistory(List<Object> conversationHistory) {
			this.conversationHistory = conversationHistory;
			return this;
		}

		public Builder toolCallRecords(List<ToolCallRecord> toolCallRecords) {
			this.toolCallRecords = toolCallRecords;
			return this;
		}

		public Builder modelCallRecords(List<ModelCallRecord> modelCallRecords) {
			this.modelCallRecords = modelCallRecords;
			return this;
		}

		public Builder customData(Map<String, Object> customData) {
			this.customData = customData;
			return this;
		}

		public Builder triggerSource(LearningTriggerSource triggerSource) {
			this.triggerSource = triggerSource;
			return this;
		}

		public DefaultLearningContext build() {
			return new DefaultLearningContext(this);
		}

	}

}

