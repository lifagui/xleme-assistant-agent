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

/**
 * 单条搜索结果
 *
 * @author Assistant Agent Team
 */
public class SearchResultItem {
	/**
	 * 结果唯一标识
	 */
	private String id;

	/**
	 * 数据源类型
	 */
	private SearchSourceType sourceType;

	/**
	 * 标题
	 */
	private String title;

	/**
	 * 摘要/片段
	 */
	private String snippet;

	/**
	 * 详细内容（可选）
	 */
	private String content;

	/**
	 * 资源链接（本地文件路径/URL/Store key等）
	 */
	private String uri;

	/**
	 * 相关度评分（0-1）
	 */
	private double score;

	/**
	 * 元数据
	 */
	private SearchMetadata metadata;

	public SearchResultItem() {
		this.metadata = new SearchMetadata();
	}

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public SearchSourceType getSourceType() {
		return sourceType;
	}

	public void setSourceType(SearchSourceType sourceType) {
		this.sourceType = sourceType;
	}

	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	public String getSnippet() {
		return snippet;
	}

	public void setSnippet(String snippet) {
		this.snippet = snippet;
	}

	public String getContent() {
		return content;
	}

	public void setContent(String content) {
		this.content = content;
	}

	public String getUri() {
		return uri;
	}

	public void setUri(String uri) {
		this.uri = uri;
	}

	public double getScore() {
		return score;
	}

	public void setScore(double score) {
		this.score = score;
	}

	public SearchMetadata getMetadata() {
		return metadata;
	}

	public void setMetadata(SearchMetadata metadata) {
		this.metadata = metadata;
	}
}

