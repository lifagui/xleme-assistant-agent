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
package com.alibaba.assistant.agent.extension.dynamic.mcp;

/**
 * MCP Server 元数据规格。
 *
 * <p>用于提供 MCP Server 的命名与描述信息，仅用于 CodeAct metadata 的生成，
 * 不用于建立连接（连接由 MCP Client Boot Starter 管理）。
 *
 * @author Assistant Agent Team
 * @since 1.0.0
 */
public class McpServerSpec {

	/**
	 * 连接名称，对应配置中的 spring.ai.mcp.client.*.connections.&lt;name&gt;
	 */
	private final String connectionName;

	/**
	 * 服务器展示名称（可能含中文/特殊字符）。
	 */
	private final String serverName;

	/**
	 * 服务器描述。
	 */
	private final String description;

	private McpServerSpec(Builder builder) {
		this.connectionName = builder.connectionName;
		this.serverName = builder.serverName != null ? builder.serverName : builder.connectionName;
		this.description = builder.description != null ? builder.description : "";
	}

	public String getConnectionName() {
		return connectionName;
	}

	public String getServerName() {
		return serverName;
	}

	public String getDescription() {
		return description;
	}

	/**
	 * 创建构建器。
	 *
	 * @param connectionName 连接名称
	 * @return 构建器
	 */
	public static Builder builder(String connectionName) {
		return new Builder(connectionName);
	}

	/**
	 * 构建器类。
	 */
	public static class Builder {

		private final String connectionName;

		private String serverName;

		private String description;

		private Builder(String connectionName) {
			this.connectionName = connectionName;
		}

		/**
		 * 设置服务器展示名称。
		 *
		 * @param serverName 服务器名称
		 * @return 构建器
		 */
		public Builder serverName(String serverName) {
			this.serverName = serverName;
			return this;
		}

		/**
		 * 设置服务器描述。
		 *
		 * @param description 描述
		 * @return 构建器
		 */
		public Builder description(String description) {
			this.description = description;
			return this;
		}

		/**
		 * 构建实例。
		 *
		 * @return McpServerSpec 实例
		 */
		public McpServerSpec build() {
			return new McpServerSpec(this);
		}

	}

}

