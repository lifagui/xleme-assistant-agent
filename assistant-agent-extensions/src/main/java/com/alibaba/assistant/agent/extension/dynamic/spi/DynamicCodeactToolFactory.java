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
package com.alibaba.assistant.agent.extension.dynamic.spi;

import com.alibaba.assistant.agent.common.tools.CodeactTool;

import java.util.List;

/**
 * 动态 CodeAct 工具工厂接口。
 *
 * <p>用于从外部来源（MCP、OpenAPI 等）动态产出 CodeactTool 实例。
 * 工厂不直接注册进 Spring；由 dynamic module 或 builder 统一注册到 CodeactToolRegistry。
 *
 * @author Assistant Agent Team
 * @since 1.0.0
 */
public interface DynamicCodeactToolFactory {

	/**
	 * 工厂的唯一标识（用于日志、诊断、冲突定位）。
	 *
	 * @return 工厂 ID
	 */
	String factoryId();

	/**
	 * 根据上下文（builder 传入的配置/外部资源）产出工具。
	 *
	 * @param context 工厂上下文
	 * @return CodeactTool 列表
	 */
	List<CodeactTool> createTools(DynamicToolFactoryContext context);

}

