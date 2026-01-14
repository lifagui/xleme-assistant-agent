package com.alibaba.assistant.agent.extension.experience.spi;

import com.alibaba.assistant.agent.extension.experience.model.Experience;
import com.alibaba.assistant.agent.extension.experience.model.ExperienceQuery;
import com.alibaba.assistant.agent.extension.experience.model.ExperienceQueryContext;
import com.alibaba.assistant.agent.extension.experience.model.ExperienceType;

import java.util.List;

/**
 * 经验读取SPI接口
 * 负责根据查询条件返回经验列表
 *
 * @author Assistant Agent Team
 */
public interface ExperienceProvider {

    /**
     * 根据查询条件检索符合条件的经验
     *
     * @param query 查询条件
     * @param context 查询上下文，可为null
     * @return 经验列表，按相关性和时间排序
     */
    List<Experience> query(ExperienceQuery query, ExperienceQueryContext context);

    /**
     * 根据经验类型快速查询
     *
     * @param type 经验类型
     * @param context 查询上下文
     * @return 经验列表
     */
    default List<Experience> queryByType(ExperienceType type, ExperienceQueryContext context) {
        ExperienceQuery query = new ExperienceQuery(type);
        return query(query, context);
    }
}
