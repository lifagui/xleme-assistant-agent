package com.alibaba.assistant.agent.extension.reply.model;

import java.util.HashMap;
import java.util.Map;

/**
 * 回复执行结果
 *
 * @author Assistant Agent Team
 */
public class ReplyResult {

	private boolean success;

	private String message;

	private Map<String, Object> metadata;

	private Object data;

	public ReplyResult() {
		this.metadata = new HashMap<>();
	}

	public ReplyResult(boolean success, String message, Map<String, Object> metadata, Object data) {
		this.success = success;
		this.message = message;
		this.metadata = metadata != null ? metadata : new HashMap<>();
		this.data = data;
	}

	public static ReplyResult success(String message) {
		return new ReplyResult(true, message, null, null);
	}

	public static ReplyResult success(String message, Object data) {
		return new ReplyResult(true, message, null, data);
	}

	public static ReplyResult failure(String message) {
		return new ReplyResult(false, message, null, null);
	}

	public static ReplyResult failure(String message, Map<String, Object> metadata) {
		return new ReplyResult(false, message, metadata, null);
	}

	// Getters and Setters
	public boolean isSuccess() {
		return success;
	}

	public void setSuccess(boolean success) {
		this.success = success;
	}

	public String getMessage() {
		return message;
	}

	public void setMessage(String message) {
		this.message = message;
	}

	public Map<String, Object> getMetadata() {
		return metadata;
	}

	public void setMetadata(Map<String, Object> metadata) {
		this.metadata = metadata;
	}

	public Object getData() {
		return data;
	}

	public void setData(Object data) {
		this.data = data;
	}

	public void putMetadata(String key, Object value) {
		this.metadata.put(key, value);
	}

}

