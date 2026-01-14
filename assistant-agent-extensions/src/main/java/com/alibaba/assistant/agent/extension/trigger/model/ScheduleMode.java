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

package com.alibaba.assistant.agent.extension.trigger.model;

/**
 * 调度模式枚举
 * 定义触发器支持的各种调度方式
 *
 * @author Assistant Agent Team
 * @since 1.0.0
 */
public enum ScheduleMode {

	/**
	 * Cron表达式调度
	 */
	CRON,

	/**
	 * 固定延迟调度
	 */
	FIXED_DELAY,

	/**
	 * 固定频率调度
	 */
	FIXED_RATE,

	/**
	 * 一次性调度
	 */
	ONE_TIME,

	/**
	 * 自定义Trigger调度
	 */
	TRIGGER

}

