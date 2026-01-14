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
 * Trigger 领域的 CodeAct 工具接口。
 *
 * <p>用于定义触发器相关的工具，支持订阅、取消、查询和手动触发等功能。
 *
 * @author Assistant Agent Team
 * @since 1.0.0
 */
public interface TriggerCodeactTool extends CodeactTool {

	/**
	 * 获取触发源类型。
	 *
	 * @return 触发源类型，例如 TIME（时间触发）、EVENT（事件触发）、CONDITION（条件触发）
	 */
	TriggerSourceType getSourceType();

	/**
	 * 触发源类型枚举
	 */
	enum TriggerSourceType {

		/**
		 * 时间触发
		 */
		TIME,

		/**
		 * 事件触发
		 */
		EVENT,

		/**
		 * 条件触发
		 */
		CONDITION,

		/**
		 * 手动触发
		 */
		MANUAL

	}

}

