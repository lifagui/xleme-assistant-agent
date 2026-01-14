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
import java.util.Map;

/**
 * Criterion execution context - provides all the information that a single criterion
 * evaluation needs, including input context, dependency results, and criterion configuration.
 *
 * @author Assistant Agent Team
 */
public class CriterionExecutionContext {

	/** The specific criterion being evaluated */
	private final EvaluationCriterion criterion;

	/** Read-only input context */
	private final EvaluationContext inputContext;

	/**
	 * Results from criteria that this criterion depends on
	 * Only contains results for criteria listed in criterion.getDependsOn()
	 * Read-only snapshot taken at the time this context was created
	 */
	private final Map<String, CriterionResult> dependencyResults;

	/**
	 * Extra bindings for batch-level data in batching scenarios
	 */
	private final Map<String, Object> extraBindings;

	// Constructors

	public CriterionExecutionContext(EvaluationCriterion criterion,
	                               EvaluationContext inputContext,
	                               Map<String, CriterionResult> dependencyResults) {
		this(criterion, inputContext, dependencyResults, Collections.emptyMap());
	}

	public CriterionExecutionContext(EvaluationCriterion criterion,
	                               EvaluationContext inputContext,
	                               Map<String, CriterionResult> dependencyResults,
	                               Map<String, Object> extraBindings) {
		this.criterion = criterion;
		this.inputContext = inputContext;
		this.dependencyResults = dependencyResults != null ? dependencyResults : Collections.emptyMap();
		this.extraBindings = extraBindings != null ? extraBindings : Collections.emptyMap();
	}

	// Getters

	/**
	 * Get the criterion being evaluated
	 */
	public EvaluationCriterion getCriterion() {
		return criterion;
	}

	/**
	 * Get the read-only input context
	 */
	public EvaluationContext getInputContext() {
		return inputContext;
	}

	/**
	 * Get results from criteria this criterion depends on
	 */
	public Map<String, CriterionResult> getDependencyResults() {
		return dependencyResults;
	}

	/**
	 * Get a specific dependency result
	 */
	public CriterionResult getDependencyResult(String criterionName) {
		return dependencyResults.get(criterionName);
	}

	/**
	 * Get extra bindings (for batching scenarios)
	 */
	public Map<String, Object> getExtraBindings() {
		return extraBindings;
	}

	/**
	 * Get a specific extra binding value
	 */
	public Object getExtraBinding(String key) {
		return extraBindings.get(key);
	}

	/**
	 * Check if an extra binding exists
	 */
	public boolean hasExtraBinding(String key) {
		return extraBindings.containsKey(key);
	}
}
