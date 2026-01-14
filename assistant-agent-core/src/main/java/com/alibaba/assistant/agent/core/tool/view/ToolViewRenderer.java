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
package com.alibaba.assistant.agent.core.tool.view;

import com.alibaba.assistant.agent.common.tools.CodeactTool;
import com.alibaba.assistant.agent.common.tools.definition.ReturnSchema;

/**
 * 工具视图渲染器接口 - 将结构化工具定义渲染为目标语言的代码 stub。
 *
 * @author Assistant Agent Team
 * @since 1.0.0
 */
public interface ToolViewRenderer {

	/**
	 * 渲染工具的代码 stub（函数定义）。
	 * @param tool CodeAct 工具
	 * @param returnSchema 返回值 schema（可能来自声明或运行时观测）
	 * @return 渲染后的代码 stub
	 */
	String renderToolStub(CodeactTool tool, ReturnSchema returnSchema);

	/**
	 * 渲染工具类的代码 stub（包含多个方法）。
	 * @param className 类名
	 * @param classDescription 类描述
	 * @param tools 工具列表
	 * @param schemaProvider 返回值 schema 提供者
	 * @return 渲染后的类代码 stub
	 */
	String renderClassStub(String className, String classDescription, java.util.List<CodeactTool> tools,
			ReturnSchemaProvider schemaProvider);

	/**
	 * 返回值 schema 提供者接口。
	 */
	@FunctionalInterface
	interface ReturnSchemaProvider {

		/**
		 * 获取指定工具的返回值 schema。
		 * @param toolName 工具名
		 * @return 返回值 schema，可能为 null
		 */
		ReturnSchema getSchema(String toolName);

	}

}

