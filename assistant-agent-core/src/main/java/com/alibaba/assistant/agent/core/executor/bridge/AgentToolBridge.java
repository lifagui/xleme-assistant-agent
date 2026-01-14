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
package com.alibaba.assistant.agent.core.executor.bridge;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.chat.model.ToolContext;
import org.springframework.ai.tool.ToolCallback;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Bridge object to expose agent tools to Python code.
 * Python code can call: agent_tools.call("tool_name", args)
 *
 * @author Assistant Agent Team
 * @since 1.0.0
 */
public class AgentToolBridge {

	private static final Logger logger = LoggerFactory.getLogger(AgentToolBridge.class);

	private final Map<String, ToolCallback> toolMap;

	public AgentToolBridge(List<ToolCallback> tools) {
		this.toolMap = new HashMap<>();
		if (tools != null) {
			for (ToolCallback tool : tools) {
				String toolName = tool.getToolDefinition().name();
				toolMap.put(toolName, tool);
			}
		}
		logger.debug("AgentToolBridge#<init> 初始化: 注册了{}个工具", toolMap.size());
	}

	/**
	 * Call a tool by name with JSON string arguments
	 * This method is exposed to Python code
	 */
	public Object call(String toolName, String argsJson) {
		logger.info("AgentToolBridge#call 调用工具: toolName={}", toolName);

		ToolCallback tool = toolMap.get(toolName);
		if (tool == null) {
			logger.error("AgentToolBridge#call 工具不存在: {}", toolName);
			throw new IllegalArgumentException("Tool not found: " + toolName);
		}

		try {
			// Create empty tool context (can be enhanced later)
			ToolContext context = new ToolContext(Map.of());

			// Call the tool
			Object result = tool.call(argsJson, context);

			logger.info("AgentToolBridge#call 工具调用成功: toolName={}", toolName);
			return result;

		} catch (Exception e) {
			logger.error("AgentToolBridge#call 工具调用失败: toolName=" + toolName, e);
			throw new RuntimeException("Tool execution failed: " + toolName, e);
		}
	}

	/**
	 * List available tool names
	 */
	public List<String> listTools() {
		return List.copyOf(toolMap.keySet());
	}

	/**
	 * Check if a tool exists
	 */
	public boolean hasTool(String toolName) {
		return toolMap.containsKey(toolName);
	}
}

