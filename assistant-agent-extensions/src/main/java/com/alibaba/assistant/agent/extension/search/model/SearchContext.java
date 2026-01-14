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
package com.alibaba.assistant.agent.extension.search.model;

import java.util.HashMap;
import java.util.Map;

/**
 * 搜索上下文信息（从OverAllState/Agent环境抽取）
 *
 * @author Assistant Agent Team
 */
public class SearchContext {
	/**
	 * 用户ID
	 */
	private String userId;

	/**
	 * 项目ID
	 */
	private String projectId;

	/**
	 * 仓库ID
	 */
	private String repoId;

	/**
	 * 当前文件路径
	 */
	private String currentFilePath;

	/**
	 * 编程语言
	 */
	private String language;

	/**
	 * 任务类型
	 */
	private String taskType;

	/**
	 * Agent类型
	 */
	private String agentType;

	/**
	 * 扩展上下文信息
	 */
	private Map<String, Object> extensions;

	public SearchContext() {
		this.extensions = new HashMap<>();
	}

	public String getUserId() {
		return userId;
	}

	public void setUserId(String userId) {
		this.userId = userId;
	}

	public String getProjectId() {
		return projectId;
	}

	public void setProjectId(String projectId) {
		this.projectId = projectId;
	}

	public String getRepoId() {
		return repoId;
	}

	public void setRepoId(String repoId) {
		this.repoId = repoId;
	}

	public String getCurrentFilePath() {
		return currentFilePath;
	}

	public void setCurrentFilePath(String currentFilePath) {
		this.currentFilePath = currentFilePath;
	}

	public String getLanguage() {
		return language;
	}

	public void setLanguage(String language) {
		this.language = language;
	}

	public String getTaskType() {
		return taskType;
	}

	public void setTaskType(String taskType) {
		this.taskType = taskType;
	}

	public String getAgentType() {
		return agentType;
	}

	public void setAgentType(String agentType) {
		this.agentType = agentType;
	}

	public Map<String, Object> getExtensions() {
		return extensions;
	}

	public void setExtensions(Map<String, Object> extensions) {
		this.extensions = extensions;
	}
}

