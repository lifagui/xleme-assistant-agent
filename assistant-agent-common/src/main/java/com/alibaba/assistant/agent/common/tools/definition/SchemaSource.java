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
 * Schema 来源枚举。
 *
 * @author Assistant Agent Team
 * @since 1.0.0
 */
public enum SchemaSource {

	/**
	 * 工具作者声明的 schema
	 */
	DECLARED("declared"),

	/**
	 * 运行时观测得到的 schema
	 */
	OBSERVED("observed");

	private final String value;

	SchemaSource(String value) {
		this.value = value;
	}

	public String getValue() {
		return value;
	}

}

