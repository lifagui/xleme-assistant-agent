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
package com.alibaba.assistant.agent.common.tools.definition;

import org.springframework.ai.tool.definition.ToolDefinition;

/**
 * CodeAct 工具结构化定义 - 扩展 Spring AI 的 ToolDefinition。
 *
 * <p>在 name/description/inputSchema 基础上，提供：
 * <ul>
 * <li>结构化参数树（从 inputSchema 解析）</li>
 * <li>返回值 schema（可声明）</li>
 * <li>返回值描述</li>
 * </ul>
 *
 * @author Assistant Agent Team
 * @since 1.0.0
 */
public interface CodeactToolDefinition extends ToolDefinition {

	// ============ 继承自 ToolDefinition ============
	// String name();
	// String description();
	// String inputSchema();

	// ============ 结构化参数（从 inputSchema 解析）============

	/**
	 * 获取结构化参数树。
	 *
	 * <p>从 inputSchema() 解析得到的结构化参数信息， 包含参数名、类型、描述、格式、枚举值、默认值等。
	 * @return 结构化参数树
	 */
	ParameterTree parameterTree();

	// ============ 返回值定义 ============

	/**
	 * 获取声明的返回值 schema（可选）。
	 *
	 * <p>工具作者可以显式声明返回值结构；如果未声明，返回 null， 系统将通过运行时观测来学习返回值结构。
	 * @return 声明的返回值 schema，未声明时返回 null
	 */
	default ReturnSchema declaredReturnSchema() {
		return null;
	}

	/**
	 * 获取返回值的自然语言描述。
	 * @return 返回值描述
	 */
	default String returnDescription() {
		return null;
	}

	/**
	 * 获取返回值的类型提示（用于代码生成）。
	 *
	 * <p>例如："Dict[str, Any]"、"List[Dict]"、"bool"
	 * @return 返回值类型提示
	 */
	default String returnTypeHint() {
		return "Dict[str, Any]";
	}

	// ============ 构建器 ============

	/**
	 * 创建构建器实例。
	 * @return 构建器
	 */
	static DefaultCodeactToolDefinition.Builder builder() {
		return DefaultCodeactToolDefinition.builder();
	}

}

