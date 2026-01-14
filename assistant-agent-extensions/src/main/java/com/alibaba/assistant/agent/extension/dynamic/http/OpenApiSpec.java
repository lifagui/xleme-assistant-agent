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
package com.alibaba.assistant.agent.extension.dynamic.http;

import java.util.HashMap;
import java.util.Map;

/**
 * OpenAPI 文档规格。
 *
 * <p>包含 OpenAPI 3.1 文档内容及相关配置。
 *
 * @author Assistant Agent Team
 * @since 1.0.0
 */
public class OpenApiSpec {

	/**
	 * OpenAPI 文档内容（JSON 或 YAML）。
	 */
	private final String content;

	/**
	 * 文档格式。
	 */
	private final Format format;

	/**
	 * 基础 URL（覆盖文档中的 servers 配置）。
	 */
	private final String baseUrl;

	/**
	 * 全局请求头。
	 */
	private final Map<String, String> globalHeaders;

	/**
	 * 请求超时（毫秒）。
	 */
	private final long timeoutMs;

	private OpenApiSpec(Builder builder) {
		this.content = builder.content;
		this.format = builder.format;
		this.baseUrl = builder.baseUrl;
		this.globalHeaders = new HashMap<>(builder.globalHeaders);
		this.timeoutMs = builder.timeoutMs;
	}

	public String getContent() {
		return content;
	}

	public Format getFormat() {
		return format;
	}

	public String getBaseUrl() {
		return baseUrl;
	}

	public Map<String, String> getGlobalHeaders() {
		return new HashMap<>(globalHeaders);
	}

	public long getTimeoutMs() {
		return timeoutMs;
	}

	/**
	 * 创建构建器。
	 *
	 * @param content OpenAPI 文档内容
	 * @return 构建器
	 */
	public static Builder builder(String content) {
		return new Builder(content);
	}

	/**
	 * 文档格式枚举。
	 */
	public enum Format {
		JSON,
		YAML
	}

	/**
	 * 构建器类。
	 */
	public static class Builder {

		private final String content;

		private Format format = Format.JSON;

		private String baseUrl;

		private final Map<String, String> globalHeaders = new HashMap<>();

		private long timeoutMs = 30000;

		private Builder(String content) {
			this.content = content;
			// 自动检测格式
			if (content != null && content.trim().startsWith("{")) {
				this.format = Format.JSON;
			}
			else {
				this.format = Format.YAML;
			}
		}

		/**
		 * 设置文档格式。
		 *
		 * @param format 格式
		 * @return 构建器
		 */
		public Builder format(Format format) {
			this.format = format;
			return this;
		}

		/**
		 * 设置基础 URL。
		 *
		 * @param baseUrl 基础 URL
		 * @return 构建器
		 */
		public Builder baseUrl(String baseUrl) {
			this.baseUrl = baseUrl;
			return this;
		}

		/**
		 * 添加全局请求头。
		 *
		 * @param name 头名称
		 * @param value 头值
		 * @return 构建器
		 */
		public Builder addHeader(String name, String value) {
			this.globalHeaders.put(name, value);
			return this;
		}

		/**
		 * 设置全局请求头。
		 *
		 * @param headers 请求头 Map
		 * @return 构建器
		 */
		public Builder globalHeaders(Map<String, String> headers) {
			this.globalHeaders.clear();
			this.globalHeaders.putAll(headers);
			return this;
		}

		/**
		 * 设置请求超时。
		 *
		 * @param timeoutMs 超时时间（毫秒）
		 * @return 构建器
		 */
		public Builder timeoutMs(long timeoutMs) {
			this.timeoutMs = timeoutMs;
			return this;
		}

		/**
		 * 构建实例。
		 *
		 * @return OpenApiSpec 实例
		 */
		public OpenApiSpec build() {
			return new OpenApiSpec(this);
		}

	}

}

