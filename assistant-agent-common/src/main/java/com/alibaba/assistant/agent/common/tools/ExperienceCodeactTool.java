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
 * Experience 领域的 CodeAct 工具接口。
 *
 * <p>用于定义经验管理相关的工具，支持保存、查询、更新经验等功能。
 *
 * @author Assistant Agent Team
 * @since 1.0.0
 */
public interface ExperienceCodeactTool extends CodeactTool {

	/**
	 * 获取经验操作类型。
	 *
	 * @return 操作类型，例如 SAVE（保存经验）、QUERY（查询经验）、UPDATE（更新经验）
	 */
	default ExperienceOperationType getOperationType() {
		return ExperienceOperationType.QUERY;
	}

	/**
	 * 经验操作类型枚举
	 */
	enum ExperienceOperationType {

		/**
		 * 保存经验
		 */
		SAVE,

		/**
		 * 查询经验
		 */
		QUERY,

		/**
		 * 更新经验
		 */
		UPDATE,

		/**
		 * 删除经验
		 */
		DELETE

	}

}

