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
import com.alibaba.cloud.ai.graph.store.Store;
import com.alibaba.cloud.ai.graph.store.StoreItem;
import com.alibaba.cloud.ai.graph.store.StoreSearchRequest;
import com.alibaba.cloud.ai.graph.store.StoreSearchResult;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.stream.Collectors;

/**
 * 基于Store的学习仓库实现
 * 提供持久化的学习记录存储
 *
 * @param <T> 学习记录类型
 * @author Assistant Agent Team
 * @since 1.0.0
 */
public class StoreLearningRepository<T> implements LearningRepository<T> {

	private static final Logger log = LoggerFactory.getLogger(StoreLearningRepository.class);

	private static final String NAMESPACE_PREFIX = "learning";

	private final Store store;

	private final ObjectMapper objectMapper;

	private final Class<T> recordType;

	public StoreLearningRepository(Store store, Class<T> recordType) {
		this.store = store;
		this.objectMapper = new ObjectMapper();
		this.recordType = recordType;
	}

	public StoreLearningRepository(Store store, ObjectMapper objectMapper, Class<T> recordType) {
		this.store = store;
		this.objectMapper = objectMapper;
		this.recordType = recordType;
	}

	@Override
	public void save(String namespace, String key, T record) {
		if (namespace == null || key == null || record == null) {
			log.warn("StoreLearningRepository#save - reason=invalid parameters, namespace={}, key={}, record={}",
					namespace, key, record);
			return;
		}

		try {
			// Convert record to Map
			@SuppressWarnings("unchecked")
			Map<String, Object> value = objectMapper.convertValue(record, Map.class);

			// Create StoreItem with hierarchical namespace
			List<String> namespacePath = buildNamespacePath(namespace);
			StoreItem item = StoreItem.of(namespacePath, key, value);

			// Store the item
			store.putItem(item);

			log.info("StoreLearningRepository#save - reason=record saved successfully, namespace={}, key={}",
					namespace, key);
		}
		catch (Exception e) {
			log.error("StoreLearningRepository#save - reason=failed to save record, namespace={}, key={}", namespace,
					key, e);
		}
	}

	@Override
	public void saveBatch(String namespace, List<T> records) {
		if (namespace == null || records == null || records.isEmpty()) {
			log.warn("StoreLearningRepository#saveBatch - reason=invalid parameters, namespace={}, recordCount={}",
					namespace, records != null ? records.size() : 0);
			return;
		}

		try {
			List<String> namespacePath = buildNamespacePath(namespace);

			for (T record : records) {
				// Generate unique key for each record
				String key = UUID.randomUUID().toString();

				@SuppressWarnings("unchecked")
				Map<String, Object> value = objectMapper.convertValue(record, Map.class);

				StoreItem item = StoreItem.of(namespacePath, key, value);
				store.putItem(item);
			}

			log.info(
					"StoreLearningRepository#saveBatch - reason=batch saved successfully, namespace={}, recordCount={}",
					namespace, records.size());
		}
		catch (Exception e) {
			log.error(
					"StoreLearningRepository#saveBatch - reason=failed to save batch, namespace={}, recordCount={}",
					namespace, records.size(), e);
		}
	}

	@Override
	public T get(String namespace, String key) {
		if (namespace == null || key == null) {
			log.warn("StoreLearningRepository#get - reason=invalid parameters, namespace={}, key={}", namespace, key);
			return null;
		}

		try {
			List<String> namespacePath = buildNamespacePath(namespace);
			Optional<StoreItem> itemOpt = store.getItem(namespacePath, key);

			if (itemOpt.isEmpty()) {
				log.debug("StoreLearningRepository#get - reason=record not found, namespace={}, key={}", namespace,
						key);
				return null;
			}

			StoreItem item = itemOpt.get();
			T record = objectMapper.convertValue(item.getValue(), recordType);

			log.debug("StoreLearningRepository#get - reason=record retrieved, namespace={}, key={}", namespace, key);
			return record;
		}
		catch (Exception e) {
			log.error("StoreLearningRepository#get - reason=failed to retrieve record, namespace={}, key={}", namespace,
					key, e);
			return null;
		}
	}

	@Override
	public List<T> search(LearningSearchRequest request) {
		if (request == null) {
			log.warn("StoreLearningRepository#search - reason=request is null");
			return new ArrayList<>();
		}

		String namespace = request.getNamespace();
		if (namespace == null) {
			log.warn("StoreLearningRepository#search - reason=namespace is null in request");
			return new ArrayList<>();
		}

		try {
			// Build Store search request
			List<String> namespacePath = buildNamespacePath(namespace);
			String namespaceStr = String.join("/", namespacePath);

			StoreSearchRequest.Builder searchBuilder = StoreSearchRequest.builder().namespace(namespaceStr);

			if (request.getQuery() != null) {
				searchBuilder.query(request.getQuery());
			}

			searchBuilder.limit(request.getLimit()).offset(request.getOffset());

			StoreSearchRequest storeRequest = searchBuilder.build();

			// Execute search
			StoreSearchResult result = store.searchItems(storeRequest);

			// Convert results to records
			List<T> records = result.getItems()
				.stream()
				.map(item -> objectMapper.convertValue(item.getValue(), recordType))
				.collect(Collectors.toList());

			log.debug(
					"StoreLearningRepository#search - reason=search completed, namespace={}, totalItems={}, returnedRecords={}",
					namespace, result.getTotalCount(), records.size());

			return records;
		}
		catch (Exception e) {
			log.error("StoreLearningRepository#search - reason=search failed, namespace={}", namespace, e);
			return new ArrayList<>();
		}
	}

	@Override
	public void delete(String namespace, String key) {
		if (namespace == null || key == null) {
			log.warn("StoreLearningRepository#delete - reason=invalid parameters, namespace={}, key={}", namespace,
					key);
			return;
		}

		try {
			List<String> namespacePath = buildNamespacePath(namespace);
			boolean deleted = store.deleteItem(namespacePath, key);

			log.info("StoreLearningRepository#delete - reason=delete operation completed, namespace={}, key={}, deleted={}",
					namespace, key, deleted);
		}
		catch (Exception e) {
			log.error("StoreLearningRepository#delete - reason=failed to delete record, namespace={}, key={}", namespace,
					key, e);
		}
	}

    @Override
    public Class<T> getSupportedRecordType() {
        return recordType;
    }

    /**
	 * Build hierarchical namespace path
	 * @param namespace namespace string
	 * @return list of namespace components
	 */
	private List<String> buildNamespacePath(String namespace) {
		List<String> path = new ArrayList<>();
		path.add(NAMESPACE_PREFIX);

		if (namespace != null && !namespace.isEmpty()) {
			// Split namespace by '/' or '.' to support hierarchical namespaces
			String[] parts = namespace.split("[/.]");
			path.addAll(Arrays.asList(parts));
		}

		return path;
	}

}

