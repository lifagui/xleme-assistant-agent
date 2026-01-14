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
package com.alibaba.assistant.agent.extension.search.spi;

import com.alibaba.assistant.agent.extension.search.model.SearchRequest;
import com.alibaba.assistant.agent.extension.search.model.SearchResultSet;

/**
 * 搜索门面接口，统一搜索入口
 *
 * @author Assistant Agent Team
 */
public interface SearchFacade {
	/**
	 * 统一搜索入口，自动组合多个数据源
	 *
	 * @param request 搜索请求
	 * @return 搜索结果集
	 */
	SearchResultSet unifiedSearch(SearchRequest request);
}

