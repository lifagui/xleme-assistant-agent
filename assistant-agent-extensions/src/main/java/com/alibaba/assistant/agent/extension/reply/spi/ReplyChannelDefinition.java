package com.alibaba.assistant.agent.extension.reply.spi;

import com.alibaba.assistant.agent.extension.reply.model.ChannelExecutionContext;
import com.alibaba.assistant.agent.extension.reply.model.ParameterSchema;
import com.alibaba.assistant.agent.extension.reply.model.ReplyResult;

import java.util.Map;

/**
 * 回复渠道定义接口
 * 定义了回复渠道的"模板"，Extension层实现此接口提供渠道能力
 *
 * @author Assistant Agent Team
 */
public interface ReplyChannelDefinition {

	/**
	 * 获取渠道唯一标识
	 * 示例：IDE_CARD, IDE_TEXT, IM_NOTIFICATION, WEBHOOK_JSON
	 *
	 * @return 渠道code
	 */
	String getChannelCode();

	/**
	 * 获取渠道功能描述（用于生成Tool的description）
	 *
	 * @return 渠道描述
	 */
	String getDescription();

	/**
	 * 获取支持的参数定义（可选）
	 * - 如果返回非空，则只允许配置中使用这些参数（严格模式）
	 * - 如果返回null，则允许任意参数（灵活模式）
	 *
	 * @return 参数schema，null表示接受任意参数
	 */
	ParameterSchema getSupportedParameters();

	/**
	 * 执行回复逻辑
	 *
	 * @param context 执行上下文，包含会话、用户、项目等信息
	 * @param params 参数映射后的实际参数
	 * @return 执行结果
	 */
	ReplyResult execute(ChannelExecutionContext context, Map<String, Object> params);

	/**
	 * 是否支持异步执行（默认false）
	 * 异步渠道如IM通知、Webhook推送等可以返回true
	 *
	 * @return true表示支持异步执行
	 */
	default boolean supportsAsync() {
		return false;
	}

}

