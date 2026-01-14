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
 * Search 领域的 CodeAct 工具接口。
 *
 * <p>用于定义搜索相关的工具，支持项目搜索、知识库搜索、Web 搜索等功能。
 *
 * @author Assistant Agent Team
 * @since 1.0.0
 */
public interface SearchCodeactTool extends CodeactTool {

	/**
	 * 获取默认搜索范围。
	 *
	 * @return 搜索范围，例如 PROJECT（项目内搜索）、KNOWLEDGE（知识库搜索）、WEB（Web 搜索）
	 */
	SearchScope getDefaultScope();

	/**
	 * 搜索范围枚举
	 */
	enum SearchScope {

		/**
		 * 项目内搜索
		 */
		PROJECT,

		/**
		 * 知识库搜索
		 */
		KNOWLEDGE,

		/**
		 * Web 搜索
		 */
		WEB,

		/**
		 * 统一搜索（聚合多种搜索）
		 */
		UNIFIED

	}

}

