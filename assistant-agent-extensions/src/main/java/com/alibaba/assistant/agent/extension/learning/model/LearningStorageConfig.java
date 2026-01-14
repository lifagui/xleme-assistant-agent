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

import java.util.HashMap;
import java.util.Map;

/**
 * 学习存储配置
 * 定义学习记录如何存储，包括命名空间、存储策略等
 *
 * @author Assistant Agent Team
 * @since 1.0.0
 */
public class LearningStorageConfig {

	/**
	 * 命名空间（用于隔离不同类型的学习数据）
	 */
	private String namespace;

	/**
	 * 命名空间策略（如何确定命名空间）
	 */
	private NamespaceStrategy namespaceStrategy;

	/**
	 * 存储类型
	 */
	private StorageType storageType;

	/**
	 * 自定义配置
	 */
	private Map<String, Object> customConfig;

	public LearningStorageConfig() {
		this.customConfig = new HashMap<>();
		this.namespaceStrategy = NamespaceStrategy.LEARNING_TYPE;
		this.storageType = StorageType.IN_MEMORY;
	}

	public LearningStorageConfig(String namespace, NamespaceStrategy namespaceStrategy, StorageType storageType) {
		this.namespace = namespace;
		this.namespaceStrategy = namespaceStrategy;
		this.storageType = storageType;
		this.customConfig = new HashMap<>();
	}

	public String getNamespace() {
		return namespace;
	}

	public void setNamespace(String namespace) {
		this.namespace = namespace;
	}

	public NamespaceStrategy getNamespaceStrategy() {
		return namespaceStrategy;
	}

	public void setNamespaceStrategy(NamespaceStrategy namespaceStrategy) {
		this.namespaceStrategy = namespaceStrategy;
	}

	public StorageType getStorageType() {
		return storageType;
	}

	public void setStorageType(StorageType storageType) {
		this.storageType = storageType;
	}

	public Map<String, Object> getCustomConfig() {
		return customConfig;
	}

	public void setCustomConfig(Map<String, Object> customConfig) {
		this.customConfig = customConfig;
	}

	/**
	 * 命名空间策略枚举
	 */
	public enum NamespaceStrategy {

		/**
		 * 使用学习类型作为命名空间
		 */
		LEARNING_TYPE,

		/**
		 * 使用触发源作为命名空间
		 */
		TRIGGER_SOURCE,

		/**
		 * 使用固定命名空间
		 */
		FIXED,

		/**
		 * 使用自定义策略
		 */
		CUSTOM

	}

	/**
	 * 存储类型枚举
	 */
	public enum StorageType {

		/**
		 * 内存存储
		 */
		IN_MEMORY,

		/**
		 * 基于Store的持久化存储
		 */
		STORE,

		/**
		 * 自定义存储
		 */
		CUSTOM

	}

	public static Builder builder() {
		return new Builder();
	}

	public static class Builder {

		private final LearningStorageConfig config;

		public Builder() {
			this.config = new LearningStorageConfig();
		}

		public Builder namespace(String namespace) {
			config.namespace = namespace;
			return this;
		}

		public Builder namespaceStrategy(NamespaceStrategy strategy) {
			config.namespaceStrategy = strategy;
			return this;
		}

		public Builder storageType(StorageType type) {
			config.storageType = type;
			return this;
		}

		public Builder customConfig(Map<String, Object> customConfig) {
			config.customConfig = customConfig;
			return this;
		}

		public Builder customConfig(String key, Object value) {
			config.customConfig.put(key, value);
			return this;
		}

		public LearningStorageConfig build() {
			return config;
		}

	}

}

