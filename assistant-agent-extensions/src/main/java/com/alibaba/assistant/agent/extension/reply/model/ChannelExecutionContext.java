package com.alibaba.assistant.agent.extension.reply.model;

import java.util.HashMap;
import java.util.Map;

/**
 * 渠道执行上下文
 * 携带会话、用户、项目等上下文信息
 *
 * @author Assistant Agent Team
 */
public class ChannelExecutionContext {

	// 会话信息
	private String sessionId;

	private String userId;

	private String projectId;

	// 执行来源
	private ExecutionSource source;

	// 工具调用元信息
	private String toolName;

	private String traceId;

	// 扩展字段
	private Map<String, Object> extensions;

	public ChannelExecutionContext() {
		this.extensions = new HashMap<>();
	}

	// Getters and Setters
	public String getSessionId() {
		return sessionId;
	}

	public void setSessionId(String sessionId) {
		this.sessionId = sessionId;
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

	public ExecutionSource getSource() {
		return source;
	}

	public void setSource(ExecutionSource source) {
		this.source = source;
	}

	public String getToolName() {
		return toolName;
	}

	public void setToolName(String toolName) {
		this.toolName = toolName;
	}

	public String getTraceId() {
		return traceId;
	}

	public void setTraceId(String traceId) {
		this.traceId = traceId;
	}

	public Map<String, Object> getExtensions() {
		return extensions;
	}

	public void setExtensions(Map<String, Object> extensions) {
		this.extensions = extensions;
	}

	public void putExtension(String key, Object value) {
		this.extensions.put(key, value);
	}

	public Object getExtension(String key) {
		return this.extensions.get(key);
	}

	/**
	 * 执行来源枚举
	 */
	public enum ExecutionSource {

		REACT, // React推理过程
		CODEACT, // CodeAct代码执行过程
		HOOK, // Hook调用
		MANUAL // 手动调用

	}

	public static Builder builder() {
		return new Builder();
	}

	public static class Builder {

		private final ChannelExecutionContext context = new ChannelExecutionContext();

		public Builder sessionId(String sessionId) {
			context.setSessionId(sessionId);
			return this;
		}

		public Builder userId(String userId) {
			context.setUserId(userId);
			return this;
		}

		public Builder projectId(String projectId) {
			context.setProjectId(projectId);
			return this;
		}

		public Builder source(ExecutionSource source) {
			context.setSource(source);
			return this;
		}

		public Builder toolName(String toolName) {
			context.setToolName(toolName);
			return this;
		}

		public Builder traceId(String traceId) {
			context.setTraceId(traceId);
			return this;
		}

		public Builder extension(String key, Object value) {
			context.putExtension(key, value);
			return this;
		}

		public ChannelExecutionContext build() {
			return context;
		}

	}

}

