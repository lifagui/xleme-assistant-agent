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
package com.alibaba.assistant.agent.core.executor.bridge;

import com.alibaba.cloud.ai.graph.OverAllState;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;
import java.util.Optional;

/**
 * Bridge object to expose agent state to Python code.
 * Python code can call: agent_state.get("key"), agent_state.set("key", value)
 *
 * @author Assistant Agent Team
 * @since 1.0.0
 */
public class StateBridge {

	private static final Logger logger = LoggerFactory.getLogger(StateBridge.class);

	private final OverAllState state;

	public StateBridge(OverAllState state) {
		this.state = state;
		logger.debug("StateBridge#<init> 初始化完成");
	}

	/**
	 * Get a value from state
	 */
	public Object get(String key) {
		logger.debug("StateBridge#get 获取状态: key={}", key);

		Optional<Object> value = state.value(key);
		return value.orElse(null);
	}

	/**
	 * Set a value in state
	 */
	public void set(String key, Object value) {
		logger.info("StateBridge#set 设置状态: key={}", key);

		state.updateState(Map.of(key, value));
	}

	/**
	 * Check if a key exists
	 */
	public boolean has(String key) {
		return state.value(key).isPresent();
	}

	/**
	 * Get all state data (read-only)
	 */
	public Map<String, Object> getAll() {
		logger.debug("StateBridge#getAll 获取全部状态");
		return state.data();
	}
}

