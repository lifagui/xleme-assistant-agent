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

import com.alibaba.cloud.ai.graph.CompiledGraph;
import com.alibaba.cloud.ai.graph.OverAllState;
import com.alibaba.cloud.ai.graph.RunnableConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;

import java.util.Map;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

/**
 * 学习调度任务
 * 包装学习图为定时任务，支持cron或固定间隔调度
 *
 * @author Assistant Agent Team
 * @since 1.0.0
 */
public class LearningScheduledTask {

	private static final Logger log = LoggerFactory.getLogger(LearningScheduledTask.class);

	private final CompiledGraph learningGraph;

	private final LearningScheduleConfig scheduleConfig;

	private volatile boolean running = false;

	public LearningScheduledTask(CompiledGraph learningGraph, LearningScheduleConfig scheduleConfig) {
		this.learningGraph = learningGraph;
		this.scheduleConfig = scheduleConfig;
	}

	/**
	 * 执行学习任务
	 */
	public void execute() {
		if (running) {
			log.warn(
					"LearningScheduledTask#execute - reason=task already running, skipping execution, taskName={}",
					scheduleConfig.getTaskName());
			return;
		}

		running = true;
		long startTime = System.currentTimeMillis();

		log.info("LearningScheduledTask#execute - reason=starting scheduled learning task, taskName={}",
				scheduleConfig.getTaskName());

		try {
			// Create initial state
			OverAllState initialState = new OverAllState();

			// Create config
			RunnableConfig config = RunnableConfig.builder().build();

			// Execute the learning graph
			Optional<OverAllState> resultOpt = learningGraph.invoke(initialState, config);

			long duration = System.currentTimeMillis() - startTime;

			if (resultOpt.isPresent()) {
				log.info(
						"LearningScheduledTask#execute - reason=learning task completed successfully, taskName={}, duration={}ms",
						scheduleConfig.getTaskName(), duration);
			}
			else {
				log.warn(
						"LearningScheduledTask#execute - reason=learning task completed but no result returned, taskName={}, duration={}ms",
						scheduleConfig.getTaskName(), duration);
			}

		}
		catch (Exception e) {
			long duration = System.currentTimeMillis() - startTime;
			log.error(
					"LearningScheduledTask#execute - reason=learning task failed, taskName={}, duration={}ms",
					scheduleConfig.getTaskName(), duration, e);
		}
		finally {
			running = false;
		}
	}

	/**
	 * Cron调度执行（需要在配置中启用）
	 * 通过@Scheduled注解或ScheduledAgentManager触发
	 */
	@Scheduled(cron = "${spring.ai.alibaba.codeact.extension.learning.offline.tasks[0].cron-expression:0 0 2 * * ?}")
	public void executeByCron() {
		if ("cron".equalsIgnoreCase(scheduleConfig.getScheduleMode())) {
			log.debug("LearningScheduledTask#executeByCron - reason=cron triggered, taskName={}",
					scheduleConfig.getTaskName());
			execute();
		}
	}

	/**
	 * 固定间隔调度执行
	 */
	@Scheduled(fixedDelayString = "${spring.ai.alibaba.codeact.extension.learning.offline.tasks[0].interval-ms:3600000}")
	public void executeByInterval() {
		if ("interval".equalsIgnoreCase(scheduleConfig.getScheduleMode())) {
			log.debug("LearningScheduledTask#executeByInterval - reason=interval triggered, taskName={}",
					scheduleConfig.getTaskName());
			execute();
		}
	}

	/**
	 * 手动触发执行
	 */
	public CompletableFuture<Map<String, Object>> executeAsync() {
		log.info("LearningScheduledTask#executeAsync - reason=manual async execution, taskName={}",
				scheduleConfig.getTaskName());

		return CompletableFuture.supplyAsync(() -> {
			execute();
			return Map.of("status", "completed", "taskName", scheduleConfig.getTaskName());
		});
	}

	/**
	 * 检查任务是否正在运行
	 */
	public boolean isRunning() {
		return running;
	}

	/**
	 * 获取任务配置
	 */
	public LearningScheduleConfig getScheduleConfig() {
		return scheduleConfig;
	}

	/**
	 * 获取学习图
	 */
	public CompiledGraph getLearningGraph() {
		return learningGraph;
	}

}

