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

package com.alibaba.assistant.agent.extension.learning.spi;

import com.alibaba.assistant.agent.extension.learning.model.LearningTask;
import com.alibaba.assistant.agent.extension.learning.model.LearningTriggerContext;

/**
 * 学习策略接口
 * 定义学习的决策逻辑，如何时触发学习、如何选择提取器和仓库等
 *
 * @author Assistant Agent Team
 * @since 1.0.0
 */
public interface LearningStrategy {

	/**
	 * 判断是否应该触发学习
	 * @param triggerContext 触发上下文
	 * @return true表示应该触发学习，false表示跳过
	 */
	boolean shouldTriggerLearning(LearningTriggerContext triggerContext);

	/**
	 * 选择合适的学习提取器
	 * @param task 学习任务
	 * @return 提取器实例，如果没有合适的返回null
	 */
	LearningExtractor<?> selectExtractor(LearningTask task);

	/**
	 * 选择合适的学习仓库
	 * @param task 学习任务
	 * @return 仓库实例，如果没有合适的返回null
	 */
	LearningRepository<?> selectRepository(LearningTask task);

	/**
	 * 解析命名空间
	 * @param task 学习任务
	 * @return 命名空间字符串
	 */
	String resolveNamespace(LearningTask task);

	/**
	 * 判断是否应该异步执行
	 * @param task 学习任务
	 * @return true表示异步执行，false表示同步执行
	 */
	boolean shouldExecuteAsync(LearningTask task);

}

