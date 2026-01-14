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
package com.alibaba.assistant.agent.core.config;

import com.alibaba.assistant.agent.core.tool.schema.DefaultReturnSchemaRegistry;
import com.alibaba.assistant.agent.core.tool.schema.ReturnSchemaRegistry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;

/**
 * CodeAct Core 自动配置类。
 *
 * <p>提供核心组件的 Spring Bean 配置，包括：
 * <ul>
 *   <li>ReturnSchemaRegistry - 返回值 schema 注册表（进程内单例）</li>
 * </ul>
 *
 * @author Assistant Agent Team
 * @since 1.0.0
 */
@AutoConfiguration
public class CodeactCoreAutoConfiguration {

	private static final Logger logger = LoggerFactory.getLogger(CodeactCoreAutoConfiguration.class);

	/**
	 * 创建 ReturnSchemaRegistry 单例 Bean。
	 *
	 * <p>这是一个进程内的单例存储，用于收集和存储工具返回值的 schema。
	 * 在整个应用生命周期内持续累积观测数据。
	 *
	 * @return ReturnSchemaRegistry 实例
	 */
	@Bean
	@ConditionalOnMissingBean
	public ReturnSchemaRegistry returnSchemaRegistry() {
		logger.info("CodeactCoreAutoConfiguration#returnSchemaRegistry - reason=创建ReturnSchemaRegistry单例Bean");
		return new DefaultReturnSchemaRegistry();
	}

}

