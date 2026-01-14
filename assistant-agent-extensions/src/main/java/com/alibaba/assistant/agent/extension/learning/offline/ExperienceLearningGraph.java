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

import com.alibaba.assistant.agent.extension.experience.model.Experience;
import com.alibaba.assistant.agent.extension.experience.spi.ExperienceRepository;
import com.alibaba.assistant.agent.extension.learning.extractor.ExperienceLearningExtractor;
import com.alibaba.assistant.agent.extension.learning.internal.DefaultLearningContext;
import com.alibaba.assistant.agent.extension.learning.model.LearningContext;
import com.alibaba.assistant.agent.extension.learning.model.LearningTriggerSource;
import com.alibaba.assistant.agent.extension.learning.spi.LearningExecutor;
import com.alibaba.assistant.agent.extension.learning.spi.LearningExtractor;
import com.alibaba.assistant.agent.extension.learning.spi.LearningRepository;
import com.alibaba.cloud.ai.graph.RunnableConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 经验学习图
 * 专门用于从历史数据中学习经验的离线学习图
 *
 * @author Assistant Agent Team
 * @since 1.0.0
 */
public class ExperienceLearningGraph extends OfflineLearningGraph {

	private static final Logger log = LoggerFactory.getLogger(ExperienceLearningGraph.class);

	private final ExperienceLearningExtractor experienceExtractor;

	private final ExperienceRepository experienceRepository;

	private final long lookbackPeriodHours;

	public ExperienceLearningGraph(LearningExecutor learningExecutor, List<LearningExtractor<?>> extractors,
			List<LearningRepository<?>> repositories, ExperienceLearningExtractor experienceExtractor,
			ExperienceRepository experienceRepository, long lookbackPeriodHours) {
		super(learningExecutor, extractors, repositories, "experience_learning_graph");
		this.experienceExtractor = experienceExtractor;
		this.experienceRepository = experienceRepository;
		this.lookbackPeriodHours = lookbackPeriodHours > 0 ? lookbackPeriodHours : 24; // 默认24小时
	}

	@Override
	protected List<Object> fetchHistoricalData(RunnableConfig config) {
		log.info(
				"ExperienceLearningGraph#fetchHistoricalData - reason=fetching historical execution data, lookbackHours={}",
				lookbackPeriodHours);

		try {
			// TODO: 实现从实际数据源获取历史执行数据
			// 可能的数据源：
			// - Store中的执行历史
			// - 日志系统
			// - 数据库记录
			// - 文件系统

			// 示例：获取最近N小时的执行记录
			Instant cutoffTime = Instant.now().minus(lookbackPeriodHours, ChronoUnit.HOURS);

			List<Object> historicalData = new ArrayList<>();

			// 这里应该查询实际的历史数据
			// 例如：从Store中查询执行记录
			// StoreSearchRequest request = StoreSearchRequest.builder()
			// .namespace("execution_history")
			// .query("success")
			// .limit(1000)
			// .build();
			// StoreSearchResult result = store.searchItems(request);
			// historicalData = result.getItems().stream()
			// .map(StoreItem::getValue)
			// .collect(Collectors.toList());

			log.info(
					"ExperienceLearningGraph#fetchHistoricalData - reason=historical data fetched, count={}, cutoffTime={}",
					historicalData.size(), cutoffTime);

			return historicalData;
		}
		catch (Exception e) {
			log.error("ExperienceLearningGraph#fetchHistoricalData - reason=failed to fetch historical data", e);
			return List.of();
		}
	}

	@Override
	protected List<Object> preprocessData(List<Object> rawData, RunnableConfig config) {
		log.info("ExperienceLearningGraph#preprocessData - reason=preprocessing execution data, count={}",
				rawData.size());

		try {
			// 过滤和清洗数据
			List<Object> processedData = rawData.stream().filter(data -> {
				// TODO: 实现过滤逻辑
				// - 过滤掉失败的执行
				// - 过滤掉不完整的数据
				// - 过滤掉重复的数据
				return true; // 示例：全部保留
			}).collect(Collectors.toList());

			log.info(
					"ExperienceLearningGraph#preprocessData - reason=preprocessing completed, original={}, filtered={}",
					rawData.size(), processedData.size());

			return processedData;
		}
		catch (Exception e) {
			log.error("ExperienceLearningGraph#preprocessData - reason=preprocessing failed", e);
			return rawData;
		}
	}

	@Override
	protected List<Object> extractLearningRecords(List<Object> processedData, RunnableConfig config) {
		log.info("ExperienceLearningGraph#extractLearningRecords - reason=extracting experiences, count={}",
				processedData.size());

		try {
			List<Object> allExperiences = new ArrayList<>();

			for (Object data : processedData) {
				// 将历史数据转换为LearningContext
				LearningContext context = convertToLearningContext(data);

				// 检查是否应该学习
				if (!experienceExtractor.shouldLearn(context)) {
					continue;
				}

				// 提取经验
				List<Experience> experiences = experienceExtractor.extract(context);
				allExperiences.addAll(experiences);
			}

			log.info(
					"ExperienceLearningGraph#extractLearningRecords - reason=extraction completed, input={}, extracted={}",
					processedData.size(), allExperiences.size());

			return allExperiences;
		}
		catch (Exception e) {
			log.error("ExperienceLearningGraph#extractLearningRecords - reason=extraction failed", e);
			return List.of();
		}
	}

	@Override
	protected int persistLearningRecords(List<Object> records, RunnableConfig config) {
		log.info("ExperienceLearningGraph#persistLearningRecords - reason=persisting experiences, count={}",
				records.size());

		try {
			int persistedCount = 0;

			for (Object record : records) {
				if (record instanceof Experience) {
					Experience experience = (Experience) record;
					experienceRepository.save(experience);
					persistedCount++;
				}
				else {
					log.warn(
							"ExperienceLearningGraph#persistLearningRecords - reason=skipping non-experience record, type={}",
							record.getClass().getName());
				}
			}

			log.info(
					"ExperienceLearningGraph#persistLearningRecords - reason=persistence completed, total={}, persisted={}",
					records.size(), persistedCount);

			return persistedCount;
		}
		catch (Exception e) {
			log.error("ExperienceLearningGraph#persistLearningRecords - reason=persistence failed", e);
			return 0;
		}
	}

	/**
	 * 将历史数据转换为LearningContext
	 */
	private LearningContext convertToLearningContext(Object data) {
		// TODO: 实现实际的转换逻辑
		// 这里需要根据历史数据的格式来提取信息
		// 例如：
		// - 提取对话历史
		// - 提取工具调用记录
		// - 提取模型调用记录
		// - 提取执行状态

		return DefaultLearningContext.builder()
			.overAllState(data)
			.triggerSource(LearningTriggerSource.SCHEDULED)
			.build();
	}

}

