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

import com.alibaba.assistant.agent.extension.trigger.model.ExecutionStatus;
import com.alibaba.assistant.agent.extension.trigger.model.TriggerExecutionRecord;

import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * 触发器执行记录存储接口
 * 提供执行记录的增删改查能力
 *
 * @author Assistant Agent Team
 * @since 1.0.0
 */
public interface TriggerExecutionLogRepository {

	/**
	 * 保存执行记录
	 * @param record 执行记录
	 */
	void save(TriggerExecutionRecord record);

	/**
	 * 根据执行ID查询记录
	 * @param executionId 执行ID
	 * @return 执行记录
	 */
	Optional<TriggerExecutionRecord> findById(String executionId);

	/**
	 * 更新执行状态
	 * @param executionId 执行ID
	 * @param status 执行状态
	 * @param errorMessage 错误信息（可选）
	 * @param outputSummary 输出摘要（可选）
	 */
	void updateStatus(String executionId, ExecutionStatus status, String errorMessage,
			Map<String, Object> outputSummary);

	/**
	 * 查询指定触发器的执行记录
	 * @param triggerId 触发器ID
	 * @param limit 最大返回数量
	 * @return 执行记录列表（按时间倒序）
	 */
	List<TriggerExecutionRecord> listByTrigger(String triggerId, int limit);

	/**
	 * 查询指定触发器的所有执行记录
	 * @param triggerId 触发器ID
	 * @return 执行记录列表
	 */
	List<TriggerExecutionRecord> findByTriggerId(String triggerId);

	/**
	 * 删除执行记录
	 * @param executionId 执行ID
	 */
	void delete(String executionId);

}

