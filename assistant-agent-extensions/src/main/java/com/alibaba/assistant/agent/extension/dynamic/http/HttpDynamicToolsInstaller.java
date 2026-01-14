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
package com.alibaba.assistant.agent.extension.dynamic.http;

import com.alibaba.assistant.agent.common.tools.CodeactTool;
import com.alibaba.assistant.agent.core.tool.CodeactToolRegistry;
import com.alibaba.assistant.agent.extension.dynamic.registry.DynamicCodeactToolFactoryRegistry;
import com.alibaba.assistant.agent.extension.dynamic.spi.DynamicToolFactoryContext;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

/**
 * HTTP 动态工具安装器。
 *
 * <p>提供便捷的方法将 OpenAPI 定义的工具注册到 CodeactToolRegistry。
 *
 * @author Assistant Agent Team
 * @since 1.0.0
 */
public class HttpDynamicToolsInstaller {

	private static final Logger logger = LoggerFactory.getLogger(HttpDynamicToolsInstaller.class);

	/**
	 * 安装 HTTP 动态工具到 CodeactToolRegistry。
	 *
	 * @param registry CodeAct 工具注册表
	 * @param openApiSpec OpenAPI 文档规格
	 * @param endpoints 要暴露的 endpoints 列表
	 * @return 安装的工具列表
	 */
	public static List<CodeactTool> install(CodeactToolRegistry registry,
			OpenApiSpec openApiSpec,
			List<EndpointSpec> endpoints) {

		return install(registry, openApiSpec, endpoints, null);
	}

	/**
	 * 安装 HTTP 动态工具到 CodeactToolRegistry。
	 *
	 * @param registry CodeAct 工具注册表
	 * @param openApiSpec OpenAPI 文档规格
	 * @param endpoints 要暴露的 endpoints 列表
	 * @param objectMapper ObjectMapper 实例（可选）
	 * @return 安装的工具列表
	 */
	public static List<CodeactTool> install(CodeactToolRegistry registry,
			OpenApiSpec openApiSpec,
			List<EndpointSpec> endpoints,
			ObjectMapper objectMapper) {

		logger.info("HttpDynamicToolsInstaller#install - reason=开始安装HTTP动态工具, endpointCount={}",
				endpoints != null ? endpoints.size() : 0);

		// 构建工厂
		HttpDynamicToolFactory factory = HttpDynamicToolFactory.builder()
				.openApiSpec(openApiSpec)
				.endpoints(endpoints)
				.build();

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
				logger.debug("HttpDynamicToolsInstaller#install - reason=注册HTTP工具成功, toolName={}",
						tool.getToolDefinition().name());
			}
			catch (Exception e) {
				logger.error("HttpDynamicToolsInstaller#install - reason=注册HTTP工具失败, toolName={}, error={}",
						tool.getToolDefinition().name(), e.getMessage(), e);
			}
		}

		logger.info("HttpDynamicToolsInstaller#install - reason=HTTP动态工具安装完成, count={}", tools.size());

		return tools;
	}

	/**
	 * 配置 HTTP 工厂到动态工具注册中心。
	 *
	 * @param factoryRegistry 动态工具工厂注册中心
	 * @param openApiSpec OpenAPI 文档规格
	 * @param endpoints 要暴露的 endpoints 列表
	 * @return 配置后的 factoryRegistry，支持链式调用
	 */
	public static DynamicCodeactToolFactoryRegistry configureRegistry(
			DynamicCodeactToolFactoryRegistry factoryRegistry,
			OpenApiSpec openApiSpec,
			List<EndpointSpec> endpoints) {

		logger.info("HttpDynamicToolsInstaller#configureRegistry - reason=配置HTTP工厂到注册中心");

		HttpDynamicToolFactory factory = HttpDynamicToolFactory.builder()
				.openApiSpec(openApiSpec)
				.endpoints(endpoints)
				.build();

		factoryRegistry.registerFactory(factory);

		return factoryRegistry;
	}

}

