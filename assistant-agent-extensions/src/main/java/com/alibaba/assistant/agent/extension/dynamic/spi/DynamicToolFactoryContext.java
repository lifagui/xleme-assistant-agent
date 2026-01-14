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
package com.alibaba.assistant.agent.extension.dynamic.spi;

import com.alibaba.assistant.agent.extension.dynamic.naming.NameNormalizer;
import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * 动态工具工厂上下文。
 *
 * <p>包含工厂创建工具时所需的共享资源和配置。
 *
 * @author Assistant Agent Team
 * @since 1.0.0
 */
public class DynamicToolFactoryContext {

	private final ObjectMapper objectMapper;

	private final NameNormalizer nameNormalizer;

	private DynamicToolFactoryContext(Builder builder) {
		this.objectMapper = builder.objectMapper != null ? builder.objectMapper : new ObjectMapper();
		this.nameNormalizer = builder.nameNormalizer != null ? builder.nameNormalizer : NameNormalizer.defaultInstance();
	}

	/**
	 * 获取 ObjectMapper。
	 *
	 * @return ObjectMapper 实例
	 */
	public ObjectMapper getObjectMapper() {
		return objectMapper;
	}

	/**
	 * 获取命名归一化器。
	 *
	 * @return NameNormalizer 实例
	 */
	public NameNormalizer getNameNormalizer() {
		return nameNormalizer;
	}

	/**
	 * 创建构建器。
	 *
	 * @return 构建器实例
	 */
	public static Builder builder() {
		return new Builder();
	}

	/**
	 * 构建器类。
	 */
	public static class Builder {

		private ObjectMapper objectMapper;

		private NameNormalizer nameNormalizer;

		/**
		 * 设置 ObjectMapper。
		 *
		 * @param objectMapper ObjectMapper 实例
		 * @return 构建器
		 */
		public Builder objectMapper(ObjectMapper objectMapper) {
			this.objectMapper = objectMapper;
			return this;
		}

		/**
		 * 设置命名归一化器。
		 *
		 * @param nameNormalizer NameNormalizer 实例
		 * @return 构建器
		 */
		public Builder nameNormalizer(NameNormalizer nameNormalizer) {
			this.nameNormalizer = nameNormalizer;
			return this;
		}

		/**
		 * 构建上下文实例。
		 *
		 * @return DynamicToolFactoryContext 实例
		 */
		public DynamicToolFactoryContext build() {
			return new DynamicToolFactoryContext(this);
		}

	}

}

