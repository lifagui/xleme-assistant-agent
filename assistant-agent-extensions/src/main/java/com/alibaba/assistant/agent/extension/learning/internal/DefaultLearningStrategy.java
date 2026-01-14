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

import com.alibaba.assistant.agent.extension.learning.model.LearningStorageConfig;
import com.alibaba.assistant.agent.extension.learning.model.LearningTask;
import com.alibaba.assistant.agent.extension.learning.model.LearningTriggerContext;
import com.alibaba.assistant.agent.extension.learning.spi.LearningExtractor;
import com.alibaba.assistant.agent.extension.learning.spi.LearningRepository;
import com.alibaba.assistant.agent.extension.learning.spi.LearningStrategy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

/**
 * 默认学习策略实现
 * 实现学习的决策逻辑
 *
 * @author Assistant Agent Team
 * @since 1.0.0
 */
public class DefaultLearningStrategy implements LearningStrategy {

	private static final Logger log = LoggerFactory.getLogger(DefaultLearningStrategy.class);

	private final List<LearningExtractor<?>> extractors;

	private final List<LearningRepository<?>> repositories;

	private final boolean asyncEnabled;

	public DefaultLearningStrategy(List<LearningExtractor<?>> extractors, List<LearningRepository<?>> repositories,
			boolean asyncEnabled) {
		this.extractors = extractors;
		this.repositories = repositories;
		this.asyncEnabled = asyncEnabled;
	}

	@Override
	public boolean shouldTriggerLearning(LearningTriggerContext triggerContext) {
		// 基础检查：上下文不能为空
		if (triggerContext == null || triggerContext.getContext() == null) {
			log.debug("DefaultLearningStrategy#shouldTriggerLearning - reason=trigger context is null");
			return false;
		}

		// 默认策略：总是触发学习，由具体的提取器决定是否真正学习
		return true;
	}

	@Override
	public LearningExtractor<?> selectExtractor(LearningTask task) {
		if (task == null || task.getLearningType() == null) {
			log.warn("DefaultLearningStrategy#selectExtractor - reason=task or learning type is null");
			return null;
		}

		// 根据学习类型选择提取器
		String learningType = task.getLearningType();
		for (LearningExtractor<?> extractor : extractors) {
			if (learningType.equals(extractor.getSupportedLearningType())) {
				log.debug(
						"DefaultLearningStrategy#selectExtractor - reason=extractor found, learningType={}, extractorClass={}",
						learningType, extractor.getClass().getSimpleName());
				return extractor;
			}
		}

		log.warn("DefaultLearningStrategy#selectExtractor - reason=no extractor found for learning type, learningType={}",
				learningType);
		return null;
	}

	@Override
	public LearningRepository<?> selectRepository(LearningTask task) {
		if (task == null) {
			log.warn("DefaultLearningStrategy#selectRepository - reason=task is null");
			return null;
		}

		if (repositories.isEmpty()) {
			log.warn("DefaultLearningStrategy#selectRepository - reason=no repositories available");
			return null;
		}

		// 如果只有一个仓库，直接返回
		if (repositories.size() == 1) {
			LearningRepository<?> repo = repositories.get(0);
			log.info("DefaultLearningStrategy#selectRepository - reason=only one repository, using {}",
					repo.getClass().getSimpleName());
			return repo;
		}

		// 根据学习类型智能选择仓库
		String learningType = task.getLearningType();

		// 对于 experience 类型，优先选择 ExperienceLearningRepository
		if ("experience".equals(learningType)) {
			for (LearningRepository<?> repo : repositories) {
				if (repo.getClass().getSimpleName().contains("ExperienceLearningRepository")) {
					log.info("DefaultLearningStrategy#selectRepository - reason=selected repository for experience learning, repositoryClass={}",
							repo.getClass().getSimpleName());
					return repo;
				}
			}
		}

		// 根据存储配置选择仓库
		LearningStorageConfig storageConfig = task.getStorageConfig();
		if (storageConfig != null && storageConfig.getStorageType() != null) {
			String storageType = storageConfig.getStorageType().name().toLowerCase();
			for (LearningRepository<?> repo : repositories) {
				String repoClassName = repo.getClass().getSimpleName().toLowerCase();
				if (repoClassName.contains(storageType)) {
					log.info("DefaultLearningStrategy#selectRepository - reason=matched repository by storage type, storageType={}, repositoryClass={}",
							storageType, repo.getClass().getSimpleName());
					return repo;
				}
			}
		}

		// 默认返回第一个仓库
		LearningRepository<?> defaultRepo = repositories.get(0);
		log.warn("DefaultLearningStrategy#selectRepository - reason=no matching repository found, using default repository, repositoryClass={}",
				defaultRepo.getClass().getSimpleName());
		return defaultRepo;
	}

	@Override
	public String resolveNamespace(LearningTask task) {
		if (task == null) {
			return "default";
		}

		LearningStorageConfig storageConfig = task.getStorageConfig();
		if (storageConfig == null) {
			return task.getLearningType() != null ? task.getLearningType() : "default";
		}

		// 如果配置中指定了命名空间，直接使用
		if (storageConfig.getNamespace() != null) {
			return storageConfig.getNamespace();
		}

		// 根据命名空间策略解析
		LearningStorageConfig.NamespaceStrategy strategy = storageConfig.getNamespaceStrategy();
		if (strategy == null) {
			strategy = LearningStorageConfig.NamespaceStrategy.LEARNING_TYPE;
		}

		switch (strategy) {
			case LEARNING_TYPE:
				return task.getLearningType() != null ? task.getLearningType() : "default";
			case TRIGGER_SOURCE:
				return task.getTriggerSource() != null ? task.getTriggerSource().name().toLowerCase() : "default";
			case FIXED:
			case CUSTOM:
			default:
				return "default";
		}
	}

	@Override
	public boolean shouldExecuteAsync(LearningTask task) {
		// 默认使用配置的异步标志
		return asyncEnabled;
	}

}

