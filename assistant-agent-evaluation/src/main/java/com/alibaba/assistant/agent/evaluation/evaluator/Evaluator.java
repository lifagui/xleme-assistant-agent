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

/**
 * Interface for evaluators that execute evaluation logic
 * Can be LLM-based, rule-based, or external service-based
 *
 * @author Assistant Agent Team
 */
public interface Evaluator {

	/**
	 * Evaluate a criterion in the given context
	 *
	 * @param executionContext The criterion execution context containing all necessary information
	 * @return The evaluation result
	 */
	CriterionResult evaluate(CriterionExecutionContext executionContext);

	/**
	 * Get the unique identifier for this evaluator
	 *
	 * @return Evaluator ID
	 */
	String getEvaluatorId();
}

