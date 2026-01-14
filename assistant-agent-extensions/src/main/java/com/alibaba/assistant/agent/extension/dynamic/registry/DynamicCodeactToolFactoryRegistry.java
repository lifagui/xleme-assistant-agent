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
package com.alibaba.assistant.agent.extension.dynamic.registry;

import com.alibaba.assistant.agent.common.tools.CodeactTool;
import com.alibaba.assistant.agent.core.tool.CodeactToolRegistry;
import com.alibaba.assistant.agent.extension.dynamic.spi.DynamicCodeactToolFactory;
import com.alibaba.assistant.agent.extension.dynamic.spi.DynamicToolFactoryContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 动态工具工厂注册中心。
 *
 * <p>管理所有动态工具工厂，并在构建时统一触发工具创建和注册。
 *
 * @author Assistant Agent Team
 * @since 1.0.0
 */
public class DynamicCodeactToolFactoryRegistry {

	private static final Logger logger = LoggerFactory.getLogger(DynamicCodeactToolFactoryRegistry.class);

	private final Map<String, DynamicCodeactToolFactory> factories = new ConcurrentHashMap<>();

	private final List<CodeactTool> createdTools = new ArrayList<>();

	private DynamicToolFactoryContext context;

	/**
	 * 设置工厂上下文。
	 *
	 * @param context 工厂上下文
	 * @return 当前实例，支持链式调用
	 */
	public DynamicCodeactToolFactoryRegistry context(DynamicToolFactoryContext context) {
		this.context = context;
		return this;
	}

	/**
	 * 注册工厂。
	 *
	 * @param factory 要注册的工厂
	 * @return 当前实例，支持链式调用
	 */
	public DynamicCodeactToolFactoryRegistry registerFactory(DynamicCodeactToolFactory factory) {
		String factoryId = factory.factoryId();

		if (factories.containsKey(factoryId)) {
			logger.warn("DynamicCodeactToolFactoryRegistry#registerFactory - reason=工厂已存在将被覆盖, factoryId={}",
					factoryId);
		}

		factories.put(factoryId, factory);
		logger.info("DynamicCodeactToolFactoryRegistry#registerFactory - reason=工厂注册成功, factoryId={}",
				factoryId);

		return this;
	}

	/**
	 * 构建所有工具。
	 *
	 * <p>遍历所有已注册的工厂，触发工具创建。
	 *
	 * @return 创建的所有工具列表
	 */
	public List<CodeactTool> buildTools() {
		logger.info("DynamicCodeactToolFactoryRegistry#buildTools - reason=开始构建动态工具, factoryCount={}",
				factories.size());

		if (context == null) {
			context = DynamicToolFactoryContext.builder().build();
			logger.debug("DynamicCodeactToolFactoryRegistry#buildTools - reason=使用默认上下文");
		}

		createdTools.clear();

		for (Map.Entry<String, DynamicCodeactToolFactory> entry : factories.entrySet()) {
			String factoryId = entry.getKey();
			DynamicCodeactToolFactory factory = entry.getValue();

			try {
				List<CodeactTool> tools = factory.createTools(context);

				if (tools != null && !tools.isEmpty()) {
					createdTools.addAll(tools);
					logger.info("DynamicCodeactToolFactoryRegistry#buildTools - reason=工厂创建工具成功, " +
							"factoryId={}, toolCount={}", factoryId, tools.size());
				}
				else {
					logger.info("DynamicCodeactToolFactoryRegistry#buildTools - reason=工厂未创建任何工具, " +
							"factoryId={}", factoryId);
				}
			}
			catch (Exception e) {
				logger.error("DynamicCodeactToolFactoryRegistry#buildTools - reason=工厂创建工具失败, " +
						"factoryId={}, error={}", factoryId, e.getMessage(), e);
			}
		}

		logger.info("DynamicCodeactToolFactoryRegistry#buildTools - reason=动态工具构建完成, totalToolCount={}",
				createdTools.size());

		return new ArrayList<>(createdTools);
	}

	/**
	 * 将所有已创建的工具注册到 CodeactToolRegistry。
	 *
	 * @param registry CodeAct 工具注册表
	 */
	public void registerToCodeactToolRegistry(CodeactToolRegistry registry) {
		logger.info("DynamicCodeactToolFactoryRegistry#registerToCodeactToolRegistry - reason=开始注册动态工具, " +
				"toolCount={}", createdTools.size());

		for (CodeactTool tool : createdTools) {
			try {
				registry.register(tool);
				logger.debug("DynamicCodeactToolFactoryRegistry#registerToCodeactToolRegistry - " +
						"reason=工具注册成功, toolName={}", tool.getToolDefinition().name());
			}
			catch (Exception e) {
				logger.error("DynamicCodeactToolFactoryRegistry#registerToCodeactToolRegistry - " +
						"reason=工具注册失败, toolName={}, error={}",
						tool.getToolDefinition().name(), e.getMessage(), e);
			}
		}

		logger.info("DynamicCodeactToolFactoryRegistry#registerToCodeactToolRegistry - reason=动态工具注册完成");
	}

	/**
	 * 获取已创建的工具列表。
	 *
	 * @return 工具列表
	 */
	public List<CodeactTool> getCreatedTools() {
		return new ArrayList<>(createdTools);
	}

	/**
	 * 获取已注册的工厂数量。
	 *
	 * @return 工厂数量
	 */
	public int getFactoryCount() {
		return factories.size();
	}

}

