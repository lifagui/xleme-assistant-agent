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

import com.alibaba.assistant.agent.extension.trigger.model.ExecutionStatus;
import com.alibaba.assistant.agent.extension.trigger.model.TriggerExecutionRecord;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Instant;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

/**
 * 基于内存的触发器执行记录存储实现
 * 适用于开发和测试场景
 *
 * @author Assistant Agent Team
 * @since 1.0.0
 */
public class InMemoryTriggerExecutionLogRepository implements TriggerExecutionLogRepository {

	private static final Logger log = LoggerFactory.getLogger(InMemoryTriggerExecutionLogRepository.class);

	private final Map<String, TriggerExecutionRecord> storage = new ConcurrentHashMap<>();

	@Override
	public void save(TriggerExecutionRecord record) {
		log.debug("InMemoryTriggerExecutionLogRepository.save 保存执行记录: executionId={}, triggerId={}",
				record.getExecutionId(), record.getTriggerId());
		storage.put(record.getExecutionId(), record);
	}

	@Override
	public Optional<TriggerExecutionRecord> findById(String executionId) {
		log.debug("InMemoryTriggerExecutionLogRepository.findById 查询执行记录: executionId={}", executionId);
		return Optional.ofNullable(storage.get(executionId));
	}

	@Override
	public void updateStatus(String executionId, ExecutionStatus status, String errorMessage,
			Map<String, Object> outputSummary) {
		log.debug("InMemoryTriggerExecutionLogRepository.updateStatus 更新执行状态: executionId={}, status={}",
				executionId, status);
		TriggerExecutionRecord record = storage.get(executionId);
		if (record != null) {
			record.setStatus(status);
			if (errorMessage != null) {
				record.setErrorMessage(errorMessage);
			}
			if (outputSummary != null) {
				record.setOutputSummary(outputSummary);
			}
			if (status == ExecutionStatus.SUCCESS || status == ExecutionStatus.FAILED
					|| status == ExecutionStatus.TIMEOUT) {
				record.setEndTime(Instant.now());
			}
		}
		else {
			log.warn("InMemoryTriggerExecutionLogRepository.updateStatus 执行记录不存在: executionId={}", executionId);
		}
	}

	@Override
	public List<TriggerExecutionRecord> listByTrigger(String triggerId, int limit) {
		log.debug("InMemoryTriggerExecutionLogRepository.listByTrigger 查询触发器执行记录: triggerId={}, limit={}",
				triggerId, limit);
		return storage.values()
			.stream()
			.filter(record -> triggerId.equals(record.getTriggerId()))
			.sorted(Comparator.comparing(TriggerExecutionRecord::getStartTime, Comparator.nullsLast(Comparator.naturalOrder())).reversed())
			.limit(limit)
			.collect(Collectors.toList());
	}

	@Override
	public List<TriggerExecutionRecord> findByTriggerId(String triggerId) {
		log.debug("InMemoryTriggerExecutionLogRepository.findByTriggerId 查询触发器所有执行记录: triggerId={}", triggerId);
		return storage.values()
			.stream()
			.filter(record -> triggerId.equals(record.getTriggerId()))
			.sorted(Comparator.comparing(TriggerExecutionRecord::getStartTime, Comparator.nullsLast(Comparator.naturalOrder())).reversed())
			.collect(Collectors.toList());
	}

	@Override
	public void delete(String executionId) {
		log.debug("InMemoryTriggerExecutionLogRepository.delete 删除执行记录: executionId={}", executionId);
		storage.remove(executionId);
	}

}

