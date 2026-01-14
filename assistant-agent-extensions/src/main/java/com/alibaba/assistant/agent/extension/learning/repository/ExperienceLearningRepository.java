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

package com.alibaba.assistant.agent.extension.learning.repository;

import com.alibaba.assistant.agent.extension.experience.model.Experience;
import com.alibaba.assistant.agent.extension.experience.spi.ExperienceRepository;
import com.alibaba.assistant.agent.extension.learning.model.LearningSearchRequest;
import com.alibaba.assistant.agent.extension.learning.spi.LearningRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

/**
 * 经验学习仓库
 * 将学习到的经验保存到经验仓库中，实现学习模块与经验模块的集成
 *
 * @author Assistant Agent Team
 * @since 1.0.0
 */
public class ExperienceLearningRepository implements LearningRepository<Experience> {

	private static final Logger log = LoggerFactory.getLogger(ExperienceLearningRepository.class);

	private final ExperienceRepository experienceRepository;

	public ExperienceLearningRepository(ExperienceRepository experienceRepository) {
		this.experienceRepository = experienceRepository;
	}

	@Override
	public void save(String namespace, String key, Experience record) {
		if (record == null) {
			log.warn("ExperienceLearningRepository#save - reason=record is null");
			return;
		}

		try {
			experienceRepository.save(record);
			log.info("ExperienceLearningRepository#save - reason=experience saved successfully, namespace={}, key={}",
					namespace, key);
		}
		catch (Exception e) {
			log.error(
					"ExperienceLearningRepository#save - reason=failed to save experience, namespace={}, key={}",
					namespace, key, e);
		}
	}

	@Override
	public void saveBatch(String namespace, List<Experience> records) {
		if (records == null || records.isEmpty()) {
			log.warn("ExperienceLearningRepository#saveBatch - reason=records is empty, namespace={}", namespace);
			return;
		}

		try {
			experienceRepository.batchSave(records);
			log.info(
					"ExperienceLearningRepository#saveBatch - reason=experiences saved successfully, namespace={}, count={}",
					namespace, records.size());
		}
		catch (Exception e) {
			log.error(
					"ExperienceLearningRepository#saveBatch - reason=failed to save experiences, namespace={}, count={}",
					namespace, records.size(), e);
		}
	}

	@Override
	public Experience get(String namespace, String key) {
		try {
			return experienceRepository.findById(key).orElse(null);
		}
		catch (Exception e) {
			log.error("ExperienceLearningRepository#get - reason=failed to get experience, namespace={}, key={}",
					namespace, key, e);
			return null;
		}
	}

	@Override
	public List<Experience> search(LearningSearchRequest request) {
		// TODO: 实现基于ExperienceProvider的搜索逻辑
		log.warn("ExperienceLearningRepository#search - reason=search not implemented yet");
		return List.of();
	}

	@Override
	public void delete(String namespace, String key) {
		try {
			boolean deleted = experienceRepository.deleteById(key);
			log.info(
					"ExperienceLearningRepository#delete - reason=delete operation completed, namespace={}, key={}, deleted={}",
					namespace, key, deleted);
		}
		catch (Exception e) {
			log.error("ExperienceLearningRepository#delete - reason=failed to delete experience, namespace={}, key={}",
					namespace, key, e);
		}
	}

	@Override
	public Class<Experience> getSupportedRecordType() {
		return Experience.class;
	}

}
