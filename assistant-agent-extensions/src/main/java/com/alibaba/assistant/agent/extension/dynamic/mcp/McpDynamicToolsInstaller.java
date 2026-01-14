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
package com.alibaba.assistant.agent.extension.dynamic.mcp;

import com.alibaba.assistant.agent.common.tools.CodeactTool;
import com.alibaba.assistant.agent.core.tool.CodeactToolRegistry;
import com.alibaba.assistant.agent.extension.dynamic.registry.DynamicCodeactToolFactoryRegistry;
import com.alibaba.assistant.agent.extension.dynamic.spi.DynamicToolFactoryContext;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.tool.ToolCallbackProvider;
import org.springframework.context.ApplicationContext;

import java.util.ArrayList;
import java.util.List;

/**
 * MCP 动态工具安装器。
 *
 * <p>提供便捷的方法将 MCP 工具注册到 CodeactToolRegistry。
 * 复用 MCP Client Boot Starter 提供的 ToolCallbackProvider。
 *
 * @author Assistant Agent Team
 * @since 1.0.0
 */
public class McpDynamicToolsInstaller {

	private static final Logger logger = LoggerFactory.getLogger(McpDynamicToolsInstaller.class);

	/**
	 * 从 Spring 容器获取 MCP ToolCallbackProvider 并安装到 CodeactToolRegistry。
	 *
	 * @param registry CodeAct 工具注册表
	 * @param applicationContext Spring 应用上下文
	 * @param serverSpecs MCP Server 元数据列表（用于命名与描述）
	 * @return 安装的工具列表
	 */
	public static List<CodeactTool> install(CodeactToolRegistry registry,
			ApplicationContext applicationContext,
			List<McpServerSpec> serverSpecs) {

		logger.info("McpDynamicToolsInstaller#install - reason=开始安装MCP动态工具");

		// 尝试从容器获取 ToolCallbackProvider
		ToolCallbackProvider provider = null;
		try {
			// 尝试获取同步版本
			provider = applicationContext.getBean("syncMcpToolCallbackProvider", ToolCallbackProvider.class);
			logger.debug("McpDynamicToolsInstaller#install - reason=获取到SyncMcpToolCallbackProvider");
		}
		catch (Exception e) {
			logger.debug("McpDynamicToolsInstaller#install - reason=未找到SyncMcpToolCallbackProvider, 尝试异步版本");
			try {
				// 尝试获取异步版本
				provider = applicationContext.getBean("asyncMcpToolCallbackProvider", ToolCallbackProvider.class);
				logger.debug("McpDynamicToolsInstaller#install - reason=获取到AsyncMcpToolCallbackProvider");
			}
			catch (Exception e2) {
				// 尝试通用名称
				try {
					provider = applicationContext.getBean(ToolCallbackProvider.class);
					logger.debug("McpDynamicToolsInstaller#install - reason=获取到ToolCallbackProvider");
				}
				catch (Exception e3) {
					logger.warn("McpDynamicToolsInstaller#install - reason=未找到ToolCallbackProvider, " +
							"请确保已配置MCP Client Boot Starter");
					return new ArrayList<>();
				}
			}
		}

		return installWithProvider(registry, provider, serverSpecs, null);
	}

	/**
	 * 使用指定的 ToolCallbackProvider 安装 MCP 工具。
	 *
	 * @param registry CodeAct 工具注册表
	 * @param provider ToolCallbackProvider 实例
	 * @param serverSpecs MCP Server 元数据列表
	 * @param objectMapper ObjectMapper 实例（可选）
	 * @return 安装的工具列表
	 */
	public static List<CodeactTool> installWithProvider(CodeactToolRegistry registry,
			ToolCallbackProvider provider,
			List<McpServerSpec> serverSpecs,
			ObjectMapper objectMapper) {

		logger.info("McpDynamicToolsInstaller#installWithProvider - reason=使用Provider安装MCP工具");

		// 构建工厂
		McpDynamicToolFactory.Builder factoryBuilder = McpDynamicToolFactory.builder()
				.toolCallbackProvider(provider);

		if (serverSpecs != null) {
			factoryBuilder.serverSpecs(serverSpecs);
		}

		McpDynamicToolFactory factory = factoryBuilder.build();

		// 构建上下文
		DynamicToolFactoryContext.Builder contextBuilder = DynamicToolFactoryContext.builder();
		if (objectMapper != null) {
			contextBuilder.objectMapper(objectMapper);
		}
		DynamicToolFactoryContext context = contextBuilder.build();

		// 创建工具
		List<CodeactTool> tools = factory.createTools(context);

		// 注册到 CodeactToolRegistry
		for (CodeactTool tool : tools) {
			try {
				registry.register(tool);
				logger.debug("McpDynamicToolsInstaller#installWithProvider - reason=注册MCP工具成功, toolName={}",
						tool.getToolDefinition().name());
			}
			catch (Exception e) {
				logger.error("McpDynamicToolsInstaller#installWithProvider - reason=注册MCP工具失败, toolName={}, error={}",
						tool.getToolDefinition().name(), e.getMessage(), e);
			}
		}

		logger.info("McpDynamicToolsInstaller#installWithProvider - reason=MCP动态工具安装完成, count={}", tools.size());

		return tools;
	}

	/**
	 * 使用 DynamicCodeactToolFactoryRegistry 安装 MCP 工具。
	 *
	 * @param factoryRegistry 动态工具工厂注册中心
	 * @param provider ToolCallbackProvider 实例
	 * @param serverSpecs MCP Server 元数据列表
	 * @return 配置后的 factoryRegistry，支持链式调用
	 */
	public static DynamicCodeactToolFactoryRegistry configureRegistry(
			DynamicCodeactToolFactoryRegistry factoryRegistry,
			ToolCallbackProvider provider,
			List<McpServerSpec> serverSpecs) {

		logger.info("McpDynamicToolsInstaller#configureRegistry - reason=配置MCP工厂到注册中心");

		McpDynamicToolFactory.Builder factoryBuilder = McpDynamicToolFactory.builder()
				.toolCallbackProvider(provider);

		if (serverSpecs != null) {
			factoryBuilder.serverSpecs(serverSpecs);
		}

		factoryRegistry.registerFactory(factoryBuilder.build());

		return factoryRegistry;
	}

}

