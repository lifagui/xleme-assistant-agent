package com.alibaba.assistant.agent.extension.reply.spi;

import com.alibaba.assistant.agent.extension.reply.config.ReplyToolConfig;

import java.util.List;

/**
 * 回复工具配置提供者接口
 * 实现此接口并注册为@Bean，可提供动态配置
 *
 * @author Assistant Agent Team
 */
public interface ReplyToolConfigProvider {

	/**
	 * 提供回复工具配置列表
	 *
	 * @return 配置列表
	 */
	List<ReplyToolConfig> provide();

}

