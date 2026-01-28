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
package com.alibaba.assistant.agent.extension.reminder.config;

import com.alibaba.assistant.agent.common.tools.CodeactTool;
import com.alibaba.assistant.agent.extension.reminder.service.ReminderService;
import com.alibaba.assistant.agent.extension.reminder.tools.ReminderCodeactToolFactory;
import com.alibaba.assistant.agent.extension.trigger.manager.TriggerManager;
import com.alibaba.assistant.agent.persistence.config.JpaConfig;
import com.alibaba.assistant.agent.persistence.repository.ReminderJpaRepository;
import com.alibaba.assistant.agent.persistence.repository.ReminderLogJpaRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;

import java.util.Collections;
import java.util.List;

/**
 * 提醒模块自动配置类
 *
 * @author Assistant Agent Team
 * @since 1.0.0
 */
@AutoConfiguration
@AutoConfigureAfter(JpaConfig.class)
@ConditionalOnProperty(prefix = "assistant.reminder", name = "enabled", havingValue = "true", matchIfMissing = true)
@EnableConfigurationProperties(ReminderProperties.class)
public class ReminderAutoConfiguration {

    private static final Logger log = LoggerFactory.getLogger(ReminderAutoConfiguration.class);

    @Bean
    @ConditionalOnMissingBean
    public ReminderService reminderService(
            ObjectProvider<ReminderJpaRepository> reminderRepositoryProvider,
            ObjectProvider<ReminderLogJpaRepository> reminderLogRepositoryProvider) {
        
        ReminderJpaRepository reminderRepository = reminderRepositoryProvider.getIfAvailable();
        ReminderLogJpaRepository reminderLogRepository = reminderLogRepositoryProvider.getIfAvailable();
        
        if (reminderRepository == null || reminderLogRepository == null) {
            log.warn("ReminderAutoConfiguration reminderService 依赖的 Repository 不可用: reminderRepository={}, reminderLogRepository={}",
                    reminderRepository != null, reminderLogRepository != null);
            return null;
        }
        
        log.info("ReminderAutoConfiguration reminderService 创建ReminderService");
        return new ReminderService(reminderRepository, reminderLogRepository);
    }

    @Bean
    @ConditionalOnMissingBean
    public ReminderCodeactToolFactory reminderCodeactToolFactory(
            ObjectProvider<ReminderService> reminderServiceProvider,
            ObjectProvider<TriggerManager> triggerManagerProvider,
            ReminderProperties properties) {
        ReminderService reminderService = reminderServiceProvider.getIfAvailable();
        TriggerManager triggerManager = triggerManagerProvider.getIfAvailable();
        
        if (reminderService == null || triggerManager == null) {
            log.warn("ReminderAutoConfiguration reminderCodeactToolFactory 依赖不满足, reminderService={}, triggerManager={}", 
                    reminderService != null, triggerManager != null);
            return null;
        }
        
        log.info("ReminderAutoConfiguration reminderCodeactToolFactory 创建ReminderCodeactTool工厂");
        return new ReminderCodeactToolFactory(reminderService, triggerManager, properties);
    }

    /**
     * Reminder工具列表Bean
     */
    @Bean
    public List<CodeactTool> reminderCodeactTools(ObjectProvider<ReminderCodeactToolFactory> factoryProvider) {
        ReminderCodeactToolFactory factory = factoryProvider.getIfAvailable();
        if (factory == null) {
            log.warn("ReminderAutoConfiguration reminderCodeactTools ReminderCodeactToolFactory 不可用");
            return Collections.emptyList();
        }
        
        log.info("ReminderAutoConfiguration reminderCodeactTools 开始创建Reminder工具");
        List<CodeactTool> tools = factory.createTools();
        log.info("ReminderAutoConfiguration reminderCodeactTools Reminder工具创建完成, count={}", tools.size());
        return tools;
    }
}
