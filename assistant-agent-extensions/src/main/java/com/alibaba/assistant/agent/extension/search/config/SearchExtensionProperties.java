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
package com.alibaba.assistant.agent.extension.search.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * 搜索扩展配置属性
 *
 * @author Assistant Agent Team
 */
@ConfigurationProperties(prefix = "spring.ai.alibaba.codeact.extension.search")
public class SearchExtensionProperties {
	/**
	 * 是否启用搜索扩展
	 */
	private boolean enabled = true;

	/**
	 * 是否启用项目上下文搜索
	 */
	private boolean projectSearchEnabled = true;

	/**
	 * 是否启用知识库搜索
	 */
	private boolean knowledgeSearchEnabled = true;

	/**
	 * 是否启用Web搜索
	 */
	private boolean webSearchEnabled = false;

	/**
	 * 默认返回结果数量
	 */
	private int defaultTopK = 10;

	/**
	 * 搜索超时时间（毫秒）
	 */
	private long searchTimeoutMs = 5000;

	/**
	 * 百度搜索API密钥（可选）
	 */
	private String baiduApiKey;

	public boolean isEnabled() {
		return enabled;
	}

	public void setEnabled(boolean enabled) {
		this.enabled = enabled;
	}

	public boolean isProjectSearchEnabled() {
		return projectSearchEnabled;
	}

	public void setProjectSearchEnabled(boolean projectSearchEnabled) {
		this.projectSearchEnabled = projectSearchEnabled;
	}

	public boolean isKnowledgeSearchEnabled() {
		return knowledgeSearchEnabled;
	}

	public void setKnowledgeSearchEnabled(boolean knowledgeSearchEnabled) {
		this.knowledgeSearchEnabled = knowledgeSearchEnabled;
	}

	public boolean isWebSearchEnabled() {
		return webSearchEnabled;
	}

	public void setWebSearchEnabled(boolean webSearchEnabled) {
		this.webSearchEnabled = webSearchEnabled;
	}

	public int getDefaultTopK() {
		return defaultTopK;
	}

	public void setDefaultTopK(int defaultTopK) {
		this.defaultTopK = defaultTopK;
	}

	public long getSearchTimeoutMs() {
		return searchTimeoutMs;
	}

	public void setSearchTimeoutMs(long searchTimeoutMs) {
		this.searchTimeoutMs = searchTimeoutMs;
	}

	public String getBaiduApiKey() {
		return baiduApiKey;
	}

	public void setBaiduApiKey(String baiduApiKey) {
		this.baiduApiKey = baiduApiKey;
	}
}

