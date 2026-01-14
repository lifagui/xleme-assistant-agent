package com.alibaba.assistant.agent.extension.experience.model;

/**
 * 经验生效范围枚举
 * 查询时优先级从高到低：USER + PROJECT -> USER -> TEAM + PROJECT -> TEAM -> PROJECT -> GLOBAL
 *
 * @author Assistant Agent Team
 */
public enum ExperienceScope {

    /**
     * 全局生效，对所有用户与项目可见
     */
    GLOBAL,

    /**
     * 团队级，在同一团队/组织下共享
     */
    TEAM,

    /**
     * 仅关联用户本人可见
     */
    USER,

    /**
     * 项目/仓库级别，在特定项目下共享
     */
    PROJECT
}
