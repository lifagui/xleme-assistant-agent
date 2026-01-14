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
 * AND gate aggregation strategy - returns true if ALL batch results are true.
 * Suitable for scenarios like "do all test cases pass".
 *
 * @author Assistant Agent Team
 */
public class AllTrueAggregationStrategy implements BatchAggregationStrategy {

	private static final Logger logger = LoggerFactory.getLogger(AllTrueAggregationStrategy.class);

	@Override
	public CriterionResult aggregate(CriterionExecutionContext baseContext, List<CriterionResult> batchResults) {
		CriterionResult aggregated = new CriterionResult();
		aggregated.setCriterionName(baseContext.getCriterion().getName());
		aggregated.setStartTimeMillis(System.currentTimeMillis());

		if (batchResults == null || batchResults.isEmpty()) {
			// Empty collection: vacuous truth - all (zero) batch results are true
			logger.debug("Empty batch results for criterion '{}', returning true (vacuous truth)",
				baseContext.getCriterion().getName());
			aggregated.setStatus(CriterionStatus.SUCCESS);
			aggregated.setValue(true);
			aggregated.setReason("No batch results to evaluate (empty collection - vacuous truth)");
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

		// Apply AND logic: all batch results must be true
		boolean allTrue = true;
		int trueCount = 0;
		List<String> falseReasons = new ArrayList<>();

		for (int i = 0; i < batchResults.size(); i++) {
			CriterionResult batchResult = batchResults.get(i);
			Object value = batchResult.getValue();
			if (value instanceof Boolean && (Boolean) value) {
				trueCount++;
			} else {
				allTrue = false;
				if (batchResult.getReason() != null) {
					falseReasons.add("Batch result " + i + ": " + batchResult.getReason());
				}
			}
		}

		aggregated.setStatus(CriterionStatus.SUCCESS);
		aggregated.setValue(allTrue);

		if (allTrue) {
			aggregated.setReason(String.format("All batch results satisfied the criterion. Total batch results: %d",
				batchResults.size()));
		} else {
			aggregated.setReason(String.format("Not all batches satisfied the criterion. Total batches: %d, True batches: %d, False batches: %d",
				batchResults.size(), trueCount, batchResults.size() - trueCount));
		}

		// Optionally store detailed batch results
		Map<String, Object> metadata = new HashMap<>();
		metadata.put("totalBatchResults", batchResults.size());
		metadata.put("trueCount", trueCount);
		metadata.put("falseCount", batchResults.size() - trueCount);
		metadata.put("batchResults", batchResults);

		aggregated.setEndTimeMillis(System.currentTimeMillis());

		logger.debug("ALL_TRUE aggregation for '{}': {} out of {} batch results were true",
			baseContext.getCriterion().getName(), trueCount, batchResults.size());

		return aggregated;
	}

	@Override
	public String getStrategyId() {
		return "ALL_TRUE";
	}
}

