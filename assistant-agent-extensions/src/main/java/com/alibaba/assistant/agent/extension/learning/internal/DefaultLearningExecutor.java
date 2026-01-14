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

import com.alibaba.assistant.agent.extension.learning.model.LearningResult;
import com.alibaba.assistant.agent.extension.learning.model.LearningTask;
import com.alibaba.assistant.agent.extension.learning.spi.LearningExecutor;
import com.alibaba.assistant.agent.extension.learning.spi.LearningExtractor;
import com.alibaba.assistant.agent.extension.learning.spi.LearningRepository;
import com.alibaba.assistant.agent.extension.learning.spi.LearningStrategy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

/**
 * 默认学习执行器实现
 * 负责协调提取器、仓库和策略，执行学习任务
 *
 * @author Assistant Agent Team
 * @since 1.0.0
 */
public class DefaultLearningExecutor implements LearningExecutor {

	private static final Logger log = LoggerFactory.getLogger(DefaultLearningExecutor.class);

	private final List<LearningExtractor<?>> extractors;

	private final List<LearningRepository<?>> repositories;

	private final LearningStrategy strategy;

	private final AsyncLearningHandler asyncHandler;

	public DefaultLearningExecutor(List<LearningExtractor<?>> extractors, List<LearningRepository<?>> repositories,
			LearningStrategy strategy, AsyncLearningHandler asyncHandler) {
		this.extractors = extractors != null ? extractors : new ArrayList<>();
		this.repositories = repositories != null ? repositories : new ArrayList<>();
		this.strategy = strategy;
		this.asyncHandler = asyncHandler;
	}

	@Override
	public LearningResult execute(LearningTask task) {
		log.info("DefaultLearningExecutor#execute - reason=start execute learning task, taskId={}, learningType={}",
				task.getId(), task.getLearningType());

		long startTime = System.currentTimeMillis();

		try {
			// 1. 选择合适的提取器
			LearningExtractor<?> extractor = strategy.selectExtractor(task);
			if (extractor == null) {
				log.warn(
						"DefaultLearningExecutor#execute - reason=no suitable extractor found, taskId={}, learningType={}",
						task.getId(), task.getLearningType());
				return LearningResult.builder()
					.taskId(task.getId())
					.success(false)
					.failureReason("No suitable extractor found")
					.duration(System.currentTimeMillis() - startTime)
					.build();
			}

			// 2. 判断是否应该学习
			if (!extractor.shouldLearn(task.getContext())) {
				log.info(
						"DefaultLearningExecutor#execute - reason=extractor decided not to learn, taskId={}, learningType={}",
						task.getId(), task.getLearningType());
				return LearningResult.builder()
					.taskId(task.getId())
					.success(true)
					.recordCount(0)
					.duration(System.currentTimeMillis() - startTime)
					.build();
			}

			// 3. 提取学习记录
			List<?> records = extractor.extract(task.getContext());
			if (records == null || records.isEmpty()) {
				log.info("DefaultLearningExecutor#execute - reason=no records extracted, taskId={}, learningType={}",
						task.getId(), task.getLearningType());
				return LearningResult.builder()
					.taskId(task.getId())
					.success(true)
					.recordCount(0)
					.duration(System.currentTimeMillis() - startTime)
					.build();
			}

			// 4. 选择合适的仓库
			LearningRepository repository = strategy.selectRepository(task);
			if (repository == null) {
				log.warn(
						"DefaultLearningExecutor#execute - reason=no suitable repository found, taskId={}, learningType={}",
						task.getId(), task.getLearningType());
				return LearningResult.builder()
					.taskId(task.getId())
					.success(false)
					.failureReason("No suitable repository found")
					.duration(System.currentTimeMillis() - startTime)
					.build();
			}

			// 5. 解析命名空间
			String namespace = strategy.resolveNamespace(task);

			// 6. 持久化学习记录
			repository.saveBatch(namespace, records);

			log.info(
					"DefaultLearningExecutor#execute - reason=learning completed successfully, taskId={}, learningType={}, recordCount={}",
					task.getId(), task.getLearningType(), records.size());

			return LearningResult.builder()
				.taskId(task.getId())
				.success(true)
				.recordCount(records.size())
				.duration(System.currentTimeMillis() - startTime)
				.build();

		}
		catch (Exception e) {
			log.error("DefaultLearningExecutor#execute - reason=learning execution failed, taskId={}, learningType={}",
					task.getId(), task.getLearningType(), e);
			return LearningResult.builder()
				.taskId(task.getId())
				.success(false)
				.failureReason(e.getMessage())
				.duration(System.currentTimeMillis() - startTime)
				.build();
		}
	}

	@Override
	public CompletableFuture<LearningResult> executeAsync(LearningTask task) {
		log.info(
				"DefaultLearningExecutor#executeAsync - reason=start async execute learning task, taskId={}, learningType={}",
				task.getId(), task.getLearningType());

		return asyncHandler.executeAsync(() -> execute(task));
	}

	@Override
	public List<LearningResult> executeBatch(List<LearningTask> tasks) {
		log.info("DefaultLearningExecutor#executeBatch - reason=start batch execute learning tasks, taskCount={}",
				tasks.size());

		return tasks.stream().map(this::execute).collect(Collectors.toList());
	}

	@Override
	public List<String> getSupportedLearningTypes() {
		return extractors.stream().map(LearningExtractor::getSupportedLearningType).collect(Collectors.toList());
	}

}

