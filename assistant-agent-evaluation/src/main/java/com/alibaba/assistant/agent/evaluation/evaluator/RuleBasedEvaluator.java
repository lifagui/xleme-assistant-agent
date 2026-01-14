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

import com.alibaba.assistant.agent.evaluation.model.CriterionExecutionContext;
import com.alibaba.assistant.agent.evaluation.model.CriterionResult;
import com.alibaba.assistant.agent.evaluation.model.CriterionStatus;

import java.util.function.Function;

/**
 * Rule-based evaluator that uses Java logic for evaluation
 * Useful for simple checks, threshold-based evaluations, etc.
 *
 * @author Assistant Agent Team
 */
public class RuleBasedEvaluator implements Evaluator {

	private final String evaluatorId;
	private final Function<CriterionExecutionContext, CriterionResult> evaluationFunction;

	public RuleBasedEvaluator(String evaluatorId, Function<CriterionExecutionContext, CriterionResult> evaluationFunction) {
		this.evaluatorId = evaluatorId;
		this.evaluationFunction = evaluationFunction;
	}

	@Override
	public CriterionResult evaluate(CriterionExecutionContext executionContext) {
		CriterionResult result = new CriterionResult();
		result.setCriterionName(executionContext.getCriterion().getName());
		result.setStartTimeMillis(System.currentTimeMillis());

		try {
			CriterionResult evaluatedResult = evaluationFunction.apply(executionContext);
			// Copy values from evaluated result
			result.setStatus(evaluatedResult.getStatus() != null ? evaluatedResult.getStatus() : CriterionStatus.SUCCESS);
			result.setValue(evaluatedResult.getValue());
			result.setReason(evaluatedResult.getReason());
			result.setRawResponse(evaluatedResult.getRawResponse());
			// Copy metadata (including experience_ids for deduplication)
			if (evaluatedResult.getMetadata() != null && !evaluatedResult.getMetadata().isEmpty()) {
				result.getMetadata().putAll(evaluatedResult.getMetadata());
			}
		} catch (Exception e) {
			result.setStatus(CriterionStatus.ERROR);
			result.setErrorMessage(e.getMessage());
		} finally {
			result.setEndTimeMillis(System.currentTimeMillis());
		}

		return result;
	}

	@Override
	public String getEvaluatorId() {
		return evaluatorId;
	}
}

