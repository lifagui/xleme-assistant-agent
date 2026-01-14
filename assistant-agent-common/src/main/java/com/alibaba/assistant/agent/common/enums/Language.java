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
package com.alibaba.assistant.agent.common.enums;

/**
 * Supported programming languages for code generation and execution.
 *
 * @author Assistant Agent Team
 * @since 1.0.0
 */
public enum Language {

	/**
	 * Python language support
	 */
	PYTHON("python", "py"),

	/**
	 * JavaScript language support (future extension)
	 */
	JAVASCRIPT("javascript", "js"),

	/**
	 * Java language support (future extension)
	 */
	JAVA("java", "java");

	private final String name;

	private final String extension;

	Language(String name, String extension) {
		this.name = name;
		this.extension = extension;
	}

	public String getName() {
		return name;
	}

	public String getExtension() {
		return extension;
	}

	public static Language fromName(String name) {
		for (Language lang : values()) {
			if (lang.name.equalsIgnoreCase(name)) {
				return lang;
			}
		}
		throw new IllegalArgumentException("Unsupported language: " + name);
	}

}

