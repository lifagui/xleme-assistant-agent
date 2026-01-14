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
 * CodeAct 阶段 Prompt 指导提供者
 *
 * <p>根据 CodeAct 阶段的评估结果生成 prompt 指导：
 * <ul>
 *   <li>purpose=咨询：只需要写一次"查询"的代码并执行，随后根据结果回答用户即可</li>
 *   <li>purpose=操作：需要先写代码，然后反问用户是否执行，用户说执行再开始执行代码</li>
 * </ul>
 *
 * @author Assistant Agent Team
 * @since 1.0.0
 */
@Component
public class CodeActPhasePromptGuidanceProvider implements EvaluationPromptGuidanceProvider {

    private static final Logger log = LoggerFactory.getLogger(CodeActPhasePromptGuidanceProvider.class);

    @Override
    public String getPhase() {
        return "CODEACT";
    }

    @Override
    public int getPriority() {
        return 10;
    }

    @Override
    public boolean shouldHandle(Map<String, String> evaluationResults) {
        // 只要有 purpose 评估结果就处理
        return evaluationResults.containsKey("purpose");
    }

    @Override
    public String generateGuidance(Map<String, String> evaluationResults) {
        String purpose = evaluationResults.get("purpose");
        log.info("CodeActPhasePromptGuidanceProvider#generateGuidance - reason=生成CodeAct阶段指导, purpose={}", purpose);

        StringBuilder sb = new StringBuilder();
        sb.append("\n【CodeAct阶段执行指导】\n\n");

        if ("咨询".equals(purpose)) {
            sb.append("【咨询类请求处理策略】\n");
            sb.append("当前请求被识别为**咨询类**请求，请按以下策略处理：\n\n");
            sb.append("1. 只需要编写一次「查询」类型的代码并执行\n");
            sb.append("2. 获取执行结果后，直接根据结果回答用户的问题\n");
            sb.append("3. 不需要反问用户是否执行，直接执行查询即可\n");
            sb.append("4. 回答时要清晰、简洁、专业\n");
        } else if ("操作".equals(purpose)) {
            sb.append("【操作类请求处理策略】\n");
            sb.append("当前请求被识别为**操作类**请求，请按以下策略处理：\n\n");
            sb.append("1. 首先编写要执行的代码，但**不要立即执行**\n");
            sb.append("2. 向用户展示代码内容，并询问用户是否确认执行\n");
            sb.append("3. 等待用户明确说「执行」、「确认」、「是」等肯定词后，再开始执行代码\n");
            sb.append("4. 执行完成后，向用户报告执行结果\n");
            sb.append("5. 对于可能产生副作用的操作（如删除、修改），务必先确认\n\n");
            sb.append("示例对话流程：\n");
            sb.append("- 用户：帮我创建一个新文件\n");
            sb.append("- 助手：我已准备好创建文件的代码：[代码内容]。请确认是否执行？\n");
            sb.append("- 用户：执行\n");
            sb.append("- 助手：[执行代码] 文件创建成功！\n");
        } else {
            // 未知值，返回 null 表示不生成指导
            return null;
        }

        sb.append("\n【CodeAct阶段指导结束】\n");
        return sb.toString();
    }
}
