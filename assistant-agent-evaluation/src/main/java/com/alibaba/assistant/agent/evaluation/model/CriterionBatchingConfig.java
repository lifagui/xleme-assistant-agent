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

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

/**
 * Configuration for batching and concurrency at the criterion level.
 * Enables a criterion to process collections in batches with controlled concurrency.
 *
 * @author Assistant Agent Team
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class CriterionBatchingConfig {

	/**
	 * Whether batching is enabled for this criterion.
	 * Default value is false
	 */
	private boolean enabled = false;

	/**
	 * Path to the source collection to be batched.
	 * Examples:
	 * - "context.tools" - from EvaluationContext
	 * - "context.input.testCases" - nested path in context
	 * - "dependencies.collect_tools.value" - from dependency result
	 */
	private String sourcePath;

	/**
	 * Maximum number of items per batch.
	 * Must be > 0 when batching is enabled.
	 */
	private int batchSize = 10;

	/**
	 * Maximum number of batches to process concurrently.
	 * - 1: sequential batch processing (no concurrency)
	 * - >1: concurrent batch processing
	 */
	private int maxConcurrentBatches = 1;

	/**
	 * The key name used to bind the current batch into the execution context.
	 * The evaluator can access the current batch via this key.
	 * Example: "toolBatch", "testCaseBatch"
	 */
	private String batchBindingKey;

	/**
	 * Strategy identifier for aggregating batch results.
	 * Built-in strategies:
	 * - "ANY_TRUE": OR gate - returns true if any batch result is true
	 * - "ALL_TRUE": AND gate - returns true if all batch results are true
	 */
	private String aggregationStrategy = "ANY_TRUE";

	public CriterionBatchingConfig() {
	}

	public boolean isEnabled() {
		return enabled;
	}

	public void setEnabled(boolean enabled) {
		this.enabled = enabled;
	}

	public String getSourcePath() {
		return sourcePath;
	}

	public void setSourcePath(String sourcePath) {
		this.sourcePath = sourcePath;
	}

	public int getBatchSize() {
		return batchSize;
	}

	public void setBatchSize(int batchSize) {
		this.batchSize = batchSize;
	}

	public int getMaxConcurrentBatches() {
		return maxConcurrentBatches;
	}

	public void setMaxConcurrentBatches(int maxConcurrentBatches) {
		this.maxConcurrentBatches = maxConcurrentBatches;
	}

	public String getBatchBindingKey() {
		return batchBindingKey;
	}

	public void setBatchBindingKey(String batchBindingKey) {
		this.batchBindingKey = batchBindingKey;
	}

	public String getAggregationStrategy() {
		return aggregationStrategy;
	}

	public void setAggregationStrategy(String aggregationStrategy) {
		this.aggregationStrategy = aggregationStrategy;
	}

	/**
	 * Validate the configuration.
	 * @throws IllegalArgumentException if configuration is invalid
	 */
	public void validate() {
		if (!enabled) {
			return;
		}

		if (sourcePath == null || sourcePath.trim().isEmpty()) {
			throw new IllegalArgumentException("sourcePath must be specified when batching is enabled");
		}

		if (batchSize <= 0) {
			throw new IllegalArgumentException("batchSize must be greater than 0");
		}

		if (maxConcurrentBatches <= 0) {
			throw new IllegalArgumentException("maxConcurrentBatches must be greater than 0");
		}

		if (batchBindingKey == null || batchBindingKey.trim().isEmpty()) {
			throw new IllegalArgumentException("batchBindingKey must be specified when batching is enabled");
		}

		if (aggregationStrategy == null || aggregationStrategy.trim().isEmpty()) {
			throw new IllegalArgumentException("aggregationStrategy must be specified when batching is enabled");
		}
	}
}

