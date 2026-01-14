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
package com.alibaba.assistant.agent.common.tools.definition;

import java.util.Objects;

/**
 * CodeactToolDefinition 的默认实现。
 *
 * @author Assistant Agent Team
 * @since 1.0.0
 */
public class DefaultCodeactToolDefinition implements CodeactToolDefinition {

	private final String name;

	private final String description;

	private final String inputSchema;

	private final ParameterTree parameterTree;

	private final ReturnSchema declaredReturnSchema;

	private final String returnDescription;

	private final String returnTypeHint;

	private DefaultCodeactToolDefinition(Builder builder) {
		this.name = builder.name;
		this.description = builder.description;
		this.inputSchema = builder.inputSchema;
		this.parameterTree = builder.parameterTree != null ? builder.parameterTree : ParameterTree.empty();
		this.declaredReturnSchema = builder.declaredReturnSchema;
		this.returnDescription = builder.returnDescription;
		this.returnTypeHint = builder.returnTypeHint != null ? builder.returnTypeHint : "Dict[str, Any]";
	}

	@Override
	public String name() {
		return name;
	}

	@Override
	public String description() {
		return description;
	}

	@Override
	public String inputSchema() {
		return inputSchema;
	}

	@Override
	public ParameterTree parameterTree() {
		return parameterTree;
	}

	@Override
	public ReturnSchema declaredReturnSchema() {
		return declaredReturnSchema;
	}

	@Override
	public String returnDescription() {
		return returnDescription;
	}

	@Override
	public String returnTypeHint() {
		return returnTypeHint;
	}

	/**
	 * 创建构建器实例。
	 * @return 构建器
	 */
	public static Builder builder() {
		return new Builder();
	}

	@Override
	public boolean equals(Object o) {
		if (this == o)
			return true;
		if (o == null || getClass() != o.getClass())
			return false;
		DefaultCodeactToolDefinition that = (DefaultCodeactToolDefinition) o;
		return Objects.equals(name, that.name);
	}

	@Override
	public int hashCode() {
		return Objects.hash(name);
	}

	@Override
	public String toString() {
		return "DefaultCodeactToolDefinition{" + "name='" + name + '\'' + ", description='" + description + '\'' + '}';
	}

	/**
	 * DefaultCodeactToolDefinition 构建器。
	 */
	public static class Builder {

		private String name;

		private String description;

		private String inputSchema;

		private ParameterTree parameterTree;

		private ReturnSchema declaredReturnSchema;

		private String returnDescription;

		private String returnTypeHint;

		public Builder name(String name) {
			this.name = name;
			return this;
		}

		public Builder description(String description) {
			this.description = description;
			return this;
		}

		public Builder inputSchema(String inputSchema) {
			this.inputSchema = inputSchema;
			return this;
		}

		public Builder parameterTree(ParameterTree parameterTree) {
			this.parameterTree = parameterTree;
			return this;
		}

		public Builder declaredReturnSchema(ReturnSchema declaredReturnSchema) {
			this.declaredReturnSchema = declaredReturnSchema;
			return this;
		}

		public Builder returnDescription(String returnDescription) {
			this.returnDescription = returnDescription;
			return this;
		}

		public Builder returnTypeHint(String returnTypeHint) {
			this.returnTypeHint = returnTypeHint;
			return this;
		}

		public DefaultCodeactToolDefinition build() {
			Objects.requireNonNull(name, "name cannot be null");
			return new DefaultCodeactToolDefinition(this);
		}

	}

}

