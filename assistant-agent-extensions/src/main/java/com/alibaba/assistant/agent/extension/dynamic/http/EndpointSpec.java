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

/**
 * HTTP Endpoint 规格。
 *
 * <p>定义要暴露的 HTTP endpoint（method + path）。
 *
 * @author Assistant Agent Team
 * @since 1.0.0
 */
public class EndpointSpec {

	/**
	 * HTTP 方法（GET/POST/PUT/DELETE/PATCH 等）。
	 */
	private final String method;

	/**
	 * URL 路径模板（如 /pets/{id}）。
	 */
	private final String path;

	/**
	 * 操作 ID（可选，用于精确匹配 OpenAPI operationId）。
	 */
	private final String operationId;

	private EndpointSpec(Builder builder) {
		this.method = builder.method.toUpperCase();
		this.path = builder.path;
		this.operationId = builder.operationId;
	}

	public String getMethod() {
		return method;
	}

	public String getPath() {
		return path;
	}

	public String getOperationId() {
		return operationId;
	}

	/**
	 * 创建构建器。
	 *
	 * @param method HTTP 方法
	 * @param path URL 路径
	 * @return 构建器
	 */
	public static Builder builder(String method, String path) {
		return new Builder(method, path);
	}

	/**
	 * 便捷方法：创建 GET endpoint。
	 *
	 * @param path URL 路径
	 * @return EndpointSpec 实例
	 */
	public static EndpointSpec get(String path) {
		return builder("GET", path).build();
	}

	/**
	 * 便捷方法：创建 POST endpoint。
	 *
	 * @param path URL 路径
	 * @return EndpointSpec 实例
	 */
	public static EndpointSpec post(String path) {
		return builder("POST", path).build();
	}

	/**
	 * 便捷方法：创建 PUT endpoint。
	 *
	 * @param path URL 路径
	 * @return EndpointSpec 实例
	 */
	public static EndpointSpec put(String path) {
		return builder("PUT", path).build();
	}

	/**
	 * 便捷方法：创建 DELETE endpoint。
	 *
	 * @param path URL 路径
	 * @return EndpointSpec 实例
	 */
	public static EndpointSpec delete(String path) {
		return builder("DELETE", path).build();
	}

	/**
	 * 构建器类。
	 */
	public static class Builder {

		private final String method;

		private final String path;

		private String operationId;

		private Builder(String method, String path) {
			this.method = method;
			this.path = path;
		}

		/**
		 * 设置操作 ID。
		 *
		 * @param operationId 操作 ID
		 * @return 构建器
		 */
		public Builder operationId(String operationId) {
			this.operationId = operationId;
			return this;
		}

		/**
		 * 构建实例。
		 *
		 * @return EndpointSpec 实例
		 */
		public EndpointSpec build() {
			return new EndpointSpec(this);
		}

	}

	@Override
	public String toString() {
		return method + " " + path + (operationId != null ? " (" + operationId + ")" : "");
	}

}

