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
package com.alibaba.assistant.agent.autoconfigure.evaluation;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * 默认评估套件配置属性
 *
 * <p>控制 react-phase-suite 和 codeact-phase-suite 的行为。
 *
 * @author Assistant Agent Team
 * @since 1.0.0
 */
@ConfigurationProperties(prefix = "spring.ai.alibaba.codeact.starter.evaluation")
public class DefaultEvaluationProperties {

    /**
     * React 阶段评估配置
     */
    private PhaseConfig reactPhase = new PhaseConfig();

    /**
     * CodeAct 阶段评估配置
     */
    private PhaseConfig codeactPhase = new PhaseConfig();

    public PhaseConfig getReactPhase() {
        return reactPhase;
    }

    public void setReactPhase(PhaseConfig reactPhase) {
        this.reactPhase = reactPhase;
    }

    public PhaseConfig getCodeactPhase() {
        return codeactPhase;
    }

    public void setCodeactPhase(PhaseConfig codeactPhase) {
        this.codeactPhase = codeactPhase;
    }

    /**
     * 经验检索配置
     */
    private ExperienceRetrievalConfig experience = new ExperienceRetrievalConfig();

    public ExperienceRetrievalConfig getExperience() {
        return experience;
    }

    public void setExperience(ExperienceRetrievalConfig experience) {
        this.experience = experience;
    }

    /**
     * 阶段评估配置
     */
    public static class PhaseConfig {

        /**
         * 是否启用该阶段的评估
         */
        private boolean enabled = true;

        /**
         * 是否启用用户输入增强（React阶段）
         */
        private boolean enhancedUserInputEnabled = true;

        /**
         * 是否启用代码任务增强（CodeAct阶段）
         */
        private boolean enhancedTaskInputEnabled = true;

        public boolean isEnabled() {
            return enabled;
        }

        public void setEnabled(boolean enabled) {
            this.enabled = enabled;
        }

        public boolean isEnhancedUserInputEnabled() {
            return enhancedUserInputEnabled;
        }

        public void setEnhancedUserInputEnabled(boolean enhancedUserInputEnabled) {
            this.enhancedUserInputEnabled = enhancedUserInputEnabled;
        }

        public boolean isEnhancedTaskInputEnabled() {
            return enhancedTaskInputEnabled;
        }

        public void setEnhancedTaskInputEnabled(boolean enhancedTaskInputEnabled) {
            this.enhancedTaskInputEnabled = enhancedTaskInputEnabled;
        }
    }

    /**
     * 经验检索配置
     */
    public static class ExperienceRetrievalConfig {

        /**
         * 是否启用经验检索作为评估 Criterion
         */
        private boolean enabled = true;

        /**
         * 每种经验类型最多检索的数量
         */
        private int maxExperiencesPerType = 3;

        /**
         * React 阶段是否启用经验检索
         */
        private boolean reactPhaseEnabled = true;

        /**
         * CodeAct 阶段是否启用经验检索
         */
        private boolean codeactPhaseEnabled = true;

        public boolean isEnabled() {
            return enabled;
        }

        public void setEnabled(boolean enabled) {
            this.enabled = enabled;
        }

        public int getMaxExperiencesPerType() {
            return maxExperiencesPerType;
        }

        public void setMaxExperiencesPerType(int maxExperiencesPerType) {
            this.maxExperiencesPerType = maxExperiencesPerType;
        }

        public boolean isReactPhaseEnabled() {
            return reactPhaseEnabled;
        }

        public void setReactPhaseEnabled(boolean reactPhaseEnabled) {
            this.reactPhaseEnabled = reactPhaseEnabled;
        }

        public boolean isCodeactPhaseEnabled() {
            return codeactPhaseEnabled;
        }

        public void setCodeactPhaseEnabled(boolean codeactPhaseEnabled) {
            this.codeactPhaseEnabled = codeactPhaseEnabled;
        }
    }
}
