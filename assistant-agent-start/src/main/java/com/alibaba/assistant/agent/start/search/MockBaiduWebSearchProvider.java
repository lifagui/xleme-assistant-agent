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

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Mock 百度Web搜索Provider
 * 通过百度搜索API获取搜索结果，用于演示和测试
 *
 * @author Assistant Agent Team
 */
@Component
public class MockBaiduWebSearchProvider implements SearchProvider {

    private static final Logger logger = LoggerFactory.getLogger(MockBaiduWebSearchProvider.class);

    private static final String BAIDU_SEARCH_URL = "https://www.baidu.com/s";
    private static final int DEFAULT_TIMEOUT = 5000;

    private final String apiKey;
    private final boolean useSimpleMode;

    /**
     * 构造函数
     *
     * @param apiKey 百度搜索API密钥（可选，如果为空则使用简单模式）
     */
    public MockBaiduWebSearchProvider(String apiKey) {
        this.apiKey = apiKey;
        this.useSimpleMode = (apiKey == null || apiKey.trim().isEmpty());

        if (useSimpleMode) {
            logger.info("MockBaiduWebSearchProvider#init - reason=使用简单模式初始化");
        } else {
            logger.info("MockBaiduWebSearchProvider#init - reason=使用API模式初始化");
        }
    }

    /**
     * 默认构造函数，使用简单模式
     */
    public MockBaiduWebSearchProvider() {
        this(null);
    }

    @Override
    public boolean supports(SearchSourceType type) {
        return SearchSourceType.WEB == type;
    }

    @Override
    public List<SearchResultItem> search(SearchRequest request) {
        logger.info("MockBaiduWebSearchProvider#search - reason=执行百度搜索, query={}", request.getQuery());

        List<SearchResultItem> results = new ArrayList<>();

        try {
            int topK = request.getPerSourceTopK().getOrDefault(SearchSourceType.WEB, request.getTopK());

            if (useSimpleMode) {
                results = searchSimpleMode(request.getQuery(), topK);
            } else {
                results = searchApiMode(request.getQuery(), topK);
            }

            logger.info("MockBaiduWebSearchProvider#search - reason=找到{}条搜索结果", results.size());
        } catch (Exception e) {
            logger.error("MockBaiduWebSearchProvider#search - reason=搜索失败, error={}", e.getMessage(), e);
        }

        return results;
    }

    /**
     * 简单模式搜索：直接请求百度搜索页面并解析HTML
     */
    private List<SearchResultItem> searchSimpleMode(String query, int topK) {
        logger.info("MockBaiduWebSearchProvider#searchSimpleMode - reason=执行简单模式搜索, query={}, topK={}", query, topK);

        List<SearchResultItem> results = new ArrayList<>();

        try {
            // 构建搜索URL
            String encodedQuery = URLEncoder.encode(query, StandardCharsets.UTF_8);
            String urlString = BAIDU_SEARCH_URL + "?wd=" + encodedQuery + "&rn=" + topK;

            logger.debug("MockBaiduWebSearchProvider#searchSimpleMode - reason=请求URL, url={}", urlString);

            // 发送HTTP请求
            URL url = new URL(urlString);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("GET");
            connection.setConnectTimeout(DEFAULT_TIMEOUT);
            connection.setReadTimeout(DEFAULT_TIMEOUT);
            connection.setRequestProperty("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36");

            int responseCode = connection.getResponseCode();
            logger.debug("MockBaiduWebSearchProvider#searchSimpleMode - reason=收到响应, responseCode={}", responseCode);

            if (responseCode == HttpURLConnection.HTTP_OK) {
                // 读取响应
                BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream(), StandardCharsets.UTF_8));
                StringBuilder response = new StringBuilder();
                String line;
                while ((line = reader.readLine()) != null) {
                    response.append(line);
                }
                reader.close();

                // 解析HTML获取搜索结果
                results = parseSearchResults(response.toString(), topK);
            } else {
                logger.warn("MockBaiduWebSearchProvider#searchSimpleMode - reason=响应码异常, responseCode={}", responseCode);
            }

            connection.disconnect();
        } catch (Exception e) {
            logger.error("MockBaiduWebSearchProvider#searchSimpleMode - reason=搜索异常, error={}", e.getMessage(), e);
        }

        return results;
    }

    /**
     * API模式搜索：使用百度搜索API
     * TODO: 需要根据实际的百度搜索API文档实现
     */
    private List<SearchResultItem> searchApiMode(String query, int topK) {
        logger.info("MockBaiduWebSearchProvider#searchApiMode - reason=执行API模式搜索, query={}, topK={}", query, topK);

        // TODO: 实现百度搜索API调用
        // 这里暂时返回模拟数据
        List<SearchResultItem> results = new ArrayList<>();

        for (int i = 0; i < Math.min(topK, 3); i++) {
            SearchResultItem item = new SearchResultItem();
            item.setId(UUID.randomUUID().toString());
            item.setSourceType(SearchSourceType.WEB);
            item.setTitle("百度搜索结果 " + (i + 1) + ": " + query);
            item.setSnippet("这是针对查询'" + query + "'的百度搜索结果摘要信息");
            item.setContent("详细内容：这是从百度搜索获取的完整内容，包含了与查询相关的详细信息。");
            item.setUri("https://www.example.com/result" + i);
            item.setScore(0.95 - i * 0.1);
            item.getMetadata().setSourceName(getName());
            item.getMetadata().setLanguage("zh");
            results.add(item);
        }

        logger.info("MockBaiduWebSearchProvider#searchApiMode - reason=API模式搜索完成, resultCount={}", results.size());

        return results;
    }

    /**
     * 解析百度搜索结果HTML
     */
    private List<SearchResultItem> parseSearchResults(String html, int maxResults) {
        logger.debug("MockBaiduWebSearchProvider#parseSearchResults - reason=开始解析搜索结果, maxResults={}", maxResults);

        List<SearchResultItem> results = new ArrayList<>();

        try {
            // 简单的正则表达式提取搜索结果
            // 注意：百度的HTML结构可能会变化，这里提供基础实现

            // 提取标题和链接的正则模式
            Pattern titlePattern = Pattern.compile("<h3[^>]*><a[^>]*href=\"([^\"]*)\"[^>]*>([^<]*)</a></h3>");
            // 提取摘要的正则模式
            Pattern snippetPattern = Pattern.compile("<div class=\"c-abstract\"[^>]*>([^<]*)</div>");

            Matcher titleMatcher = titlePattern.matcher(html);
            List<String[]> titleUrlPairs = new ArrayList<>();

            while (titleMatcher.find() && titleUrlPairs.size() < maxResults) {
                String url = titleMatcher.group(1);
                String title = titleMatcher.group(2);
                titleUrlPairs.add(new String[]{title, url});
            }

            // 如果没有提取到结果，使用模拟数据
            if (titleUrlPairs.isEmpty()) {
                logger.info("MockBaiduWebSearchProvider#parseSearchResults - reason=未能解析到实际结果，使用模拟数据");
                return createMockResults(maxResults);
            }

            // 提取摘要
            Matcher snippetMatcher = snippetPattern.matcher(html);
            List<String> snippets = new ArrayList<>();

            while (snippetMatcher.find() && snippets.size() < maxResults) {
                snippets.add(snippetMatcher.group(1));
            }

            // 组装结果
            for (int i = 0; i < titleUrlPairs.size(); i++) {
                String[] pair = titleUrlPairs.get(i);
                String title = pair[0];
                String url = pair[1];
                String snippet = i < snippets.size() ? snippets.get(i) : "暂无摘要";

                SearchResultItem item = new SearchResultItem();
                item.setId(UUID.randomUUID().toString());
                item.setSourceType(SearchSourceType.WEB);
                item.setTitle(cleanHtmlText(title));
                item.setSnippet(cleanHtmlText(snippet));
                item.setUri(url);
                item.setScore(1.0 - i * 0.05);
                item.getMetadata().setSourceName(getName());
                item.getMetadata().setLanguage("zh");

                results.add(item);
            }

            logger.info("MockBaiduWebSearchProvider#parseSearchResults - reason=解析完成, resultCount={}", results.size());
        } catch (Exception e) {
            logger.error("MockBaiduWebSearchProvider#parseSearchResults - reason=解析失败, error={}", e.getMessage(), e);
            // 解析失败时返回模拟数据
            return createMockResults(maxResults);
        }

        return results;
    }

    /**
     * 创建模拟搜索结果
     */
    private List<SearchResultItem> createMockResults(int count) {
        logger.info("MockBaiduWebSearchProvider#createMockResults - reason=创建模拟搜索结果, count={}", count);

        List<SearchResultItem> results = new ArrayList<>();

        for (int i = 0; i < Math.min(count, 5); i++) {
            SearchResultItem item = new SearchResultItem();
            item.setId(UUID.randomUUID().toString());
            item.setSourceType(SearchSourceType.WEB);
            item.setTitle("百度搜索结果 " + (i + 1));
            item.setSnippet("这是模拟的百度搜索结果摘要信息，实际使用时会返回真实的搜索结果。");
            item.setContent("完整内容：这是从百度搜索获取的模拟数据，实际环境中会包含真实的网页内容。");
            item.setUri("https://www.baidu.com/link?url=example" + i);
            item.setScore(0.9 - i * 0.1);
            item.getMetadata().setSourceName(getName());
            item.getMetadata().setLanguage("zh");
            results.add(item);
        }

        return results;
    }

    /**
     * 清理HTML文本，移除标签和特殊字符
     */
    private String cleanHtmlText(String html) {
        if (html == null) {
            return "";
        }

        // 移除HTML标签
        String text = html.replaceAll("<[^>]*>", "");
        // 解码HTML实体
        text = text.replace("&nbsp;", " ")
                .replace("&lt;", "<")
                .replace("&gt;", ">")
                .replace("&amp;", "&")
                .replace("&quot;", "\"");

        return text.trim();
    }

    @Override
    public String getName() {
        return "MockBaiduWebSearchProvider";
    }
}

