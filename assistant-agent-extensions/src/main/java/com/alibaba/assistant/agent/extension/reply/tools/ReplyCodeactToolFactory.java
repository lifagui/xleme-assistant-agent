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
package com.alibaba.assistant.agent.extension.reply.tools;

import com.alibaba.assistant.agent.common.tools.ReplyCodeactTool;
import com.alibaba.assistant.agent.extension.reply.config.ReplyToolConfig;
import com.alibaba.assistant.agent.extension.reply.model.ParameterSchema;
import com.alibaba.assistant.agent.extension.reply.spi.ReplyChannelDefinition;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Reply CodeactTool 工厂类。
 *
 * <p>负责根据配置创建 ReplyCodeactTool 实例。
 *
 * @author Assistant Agent Team
 * @since 1.0.0
 */
public class ReplyCodeactToolFactory {

	private static final Logger log = LoggerFactory.getLogger(ReplyCodeactToolFactory.class);

	private final Map<String, ReplyChannelDefinition> channelMap;

	/**
	 * 构造工厂实例。
	 *
	 * @param channelDefinitions 渠道定义列表
	 */
	public ReplyCodeactToolFactory(List<ReplyChannelDefinition> channelDefinitions) {
		this.channelMap = new HashMap<>();
		for (ReplyChannelDefinition channel : channelDefinitions) {
			channelMap.put(channel.getChannelCode(), channel);
		}
		log.info("ReplyCodeactToolFactory#init - reason=工厂初始化完成, channels={}", channelMap.size());
	}

	/**
	 * 根据配置列表创建 ReplyCodeactTool 列表。
	 *
	 * @param configs 工具配置列表
	 * @return ReplyCodeactTool 列表
	 */
	public List<ReplyCodeactTool> createTools(List<ReplyToolConfig> configs) {
		List<ReplyCodeactTool> tools = new ArrayList<>();

		for (ReplyToolConfig config : configs) {
			try {
				ReplyCodeactTool tool = createTool(config);
				if (tool != null) {
					tools.add(tool);
					log.info("ReplyCodeactToolFactory#createTools - reason=创建工具成功, toolName={}, channelCode={}",
							config.getToolName(), config.getChannelCode());
				}
			}
			catch (Exception e) {
				log.error("ReplyCodeactToolFactory#createTools - reason=创建工具失败, toolName={}, error={}",
						config.getToolName(), e.getMessage(), e);
			}
		}

		log.info("ReplyCodeactToolFactory#createTools - reason=工具创建完成, totalCount={}", tools.size());

		return tools;
	}

	/**
	 * 根据单个配置创建 ReplyCodeactTool。
	 *
	 * @param config 工具配置
	 * @return ReplyCodeactTool 实例
	 */
	public ReplyCodeactTool createTool(ReplyToolConfig config) {
		// 查找对应的渠道定义
		ReplyChannelDefinition channel = channelMap.get(config.getChannelCode());
		if (channel == null) {
			log.error("ReplyCodeactToolFactory#createTool - reason=渠道不存在, channelCode={}", config.getChannelCode());
			return null;
		}

		// 构建参数模式
		ParameterSchema parameterSchema = buildParameterSchema(config, channel);

		// 确定渠道类型
		ReplyCodeactTool.ReplyChannelType channelType = determineChannelType(config);

		// 创建工具实例
		return new BaseReplyCodeactTool(config.getToolName(), config.getDescription(), channel, config, parameterSchema,
				channelType);
	}

	/**
	 * 构建参数模式。
	 */
	private ParameterSchema buildParameterSchema(ReplyToolConfig config, ReplyChannelDefinition channel) {
		// 从配置和渠道定义中构建参数模式
		return ParameterSchema.builder()
                .parameter("message", ParameterSchema.ParameterType.STRING, true, "要发送的消息内容")
                .build();
	}

	/**
	 * 确定渠道类型。
	 */
	private ReplyCodeactTool.ReplyChannelType determineChannelType(ReplyToolConfig config) {
		// 可以从配置中读取，或根据渠道代码推断
		// 这里简单返回 PRIMARY，实际应该从配置中获取
		return ReplyCodeactTool.ReplyChannelType.PRIMARY;
	}

}

