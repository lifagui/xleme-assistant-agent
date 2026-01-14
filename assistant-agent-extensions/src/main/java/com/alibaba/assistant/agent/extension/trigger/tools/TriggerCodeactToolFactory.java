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
package com.alibaba.assistant.agent.extension.trigger.tools;

import com.alibaba.assistant.agent.common.tools.TriggerCodeactTool;
import com.alibaba.assistant.agent.extension.trigger.manager.TriggerManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;

/**
 * Trigger CodeactTool 工厂类。
 *
 * <p>负责创建所有 Trigger 相关的 CodeactTool 实例。
 *
 * @author Assistant Agent Team
 * @since 1.0.0
 */
public class TriggerCodeactToolFactory {

	private static final Logger log = LoggerFactory.getLogger(TriggerCodeactToolFactory.class);

	private final TriggerManager triggerManager;

	/**
	 * 构造工厂实例。
	 *
	 * @param triggerManager 触发器管理器
	 */
	public TriggerCodeactToolFactory(TriggerManager triggerManager) {
		this.triggerManager = triggerManager;
	}

	/**
	 * 创建所有 Trigger CodeactTool。
	 *
	 * @return TriggerCodeactTool 列表
	 */
	public List<TriggerCodeactTool> createTools() {
		List<TriggerCodeactTool> tools = new ArrayList<>();

		// 订阅触发器
		tools.add(new SubscribeTriggerCodeactTool(triggerManager));
		log.info("TriggerCodeactToolFactory#createTools - reason=创建SubscribeTriggerCodeactTool成功");

		// 取消订阅触发器
		tools.add(new UnsubscribeTriggerCodeactTool(triggerManager));
		log.info("TriggerCodeactToolFactory#createTools - reason=创建UnsubscribeTriggerCodeactTool成功");

		// 列出触发器
		tools.add(new ListTriggersCodeactTool(triggerManager));
		log.info("TriggerCodeactToolFactory#createTools - reason=创建ListTriggersCodeactTool成功");

		// 获取触发器详情
		tools.add(new GetTriggerDetailCodeactTool(triggerManager));
		log.info("TriggerCodeactToolFactory#createTools - reason=创建GetTriggerDetailCodeactTool成功");

		log.info("TriggerCodeactToolFactory#createTools - reason=触发器工具创建完成, count={}", tools.size());

		return tools;
	}

}

