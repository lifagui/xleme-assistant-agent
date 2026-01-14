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

import java.util.HashMap;
import java.util.Map;

/**
 * Overall result of an evaluation execution
 * Aggregates results from all criteria
 *
 * @author Assistant Agent Team
 */
public class EvaluationResult {

	/**
	 * ID of the evaluation suite
	 */
	private String suiteId;

	/**
	 * Name of the evaluation suite
	 */
	private String suiteName;

	/**
	 * Results for each criterion, mapped by criterion name
	 */
	private Map<String, CriterionResult> criteriaResults = new HashMap<>();

	/**
	 * Summary statistics
	 */
	private EvaluationStatistics statistics;

	/**
	 * Timestamp when evaluation started
	 */
	private Long startTimeMillis;

	/**
	 * Timestamp when evaluation completed
	 */
	private Long endTimeMillis;

	// Getters and Setters

	public String getSuiteId() {
		return suiteId;
	}

	public void setSuiteId(String suiteId) {
		this.suiteId = suiteId;
	}

	public String getSuiteName() {
		return suiteName;
	}

	public void setSuiteName(String suiteName) {
		this.suiteName = suiteName;
	}

	public Map<String, CriterionResult> getCriteriaResults() {
		return criteriaResults;
	}

	public void setCriteriaResults(Map<String, CriterionResult> criteriaResults) {
		this.criteriaResults = criteriaResults;
	}

	public EvaluationStatistics getStatistics() {
		return statistics;
	}

	public void setStatistics(EvaluationStatistics statistics) {
		this.statistics = statistics;
	}

	public Long getStartTimeMillis() {
		return startTimeMillis;
	}

	public void setStartTimeMillis(Long startTimeMillis) {
		this.startTimeMillis = startTimeMillis;
	}

	public Long getEndTimeMillis() {
		return endTimeMillis;
	}

	public void setEndTimeMillis(Long endTimeMillis) {
		this.endTimeMillis = endTimeMillis;
	}

	/**
	 * Get total execution duration in milliseconds
	 */
	public Long getDurationMillis() {
		if (startTimeMillis != null && endTimeMillis != null) {
			return endTimeMillis - startTimeMillis;
		}
		return null;
	}

	/**
	 * Get result for a specific criterion
	 */
	public CriterionResult getCriterionResult(String criterionId) {
		return criteriaResults.get(criterionId);
	}

	/**
	 * Add a criterion result
	 */
	public void addCriterionResult(String criterionId, CriterionResult result) {
		this.criteriaResults.put(criterionId, result);
	}

	/**
	 * Statistics about evaluation execution
	 */
	public static class EvaluationStatistics {
		private int totalCriteria;
		private int successCount;
		private int failedCount;
		private int skippedCount;
		private int timeoutCount;
		private int errorCount;

		public int getTotalCriteria() {
			return totalCriteria;
		}

		public void setTotalCriteria(int totalCriteria) {
			this.totalCriteria = totalCriteria;
		}

		public int getSuccessCount() {
			return successCount;
		}

		public void setSuccessCount(int successCount) {
			this.successCount = successCount;
		}

		public int getFailedCount() {
			return failedCount;
		}

		public void setFailedCount(int failedCount) {
			this.failedCount = failedCount;
		}

		public int getSkippedCount() {
			return skippedCount;
		}

		public void setSkippedCount(int skippedCount) {
			this.skippedCount = skippedCount;
		}

		public int getTimeoutCount() {
			return timeoutCount;
		}

		public void setTimeoutCount(int timeoutCount) {
			this.timeoutCount = timeoutCount;
		}

		public int getErrorCount() {
			return errorCount;
		}

		public void setErrorCount(int errorCount) {
			this.errorCount = errorCount;
		}
	}
}

