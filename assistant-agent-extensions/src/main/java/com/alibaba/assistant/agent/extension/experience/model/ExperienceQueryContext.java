package com.alibaba.assistant.agent.extension.experience.model;

/**
 * 经验查询上下文
 * Hook基于OverAllState/RunnableConfig构造的上下文信息
 *
 * @author Assistant Agent Team
 */
public class ExperienceQueryContext {

    /**
     * 用户查询内容（用于经验检索）
     */
    private String userQuery;

    /**
     * 用户ID
     */
    private String userId;

    /**
     * 项目ID
     */
    private String projectId;

    /**
     * 仓库ID
     */
    private String repoId;

    /**
     * 当前文件路径
     */
    private String currentFilePath;

    /**
     * 任务类型
     */
    private String taskType;

    /**
     * Agent名称
     */
    private String agentName;

    /**
     * Agent类型
     */
    private String agentType;

    /**
     * 场景标签
     */
    private String sceneTags;

    /**
     * 编程语言
     */
    private String language;

    public ExperienceQueryContext() {
    }

    public String getUserQuery() {
        return userQuery;
    }

    public void setUserQuery(String userQuery) {
        this.userQuery = userQuery;
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

    public String getRepoId() {
        return repoId;
    }

    public void setRepoId(String repoId) {
        this.repoId = repoId;
    }

    public String getCurrentFilePath() {
        return currentFilePath;
    }

    public void setCurrentFilePath(String currentFilePath) {
        this.currentFilePath = currentFilePath;
    }

    public String getTaskType() {
        return taskType;
    }

    public void setTaskType(String taskType) {
        this.taskType = taskType;
    }

    public String getAgentName() {
        return agentName;
    }

    public void setAgentName(String agentName) {
        this.agentName = agentName;
    }

    public String getAgentType() {
        return agentType;
    }

    public void setAgentType(String agentType) {
        this.agentType = agentType;
    }

    public String getSceneTags() {
        return sceneTags;
    }

    public void setSceneTags(String sceneTags) {
        this.sceneTags = sceneTags;
    }

    public String getLanguage() {
        return language;
    }

    public void setLanguage(String language) {
        this.language = language;
    }
}
