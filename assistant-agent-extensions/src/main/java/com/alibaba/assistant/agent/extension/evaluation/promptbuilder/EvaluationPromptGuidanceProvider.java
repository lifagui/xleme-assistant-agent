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
package com.alibaba.assistant.agent.extension.evaluation.promptbuilder;

import java.util.Map;

/**
 * 评估结果到 Prompt 的转换器接口
 *
 * <p>这是 evaluation 和 prompt builder 对接的核心机制接口。
 * 实现类负责根据评估结果生成相应的 prompt 指导。
 *
 * <p>使用方式：
 * <pre>
 * &#64;Component
 * public class MyPromptGuidanceProvider implements EvaluationPromptGuidanceProvider {
 *     &#64;Override
 *     public String getPhase() { return "REACT"; }
 *
 *     &#64;Override
 *     public String generateGuidance(Map&lt;String, String&gt; evaluationResults) {
 *         String isFuzzy = evaluationResults.get("is_fuzzy");
 *         if ("模糊".equals(isFuzzy)) {
 *             return "【模糊意图处理】请先向用户澄清需求...";
 *         }
 *         return null;
 *     }
 * }
 * </pre>
 *
 * @author Assistant Agent Team
 * @since 1.0.0
 */
public interface EvaluationPromptGuidanceProvider {

    /**
     * 获取此 Provider 适用的阶段
     *
     * @return 阶段标识，如 "REACT" 或 "CODEACT"
     */
    String getPhase();

    /**
     * 获取此 Provider 的优先级（数值越小优先级越高）
     *
     * @return 优先级，默认为 100
     */
    default int getPriority() {
        return 100;
    }

    /**
     * 根据评估结果生成 prompt 指导
     *
     * @param evaluationResults 评估结果 Map，key 为 criterion 名称，value 为评估值
     * @return prompt 指导文本，返回 null 或空字符串表示不需要额外指导
     */
    String generateGuidance(Map<String, String> evaluationResults);

    /**
     * 检查此 Provider 是否应该处理给定的评估结果
     *
     * @param evaluationResults 评估结果
     * @return true 表示应该处理
     */
    default boolean shouldHandle(Map<String, String> evaluationResults) {
        return true;
    }
}
