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
 * 原始类型节点 - 表示基本数据类型。
 *
 * @author Assistant Agent Team
 * @since 1.0.0
 */
public class PrimitiveShapeNode extends ShapeNode {

	private final PrimitiveType type;

	public PrimitiveShapeNode(PrimitiveType type) {
		super();
		this.type = type;
	}

	public PrimitiveShapeNode(PrimitiveType type, boolean optional, String description) {
		super(optional, description);
		this.type = type;
	}

	/**
	 * 获取原始类型。
	 * @return 原始类型
	 */
	public PrimitiveType getType() {
		return type;
	}

	@Override
	public String getPythonTypeHint() {
		String hint = type.getPythonType();
		if (optional && type != PrimitiveType.NULL) {
			return "Optional[" + hint + "]";
		}
		return hint;
	}

	@Override
	public String getTypeName() {
		return type.getJsonType();
	}

	@Override
	public boolean isPrimitive() {
		return true;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o)
			return true;
		if (o == null || getClass() != o.getClass())
			return false;
		PrimitiveShapeNode that = (PrimitiveShapeNode) o;
		return type == that.type;
	}

	@Override
	public int hashCode() {
		return Objects.hash(type);
	}

	@Override
	public String toString() {
		return "PrimitiveShapeNode{" + "type=" + type + ", optional=" + optional + '}';
	}

}

