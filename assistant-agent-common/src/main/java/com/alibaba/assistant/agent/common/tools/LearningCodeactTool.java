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
package com.alibaba.assistant.agent.common.tools;

/**
 * Learning 领域的 CodeAct 工具接口。
 *
 * <p>用于定义学习任务相关的工具，支持触发学习、查询学习状态等功能。
 *
 * @author Assistant Agent Team
 * @since 1.0.0
 */
public interface LearningCodeactTool extends CodeactTool {

	/**
	 * 获取学习操作类型。
	 *
	 * @return 操作类型，例如 TRIGGER（触发学习）、QUERY（查询状态）
	 */
	default LearningOperationType getOperationType() {
		return LearningOperationType.QUERY;
	}

	/**
	 * 学习操作类型枚举
	 */
	enum LearningOperationType {

		/**
		 * 触发学习任务
		 */
		TRIGGER,

		/**
		 * 查询学习状态
		 */
		QUERY,

		/**
		 * 停止学习任务
		 */
		STOP

	}

}

