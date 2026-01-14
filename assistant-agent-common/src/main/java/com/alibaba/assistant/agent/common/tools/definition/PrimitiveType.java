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
 * 原始类型枚举 - 用于 PrimitiveShapeNode。
 *
 * @author Assistant Agent Team
 * @since 1.0.0
 */
public enum PrimitiveType {

	/**
	 * 字符串类型
	 */
	STRING("string", "str"),

	/**
	 * 整数类型
	 */
	INTEGER("integer", "int"),

	/**
	 * 数值类型
	 */
	NUMBER("number", "float"),

	/**
	 * 布尔类型
	 */
	BOOLEAN("boolean", "bool"),

	/**
	 * 空类型
	 */
	NULL("null", "None");

	private final String jsonType;

	private final String pythonType;

	PrimitiveType(String jsonType, String pythonType) {
		this.jsonType = jsonType;
		this.pythonType = pythonType;
	}

	public String getJsonType() {
		return jsonType;
	}

	public String getPythonType() {
		return pythonType;
	}

	/**
	 * 从 JSON 类型名解析。
	 * @param jsonType JSON 类型名
	 * @return 对应的 PrimitiveType，未知返回 STRING
	 */
	public static PrimitiveType fromJsonType(String jsonType) {
		if (jsonType == null) {
			return STRING;
		}
		for (PrimitiveType type : values()) {
			if (type.jsonType.equalsIgnoreCase(jsonType)) {
				return type;
			}
		}
		return STRING;
	}

	/**
	 * 从 Java 对象推断类型。
	 * @param value Java 对象
	 * @return 对应的 PrimitiveType
	 */
	public static PrimitiveType fromValue(Object value) {
		if (value == null) {
			return NULL;
		}
		if (value instanceof String) {
			return STRING;
		}
		if (value instanceof Integer || value instanceof Long) {
			return INTEGER;
		}
		if (value instanceof Number) {
			return NUMBER;
		}
		if (value instanceof Boolean) {
			return BOOLEAN;
		}
		return STRING;
	}

}

