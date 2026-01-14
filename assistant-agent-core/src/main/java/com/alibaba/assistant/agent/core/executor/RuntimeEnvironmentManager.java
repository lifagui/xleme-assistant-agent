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
package com.alibaba.assistant.agent.core.executor;

import com.alibaba.assistant.agent.core.context.CodeContext;
import com.alibaba.assistant.agent.common.enums.Language;

import java.util.Map;

/**
 * Manages the runtime environment for code execution.
 * Handles imports, environment setup, and code wrapping for different languages.
 *
 * @author Assistant Agent Team
 * @since 1.0.0
 */
public interface RuntimeEnvironmentManager {

	/**
	 * Get the supported language
	 */
	Language getSupportedLanguage();

	/**
	 * Generate imports based on the code context
	 * @param context the code context containing registered functions
	 * @return import statements as a string
	 */
	String generateImports(CodeContext context);

	/**
	 * Wrap a function code with necessary boilerplate
	 * @param functionCode the function code (function definition only)
	 * @param context the code context
	 * @return complete executable code
	 */
	String wrapFunctionCode(String functionCode, CodeContext context);

	/**
	 * Generate code to call a function
	 * @param functionName the function name to call
	 * @param args arguments as a map of parameter names to values
	 * @return code to call the function
	 */
	String generateFunctionCall(String functionName, Map<String, Object> args);

	/**
	 * Extract function name from code (if not explicitly provided)
	 * @param code the function code
	 * @return extracted function name, or null if not found
	 */
	String extractFunctionName(String code);
}

