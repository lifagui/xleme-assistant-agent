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

package com.alibaba.assistant.agent.extension.learning.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.ArrayList;
import java.util.List;

/**
 * 学习模块配置属性
 *
 * @author Assistant Agent Team
 * @since 1.0.0
 */
@ConfigurationProperties(prefix = "spring.ai.alibaba.codeact.extension.learning")
public class LearningExtensionProperties {

	/**
	 * 是否启用学习模块
	 */
	private boolean enabled = true;

	/**
	 * 在线学习配置
	 */
	private OnlineLearningConfig online = new OnlineLearningConfig();

	/**
	 * 离线学习配置
	 */
	private OfflineLearningConfig offline = new OfflineLearningConfig();

	/**
	 * 存储配置
	 */
	private StorageConfig storage = new StorageConfig();

	/**
	 * 异步学习配置
	 */
	private AsyncConfig async = new AsyncConfig();

	public boolean isEnabled() {
		return enabled;
	}

	public void setEnabled(boolean enabled) {
		this.enabled = enabled;
	}

	public OnlineLearningConfig getOnline() {
		return online;
	}

	public void setOnline(OnlineLearningConfig online) {
		this.online = online;
	}

	public OfflineLearningConfig getOffline() {
		return offline;
	}

	public void setOffline(OfflineLearningConfig offline) {
		this.offline = offline;
	}

	public StorageConfig getStorage() {
		return storage;
	}

	public void setStorage(StorageConfig storage) {
		this.storage = storage;
	}

	public AsyncConfig getAsync() {
		return async;
	}

	public void setAsync(AsyncConfig async) {
		this.async = async;
	}

	/**
	 * 在线学习配置
	 */
	public static class OnlineLearningConfig {

		/**
		 * 是否启用在线学习
		 */
		private boolean enabled = true;

		/**
		 * Agent执行后学习配置
		 */
		private AfterAgentConfig afterAgent = new AfterAgentConfig();

		/**
		 * 模型调用后学习配置
		 */
		private AfterModelConfig afterModel = new AfterModelConfig();

		/**
		 * 工具拦截器学习配置
		 */
		private ToolInterceptorConfig toolInterceptor = new ToolInterceptorConfig();

		public boolean isEnabled() {
			return enabled;
		}

		public void setEnabled(boolean enabled) {
			this.enabled = enabled;
		}

		public AfterAgentConfig getAfterAgent() {
			return afterAgent;
		}

		public void setAfterAgent(AfterAgentConfig afterAgent) {
			this.afterAgent = afterAgent;
		}

		public AfterModelConfig getAfterModel() {
			return afterModel;
		}

		public void setAfterModel(AfterModelConfig afterModel) {
			this.afterModel = afterModel;
		}

		public ToolInterceptorConfig getToolInterceptor() {
			return toolInterceptor;
		}

		public void setToolInterceptor(ToolInterceptorConfig toolInterceptor) {
			this.toolInterceptor = toolInterceptor;
		}

	}

	/**
	 * Agent执行后学习配置
	 */
	public static class AfterAgentConfig {

		private boolean enabled = true;

		private List<String> learningTypes = new ArrayList<>(List.of("experience"));

		public boolean isEnabled() {
			return enabled;
		}

		public void setEnabled(boolean enabled) {
			this.enabled = enabled;
		}

		public List<String> getLearningTypes() {
			return learningTypes;
		}

		public void setLearningTypes(List<String> learningTypes) {
			this.learningTypes = learningTypes;
		}

	}

	/**
	 * 模型调用后学习配置
	 */
	public static class AfterModelConfig {

		private boolean enabled = false;

		public boolean isEnabled() {
			return enabled;
		}

		public void setEnabled(boolean enabled) {
			this.enabled = enabled;
		}

	}

	/**
	 * 工具拦截器学习配置
	 */
	public static class ToolInterceptorConfig {

		private boolean enabled = false;

		private List<String> includedTools = new ArrayList<>();

		public boolean isEnabled() {
			return enabled;
		}

		public void setEnabled(boolean enabled) {
			this.enabled = enabled;
		}

		public List<String> getIncludedTools() {
			return includedTools;
		}

		public void setIncludedTools(List<String> includedTools) {
			this.includedTools = includedTools;
		}

	}

	/**
	 * 离线学习配置
	 */
	public static class OfflineLearningConfig {

		/**
		 * 是否启用离线学习
		 */
		private boolean enabled = false;

		/**
		 * 调度任务配置列表
		 */
		private List<ScheduledTaskConfig> tasks = new ArrayList<>();

		public boolean isEnabled() {
			return enabled;
		}

		public void setEnabled(boolean enabled) {
			this.enabled = enabled;
		}

		public List<ScheduledTaskConfig> getTasks() {
			return tasks;
		}

		public void setTasks(List<ScheduledTaskConfig> tasks) {
			this.tasks = tasks;
		}

	}

	/**
	 * 调度任务配置
	 */
	public static class ScheduledTaskConfig {

		private String taskName;

		private String graphName;

		private String scheduleMode = "cron";

		private String cronExpression;

		private Long intervalMs;

		private Long lookbackPeriod;

		public String getTaskName() {
			return taskName;
		}

		public void setTaskName(String taskName) {
			this.taskName = taskName;
		}

		public String getGraphName() {
			return graphName;
		}

		public void setGraphName(String graphName) {
			this.graphName = graphName;
		}

		public String getScheduleMode() {
			return scheduleMode;
		}

		public void setScheduleMode(String scheduleMode) {
			this.scheduleMode = scheduleMode;
		}

		public String getCronExpression() {
			return cronExpression;
		}

		public void setCronExpression(String cronExpression) {
			this.cronExpression = cronExpression;
		}

		public Long getIntervalMs() {
			return intervalMs;
		}

		public void setIntervalMs(Long intervalMs) {
			this.intervalMs = intervalMs;
		}

		public Long getLookbackPeriod() {
			return lookbackPeriod;
		}

		public void setLookbackPeriod(Long lookbackPeriod) {
			this.lookbackPeriod = lookbackPeriod;
		}

	}

	/**
	 * 存储配置
	 */
	public static class StorageConfig {

		/**
		 * 存储类型：in-memory, store, custom
		 */
		private String type = "in-memory";

		/**
		 * 默认命名空间
		 */
		private String defaultNamespace = "default";

		public String getType() {
			return type;
		}

		public void setType(String type) {
			this.type = type;
		}

		public String getDefaultNamespace() {
			return defaultNamespace;
		}

		public void setDefaultNamespace(String defaultNamespace) {
			this.defaultNamespace = defaultNamespace;
		}

	}

	/**
	 * 异步学习配置
	 */
	public static class AsyncConfig {

		/**
		 * 是否启用异步执行
		 */
		private boolean enabled = true;

		/**
		 * 线程池大小
		 */
		private int threadPoolSize = 2;

		/**
		 * 队列容量
		 */
		private int queueCapacity = 100;

		/**
		 * 拒绝策略：caller-runs, abort, discard, discard-oldest
		 */
		private String rejectionPolicy = "caller-runs";

		public boolean isEnabled() {
			return enabled;
		}

		public void setEnabled(boolean enabled) {
			this.enabled = enabled;
		}

		public int getThreadPoolSize() {
			return threadPoolSize;
		}

		public void setThreadPoolSize(int threadPoolSize) {
			this.threadPoolSize = threadPoolSize;
		}

		public int getQueueCapacity() {
			return queueCapacity;
		}

		public void setQueueCapacity(int queueCapacity) {
			this.queueCapacity = queueCapacity;
		}

		public String getRejectionPolicy() {
			return rejectionPolicy;
		}

		public void setRejectionPolicy(String rejectionPolicy) {
			this.rejectionPolicy = rejectionPolicy;
		}

	}

}

