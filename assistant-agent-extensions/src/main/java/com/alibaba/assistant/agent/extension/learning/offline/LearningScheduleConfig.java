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

package com.alibaba.assistant.agent.extension.learning.offline;

/**
 * 学习调度配置
 * 定义离线学习任务的调度参数
 *
 * @author Assistant Agent Team
 * @since 1.0.0
 */
public class LearningScheduleConfig {

	/**
	 * 任务名称
	 */
	private String taskName;

	/**
	 * 学习图名称
	 */
	private String graphName;

	/**
	 * 调度模式：cron | interval
	 */
	private String scheduleMode;

	/**
	 * Cron表达式（当scheduleMode=cron时使用）
	 */
	private String cronExpression;

	/**
	 * 间隔时间（毫秒，当scheduleMode=interval时使用）
	 */
	private Long intervalMs;

	/**
	 * 回看时间窗口（小时，用于获取历史数据）
	 */
	private Long lookbackPeriod;

	public LearningScheduleConfig() {
	}

	public LearningScheduleConfig(String taskName, String graphName) {
		this.taskName = taskName;
		this.graphName = graphName;
		this.scheduleMode = "cron";
	}

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

	public static Builder builder() {
		return new Builder();
	}

	public static class Builder {

		private final LearningScheduleConfig config;

		public Builder() {
			this.config = new LearningScheduleConfig();
		}

		public Builder taskName(String taskName) {
			config.taskName = taskName;
			return this;
		}

		public Builder graphName(String graphName) {
			config.graphName = graphName;
			return this;
		}

		public Builder scheduleMode(String scheduleMode) {
			config.scheduleMode = scheduleMode;
			return this;
		}

		public Builder cronExpression(String cronExpression) {
			config.cronExpression = cronExpression;
			return this;
		}

		public Builder intervalMs(Long intervalMs) {
			config.intervalMs = intervalMs;
			return this;
		}

		public Builder lookbackPeriod(Long lookbackPeriod) {
			config.lookbackPeriod = lookbackPeriod;
			return this;
		}

		public LearningScheduleConfig build() {
			return config;
		}

	}

}

