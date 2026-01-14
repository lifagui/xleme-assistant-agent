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
package com.alibaba.assistant.agent.core.tool;

import com.alibaba.assistant.agent.common.tools.CodeactTool;
import com.alibaba.assistant.agent.common.enums.Language;
import com.alibaba.assistant.agent.common.tools.definition.CodeactToolDefinition;
import com.alibaba.assistant.agent.common.tools.definition.ReturnSchema;
import com.alibaba.assistant.agent.core.tool.schema.ReturnSchemaRegistry;

import java.util.List;
import java.util.Optional;

/**
 * CodeactTool 注册表接口 - 新机制。
 *
 * <p>管理和查询所有注册的 CodeactTool 实例，并提供结构化工具定义和返回值 schema 的查询能力。
 *
 * @author Assistant Agent Team
 * @since 1.0.0
 */
public interface CodeactToolRegistry {

	/**
	 * 注册一个 CodeactTool。
	 * @param tool 要注册的工具
	 */
	void register(CodeactTool tool);

	/**
	 * 根据工具名称获取工具。
	 * @param name 工具名称
	 * @return Optional 包装的工具实例
	 */
	Optional<CodeactTool> getTool(String name);

	/**
	 * 根据别名获取工具。
	 * @param alias 工具别名
	 * @return Optional 包装的工具实例
	 */
	Optional<CodeactTool> getToolByAlias(String alias);

	/**
	 * 获取所有注册的工具。
	 * @return 所有工具的列表
	 */
	List<CodeactTool> getAllTools();

	/**
	 * 获取支持特定语言的所有工具。
	 * @param language 编程语言
	 * @return 支持该语言的工具列表
	 */
	List<CodeactTool> getToolsForLanguage(Language language);

	// ============ 结构化定义查询 ============

	/**
	 * 获取工具的结构化定义。
	 * @param toolName 工具名称
	 * @return Optional 包装的工具定义
	 */
	Optional<CodeactToolDefinition> getToolDefinition(String toolName);

	/**
	 * 获取工具的返回值 schema。
	 * @param toolName 工具名称
	 * @return Optional 包装的返回值 schema
	 */
	Optional<ReturnSchema> getReturnSchema(String toolName);

	// ============ Prompt 生成 ============

	/**
	 * 生成结构化的工具提示词。
	 *
	 * <p>使用结构化的参数树和返回值 schema 生成代码 stub。
	 * @param language 编程语言
	 * @return 结构化的工具提示词
	 */
	String generateStructuredToolPrompt(Language language);

	/**
	 * 生成工具描述提示词。
	 *
	 * <p>用于在代码生成时告诉 LLM 有哪些可用的工具。
	 * @param language 编程语言
	 * @return 工具描述提示词
	 * @deprecated 使用 {@link #generateStructuredToolPrompt(com.alibaba.assistant.agent.common.enums.Language)} 替代
	 */
	@Deprecated
	String generateToolDescriptionPrompt(Language language);

	// ============ 返回值 schema 注册表 ============

	/**
	 * 获取返回值 schema 注册表。
	 * @return 返回值 schema 注册表
	 */
	ReturnSchemaRegistry getReturnSchemaRegistry();

}

