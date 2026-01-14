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
package com.alibaba.assistant.agent.evaluation.model;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * Evaluation input context - contains only the input data and necessary environment information.
 * This context is immutable after construction and represents the input for a single evaluation run.
 *
 * Design Notes:
 * - This context is read-only for evaluators and execution framework
 * - No execution state or results are stored here (moved to execution contexts)
 * - Thread-safe by design as it's immutable
 * - Execution state and result propagation is handled by SuiteExecutionContext and CriterionExecutionContext
 *
 * @author Assistant Agent Team
 */
public class EvaluationContext {

	/**
	 * Original input data (user input, test data, etc.)
	 * Immutable after construction
	 */
	private final Map<String, Object> input;

	/**
	 * Execution result from the evaluated target
	 * (code execution result, tool call trace, logs, etc.)
	 * Immutable after construction. Can be empty if evaluation is input-only.
	 */
	private final Map<String, Object> executionResult;

	/**
	 * Environment information (session id, tenant, runtime mode, model version, etc.)
	 * Contains only metadata that evaluators actually need.
	 * Immutable after construction
	 */
	private final Map<String, Object> environment;

	// Constructors

	/**
	 * Constructor with all parameters
	 */
	public EvaluationContext(Map<String, Object> input,
	                       Map<String, Object> executionResult,
	                       Map<String, Object> environment) {
		this.input = input != null ? Collections.unmodifiableMap(new HashMap<>(input)) : Collections.emptyMap();
		this.executionResult = executionResult != null ? Collections.unmodifiableMap(new HashMap<>(executionResult)) : Collections.emptyMap();
		this.environment = environment != null ? Collections.unmodifiableMap(new HashMap<>(environment)) : Collections.emptyMap();
	}

	/**
	 * Constructor with input and execution result only
	 */
	public EvaluationContext(Map<String, Object> input, Map<String, Object> executionResult) {
		this(input, executionResult, null);
	}

	/**
	 * Constructor with input only
	 */
	public EvaluationContext(Map<String, Object> input) {
		this(input, null, null);
	}

	/**
	 * Default constructor for empty context
	 */
	public EvaluationContext() {
		this(null, null, null);
	}

	// Read-only Getters

	/**
	 * Get input data (read-only)
	 */
	public Map<String, Object> getInput() {
		return input;
	}

	/**
	 * Get execution result data (read-only)
	 */
	public Map<String, Object> getExecutionResult() {
		return executionResult;
	}

	/**
	 * Get environment data (read-only)
	 */
	public Map<String, Object> getEnvironment() {
		return environment;
	}

	/**
	 * Get a specific input value
	 */
	public Object getInputValue(String key) {
		return input.get(key);
	}

	/**
	 * Get a specific execution result value
	 */
	public Object getExecutionResultValue(String key) {
		return executionResult.get(key);
	}

	/**
	 * Get an environment value
	 */
	public Object getEnvironmentValue(String key) {
		return environment.get(key);
	}
}

