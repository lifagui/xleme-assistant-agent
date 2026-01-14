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
package com.alibaba.assistant.agent.evaluation.aggregation;

import com.alibaba.assistant.agent.evaluation.model.CriterionExecutionContext;
import com.alibaba.assistant.agent.evaluation.model.CriterionResult;

import java.util.List;

/**
 * Strategy interface for aggregating batch evaluation results.
 * Implementations define how multiple batch results are combined into a single result.
 *
 * @author Assistant Agent Team
 */
public interface BatchAggregationStrategy {

	/**
	 * Aggregate multiple batch results into a single criterion result.
	 *
	 * @param baseContext The original criterion execution context (without batch bindings)
	 * @param batchResults List of results from evaluating individual batches
	 * @return Aggregated criterion result
	 */
	CriterionResult aggregate(CriterionExecutionContext baseContext, List<CriterionResult> batchResults);

	/**
	 * Get the strategy identifier.
	 * @return Strategy ID (e.g., "ANY_TRUE", "ALL_TRUE")
	 */
	String getStrategyId();
}

