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
package com.alibaba.assistant.agent.extension.search.config;

import com.alibaba.assistant.agent.common.tools.SearchCodeactTool;
import com.alibaba.assistant.agent.extension.search.internal.DefaultSearchFacade;
import com.alibaba.assistant.agent.extension.search.spi.SearchFacade;
import com.alibaba.assistant.agent.extension.search.spi.SearchProvider;
import com.alibaba.assistant.agent.extension.search.tools.SearchCodeactToolFactory;
import com.alibaba.assistant.agent.extension.search.tools.UnifiedSearchCodeactTool;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;

import java.util.ArrayList;
import java.util.List;

/**
 * 搜索扩展自动配置（框架层）
 *
 * <p>框架只提供 SearchFacade、SearchCodeactToolFactory 等基础设施。
 * 具体的 SearchProvider 实现（如 Mock Provider）应由使用者在自己的模块中注册。
 *
 * @author Assistant Agent Team
 */
@AutoConfiguration
@ConditionalOnProperty(prefix = "spring.ai.alibaba.codeact.extension.search", name = "enabled", havingValue = "true", matchIfMissing = true)
@EnableConfigurationProperties(SearchExtensionProperties.class)
public class SearchExtensionAutoConfiguration {
	private static final Logger logger = LoggerFactory.getLogger(SearchExtensionAutoConfiguration.class);

	public SearchExtensionAutoConfiguration() {
		logger.info("SearchExtensionAutoConfiguration#init - reason=search extension framework is enabled");
	}


	@Bean
	public SearchFacade searchFacade(List<SearchProvider> providers) {
		logger.info("SearchExtensionAutoConfiguration#searchFacade - reason=creating search facade with {} providers",
				providers != null ? providers.size() : 0);
		return new DefaultSearchFacade(providers != null ? providers : new ArrayList<>());
	}

	@Bean
	public SearchCodeactToolFactory searchCodeactToolFactory(List<SearchProvider> providers) {
		logger.info("SearchExtensionAutoConfiguration#searchCodeactToolFactory - reason=创建SearchCodeactTool工厂, providerCount={}",
				providers != null ? providers.size() : 0);
		return new SearchCodeactToolFactory(providers != null ? providers : new ArrayList<>());
	}

	@Bean
	public UnifiedSearchCodeactTool unifiedSearchCodeactTool(List<SearchProvider> providers) {
		logger.info("SearchExtensionAutoConfiguration#unifiedSearchCodeactTool - reason=创建统一搜索工具, providerCount={}",
				providers != null ? providers.size() : 0);
		return new UnifiedSearchCodeactTool(providers != null ? providers : new ArrayList<>());
	}

	/**
	 * Search工具列表Bean - 直接作为List返回，让Spring自动注入
	 */
	@Bean
	public List<SearchCodeactTool> searchCodeactTools(SearchCodeactToolFactory factory) {
		logger.info("SearchExtensionAutoConfiguration#searchCodeactTools - reason=开始创建Search工具");

		List<SearchCodeactTool> tools = factory.createTools();

		logger.info("SearchExtensionAutoConfiguration#searchCodeactTools - reason=Search工具创建完成, count={}", tools.size());

		// 打印每个工具的详情
		for (int i = 0; i < tools.size(); i++) {
			SearchCodeactTool tool = tools.get(i);
			logger.info("SearchExtensionAutoConfiguration#searchCodeactTools - reason=创建Search工具, index={}, name={}, description={}",
				i + 1, tool.getToolDefinition().name(), tool.getToolDefinition().description());
		}

		return tools;
	}
}

