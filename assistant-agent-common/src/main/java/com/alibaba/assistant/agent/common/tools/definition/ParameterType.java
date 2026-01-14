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
 * 参数类型枚举 - 对应 JSON Schema 的 type 字段。
 *
 * @author Assistant Agent Team
 * @since 1.0.0
 */
public enum ParameterType {

	/**
	 * 字符串类型
	 */
	STRING("string", "str"),

	/**
	 * 整数类型
	 */
	INTEGER("integer", "int"),

	/**
	 * 数值类型（浮点数）
	 */
	NUMBER("number", "float"),

	/**
	 * 布尔类型
	 */
	BOOLEAN("boolean", "bool"),

	/**
	 * 对象类型
	 */
	OBJECT("object", "Dict[str, Any]"),

	/**
	 * 数组类型
	 */
	ARRAY("array", "List"),

	/**
	 * 空类型
	 */
	NULL("null", "None"),

	/**
	 * 未知类型
	 */
	UNKNOWN("unknown", "Any");

	private final String jsonSchemaType;

	private final String pythonType;

	ParameterType(String jsonSchemaType, String pythonType) {
		this.jsonSchemaType = jsonSchemaType;
		this.pythonType = pythonType;
	}

	/**
	 * 获取 JSON Schema 类型名称。
	 * @return JSON Schema 类型名称
	 */
	public String getJsonSchemaType() {
		return jsonSchemaType;
	}

	/**
	 * 获取 Python 类型名称。
	 * @return Python 类型名称
	 */
	public String getPythonType() {
		return pythonType;
	}

	/**
	 * 从 JSON Schema 类型名称解析枚举值。
	 * @param jsonSchemaType JSON Schema 类型名称
	 * @return 对应的 ParameterType，未知类型返回 UNKNOWN
	 */
	public static ParameterType fromJsonSchemaType(String jsonSchemaType) {
		if (jsonSchemaType == null) {
			return UNKNOWN;
		}
		for (ParameterType type : values()) {
			if (type.jsonSchemaType.equalsIgnoreCase(jsonSchemaType)) {
				return type;
			}
		}
		return UNKNOWN;
	}

}

