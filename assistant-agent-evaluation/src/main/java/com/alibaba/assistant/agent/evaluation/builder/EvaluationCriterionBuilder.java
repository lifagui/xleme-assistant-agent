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
package com.alibaba.assistant.agent.evaluation.builder;

import com.alibaba.assistant.agent.evaluation.model.EvaluationCriterion;
import com.alibaba.assistant.agent.evaluation.model.EvaluatorType;
import com.alibaba.assistant.agent.evaluation.model.ReasoningPolicy;
import com.alibaba.assistant.agent.evaluation.model.ResultType;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

/**
 * Builder for creating EvaluationCriterion instances
 *
 * @author Assistant Agent Team
 */
public class EvaluationCriterionBuilder {

	private final EvaluationCriterion criterion;

	public EvaluationCriterionBuilder(String name) {
		this.criterion = new EvaluationCriterion();
		this.criterion.setName(name);
	}

	public static EvaluationCriterionBuilder create(String name) {
		return new EvaluationCriterionBuilder(name);
	}

	public EvaluationCriterionBuilder description(String description) {
		criterion.setDescription(description);
		return this;
	}

	public EvaluationCriterionBuilder resultType(ResultType resultType) {
		criterion.setResultType(resultType);
		return this;
	}

	public EvaluationCriterionBuilder options(String... options) {
		criterion.setOptions(Arrays.asList(options));
		return this;
	}

	public EvaluationCriterionBuilder options(List<String> options) {
		criterion.setOptions(options);
		return this;
	}

	public EvaluationCriterionBuilder dependsOn(String... dependencyNames) {
		criterion.setDependsOn(Arrays.asList(dependencyNames));
		return this;
	}

	public EvaluationCriterionBuilder dependsOn(List<String> dependencyNames) {
		criterion.setDependsOn(dependencyNames);
		return this;
	}

	public EvaluationCriterionBuilder evaluatorRef(String evaluatorRef) {
		criterion.setEvaluatorRef(evaluatorRef);
		return this;
	}

	public EvaluationCriterionBuilder evaluatorType(EvaluatorType evaluatorType) {
		criterion.setEvaluatorType(evaluatorType);
		return this;
	}

	public EvaluationCriterionBuilder config(Map<String, Object> config) {
		criterion.setConfig(config);
		return this;
	}

	public EvaluationCriterionBuilder workingMechanism(String workingMechanism) {
		criterion.setWorkingMechanism(workingMechanism);
		return this;
	}

	public EvaluationCriterionBuilder fewShots(List<EvaluationCriterion.FewShotExample> fewShots) {
		criterion.setFewShots(fewShots);
		return this;
	}

	public EvaluationCriterionBuilder addFewShot(String input, String context, String expectedOutput) {
		if (criterion.getFewShots() == null) {
			criterion.setFewShots(new ArrayList<>());
		}
		criterion.getFewShots().add(new EvaluationCriterion.FewShotExample(input, context, expectedOutput));
		return this;
	}

	public EvaluationCriterionBuilder reasoningPolicy(ReasoningPolicy reasoningPolicy) {
		criterion.setReasoningPolicy(reasoningPolicy);
		return this;
	}

	public EvaluationCriterionBuilder promptTemplate(String promptTemplate) {
		criterion.setCustomPrompt(promptTemplate);
		return this;
	}

	public EvaluationCriterionBuilder contextBindings(String... bindings) {
		criterion.setContextBindings(Arrays.asList(bindings));
		return this;
	}

	public EvaluationCriterionBuilder contextBindings(List<String> bindings) {
		criterion.setContextBindings(bindings);
		return this;
	}

	public EvaluationCriterion build() {
		// Validate required fields
		if (criterion.getName() == null || criterion.getName().isEmpty()) {
			throw new IllegalStateException("Criterion name is required");
		}
		return criterion;
	}
}
