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
package com.alibaba.assistant.agent.start.search;

import com.alibaba.assistant.agent.extension.search.model.SearchRequest;
import com.alibaba.assistant.agent.extension.search.model.SearchResultItem;
import com.alibaba.assistant.agent.extension.search.model.SearchSourceType;
import com.alibaba.assistant.agent.extension.search.spi.SearchProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Mock 项目上下文搜索Provider（基于Store/代码索引）
 * 提供简单的模拟实现用于演示和测试
 *
 * @author Assistant Agent Team
 */
@Component
public class MockProjectContextSearchProvider implements SearchProvider {

    private static final Logger logger = LoggerFactory.getLogger(MockProjectContextSearchProvider.class);

    @Override
    public boolean supports(SearchSourceType type) {
        return SearchSourceType.PROJECT == type;
    }

    @Override
    public List<SearchResultItem> search(SearchRequest request) {
        logger.info("MockProjectContextSearchProvider#search - reason=execute project search with query={}", request.getQuery());

        List<SearchResultItem> results = new ArrayList<>();

        try {
            // TODO: 实际实现应该从Store/代码索引中搜索
            // 这里提供简单的模拟数据
            int topK = request.getPerSourceTopK().getOrDefault(SearchSourceType.PROJECT, request.getTopK());

            // 模拟返回结果
            for (int i = 0; i < Math.min(topK, 3); i++) {
                SearchResultItem item = new SearchResultItem();
                item.setId(UUID.randomUUID().toString());
                item.setSourceType(SearchSourceType.PROJECT);
                item.setTitle("项目代码示例 " + (i + 1));
                item.setSnippet("这是针对查询'" + request.getQuery() + "'的项目代码片段");
                item.setContent("// 示例代码内容\npublic class Example {\n  // ...\n}");
                item.setUri("src/main/java/Example" + i + ".java");
                item.setScore(0.9 - i * 0.1);
                item.getMetadata().setSourceName(getName());
                item.getMetadata().setLanguage(request.getContext().getLanguage());
                results.add(item);
            }

            logger.info("MockProjectContextSearchProvider#search - reason=found {} results", results.size());
        } catch (Exception e) {
            logger.error("MockProjectContextSearchProvider#search - reason=search failed with error", e);
        }

        return results;
    }

    @Override
    public String getName() {
        return "MockProjectContextSearchProvider";
    }
}

