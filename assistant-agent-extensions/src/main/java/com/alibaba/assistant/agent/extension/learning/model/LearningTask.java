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

/**
 * 学习任务接口
 * 表示一个学习任务，包含学习所需的所有信息
 *
 * @author Assistant Agent Team
 * @since 1.0.0
 */
public interface LearningTask {

	/**
	 * 获取任务ID
	 * @return 任务ID
	 */
	String getId();

	/**
	 * 获取学习类型
	 * @return 学习类型（如：experience、pattern、error等）
	 */
	String getLearningType();

	/**
	 * 获取触发来源
	 * @return 触发来源
	 */
	LearningTriggerSource getTriggerSource();

	/**
	 * 获取学习上下文
	 * @return 学习上下文
	 */
	LearningContext getContext();

	/**
	 * 获取存储配置
	 * @return 存储配置
	 */
	LearningStorageConfig getStorageConfig();

	/**
	 * 获取任务元数据
	 * @return 任务元数据
	 */
	LearningTaskMetadata getMetadata();

}

