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

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;

/**
 * 对象类型节点 - 表示具有字段的对象结构。
 *
 * @author Assistant Agent Team
 * @since 1.0.0
 */
public class ObjectShapeNode extends ShapeNode {

	private final Map<String, ShapeNode> fields;

	public ObjectShapeNode() {
		super();
		this.fields = new LinkedHashMap<>();
	}

	public ObjectShapeNode(Map<String, ShapeNode> fields) {
		super();
		this.fields = new LinkedHashMap<>(fields);
	}

	public ObjectShapeNode(Map<String, ShapeNode> fields, boolean optional, String description) {
		super(optional, description);
		this.fields = new LinkedHashMap<>(fields);
	}

	/**
	 * 获取所有字段。
	 * @return 字段映射（不可变）
	 */
	public Map<String, ShapeNode> getFields() {
		return Collections.unmodifiableMap(fields);
	}

	/**
	 * 添加或更新字段。
	 * @param name 字段名
	 * @param shape 字段的 shape
	 */
	public void putField(String name, ShapeNode shape) {
		fields.put(name, shape);
	}

	/**
	 * 获取指定字段。
	 * @param name 字段名
	 * @return 字段的 shape，不存在返回 null
	 */
	public ShapeNode getField(String name) {
		return fields.get(name);
	}

	/**
	 * 检查是否包含字段。
	 * @param name 字段名
	 * @return true 表示包含
	 */
	public boolean hasField(String name) {
		return fields.containsKey(name);
	}

	/**
	 * 获取字段数量。
	 * @return 字段数量
	 */
	public int getFieldCount() {
		return fields.size();
	}

	@Override
	public String getPythonTypeHint() {
		if (optional) {
			return "Optional[Dict[str, Any]]";
		}
		return "Dict[str, Any]";
	}

	@Override
	public String getTypeName() {
		return "object";
	}

	@Override
	public boolean isObject() {
		return true;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o)
			return true;
		if (o == null || getClass() != o.getClass())
			return false;
		ObjectShapeNode that = (ObjectShapeNode) o;
		return Objects.equals(fields, that.fields);
	}

	@Override
	public int hashCode() {
		return Objects.hash(fields);
	}

	@Override
	public String toString() {
		return "ObjectShapeNode{" + "fieldCount=" + fields.size() + ", optional=" + optional + '}';
	}

}

