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
package com.alibaba.assistant.agent.extension.search.model;

import java.util.*;

/**
 * 搜索结果集合
 *
 * @author Assistant Agent Team
 */
public class SearchResultSet {
	/**
	 * 查询语句
	 */
	private String query;

	/**
	 * 所有结果项
	 */
	private List<SearchResultItem> items;

	/**
	 * 按数据源类型分组的结果
	 */
	private Map<SearchSourceType, List<SearchResultItem>> groupedBySource;

	/**
	 * 使用的数据源集合
	 */
	private Set<SearchSourceType> usedSources;

	/**
	 * 元数据（如是否命中缓存、调用时长、降级信息等）
	 */
	private Map<String, Object> metadata;

	public SearchResultSet() {
		this.items = new ArrayList<>();
		this.groupedBySource = new HashMap<>();
		this.usedSources = new HashSet<>();
		this.metadata = new HashMap<>();
	}

	public SearchResultSet(String query) {
		this();
		this.query = query;
	}

	public String getQuery() {
		return query;
	}

	public void setQuery(String query) {
		this.query = query;
	}

	public List<SearchResultItem> getItems() {
		return items;
	}

	public void setItems(List<SearchResultItem> items) {
		this.items = items;
	}

	public Map<SearchSourceType, List<SearchResultItem>> getGroupedBySource() {
		return groupedBySource;
	}

	public void setGroupedBySource(Map<SearchSourceType, List<SearchResultItem>> groupedBySource) {
		this.groupedBySource = groupedBySource;
	}

	public Set<SearchSourceType> getUsedSources() {
		return usedSources;
	}

	public void setUsedSources(Set<SearchSourceType> usedSources) {
		this.usedSources = usedSources;
	}

	public Map<String, Object> getMetadata() {
		return metadata;
	}

	public void setMetadata(Map<String, Object> metadata) {
		this.metadata = metadata;
	}

	/**
	 * 添加单个搜索结果项
	 */
	public void addItem(SearchResultItem item) {
		this.items.add(item);
		this.usedSources.add(item.getSourceType());
		this.groupedBySource.computeIfAbsent(item.getSourceType(), k -> new ArrayList<>()).add(item);
	}

	/**
	 * 批量添加搜索结果项
	 */
	public void addItems(List<SearchResultItem> items) {
		for (SearchResultItem item : items) {
			addItem(item);
		}
	}

	/**
	 * 获取结果总数
	 */
	public int getTotalCount() {
		return items.size();
	}

	/**
	 * 创建成功的搜索结果集。
	 *
	 * @param items 搜索结果项列表
	 * @return 搜索结果集
	 */
	public static SearchResultSet success(List<SearchResultItem> items) {
		SearchResultSet resultSet = new SearchResultSet();
		if (items != null) {
			resultSet.addItems(items);
		}
		resultSet.getMetadata().put("success", true);
		return resultSet;
	}

	/**
	 * 创建空的搜索结果集。
	 *
	 * @param message 消息
	 * @return 搜索结果集
	 */
	public static SearchResultSet empty(String message) {
		SearchResultSet resultSet = new SearchResultSet();
		resultSet.getMetadata().put("success", false);
		resultSet.getMetadata().put("message", message);
		return resultSet;
	}
}
