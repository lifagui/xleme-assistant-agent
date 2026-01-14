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

import java.util.HashMap;
import java.util.Map;

/**
 * 搜索元数据
 *
 * @author Assistant Agent Team
 */
public class SearchMetadata {
	/**
	 * 数据源名称
	 */
	private String sourceName;

	/**
	 * 编程语言
	 */
	private String language;

	/**
	 * 标签集合
	 */
	private Map<String, String> tags;

	/**
	 * 创建时间
	 */
	private Long createdAt;

	/**
	 * 更新时间
	 */
	private Long updatedAt;

	/**
	 * 扩展信息
	 */
	private Map<String, Object> extensions;

	public SearchMetadata() {
		this.tags = new HashMap<>();
		this.extensions = new HashMap<>();
	}

	public String getSourceName() {
		return sourceName;
	}

	public void setSourceName(String sourceName) {
		this.sourceName = sourceName;
	}

	public String getLanguage() {
		return language;
	}

	public void setLanguage(String language) {
		this.language = language;
	}

	public Map<String, String> getTags() {
		return tags;
	}

	public void setTags(Map<String, String> tags) {
		this.tags = tags;
	}

	public Long getCreatedAt() {
		return createdAt;
	}

	public void setCreatedAt(Long createdAt) {
		this.createdAt = createdAt;
	}

	public Long getUpdatedAt() {
		return updatedAt;
	}

	public void setUpdatedAt(Long updatedAt) {
		this.updatedAt = updatedAt;
	}

	public Map<String, Object> getExtensions() {
		return extensions;
	}

	public void setExtensions(Map<String, Object> extensions) {
		this.extensions = extensions;
	}
}

