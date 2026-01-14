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
package com.alibaba.assistant.agent.common.tools;

/**
 * Reply 领域的 CodeAct 工具接口。
 *
 * <p>用于定义回复相关的工具，支持向用户发送消息等功能。
 *
 * @author Assistant Agent Team
 * @since 1.0.0
 */
public interface ReplyCodeactTool extends CodeactTool {

	/**
	 * 获取回复渠道类型。
	 *
	 * @return 渠道类型，例如 PRIMARY（主要渠道）、SECONDARY（次要渠道）
	 */
	ReplyChannelType getChannelType();

	/**
	 * 回复渠道类型枚举
	 */
	enum ReplyChannelType {

		/**
		 * 主要渠道
		 */
		PRIMARY,

		/**
		 * 次要渠道
		 */
		SECONDARY

	}

}

