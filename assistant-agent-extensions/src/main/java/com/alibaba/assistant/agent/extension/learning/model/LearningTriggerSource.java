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

package com.alibaba.assistant.agent.extension.learning.model;

/**
 * 学习触发来源枚举
 * 定义学习过程可以从哪些执行点触发
 *
 * @author Assistant Agent Team
 * @since 1.0.0
 */
public enum LearningTriggerSource {

	/**
	 * Agent执行完成后触发学习
	 */
	AFTER_AGENT,

	/**
	 * 模型调用完成后触发学习
	 */
	AFTER_MODEL,

	/**
	 * Agent执行前触发学习
	 */
	BEFORE_AGENT,

	/**
	 * 模型调用前触发学习
	 */
	BEFORE_MODEL,

	/**
	 * 工具拦截器触发学习
	 */
	TOOL_INTERCEPTOR,

	/**
	 * 定时调度触发学习
	 */
	SCHEDULED,

	/**
	 * 事件驱动触发学习
	 */
	EVENT_DRIVEN

}

