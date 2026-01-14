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
package com.alibaba.assistant.agent.start.config;

import com.alibaba.assistant.agent.extension.evaluation.promptbuilder.EvaluationPromptGuidanceProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.Map;

/**
 * React 阶段 Prompt 指导提供者
 *
 * <p>根据 React 阶段的评估结果生成 prompt 指导：
 * <ul>
 *   <li>is_fuzzy=模糊：需要让用户明确需求</li>
 *   <li>is_fuzzy=清晰：按照用户的要求进行操作</li>
 * </ul>
 *
 * @author Assistant Agent Team
 * @since 1.0.0
 */
@Component
public class ReactPhasePromptGuidanceProvider implements EvaluationPromptGuidanceProvider {

    private static final Logger log = LoggerFactory.getLogger(ReactPhasePromptGuidanceProvider.class);

    @Override
    public String getPhase() {
        return "REACT";
    }

    @Override
    public int getPriority() {
        return 10;
    }

    @Override
    public boolean shouldHandle(Map<String, String> evaluationResults) {
        // 只要有 is_fuzzy 评估结果就处理
        return evaluationResults.containsKey("is_fuzzy");
    }

    @Override
    public String generateGuidance(Map<String, String> evaluationResults) {
        String isFuzzy = evaluationResults.get("is_fuzzy");
        log.info("ReactPhasePromptGuidanceProvider#generateGuidance - reason=生成React阶段指导, is_fuzzy={}", isFuzzy);

        StringBuilder sb = new StringBuilder();
        sb.append("\n【React阶段执行指导】\n\n");

        if ("模糊".equals(isFuzzy)) {
            sb.append("【模糊意图处理策略】\n");
            sb.append("当前用户意图被识别为**模糊**，请按以下策略处理：\n\n");
            sb.append("1. 不要立即执行代码或做出重要决策\n");
            sb.append("2. 先向用户澄清具体的需求和意图\n");
            sb.append("3. 可以提供几个可能的理解方向，让用户选择\n");
            sb.append("4. 等用户明确需求后，再进行下一步操作\n\n");
            sb.append("示例回复：「我注意到您的需求还不太明确，请问您是想要...还是...？」\n");
        } else if ("清晰".equals(isFuzzy)) {
            sb.append("【清晰意图处理策略】\n");
            sb.append("当前用户意图被识别为**清晰**，请按以下策略处理：\n\n");
            sb.append("1. 直接按照用户的要求进行操作\n");
            sb.append("2. 无需额外澄清，直接执行\n");
            sb.append("3. 高效完成任务，给出清晰的结果反馈\n");
        } else {
            // 未知值，返回 null 表示不生成指导
            return null;
        }

        sb.append("\n【React阶段指导结束】\n");
        return sb.toString();
    }
}
