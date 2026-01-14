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

/**
 * 未知类型节点 - 表示无法确定的类型。
 *
 * @author Assistant Agent Team
 * @since 1.0.0
 */
public class UnknownShapeNode extends ShapeNode {

	public UnknownShapeNode() {
		super();
	}

	public UnknownShapeNode(boolean optional, String description) {
		super(optional, description);
	}

	@Override
	public String getPythonTypeHint() {
		if (optional) {
			return "Optional[Any]";
		}
		return "Any";
	}

	@Override
	public String getTypeName() {
		return "unknown";
	}

	@Override
	public boolean isUnknown() {
		return true;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o)
			return true;
		return o != null && getClass() == o.getClass();
	}

	@Override
	public int hashCode() {
		return UnknownShapeNode.class.hashCode();
	}

	@Override
	public String toString() {
		return "UnknownShapeNode{optional=" + optional + '}';
	}

}

