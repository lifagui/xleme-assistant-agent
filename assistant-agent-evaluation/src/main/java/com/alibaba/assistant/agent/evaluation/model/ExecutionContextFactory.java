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
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Factory for creating CriterionExecutionContext instances.
 *
 * This factory centralizes the creation logic and dependency resolution,
 * simplifying the construction of execution contexts for individual criteria.
 *
 * @author Assistant Agent Team
 */
public class ExecutionContextFactory {

    /**
     * Create a CriterionExecutionContext for a specific criterion
     *
     * @param criterion the evaluation criterion
     * @param inputContext the input context
     * @param allResults all available results (for dependency resolution)
     * @return configured CriterionExecutionContext
     */
    public static CriterionExecutionContext createCriterionContext(
            EvaluationCriterion criterion,
            EvaluationContext inputContext,
            Map<String, CriterionResult> allResults) {
        return createCriterionContext(criterion, inputContext, allResults, null);
    }

    /**
     * Create a CriterionExecutionContext for a specific criterion with extra bindings
     *
     * @param criterion the evaluation criterion
     * @param inputContext the input context
     * @param allResults all available results (for dependency resolution)
     * @param extraBindings extra bindings for batching data
     * @return configured CriterionExecutionContext
     */
    public static CriterionExecutionContext createCriterionContext(
            EvaluationCriterion criterion,
            EvaluationContext inputContext,
            Map<String, CriterionResult> allResults,
            Map<String, Object> extraBindings) {

        // Extract dependency results
        Map<String, CriterionResult> dependencyResults = extractDependencyResults(criterion, allResults);

        return new CriterionExecutionContext(criterion, inputContext, dependencyResults, extraBindings);
    }

    /**
     * Extract dependency results for a criterion from all available results
     *
     * @param criterion the criterion to extract dependencies for
     * @param allResults all available results
     * @return map containing only the dependency results this criterion needs
     */
    public static Map<String, CriterionResult> extractDependencyResults(
            EvaluationCriterion criterion,
            Map<String, CriterionResult> allResults) {

        List<String> dependencies = criterion.getDependsOn();
        if (dependencies == null || dependencies.isEmpty()) {
            return Collections.emptyMap();
        }

        Map<String, CriterionResult> dependencyResults = new HashMap<>();
        for (String depName : dependencies) {
            CriterionResult depResult = allResults.get(depName);
            if (depResult != null) {
                dependencyResults.put(depName, depResult);
            }
        }

        return Collections.unmodifiableMap(dependencyResults);
    }
}
