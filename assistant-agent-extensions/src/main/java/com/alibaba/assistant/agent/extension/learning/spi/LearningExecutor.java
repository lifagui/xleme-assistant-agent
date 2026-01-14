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

import com.alibaba.assistant.agent.extension.learning.model.LearningResult;
import com.alibaba.assistant.agent.extension.learning.model.LearningTask;

import java.util.List;
import java.util.concurrent.CompletableFuture;

/**
 * 学习执行器接口
 * 负责执行学习任务，将学习记录持久化
 *
 * @author Assistant Agent Team
 * @since 1.0.0
 */
public interface LearningExecutor {

	/**
	 * 同步执行学习任务
	 * @param task 学习任务
	 * @return 学习结果
	 */
	LearningResult execute(LearningTask task);

	/**
	 * 异步执行学习任务
	 * @param task 学习任务
	 * @return 学习结果的CompletableFuture
	 */
	CompletableFuture<LearningResult> executeAsync(LearningTask task);

	/**
	 * 批量执行学习任务
	 * @param tasks 学习任务列表
	 * @return 学习结果列表
	 */
	List<LearningResult> executeBatch(List<LearningTask> tasks);

	/**
	 * 获取支持的学习类型
	 * @return 支持的学习类型列表
	 */
	List<String> getSupportedLearningTypes();

}

