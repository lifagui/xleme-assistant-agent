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
 * 返回值结构节点 - 抽象基类。
 *
 * <p>描述工具返回值的结构，用于生成返回值文档和类型提示。
 *
 * @author Assistant Agent Team
 * @since 1.0.0
 */
public abstract class ShapeNode {

	protected boolean optional;

	protected String description;

	protected ShapeNode() {
		this.optional = false;
		this.description = null;
	}

	protected ShapeNode(boolean optional, String description) {
		this.optional = optional;
		this.description = description;
	}

	/**
	 * 是否可选（可能为 null）。
	 * @return true 表示可选
	 */
	public boolean isOptional() {
		return optional;
	}

	/**
	 * 设置是否可选。
	 * @param optional 是否可选
	 */
	public void setOptional(boolean optional) {
		this.optional = optional;
	}

	/**
	 * 获取描述。
	 * @return 描述
	 */
	public String getDescription() {
		return description;
	}

	/**
	 * 设置描述。
	 * @param description 描述
	 */
	public void setDescription(String description) {
		this.description = description;
	}

	/**
	 * 获取 Python 类型提示。
	 * @return Python 类型提示字符串
	 */
	public abstract String getPythonTypeHint();

	/**
	 * 获取节点类型名称。
	 * @return 节点类型名称
	 */
	public abstract String getTypeName();

	/**
	 * 判断是否是原始类型。
	 * @return true 表示是原始类型
	 */
	public boolean isPrimitive() {
		return false;
	}

	/**
	 * 判断是否是对象类型。
	 * @return true 表示是对象类型
	 */
	public boolean isObject() {
		return false;
	}

	/**
	 * 判断是否是数组类型。
	 * @return true 表示是数组类型
	 */
	public boolean isArray() {
		return false;
	}

	/**
	 * 判断是否是联合类型。
	 * @return true 表示是联合类型
	 */
	public boolean isUnion() {
		return false;
	}

	/**
	 * 判断是否是未知类型。
	 * @return true 表示是未知类型
	 */
	public boolean isUnknown() {
		return false;
	}

}

