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
 * 搜索请求
 *
 * @author Assistant Agent Team
 */
public class SearchRequest {
	/**
	 * 查询语句（自然语言或关键字）
	 */
	private String query;

	/**
	 * 指定的数据源类型
	 */
	private Set<SearchSourceType> sourceTypes;

	/**
	 * 统一的返回条数上限
	 */
	private int topK = 10;

	/**
	 * 每类数据源的topK限制
	 */
	private Map<SearchSourceType, Integer> perSourceTopK;

	/**
	 * 通用过滤条件
	 */
	private Map<String, Object> filters;

	/**
	 * 上下文信息（从OverAllState/Agent环境抽取）
	 */
	private SearchContext context;

	public SearchRequest() {
		this.sourceTypes = new HashSet<>();
		this.perSourceTopK = new HashMap<>();
		this.filters = new HashMap<>();
		this.context = new SearchContext();
	}

	public SearchRequest(String query) {
		this();
		this.query = query;
	}

	public String getQuery() {
		return query;
	}

	public void setQuery(String query) {
		this.query = query;
	}

	public Set<SearchSourceType> getSourceTypes() {
		return sourceTypes;
	}

	public void setSourceTypes(Set<SearchSourceType> sourceTypes) {
		this.sourceTypes = sourceTypes;
	}

	public int getTopK() {
		return topK;
	}

	public void setTopK(int topK) {
		this.topK = topK;
	}

	public Map<SearchSourceType, Integer> getPerSourceTopK() {
		return perSourceTopK;
	}

	public void setPerSourceTopK(Map<SearchSourceType, Integer> perSourceTopK) {
		this.perSourceTopK = perSourceTopK;
	}

	public Map<String, Object> getFilters() {
		return filters;
	}

	public void setFilters(Map<String, Object> filters) {
		this.filters = filters;
	}

	public SearchContext getContext() {
		return context;
	}

	public void setContext(SearchContext context) {
		this.context = context;
	}
}

