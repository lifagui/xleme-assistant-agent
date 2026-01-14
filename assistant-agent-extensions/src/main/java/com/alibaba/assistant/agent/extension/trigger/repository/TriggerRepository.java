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

package com.alibaba.assistant.agent.extension.trigger.repository;

import com.alibaba.assistant.agent.extension.trigger.model.SourceType;
import com.alibaba.assistant.agent.extension.trigger.model.TriggerDefinition;
import com.alibaba.assistant.agent.extension.trigger.model.TriggerStatus;

import java.util.List;
import java.util.Optional;

/**
 * 触发器定义存储接口
 * 提供触发器定义的增删改查能力
 *
 * @author Assistant Agent Team
 * @since 1.0.0
 */
public interface TriggerRepository {

	/**
	 * 保存触发器定义
	 * @param definition 触发器定义
	 */
	void save(TriggerDefinition definition);

	/**
	 * 根据ID查询触发器定义
	 * @param triggerId 触发器ID
	 * @return 触发器定义，不存在返回empty
	 */
	Optional<TriggerDefinition> findById(String triggerId);

	/**
	 * 根据来源查询触发器列表
	 * @param sourceType 来源类型
	 * @param sourceId 来源ID
	 * @return 触发器列表
	 */
	List<TriggerDefinition> findBySource(SourceType sourceType, String sourceId);

	/**
	 * 根据状态查询触发器列表
	 * @param status 触发器状态
	 * @return 触发器列表
	 */
	List<TriggerDefinition> findByStatus(TriggerStatus status);

	/**
	 * 更新触发器状态
	 * @param triggerId 触发器ID
	 * @param status 新状态
	 */
	void updateStatus(String triggerId, TriggerStatus status);

	/**
	 * 删除触发器定义
	 * @param triggerId 触发器ID
	 */
	void delete(String triggerId);

	/**
	 * 查询所有触发器
	 * @return 所有触发器列表
	 */
	List<TriggerDefinition> findAll();

}

