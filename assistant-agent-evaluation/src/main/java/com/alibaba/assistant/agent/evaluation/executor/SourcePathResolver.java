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
package com.alibaba.assistant.agent.evaluation.executor;

import com.alibaba.assistant.agent.evaluation.model.CriterionResult;
import com.alibaba.assistant.agent.evaluation.model.EvaluationContext;

import java.util.Collection;
import java.util.Map;

/**
 * Utility for resolving source paths in batching configurations.
 * Supports paths like:
 * - "context.tools"
 * - "context.input.testCases"
 * - "dependencies.collect_tools.value"
 *
 * @author Assistant Agent Team
 */
public class SourcePathResolver {

	/**
	 * Resolve a source path to get the collection to be batched.
	 *
	 * @param sourcePath The path string (e.g., "context.tools" or "dependencies.criterionName.value")
	 * @param evaluationContext The evaluation context
	 * @param dependencyResults Map of dependency results
	 * @return The resolved object (might be a collection, single object, or null)
	 */
	public static Object resolve(String sourcePath,
	                              EvaluationContext evaluationContext,
	                              Map<String, CriterionResult> dependencyResults) {
		if (sourcePath == null || sourcePath.trim().isEmpty()) {
			return null;
		}

		String[] parts = sourcePath.split("\\.", 2);
		if (parts.length == 0) {
			return null;
		}

		String root = parts[0];
		String remainingPath = parts.length > 1 ? parts[1] : null;

		if ("context".equals(root)) {
			return resolveFromContext(remainingPath, evaluationContext);
		} else if ("dependencies".equals(root)) {
			return resolveFromDependencies(remainingPath, dependencyResults);
		}

		return null;
	}

	/**
	 * Resolve path from evaluation context.
	 * Supports paths like:
	 * - "input.userInput" -> context.getInput().get("userInput")
	 * - "executionResult.knowledgeSearchHits" -> context.getExecutionResult().get("knowledgeSearchHits")
	 * - "environment.sessionId" -> context.getEnvironment().get("sessionId")
	 * - "tools" (legacy, direct input access) -> context.getInput().get("tools")
	 */
	private static Object resolveFromContext(String path, EvaluationContext context) {
		if (context == null || path == null) {
			return null;
		}

		// Parse the first segment to determine which context map to use
		String[] parts = path.split("\\.", 2);
		String firstSegment = parts[0];
		String remainingPath = parts.length > 1 ? parts[1] : null;

		Map<String, Object> targetMap;

		// Determine which context map to access
		switch (firstSegment) {
			case "input":
				targetMap = context.getInput();
				break;
			case "executionResult":
				targetMap = context.getExecutionResult();
				break;
			case "environment":
				targetMap = context.getEnvironment();
				break;
			default:
				// Legacy behavior: if no specific prefix, assume it's in input
				targetMap = context.getInput();
				remainingPath = path; // Use the full path for navigation
				break;
		}

		if (targetMap == null) {
			return null;
		}

		// If no remaining path (e.g., just "context.input"), return the whole map
		if (remainingPath == null) {
			return targetMap;
		}

		// Navigate nested paths like "input.nested.field"
		return navigatePath(targetMap, remainingPath);
	}

	/**
	 * Resolve path from dependency results.
	 * Example: "collect_tools.value" -> dependencies.get("collect_tools").getValue()
	 */
	private static Object resolveFromDependencies(String path, Map<String, CriterionResult> dependencies) {
		if (dependencies == null || path == null) {
			return null;
		}

		String[] parts = path.split("\\.", 2);
		String criterionName = parts[0];
		String remainingPath = parts.length > 1 ? parts[1] : null;

		CriterionResult depResult = dependencies.get(criterionName);
		if (depResult == null) {
			return null;
		}

		// If no remaining path, return the whole result's value
		if (remainingPath == null) {
			return depResult.getValue();
		}

		// If there's a remaining path like "value.items", navigate into the result's value
		Object value = depResult.getValue();
		if (value == null) {
			return null;
		}

		// Handle special case: "value" refers to the result's value itself
		if ("value".equals(remainingPath)) {
			return value;
		}

		// Navigate into nested structure if value is a map
		if (value instanceof Map) {
			return navigatePath((Map<?, ?>) value, remainingPath);
		}

		return value;
	}

	/**
	 * Navigate a nested path in a map structure.
	 * Example: "input.testCases" navigates map.get("input").get("testCases")
	 */
	@SuppressWarnings("unchecked")
	private static Object navigatePath(Map<?, ?> map, String path) {
		if (map == null || path == null) {
			return null;
		}

		String[] parts = path.split("\\.", 2);
		String key = parts[0];
		Object value = map.get(key);

		if (value == null) {
			return null;
		}

		// If no more path segments, return current value
		if (parts.length == 1) {
			return value;
		}

		// Continue navigating if value is a map
		if (value instanceof Map) {
			return navigatePath((Map<?, ?>) value, parts[1]);
		}

		// Can't navigate further
		return null;
	}

	/**
	 * Check if an object is a collection type.
	 */
	public static boolean isCollection(Object obj) {
		if (obj == null) {
			return false;
		}
		return obj instanceof Collection || obj.getClass().isArray();
	}

	/**
	 * Convert an object to a collection if possible.
	 * Returns null if not a collection.
	 */
	@SuppressWarnings("unchecked")
	public static Collection<?> toCollection(Object obj) {
		if (obj == null) {
			return null;
		}
		if (obj instanceof Collection) {
			return (Collection<?>) obj;
		}
		if (obj.getClass().isArray()) {
			return java.util.Arrays.asList((Object[]) obj);
		}
		return null;
	}
}

