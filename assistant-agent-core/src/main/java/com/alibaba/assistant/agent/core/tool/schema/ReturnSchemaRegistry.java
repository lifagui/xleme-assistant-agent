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
package com.alibaba.assistant.agent.core.tool.schema;

import com.alibaba.assistant.agent.common.tools.definition.ReturnSchema;

import java.util.List;
import java.util.Optional;

/**
 * 返回值 schema 注册表接口。
 *
 * <p>管理工具的返回值结构信息，支持声明式定义和运行时观测两种来源。
 *
 * @author Assistant Agent Team
 * @since 1.0.0
 */
public interface ReturnSchemaRegistry {

	/**
	 * 注册工具声明的返回值 schema。
	 * @param toolName 工具名
	 * @param declaredSchema 声明的 schema
	 */
	void registerDeclared(String toolName, ReturnSchema declaredSchema);

	/**
	 * 观测工具的实际返回值，更新 schema。
	 *
	 * <p>会自动与已有 schema 合并。
	 * @param toolName 工具名
	 * @param resultJson 实际返回的 JSON 字符串
	 * @param success 是否成功（用于区分 successShape 和 errorShape）
	 */
	void observe(String toolName, String resultJson, boolean success);

	/**
	 * 获取工具的返回值 schema。
	 * @param toolName 工具名
	 * @return Optional 包装的返回值 schema
	 */
	Optional<ReturnSchema> getSchema(String toolName);

	/**
	 * 获取所有已知返回值 schema 的工具名列表。
	 * @return 工具名列表
	 */
	List<String> getToolsWithSchema();

	/**
	 * 清除指定工具的运行时观测数据（保留声明数据）。
	 * @param toolName 工具名
	 */
	void clearObserved(String toolName);

	/**
	 * 清除所有运行时观测数据。
	 */
	void clearAllObserved();

}

