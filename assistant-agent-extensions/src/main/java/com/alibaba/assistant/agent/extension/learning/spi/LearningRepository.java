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

package com.alibaba.assistant.agent.extension.learning.spi;

import com.alibaba.assistant.agent.extension.learning.model.LearningSearchRequest;

import java.util.List;

/**
 * 学习仓库接口
 * 负责学习记录的持久化存储和检索
 *
 * @param <T> 学习记录类型
 * @author Assistant Agent Team
 * @since 1.0.0
 */
public interface LearningRepository<T> {

	/**
	 * 保存单条学习记录
	 * @param namespace 命名空间
	 * @param key 键（记录ID）
	 * @param record 学习记录
	 */
	void save(String namespace, String key, T record);

	/**
	 * 批量保存学习记录
	 * @param namespace 命名空间
	 * @param records 学习记录列表
	 */
	void saveBatch(String namespace, List<T> records);

	/**
	 * 获取学习记录
	 * @param namespace 命名空间
	 * @param key 键（记录ID）
	 * @return 学习记录，如果不存在返回null
	 */
	T get(String namespace, String key);

	/**
	 * 搜索学习记录
	 * @param request 搜索请求
	 * @return 搜索结果列表
	 */
	List<T> search(LearningSearchRequest request);

	/**
	 * 删除学习记录
	 * @param namespace 命名空间
	 * @param key 键（记录ID）
	 */
	void delete(String namespace, String key);

	/**
	 * 获取仓库支持的记录类型
	 * <p>用于学习策略根据提取器的输出类型选择合适的仓库
	 *
	 * @return 支持的记录类型的Class对象
	 */
	Class<T> getSupportedRecordType();

}

