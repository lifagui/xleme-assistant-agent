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

import java.util.List;
import java.util.Map;

/**
 * 学习上下文接口
 * 封装学习过程所需的所有上下文信息
 *
 * @author Assistant Agent Team
 * @since 1.0.0
 */
public interface LearningContext {

	/**
	 * 获取全局状态（Agent执行状态）
	 * @return 全局状态对象，可能为null
	 */
	Object getOverAllState();

	/**
	 * 获取对话历史
	 * @return 对话历史列表
	 */
	List<Object> getConversationHistory();

	/**
	 * 获取工具调用记录
	 * @return 工具调用记录列表
	 */
	List<ToolCallRecord> getToolCallRecords();

	/**
	 * 获取模型调用记录
	 * @return 模型调用记录列表
	 */
	List<ModelCallRecord> getModelCallRecords();

	/**
	 * 获取自定义数据
	 * @return 自定义数据Map
	 */
	Map<String, Object> getCustomData();

	/**
	 * 获取触发源
	 * @return 学习触发来源
	 */
	LearningTriggerSource getTriggerSource();

	/**
	 * 创建Builder实例
	 * @return Builder
	 */
	static Builder builder() {
		return new Builder();
	}

	/**
	 * LearningContext Builder
	 */
	class Builder {

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

		public LearningContext build() {
			return new LearningContext() {

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
			};
		}

	}

}

