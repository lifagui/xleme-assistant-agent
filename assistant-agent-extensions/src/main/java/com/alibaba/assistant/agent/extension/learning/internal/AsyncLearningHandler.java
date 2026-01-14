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

package com.alibaba.assistant.agent.extension.learning.internal;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.*;

/**
 * 异步学习处理器
 * 管理异步学习任务的执行，包括线程池管理和任务拒绝策略
 *
 * @author Assistant Agent Team
 * @since 1.0.0
 */
public class AsyncLearningHandler {

	private static final Logger log = LoggerFactory.getLogger(AsyncLearningHandler.class);

	private final ExecutorService executorService;

	public AsyncLearningHandler(int threadPoolSize, int queueCapacity) {
		this.executorService = new ThreadPoolExecutor(threadPoolSize, threadPoolSize, 60L, TimeUnit.SECONDS,
				new LinkedBlockingQueue<>(queueCapacity), new ThreadFactory() {
					private int count = 0;

					@Override
					public Thread newThread(Runnable r) {
						return new Thread(r, "learning-async-" + (count++));
					}
				}, new ThreadPoolExecutor.CallerRunsPolicy());

		log.info(
				"AsyncLearningHandler#constructor - reason=async learning handler initialized, threadPoolSize={}, queueCapacity={}",
				threadPoolSize, queueCapacity);
	}

	/**
	 * 异步执行任务
	 * @param task 任务
	 * @param <T> 任务返回类型
	 * @return CompletableFuture
	 */
	public <T> CompletableFuture<T> executeAsync(Callable<T> task) {
		return CompletableFuture.supplyAsync(() -> {
			try {
				return task.call();
			}
			catch (Exception e) {
				log.error("AsyncLearningHandler#executeAsync - reason=async task execution failed", e);
				throw new CompletionException(e);
			}
		}, executorService);
	}

	/**
	 * 关闭线程池
	 */
	public void shutdown() {
		log.info("AsyncLearningHandler#shutdown - reason=shutting down async learning handler");
		executorService.shutdown();
		try {
			if (!executorService.awaitTermination(30, TimeUnit.SECONDS)) {
				executorService.shutdownNow();
			}
		}
		catch (InterruptedException e) {
			executorService.shutdownNow();
			Thread.currentThread().interrupt();
		}
	}

}

