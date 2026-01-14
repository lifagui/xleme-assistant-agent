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

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

/**
 * 参数节点 - 描述单个参数的结构化信息。
 *
 * <p>从 JSON Schema 解析得到，包含参数的名称、类型、描述、约束等信息。
 *
 * @author Assistant Agent Team
 * @since 1.0.0
 */
public class ParameterNode {

	private final String name;

	private final ParameterType type;

	private final String description;

	private final boolean required;

	private final Object defaultValue;

	private final String format;

	private final List<Object> enumValues;

	private final String constraint;

	private final ParameterNode items;

	private final List<ParameterNode> properties;

	private final List<ParameterNode> unionVariants;

	private ParameterNode(Builder builder) {
		this.name = builder.name;
		this.type = builder.type;
		this.description = builder.description;
		this.required = builder.required;
		this.defaultValue = builder.defaultValue;
		this.format = builder.format;
		this.enumValues = builder.enumValues != null ? Collections.unmodifiableList(builder.enumValues)
				: Collections.emptyList();
		this.constraint = builder.constraint;
		this.items = builder.items;
		this.properties = builder.properties != null ? Collections.unmodifiableList(builder.properties)
				: Collections.emptyList();
		this.unionVariants = builder.unionVariants != null ? Collections.unmodifiableList(builder.unionVariants)
				: Collections.emptyList();
	}

	/**
	 * 获取参数名称。
	 * @return 参数名称
	 */
	public String getName() {
		return name;
	}

	/**
	 * 获取参数类型。
	 * @return 参数类型
	 */
	public ParameterType getType() {
		return type;
	}

	/**
	 * 获取参数描述。
	 * @return 参数描述
	 */
	public String getDescription() {
		return description;
	}

	/**
	 * 是否必填参数。
	 * @return true 表示必填
	 */
	public boolean isRequired() {
		return required;
	}

	/**
	 * 获取默认值。
	 * @return 默认值，无默认值时返回 null
	 */
	public Object getDefaultValue() {
		return defaultValue;
	}

	/**
	 * 获取格式约束（如 date-time, email 等）。
	 * @return 格式约束
	 */
	public String getFormat() {
		return format;
	}

	/**
	 * 获取枚举候选值。
	 * @return 枚举候选值列表
	 */
	public List<Object> getEnumValues() {
		return enumValues;
	}

	/**
	 * 获取约束说明。
	 * @return 约束说明
	 */
	public String getConstraint() {
		return constraint;
	}

	/**
	 * 获取数组元素类型（当 type 为 ARRAY 时有效）。
	 * @return 数组元素的 ParameterNode
	 */
	public ParameterNode getItems() {
		return items;
	}

	/**
	 * 获取嵌套对象字段（当 type 为 OBJECT 时有效）。
	 * @return 嵌套对象的字段列表
	 */
	public List<ParameterNode> getProperties() {
		return properties;
	}

	/**
	 * 获取联合类型候选（当有多个可能类型时有效）。
	 * @return 联合类型候选列表
	 */
	public List<ParameterNode> getUnionVariants() {
		return unionVariants;
	}

	/**
	 * 判断是否有默认值。
	 * @return true 表示有默认值
	 */
	public boolean hasDefaultValue() {
		return defaultValue != null;
	}

	/**
	 * 判断是否有枚举值。
	 * @return true 表示有枚举值
	 */
	public boolean hasEnumValues() {
		return enumValues != null && !enumValues.isEmpty();
	}

	/**
	 * 判断是否有格式约束。
	 * @return true 表示有格式约束
	 */
	public boolean hasFormat() {
		return format != null && !format.isEmpty();
	}

	/**
	 * 判断是否是复合类型（对象或数组）。
	 * @return true 表示是复合类型
	 */
	public boolean isCompositeType() {
		return type == ParameterType.OBJECT || type == ParameterType.ARRAY;
	}

	/**
	 * 获取 Python 类型提示字符串。
	 * @return Python 类型提示
	 */
	public String getPythonTypeHint() {
		if (hasEnumValues()) {
			return "Literal[" + formatEnumValues() + "]";
		}

		switch (type) {
			case ARRAY:
				if (items != null) {
					return "List[" + items.getPythonTypeHint() + "]";
				}
				return "List[Any]";
			case OBJECT:
				if (properties != null && !properties.isEmpty()) {
					return "Dict[str, Any]";
				}
				return "Dict[str, Any]";
			default:
				return type.getPythonType();
		}
	}

	private String formatEnumValues() {
		StringBuilder sb = new StringBuilder();
		for (int i = 0; i < enumValues.size(); i++) {
			if (i > 0) {
				sb.append(", ");
			}
			Object value = enumValues.get(i);
			if (value instanceof String) {
				sb.append("\"").append(value).append("\"");
			}
			else {
				sb.append(value);
			}
		}
		return sb.toString();
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
		ParameterNode that = (ParameterNode) o;
		return required == that.required && Objects.equals(name, that.name) && type == that.type
				&& Objects.equals(description, that.description);
	}

	@Override
	public int hashCode() {
		return Objects.hash(name, type, required);
	}

	@Override
	public String toString() {
		return "ParameterNode{" + "name='" + name + '\'' + ", type=" + type + ", required=" + required + ", description='"
				+ description + '\'' + '}';
	}

	/**
	 * ParameterNode 构建器。
	 */
	public static class Builder {

		private String name;

		private ParameterType type = ParameterType.UNKNOWN;

		private String description;

		private boolean required = false;

		private Object defaultValue;

		private String format;

		private List<Object> enumValues;

		private String constraint;

		private ParameterNode items;

		private List<ParameterNode> properties;

		private List<ParameterNode> unionVariants;

		public Builder name(String name) {
			this.name = name;
			return this;
		}

		public Builder type(ParameterType type) {
			this.type = type;
			return this;
		}

		public Builder description(String description) {
			this.description = description;
			return this;
		}

		public Builder required(boolean required) {
			this.required = required;
			return this;
		}

		public Builder defaultValue(Object defaultValue) {
			this.defaultValue = defaultValue;
			return this;
		}

		public Builder format(String format) {
			this.format = format;
			return this;
		}

		public Builder enumValues(List<Object> enumValues) {
			this.enumValues = new ArrayList<>(enumValues);
			return this;
		}

		public Builder addEnumValue(Object enumValue) {
			if (this.enumValues == null) {
				this.enumValues = new ArrayList<>();
			}
			this.enumValues.add(enumValue);
			return this;
		}

		public Builder constraint(String constraint) {
			this.constraint = constraint;
			return this;
		}

		public Builder items(ParameterNode items) {
			this.items = items;
			return this;
		}

		public Builder properties(List<ParameterNode> properties) {
			this.properties = new ArrayList<>(properties);
			return this;
		}

		public Builder addProperty(ParameterNode property) {
			if (this.properties == null) {
				this.properties = new ArrayList<>();
			}
			this.properties.add(property);
			return this;
		}

		public Builder unionVariants(List<ParameterNode> unionVariants) {
			this.unionVariants = new ArrayList<>(unionVariants);
			return this;
		}

		public Builder addUnionVariant(ParameterNode variant) {
			if (this.unionVariants == null) {
				this.unionVariants = new ArrayList<>();
			}
			this.unionVariants.add(variant);
			return this;
		}

		public ParameterNode build() {
			return new ParameterNode(this);
		}

	}

}

