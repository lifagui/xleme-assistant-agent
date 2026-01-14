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

package com.alibaba.assistant.agent.extension.trigger.repository;

import com.alibaba.assistant.agent.extension.trigger.model.SourceType;
import com.alibaba.assistant.agent.extension.trigger.model.TriggerDefinition;
import com.alibaba.assistant.agent.extension.trigger.model.TriggerStatus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

/**
 * 基于内存的触发器定义存储实现
 * 适用于开发和测试场景
 *
 * @author Assistant Agent Team
 * @since 1.0.0
 */
public class InMemoryTriggerRepository implements TriggerRepository {

	private static final Logger log = LoggerFactory.getLogger(InMemoryTriggerRepository.class);

	private final Map<String, TriggerDefinition> storage = new ConcurrentHashMap<>();

	@Override
	public void save(TriggerDefinition definition) {
		log.debug("InMemoryTriggerRepository.save 保存触发器定义: triggerId={}", definition.getTriggerId());
		storage.put(definition.getTriggerId(), definition);
	}

	@Override
	public Optional<TriggerDefinition> findById(String triggerId) {
		log.debug("InMemoryTriggerRepository.findById 查询触发器: triggerId={}", triggerId);
		return Optional.ofNullable(storage.get(triggerId));
	}

	@Override
	public List<TriggerDefinition> findBySource(SourceType sourceType, String sourceId) {
		log.debug("InMemoryTriggerRepository.findBySource 按来源查询: sourceType={}, sourceId={}", sourceType,
				sourceId);
		return storage.values()
			.stream()
			.filter(def -> def.getSourceType() == sourceType && sourceId.equals(def.getSourceId()))
			.collect(Collectors.toList());
	}

	@Override
	public List<TriggerDefinition> findByStatus(TriggerStatus status) {
		log.debug("InMemoryTriggerRepository.findByStatus 按状态查询: status={}", status);
		return storage.values().stream().filter(def -> def.getStatus() == status).collect(Collectors.toList());
	}

	@Override
	public void updateStatus(String triggerId, TriggerStatus status) {
		log.debug("InMemoryTriggerRepository.updateStatus 更新触发器状态: triggerId={}, status={}", triggerId, status);
		TriggerDefinition definition = storage.get(triggerId);
		if (definition != null) {
			definition.setStatus(status);
		}
		else {
			log.warn("InMemoryTriggerRepository.updateStatus 触发器不存在: triggerId={}", triggerId);
		}
	}

	@Override
	public void delete(String triggerId) {
		log.debug("InMemoryTriggerRepository.delete 删除触发器: triggerId={}", triggerId);
		storage.remove(triggerId);
	}

	@Override
	public List<TriggerDefinition> findAll() {
		log.debug("InMemoryTriggerRepository.findAll 查询所有触发器");
		return List.copyOf(storage.values());
	}

}

