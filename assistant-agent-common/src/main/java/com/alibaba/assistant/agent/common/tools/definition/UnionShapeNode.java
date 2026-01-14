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
import java.util.stream.Collectors;

/**
 * 联合类型节点 - 表示多种可能的类型。
 *
 * @author Assistant Agent Team
 * @since 1.0.0
 */
public class UnionShapeNode extends ShapeNode {

	private final List<ShapeNode> variants;

	public UnionShapeNode() {
		super();
		this.variants = new ArrayList<>();
	}

	public UnionShapeNode(List<ShapeNode> variants) {
		super();
		this.variants = new ArrayList<>(variants);
	}

	public UnionShapeNode(List<ShapeNode> variants, boolean optional, String description) {
		super(optional, description);
		this.variants = new ArrayList<>(variants);
	}

	/**
	 * 获取所有变体类型。
	 * @return 变体类型列表（不可变）
	 */
	public List<ShapeNode> getVariants() {
		return Collections.unmodifiableList(variants);
	}

	/**
	 * 添加变体类型。
	 * @param variant 变体类型
	 */
	public void addVariant(ShapeNode variant) {
		if (variant != null && !variants.contains(variant)) {
			variants.add(variant);
		}
	}

	/**
	 * 获取变体数量。
	 * @return 变体数量
	 */
	public int getVariantCount() {
		return variants.size();
	}

	@Override
	public String getPythonTypeHint() {
		if (variants.isEmpty()) {
			return "Any";
		}
		if (variants.size() == 1) {
			return variants.get(0).getPythonTypeHint();
		}

		String unionHint = "Union[" + variants.stream().map(ShapeNode::getPythonTypeHint).collect(Collectors.joining(", "))
				+ "]";

		if (optional) {
			return "Optional[" + unionHint + "]";
		}
		return unionHint;
	}

	@Override
	public String getTypeName() {
		return "union";
	}

	@Override
	public boolean isUnion() {
		return true;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o)
			return true;
		if (o == null || getClass() != o.getClass())
			return false;
		UnionShapeNode that = (UnionShapeNode) o;
		return Objects.equals(variants, that.variants);
	}

	@Override
	public int hashCode() {
		return Objects.hash(variants);
	}

	@Override
	public String toString() {
		return "UnionShapeNode{" + "variantCount=" + variants.size() + ", optional=" + optional + '}';
	}

}

