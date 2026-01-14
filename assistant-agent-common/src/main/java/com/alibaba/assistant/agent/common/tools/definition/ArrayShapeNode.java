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
 * 数组类型节点 - 表示数组/列表结构。
 *
 * @author Assistant Agent Team
 * @since 1.0.0
 */
public class ArrayShapeNode extends ShapeNode {

	private ShapeNode itemShape;

	public ArrayShapeNode() {
		super();
		this.itemShape = new UnknownShapeNode();
	}

	public ArrayShapeNode(ShapeNode itemShape) {
		super();
		this.itemShape = itemShape != null ? itemShape : new UnknownShapeNode();
	}

	public ArrayShapeNode(ShapeNode itemShape, boolean optional, String description) {
		super(optional, description);
		this.itemShape = itemShape != null ? itemShape : new UnknownShapeNode();
	}

	/**
	 * 获取数组元素的 shape。
	 * @return 元素 shape
	 */
	public ShapeNode getItemShape() {
		return itemShape;
	}

	/**
	 * 设置数组元素的 shape。
	 * @param itemShape 元素 shape
	 */
	public void setItemShape(ShapeNode itemShape) {
		this.itemShape = itemShape != null ? itemShape : new UnknownShapeNode();
	}

	@Override
	public String getPythonTypeHint() {
		String itemHint = itemShape.getPythonTypeHint();
		String hint = "List[" + itemHint + "]";
		if (optional) {
			return "Optional[" + hint + "]";
		}
		return hint;
	}

	@Override
	public String getTypeName() {
		return "array";
	}

	@Override
	public boolean isArray() {
		return true;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o)
			return true;
		if (o == null || getClass() != o.getClass())
			return false;
		ArrayShapeNode that = (ArrayShapeNode) o;
		return Objects.equals(itemShape, that.itemShape);
	}

	@Override
	public int hashCode() {
		return Objects.hash(itemShape);
	}

	@Override
	public String toString() {
		return "ArrayShapeNode{" + "itemShape=" + itemShape + ", optional=" + optional + '}';
	}

}

