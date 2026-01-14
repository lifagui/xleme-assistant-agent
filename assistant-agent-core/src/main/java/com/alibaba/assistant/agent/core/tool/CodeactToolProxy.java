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
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.chat.model.ToolContext;

/**
 * CodeactTool 代理类 - 新机制。
 *
 * <p>提供给 GraalVM 中的 Python 代码调用的工具代理，负责查找和执行 CodeactTool。
 *
 * @author Assistant Agent Team
 * @since 1.0.0
 */
public class CodeactToolProxy {

	private static final Logger logger = LoggerFactory.getLogger(CodeactToolProxy.class);

	private final CodeactToolRegistry registry;

	private final ToolContext toolContext;

	private final ObjectMapper objectMapper = new ObjectMapper();

	/**
	 * 构造函数。
	 *
	 * @param registry 工具注册表
	 * @param toolContext 工具上下文
	 */
	public CodeactToolProxy(CodeactToolRegistry registry, ToolContext toolContext) {
		this.registry = registry;
		this.toolContext = toolContext;
		logger.info("CodeactToolProxy#<init> - reason=工具代理对象初始化完成");
	}

	/**
	 * 调用工具。
	 *
	 * @param toolName 工具名称
	 * @param argsJson 参数（JSON 字符串）
	 * @return 工具执行结果（JSON 字符串）
	 */
	public String call(String toolName, String argsJson) {
		logger.info("CodeactToolProxy#call - reason=调用工具, toolName={}, argsJsonLength={}",
				toolName, argsJson != null ? argsJson.length() : 0);

		try {
			logger.debug("CodeactToolProxy#call - reason=参数内容, args={}", argsJson);

			// 从注册表获取工具
			CodeactTool tool = registry.getTool(toolName)
					.orElseThrow(() -> new IllegalArgumentException("Tool not found: " + toolName));

			// 调用工具
			String result = tool.call(argsJson, toolContext);

			logger.info("CodeactToolProxy#call - reason=工具调用成功, toolName={}, resultLength={}",
					toolName, result != null ? result.length() : 0);

			if (logger.isDebugEnabled()) {
				logger.debug("CodeactToolProxy#call - reason=返回内容, result={}", result);
			}

			return result;
		}
		catch (Exception e) {
			logger.error("CodeactToolProxy#call - reason=工具调用失败, toolName=" + toolName, e);
			return "{\"error\": \"" + e.getMessage().replace("\"", "\\\"") + "\"}";
		}
	}

}

