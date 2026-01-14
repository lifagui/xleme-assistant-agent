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
import com.alibaba.assistant.agent.evaluation.model.CriterionStatus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * OR gate aggregation strategy - returns true if ANY batch result is true.
 * Suitable for scenarios like "does at least one tool satisfy the requirement".
 *
 * @author Assistant Agent Team
 */
public class AnyTrueAggregationStrategy implements BatchAggregationStrategy {

	private static final Logger logger = LoggerFactory.getLogger(AnyTrueAggregationStrategy.class);

	@Override
	public CriterionResult aggregate(CriterionExecutionContext baseContext, List<CriterionResult> batchResults) {
		CriterionResult aggregated = new CriterionResult();
		aggregated.setCriterionName(baseContext.getCriterion().getName());
		aggregated.setStartTimeMillis(System.currentTimeMillis());

		if (batchResults == null || batchResults.isEmpty()) {
			// Empty collection: no true batch results, result is false or SKIPPED
			logger.debug("Empty batch results for criterion '{}', returning false", baseContext.getCriterion().getName());
			aggregated.setStatus(CriterionStatus.SUCCESS);
			aggregated.setValue(false);
			aggregated.setReason("No batch results to evaluate (empty collection)");
			aggregated.setEndTimeMillis(System.currentTimeMillis());
			return aggregated;
		}

		// Check for errors first
		boolean hasError = batchResults.stream().anyMatch(r -> r.getStatus() == CriterionStatus.ERROR);
		if (hasError) {
			logger.warn("At least one batch result has ERROR status for criterion '{}'",
				baseContext.getCriterion().getName());
			aggregated.setStatus(CriterionStatus.ERROR);
			aggregated.setErrorMessage("One or more batch evaluations failed with ERROR");
			aggregated.setValue(false);
			aggregated.setEndTimeMillis(System.currentTimeMillis());
			return aggregated;
		}

		// Check for timeouts
		boolean hasTimeout = batchResults.stream().anyMatch(r -> r.getStatus() == CriterionStatus.TIMEOUT);
		if (hasTimeout) {
			logger.warn("At least one batch result has TIMEOUT status for criterion '{}'",
				baseContext.getCriterion().getName());
			aggregated.setStatus(CriterionStatus.TIMEOUT);
			aggregated.setValue(false);
			aggregated.setEndTimeMillis(System.currentTimeMillis());
			return aggregated;
		}

		// Apply OR logic: if any batch result is true, result is true
		boolean anyTrue = false;
		List<String> trueReasons = new ArrayList<>();

		for (CriterionResult batchResult : batchResults) {
			Object value = batchResult.getValue();
			if (value instanceof Boolean && (Boolean) value) {
				anyTrue = true;
				if (batchResult.getReason() != null) {
					trueReasons.add(batchResult.getReason());
				}
			}
		}

		aggregated.setStatus(CriterionStatus.SUCCESS);
		aggregated.setValue(anyTrue);

		if (anyTrue) {
			aggregated.setReason(String.format("At least one batch result satisfied the criterion. Total batch results: %d, True batch results: %d",
				batchResults.size(), trueReasons.size()));
		} else {
			aggregated.setReason(String.format("No batch results satisfied the criterion. Total batch results: %d",
				batchResults.size()));
		}

		// Optionally store detailed batch results
		Map<String, Object> metadata = new HashMap<>();
		metadata.put("totalBatchResults", batchResults.size());
		metadata.put("trueCount", trueReasons.size());
		metadata.put("batchResults", batchResults);

		aggregated.setEndTimeMillis(System.currentTimeMillis());

		logger.debug("ANY_TRUE aggregation for '{}': {} out of {} batch results were true",
			baseContext.getCriterion().getName(), trueReasons.size(), batchResults.size());

		return aggregated;
	}

	@Override
	public String getStrategyId() {
		return "ANY_TRUE";
	}
}

