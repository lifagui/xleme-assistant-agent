package com.alibaba.assistant.agent.extension.reply.config;

import com.alibaba.assistant.agent.common.tools.ReplyCodeactTool;
import com.alibaba.assistant.agent.extension.reply.spi.ReplyChannelDefinition;
import com.alibaba.assistant.agent.extension.reply.tools.ReplyCodeactToolFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.ArrayList;
import java.util.List;

/**
 * 回复模块自动配置 - 基于新的 CodeactTool 机制
 *
 * <p>框架只提供 ReplyCodeactToolFactory 等基础设施。
 * 具体的 ReplyChannelDefinition 实现（如 IDE 渠道）应由使用者在自己的模块中注册。
 *
 * @author Assistant Agent Team
 */
@Configuration
@EnableConfigurationProperties(ReplyExtensionProperties.class)
@ConditionalOnProperty(prefix = "spring.ai.alibaba.codeact.extension.reply", name = "enabled",
		havingValue = "true", matchIfMissing = true)
public class ReplyExtensionAutoConfiguration {

	private static final Logger log = LoggerFactory.getLogger(ReplyExtensionAutoConfiguration.class);

	public ReplyExtensionAutoConfiguration() {
		log.info("ReplyExtensionAutoConfiguration#init - reason=reply module enabled");
	}


	/**
	 * Reply CodeactTool 工厂 - 新机制
	 */
	@Bean
	public ReplyCodeactToolFactory replyCodeactToolFactory(List<ReplyChannelDefinition> channelDefinitions) {
		log.info("ReplyExtensionAutoConfiguration#replyCodeactToolFactory - reason=创建CodeactTool工厂, channels={}",
				channelDefinitions.size());
		return new ReplyCodeactToolFactory(channelDefinitions);
	}

	/**
	 * Reply工具列表Bean - 直接作为List返回，让Spring自动注入
	 *
	 * 注意：这个方法会在所有普通Bean创建之后才执行，所以可以安全地使用工厂和配置
	 */
	@Bean
	public List<ReplyCodeactTool> replyCodeactTools(
			ReplyCodeactToolFactory factory,
			ReplyExtensionProperties properties) {

		log.info("ReplyExtensionAutoConfiguration#replyCodeactTools - reason=开始创建Reply工具");

		// 加载配置
		List<ReplyToolConfig> configs = new ArrayList<>();
		if (properties.getTools() != null) {
			configs.addAll(properties.getTools());
		}

		// 创建工具
		List<ReplyCodeactTool> tools = factory.createTools(configs);

		log.info("ReplyExtensionAutoConfiguration#replyCodeactTools - reason=Reply工具创建完成, count={}", tools.size());

		// 打印每个工具的详情
		for (int i = 0; i < tools.size(); i++) {
			ReplyCodeactTool tool = tools.get(i);
			log.info("ReplyExtensionAutoConfiguration#replyCodeactTools - reason=创建Reply工具, index={}, name={}, description={}",
				i + 1, tool.getToolDefinition().name(), tool.getToolDefinition().description());
		}

		return tools;
	}

}
