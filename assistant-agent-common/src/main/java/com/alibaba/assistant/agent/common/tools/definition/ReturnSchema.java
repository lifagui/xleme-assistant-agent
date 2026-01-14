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

import java.time.Instant;
import java.util.Collections;
import java.util.EnumSet;
import java.util.Objects;
import java.util.Set;

/**
 * 返回值结构 schema - 描述工具返回值的结构。
 *
 * <p>可以来自工具作者声明或运行时观测。
 *
 * @author Assistant Agent Team
 * @since 1.0.0
 */
public class ReturnSchema {

	private final String toolName;

	private final ShapeNode successShape;

	private final ShapeNode errorShape;

	private final String description;

	private final String typeHint;

	private final long sampleCount;

	private final Instant lastUpdatedAt;

	private final Set<SchemaSource> sources;

	private ReturnSchema(Builder builder) {
		this.toolName = builder.toolName;
		this.successShape = builder.successShape;
		this.errorShape = builder.errorShape;
		this.description = builder.description;
		this.typeHint = builder.typeHint;
		this.sampleCount = builder.sampleCount;
		this.lastUpdatedAt = builder.lastUpdatedAt;
		this.sources = builder.sources != null ? Collections.unmodifiableSet(builder.sources)
				: Collections.singleton(SchemaSource.DECLARED);
	}

	/**
	 * 获取工具名称。
	 * @return 工具名称
	 */
	public String getToolName() {
		return toolName;
	}

	/**
	 * 获取成功返回的 shape。
	 * @return 成功返回的 shape
	 */
	public ShapeNode getSuccessShape() {
		return successShape;
	}

	/**
	 * 获取错误返回的 shape。
	 * @return 错误返回的 shape，可能为 null
	 */
	public ShapeNode getErrorShape() {
		return errorShape;
	}

	/**
	 * 获取返回值描述。
	 * @return 返回值描述
	 */
	public String getDescription() {
		return description;
	}

	/**
	 * 获取类型提示（用于代码生成）。
	 * @return 类型提示
	 */
	public String getTypeHint() {
		return typeHint;
	}

	/**
	 * 获取采样次数。
	 * @return 采样次数
	 */
	public long getSampleCount() {
		return sampleCount;
	}

	/**
	 * 获取最后更新时间。
	 * @return 最后更新时间
	 */
	public Instant getLastUpdatedAt() {
		return lastUpdatedAt;
	}

	/**
	 * 获取 schema 来源。
	 * @return 来源集合
	 */
	public Set<SchemaSource> getSources() {
		return sources;
	}

	/**
	 * 判断是否来自声明。
	 * @return true 表示来自声明
	 */
	public boolean isDeclared() {
		return sources.contains(SchemaSource.DECLARED);
	}

	/**
	 * 判断是否来自观测。
	 * @return true 表示来自观测
	 */
	public boolean isObserved() {
		return sources.contains(SchemaSource.OBSERVED);
	}

	/**
	 * 获取主要的 Python 类型提示。
	 * @return Python 类型提示
	 */
	public String getPythonTypeHint() {
		if (typeHint != null && !typeHint.isEmpty()) {
			return typeHint;
		}
		if (successShape != null) {
			return successShape.getPythonTypeHint();
		}
		return "Dict[str, Any]";
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
		ReturnSchema that = (ReturnSchema) o;
		return Objects.equals(toolName, that.toolName) && Objects.equals(successShape, that.successShape);
	}

	@Override
	public int hashCode() {
		return Objects.hash(toolName, successShape);
	}

	@Override
	public String toString() {
		return "ReturnSchema{" + "toolName='" + toolName + '\'' + ", sampleCount=" + sampleCount + ", sources=" + sources
				+ '}';
	}

	/**
	 * ReturnSchema 构建器。
	 */
	public static class Builder {

		private String toolName;

		private ShapeNode successShape;

		private ShapeNode errorShape;

		private String description;

		private String typeHint;

		private long sampleCount = 0;

		private Instant lastUpdatedAt = Instant.now();

		private Set<SchemaSource> sources = EnumSet.of(SchemaSource.DECLARED);

		public Builder toolName(String toolName) {
			this.toolName = toolName;
			return this;
		}

		public Builder successShape(ShapeNode successShape) {
			this.successShape = successShape;
			return this;
		}

		public Builder errorShape(ShapeNode errorShape) {
			this.errorShape = errorShape;
			return this;
		}

		public Builder description(String description) {
			this.description = description;
			return this;
		}

		public Builder typeHint(String typeHint) {
			this.typeHint = typeHint;
			return this;
		}

		public Builder sampleCount(long sampleCount) {
			this.sampleCount = sampleCount;
			return this;
		}

		public Builder lastUpdatedAt(Instant lastUpdatedAt) {
			this.lastUpdatedAt = lastUpdatedAt;
			return this;
		}

		public Builder sources(Set<SchemaSource> sources) {
			this.sources = EnumSet.copyOf(sources);
			return this;
		}

		public Builder addSource(SchemaSource source) {
			this.sources.add(source);
			return this;
		}

		public ReturnSchema build() {
			return new ReturnSchema(this);
		}

	}

}

