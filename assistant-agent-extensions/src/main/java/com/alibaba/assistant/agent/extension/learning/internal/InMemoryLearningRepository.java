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

import com.alibaba.assistant.agent.extension.learning.model.LearningSearchRequest;
import com.alibaba.assistant.agent.extension.learning.spi.LearningRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

/**
 * 内存学习仓库实现
 * 基于ConcurrentHashMap的内存存储，用于开发和测试
 *
 * @param <T> 学习记录类型
 * @author Assistant Agent Team
 * @since 1.0.0
 */
public class InMemoryLearningRepository<T> implements LearningRepository<T> {

	private static final Logger log = LoggerFactory.getLogger(InMemoryLearningRepository.class);

	/**
	 * 存储结构：namespace -> (key -> record)
	 */
	private final Map<String, Map<String, T>> storage = new ConcurrentHashMap<>();

	/**
	 * 仓库支持的记录类型
	 */
	private final Class<T> recordType;

	/**
	 * 构造函数
	 * @param recordType 记录类型
	 */
	public InMemoryLearningRepository(Class<T> recordType) {
		this.recordType = recordType;
	}

	@Override
	public void save(String namespace, String key, T record) {
		if (namespace == null || key == null || record == null) {
			log.warn("InMemoryLearningRepository#save - reason=invalid parameters, namespace={}, key={}, record={}",
					namespace, key, record);
			return;
		}

		storage.computeIfAbsent(namespace, k -> new ConcurrentHashMap<>()).put(key, record);

		log.debug("InMemoryLearningRepository#save - reason=record saved, namespace={}, key={}", namespace, key);
	}

	@Override
	public void saveBatch(String namespace, List<T> records) {
		if (namespace == null || records == null || records.isEmpty()) {
			log.warn(
					"InMemoryLearningRepository#saveBatch - reason=invalid parameters, namespace={}, recordCount={}",
					namespace, records != null ? records.size() : 0);
			return;
		}

		Map<String, T> namespaceStorage = storage.computeIfAbsent(namespace, k -> new ConcurrentHashMap<>());

		for (T record : records) {
			// 生成唯一键
			String key = UUID.randomUUID().toString();
			namespaceStorage.put(key, record);
		}

		log.info(
				"InMemoryLearningRepository#saveBatch - reason=batch saved successfully, namespace={}, recordCount={}",
				namespace, records.size());
	}

	@Override
	public T get(String namespace, String key) {
		if (namespace == null || key == null) {
			log.warn("InMemoryLearningRepository#get - reason=invalid parameters, namespace={}, key={}", namespace,
					key);
			return null;
		}

		Map<String, T> namespaceStorage = storage.get(namespace);
		if (namespaceStorage == null) {
			log.debug("InMemoryLearningRepository#get - reason=namespace not found, namespace={}", namespace);
			return null;
		}

		T record = namespaceStorage.get(key);
		log.debug("InMemoryLearningRepository#get - reason=record retrieved, namespace={}, key={}, found={}", namespace,
				key, record != null);

		return record;
	}

	@Override
	public List<T> search(LearningSearchRequest request) {
		if (request == null) {
			log.warn("InMemoryLearningRepository#search - reason=request is null");
			return new ArrayList<>();
		}

		String namespace = request.getNamespace();
		if (namespace == null) {
			log.warn("InMemoryLearningRepository#search - reason=namespace is null in request");
			return new ArrayList<>();
		}

		Map<String, T> namespaceStorage = storage.get(namespace);
		if (namespaceStorage == null) {
			log.debug("InMemoryLearningRepository#search - reason=namespace not found, namespace={}", namespace);
			return new ArrayList<>();
		}

		// 简单的分页实现
		List<T> allRecords = new ArrayList<>(namespaceStorage.values());
		int offset = request.getOffset();
		int limit = request.getLimit();

		List<T> result = allRecords.stream().skip(offset).limit(limit).collect(Collectors.toList());

		log.debug(
				"InMemoryLearningRepository#search - reason=search completed, namespace={}, totalRecords={}, returnedRecords={}",
				namespace, allRecords.size(), result.size());

		return result;
	}

	@Override
	public void delete(String namespace, String key) {
		if (namespace == null || key == null) {
			log.warn("InMemoryLearningRepository#delete - reason=invalid parameters, namespace={}, key={}", namespace,
					key);
			return;
		}

		Map<String, T> namespaceStorage = storage.get(namespace);
		if (namespaceStorage != null) {
			T removed = namespaceStorage.remove(key);
			log.debug("InMemoryLearningRepository#delete - reason=record deleted, namespace={}, key={}, deleted={}",
					namespace, key, removed != null);
		}
	}

	/**
	 * 获取命名空间中的记录数量
	 * @param namespace 命名空间
	 * @return 记录数量
	 */
	public int getRecordCount(String namespace) {
		Map<String, T> namespaceStorage = storage.get(namespace);
		return namespaceStorage != null ? namespaceStorage.size() : 0;
	}

	/**
	 * 清空命名空间
	 * @param namespace 命名空间
	 */
	public void clearNamespace(String namespace) {
		storage.remove(namespace);
		log.info("InMemoryLearningRepository#clearNamespace - reason=namespace cleared, namespace={}", namespace);
	}

	/**
	 * 清空所有数据
	 */
	public void clearAll() {
		storage.clear();
		log.info("InMemoryLearningRepository#clearAll - reason=all data cleared");
	}

	@Override
	public Class<T> getSupportedRecordType() {
		return recordType;
	}

}
