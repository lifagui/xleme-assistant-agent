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

package com.alibaba.assistant.agent.extension.trigger.model;

import java.time.Instant;
import java.util.HashMap;
import java.util.Map;

/**
 * 触发器执行记录
 * 代表一次触发器执行过程的完整记录
 *
 * @author Assistant Agent Team
 * @since 1.0.0
 */
public class TriggerExecutionRecord {

	/**
	 * 执行记录唯一标识
	 */
	private String executionId;

	/**
	 * 所属触发器ID
	 */
	private String triggerId;

	/**
	 * 预期执行时间
	 */
	private Instant scheduledTime;

	/**
	 * 实际开始时间
	 */
	private Instant startTime;

	/**
	 * 结束时间
	 */
	private Instant endTime;

	/**
	 * 执行状态
	 */
	private ExecutionStatus status;

	/**
	 * 错误信息
	 */
	private String errorMessage;

	/**
	 * 错误堆栈（截断后）
	 */
	private String errorStack;

	/**
	 * 输出摘要
	 */
	private Map<String, Object> outputSummary;

	/**
	 * 后端任务ID（调度引擎中的任务ID）
	 */
	private String backendTaskId;

	/**
	 * 执行线程ID
	 */
	private String threadId;

	/**
	 * 沙箱ID
	 */
	private String sandboxId;

	/**
	 * 重试次数
	 */
	private Integer retryCount;

	public TriggerExecutionRecord() {
		this.outputSummary = new HashMap<>();
		this.status = ExecutionStatus.PENDING;
		this.retryCount = 0;
	}

	public TriggerExecutionRecord(String executionId, String triggerId) {
		this();
		this.executionId = executionId;
		this.triggerId = triggerId;
	}

	// Getters and Setters

	public String getExecutionId() {
		return executionId;
	}

	public void setExecutionId(String executionId) {
		this.executionId = executionId;
	}

	public String getTriggerId() {
		return triggerId;
	}

	public void setTriggerId(String triggerId) {
		this.triggerId = triggerId;
	}

	public Instant getScheduledTime() {
		return scheduledTime;
	}

	public void setScheduledTime(Instant scheduledTime) {
		this.scheduledTime = scheduledTime;
	}

	public Instant getStartTime() {
		return startTime;
	}

	public void setStartTime(Instant startTime) {
		this.startTime = startTime;
	}

	public Instant getEndTime() {
		return endTime;
	}

	public void setEndTime(Instant endTime) {
		this.endTime = endTime;
	}

	public ExecutionStatus getStatus() {
		return status;
	}

	public void setStatus(ExecutionStatus status) {
		this.status = status;
	}

	public String getErrorMessage() {
		return errorMessage;
	}

	public void setErrorMessage(String errorMessage) {
		this.errorMessage = errorMessage;
	}

	public String getErrorStack() {
		return errorStack;
	}

	public void setErrorStack(String errorStack) {
		this.errorStack = errorStack;
	}

	public Map<String, Object> getOutputSummary() {
		return outputSummary;
	}

	public void setOutputSummary(Map<String, Object> outputSummary) {
		this.outputSummary = outputSummary;
	}

	public String getBackendTaskId() {
		return backendTaskId;
	}

	public void setBackendTaskId(String backendTaskId) {
		this.backendTaskId = backendTaskId;
	}

	public String getThreadId() {
		return threadId;
	}

	public void setThreadId(String threadId) {
		this.threadId = threadId;
	}

	public String getSandboxId() {
		return sandboxId;
	}

	public void setSandboxId(String sandboxId) {
		this.sandboxId = sandboxId;
	}

	public Integer getRetryCount() {
		return retryCount;
	}

	public void setRetryCount(Integer retryCount) {
		this.retryCount = retryCount;
	}

	public void incrementRetryCount() {
		if (this.retryCount == null) {
			this.retryCount = 1;
		}
		else {
			this.retryCount++;
		}
	}

	/**
	 * 计算执行耗时（毫秒）
	 * @return 耗时，如果未完成则返回null
	 */
	public Long getDurationMs() {
		if (startTime != null && endTime != null) {
			return endTime.toEpochMilli() - startTime.toEpochMilli();
		}
		return null;
	}

	@Override
	public String toString() {
		return "TriggerExecutionRecord{" + "executionId='" + executionId + '\'' + ", triggerId='" + triggerId + '\''
				+ ", status=" + status + ", startTime=" + startTime + ", endTime=" + endTime + ", retryCount="
				+ retryCount + '}';
	}

}

