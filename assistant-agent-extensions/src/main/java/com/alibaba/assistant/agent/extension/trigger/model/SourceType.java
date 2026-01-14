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
 * 来源类型枚举
 *
 * @author Assistant Agent Team
 * @since 1.0.0
 */
public enum SourceType {

	/**
	 * 用户级别触发器
	 */
	USER,

	/**
	 * 群组级别触发器
	 */
	GROUP,

	/**
	 * 全局触发器
	 */
	GLOBAL

}

