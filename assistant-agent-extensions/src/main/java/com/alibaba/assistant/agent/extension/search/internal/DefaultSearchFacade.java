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
package com.alibaba.assistant.agent.extension.search.internal;

import com.alibaba.assistant.agent.extension.search.model.SearchRequest;
import com.alibaba.assistant.agent.extension.search.model.SearchResultItem;
import com.alibaba.assistant.agent.extension.search.model.SearchResultSet;
import com.alibaba.assistant.agent.extension.search.model.SearchSourceType;
import com.alibaba.assistant.agent.extension.search.spi.SearchFacade;
import com.alibaba.assistant.agent.extension.search.spi.SearchProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.stream.Collectors;

/**
 * 默认搜索门面实现
 * 协调多个SearchProvider，应用合并策略
 *
 * @author Assistant Agent Team
 */
public class DefaultSearchFacade implements SearchFacade {
	private static final Logger logger = LoggerFactory.getLogger(DefaultSearchFacade.class);

	private final List<SearchProvider> providers;

	public DefaultSearchFacade(List<SearchProvider> providers) {
		this.providers = providers != null ? providers : new ArrayList<>();
		logger.info("DefaultSearchFacade#init - reason=initialized with {} providers", this.providers.size());
	}

	@Override
	public SearchResultSet unifiedSearch(SearchRequest request) {
		long startTime = System.currentTimeMillis();
		logger.info("DefaultSearchFacade#unifiedSearch - reason=start search with query={}, sourceTypes={}",
				request.getQuery(), request.getSourceTypes());

		SearchResultSet resultSet = new SearchResultSet(request.getQuery());

		try {
			// 如果未指定数据源，默认使用所有支持的数据源
			Set<SearchSourceType> targetSources = request.getSourceTypes();
			if (targetSources == null || targetSources.isEmpty()) {
				targetSources = EnumSet.of(SearchSourceType.PROJECT, SearchSourceType.KNOWLEDGE);
			}

			// 并行调用各个Provider
			Map<SearchSourceType, List<SearchResultItem>> resultsPerSource = new HashMap<>();

			for (SearchSourceType sourceType : targetSources) {
				try {
					List<SearchProvider> matchedProviders = providers.stream()
							.filter(p -> p.supports(sourceType))
							.toList();

					if (matchedProviders.isEmpty()) {
						logger.warn("DefaultSearchFacade#unifiedSearch - reason=no provider found for sourceType={}", sourceType);
						continue;
					}

					// 使用第一个匹配的Provider
					SearchProvider provider = matchedProviders.get(0);
					List<SearchResultItem> items = provider.search(request);
					if (items != null && !items.isEmpty()) {
						resultsPerSource.put(sourceType, items);
					}
				} catch (Exception e) {
					logger.error("DefaultSearchFacade#unifiedSearch - reason=provider failed for sourceType={}", sourceType, e);
					// 单个Provider失败不影响整体流程
				}
			}

			// 合并结果
			mergeResults(resultSet, resultsPerSource, request.getTopK());

			long duration = System.currentTimeMillis() - startTime;
			resultSet.getMetadata().put("duration_ms", duration);
			resultSet.getMetadata().put("cached", false);

			logger.info("DefaultSearchFacade#unifiedSearch - reason=merged {} results from {} sources in {}ms",
					resultSet.getTotalCount(), resultSet.getUsedSources().size(), duration);

		} catch (Exception e) {
			logger.error("DefaultSearchFacade#unifiedSearch - reason=unified search failed with error", e);
		}

		return resultSet;
	}

	/**
	 * 合并多个数据源的结果
	 */
	private void mergeResults(SearchResultSet resultSet, Map<SearchSourceType, List<SearchResultItem>> resultsPerSource, int topK) {
		// 简单策略：按score排序，取topK
		List<SearchResultItem> allItems = resultsPerSource.values().stream()
				.flatMap(List::stream)
				.sorted(Comparator.comparingDouble(SearchResultItem::getScore).reversed())
				.limit(topK)
				.collect(Collectors.toList());

		resultSet.addItems(allItems);
	}
}

