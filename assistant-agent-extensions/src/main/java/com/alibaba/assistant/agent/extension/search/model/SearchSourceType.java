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
package com.alibaba.assistant.agent.extension.search.model;

/**
 * 搜索数据源类型枚举
 *
 * @author Assistant Agent Team
 */
public enum SearchSourceType {
	/**
	 * 项目上下文（代码、配置、日志等）
	 */
	PROJECT,

	/**
	 * 团队/组织知识库（FAQ、规范、最佳实践）
	 */
	KNOWLEDGE,

	/**
	 * Web搜索（公开搜索引擎）
	 */
	WEB,

	/**
	 * 经验池
	 */
	EXPERIENCE,

	/**
	 * 自定义数据源
	 */
	CUSTOM
}

