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
package com.alibaba.assistant.agent.evaluation.evaluator;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Registry for managing evaluator instances
 *
 * @author Assistant Agent Team
 */
public class EvaluatorRegistry {

	private static final Logger logger = LoggerFactory.getLogger(EvaluatorRegistry.class);

	private final Map<String, Evaluator> evaluators = new ConcurrentHashMap<>();
	private String defaultEvaluatorId;

	/**
	 * Register an evaluator with ID conflict detection
	 */
	public void registerEvaluator(Evaluator evaluator) {
		String id = evaluator.getEvaluatorId();
		if (id == null || id.trim().isEmpty()) {
			throw new IllegalArgumentException("Evaluator ID cannot be null or empty");
		}

		if (evaluators.containsKey(id)) {
			logger.warn("Evaluator with ID '{}' already exists, will be replaced. " +
					"Previous: {}, New: {}",
					id,
					evaluators.get(id).getClass().getSimpleName(),
					evaluator.getClass().getSimpleName());
		}

		evaluators.put(id, evaluator);
		logger.debug("Registered evaluator '{}' of type {}", id, evaluator.getClass().getSimpleName());
	}

	/**
	 * Get an evaluator by ID
	 */
	public Evaluator getEvaluator(String evaluatorId) {
		return evaluators.get(evaluatorId);
	}

	/**
	 * Get the default evaluator
	 */
	public Evaluator getDefaultEvaluator() {
		if (defaultEvaluatorId != null) {
			return evaluators.get(defaultEvaluatorId);
		}
		// Return first evaluator if no default is set
		return evaluators.values().stream().findFirst().orElse(null);
	}

	/**
	 * Set the default evaluator ID with validation
	 */
	public void setDefaultEvaluatorId(String evaluatorId) {
		if (evaluatorId != null && !evaluators.containsKey(evaluatorId)) {
			logger.warn("Setting default evaluator ID '{}' which is not currently registered", evaluatorId);
		}
		this.defaultEvaluatorId = evaluatorId;
	}

	/**
	 * Check if an evaluator is registered
	 */
	public boolean hasEvaluator(String evaluatorId) {
		return evaluators.containsKey(evaluatorId);
	}

	/**
	 * Get all registered evaluator IDs
	 */
	public java.util.Set<String> getEvaluatorIds() {
		return evaluators.keySet();
	}
}
