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

package com.alibaba.assistant.agent.extension.learning.model;

import java.time.Instant;

/**
 * 学习搜索请求
 * 用于从学习仓库中查询学习记录
 *
 * @author Assistant Agent Team
 * @since 1.0.0
 */
public class LearningSearchRequest {

	/**
	 * 命名空间
	 */
	private String namespace;

	/**
	 * 学习类型
	 */
	private String learningType;

	/**
	 * 查询文本
	 */
	private String query;

	/**
	 * 时间范围 - 开始时间
	 */
	private Instant timeRangeStart;

	/**
	 * 时间范围 - 结束时间
	 */
	private Instant timeRangeEnd;

	/**
	 * 返回数量限制
	 */
	private int limit = 10;

	/**
	 * 偏移量
	 */
	private int offset = 0;

	public LearningSearchRequest() {
	}

	public LearningSearchRequest(String namespace, String learningType) {
		this.namespace = namespace;
		this.learningType = learningType;
	}

	public String getNamespace() {
		return namespace;
	}

	public void setNamespace(String namespace) {
		this.namespace = namespace;
	}

	public String getLearningType() {
		return learningType;
	}

	public void setLearningType(String learningType) {
		this.learningType = learningType;
	}

	public String getQuery() {
		return query;
	}

	public void setQuery(String query) {
		this.query = query;
	}

	public Instant getTimeRangeStart() {
		return timeRangeStart;
	}

	public void setTimeRangeStart(Instant timeRangeStart) {
		this.timeRangeStart = timeRangeStart;
	}

	public Instant getTimeRangeEnd() {
		return timeRangeEnd;
	}

	public void setTimeRangeEnd(Instant timeRangeEnd) {
		this.timeRangeEnd = timeRangeEnd;
	}

	public int getLimit() {
		return limit;
	}

	public void setLimit(int limit) {
		this.limit = limit;
	}

	public int getOffset() {
		return offset;
	}

	public void setOffset(int offset) {
		this.offset = offset;
	}

	public static Builder builder() {
		return new Builder();
	}

	public static class Builder {

		private final LearningSearchRequest request;

		public Builder() {
			this.request = new LearningSearchRequest();
		}

		public Builder namespace(String namespace) {
			request.namespace = namespace;
			return this;
		}

		public Builder learningType(String learningType) {
			request.learningType = learningType;
			return this;
		}

		public Builder query(String query) {
			request.query = query;
			return this;
		}

		public Builder timeRangeStart(Instant timeRangeStart) {
			request.timeRangeStart = timeRangeStart;
			return this;
		}

		public Builder timeRangeEnd(Instant timeRangeEnd) {
			request.timeRangeEnd = timeRangeEnd;
			return this;
		}

		public Builder limit(int limit) {
			request.limit = limit;
			return this;
		}

		public Builder offset(int offset) {
			request.offset = offset;
			return this;
		}

		public LearningSearchRequest build() {
			return request;
		}

	}

}

