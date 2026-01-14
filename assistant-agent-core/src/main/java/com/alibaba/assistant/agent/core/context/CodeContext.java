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
package com.alibaba.assistant.agent.core.context;

import com.alibaba.assistant.agent.core.model.GeneratedCode;
import com.alibaba.assistant.agent.common.enums.Language;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Manages the code context for a specific programming language.
 * This is an in-memory structure that holds all registered functions and their metadata.
 * It should be restored from Store on session recovery.
 *
 * @author Assistant Agent Team
 * @since 1.0.0
 */
public class CodeContext {

	/**
	 * Programming language for this context
	 */
	private final Language language;

	/**
	 * Map of function name to GeneratedCode
	 */
	private final Map<String, GeneratedCode> functions;

	/**
	 * Set of required imports (auto-computed)
	 */
	private final Set<String> requiredImports;

	/**
	 * Last update timestamp
	 */
	private long lastUpdated;

	public CodeContext(Language language) {
		this.language = language;
		this.functions = new ConcurrentHashMap<>();
		this.requiredImports = new LinkedHashSet<>();
		this.lastUpdated = System.currentTimeMillis();
	}

	/**
	 * Register a new function or update an existing one
	 */
	public void registerFunction(GeneratedCode code) {
		if (code == null || code.getFunctionName() == null) {
			throw new IllegalArgumentException("Code and function name cannot be null");
		}

		if (code.getLanguage() != this.language) {
			throw new IllegalArgumentException(
				"Language mismatch: expected " + this.language + ", got " + code.getLanguage()
			);
		}

		functions.put(code.getFunctionName(), code);
		this.lastUpdated = System.currentTimeMillis();
	}

	/**
	 * Get a function by name
	 */
	public Optional<GeneratedCode> getFunction(String functionName) {
		return Optional.ofNullable(functions.get(functionName));
	}

	/**
	 * Get all registered functions
	 */
	public Collection<GeneratedCode> getAllFunctions() {
		return Collections.unmodifiableCollection(functions.values());
	}

	/**
	 * Check if a function exists
	 */
	public boolean hasFunction(String functionName) {
		return functions.containsKey(functionName);
	}

	/**
	 * Get function count
	 */
	public int getFunctionCount() {
		return functions.size();
	}

	/**
	 * Get all function names
	 */
	public Set<String> getFunctionNames() {
		return Collections.unmodifiableSet(functions.keySet());
	}

	/**
	 * Add a required import
	 */
	public void addImport(String importStatement) {
		requiredImports.add(importStatement);
		this.lastUpdated = System.currentTimeMillis();
	}

	/**
	 * Get all required imports
	 */
	public Set<String> getRequiredImports() {
		return Collections.unmodifiableSet(requiredImports);
	}

	/**
	 * Clear all imports
	 */
	public void clearImports() {
		requiredImports.clear();
		this.lastUpdated = System.currentTimeMillis();
	}

	/**
	 * Get the programming language
	 */
	public Language getLanguage() {
		return language;
	}

	/**
	 * Get last update timestamp
	 */
	public long getLastUpdated() {
		return lastUpdated;
	}

	/**
	 * Clear all functions
	 */
	public void clear() {
		functions.clear();
		requiredImports.clear();
		this.lastUpdated = System.currentTimeMillis();
	}

	@Override
	public String toString() {
		return "CodeContext{" +
				"language=" + language +
				", functionCount=" + functions.size() +
				", importCount=" + requiredImports.size() +
				'}';
	}
}

