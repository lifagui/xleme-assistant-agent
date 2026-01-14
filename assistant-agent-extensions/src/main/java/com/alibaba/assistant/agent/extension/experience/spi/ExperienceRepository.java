package com.alibaba.assistant.agent.extension.experience.spi;

import com.alibaba.assistant.agent.extension.experience.model.Experience;
import com.alibaba.assistant.agent.extension.experience.model.ExperienceScope;
import com.alibaba.assistant.agent.extension.experience.model.ExperienceType;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

/**
 * 经验写入与管理SPI接口
 * 负责经验池的增删改查管理
 *
 * @author Assistant Agent Team
 */
public interface ExperienceRepository {

    /**
     * 保存单个经验
     *
     * @param experience 经验对象
     * @return 保存后的经验对象
     */
    Experience save(Experience experience);

    /**
     * 批量保存经验
     *
     * @param experiences 经验列表
     * @return 保存后的经验列表
     */
    List<Experience> batchSave(Collection<Experience> experiences);

    /**
     * 根据ID删除经验
     *
     * @param id 经验ID
     * @return 删除是否成功
     */
    boolean deleteById(String id);

    /**
     * 根据ID查找经验
     *
     * @param id 经验ID
     * @return 经验对象，如果不存在返回empty
     */
    Optional<Experience> findById(String id);

    /**
     * 根据类型和范围查找经验
     *
     * @param type 经验类型
     * @param scope 生效范围
     * @param ownerId 所有者ID，可为null
     * @param projectId 项目ID，可为null
     * @return 经验列表
     */
    List<Experience> findByTypeAndScope(ExperienceType type, ExperienceScope scope, String ownerId, String projectId);

    /**
     * 统计经验数量
     *
     * @return 总经验数量
     */
    long count();

    /**
     * 根据条件统计经验数量
     *
     * @param type 经验类型，可为null
     * @param scope 生效范围，可为null
     * @return 符合条件的经验数量
     */
    long countByTypeAndScope(ExperienceType type, ExperienceScope scope);
}
