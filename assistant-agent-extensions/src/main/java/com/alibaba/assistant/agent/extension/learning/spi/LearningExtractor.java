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

import com.alibaba.assistant.agent.extension.learning.model.LearningContext;

import java.util.List;

/**
 * 学习提取器接口
 * 负责从学习上下文中提取学习记录
 *
 * @param <T> 学习记录类型
 * @author Assistant Agent Team
 * @since 1.0.0
 */
public interface LearningExtractor<T> {

	/**
	 * 判断是否应该进行学习
	 * @param context 学习上下文
	 * @return true表示应该学习，false表示跳过
	 */
	boolean shouldLearn(LearningContext context);

	/**
	 * 从上下文中提取学习记录
	 * @param context 学习上下文
	 * @return 提取的学习记录列表
	 */
	List<T> extract(LearningContext context);

	/**
	 * 获取支持的学习类型
	 * @return 学习类型（如：experience、pattern、error等）
	 */
	String getSupportedLearningType();

	/**
	 * 获取提取器产出的记录类型
	 * <p>用于学习策略匹配合适的存储仓库
	 *
	 * @return 记录类型的Class对象
	 */
	Class<T> getRecordType();

}

