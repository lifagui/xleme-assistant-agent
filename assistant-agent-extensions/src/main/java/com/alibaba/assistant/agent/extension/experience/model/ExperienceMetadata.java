package com.alibaba.assistant.agent.extension.experience.model;

import java.util.HashMap;
import java.util.Map;

/**
 * 经验附加元信息
 *
 * @author Assistant Agent Team
 */
public class ExperienceMetadata {

    /**
     * 经验来源
     */
    private String source;

    /**
     * 置信度或质量评分 (0.0-1.0)
     */
    private Double confidence;

    /**
     * 版本信息
     */
    private String version;

    /**
     * 扩展属性
     */
    private Map<String, Object> properties = new HashMap<>();

    public ExperienceMetadata() {
    }

    public ExperienceMetadata(String source, Double confidence, String version) {
        this.source = source;
        this.confidence = confidence;
        this.version = version;
    }

    public String getSource() {
        return source;
    }

    public void setSource(String source) {
        this.source = source;
    }

    public Double getConfidence() {
        return confidence;
    }

    public void setConfidence(Double confidence) {
        this.confidence = confidence;
    }

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    public Map<String, Object> getProperties() {
        return properties;
    }

    public void setProperties(Map<String, Object> properties) {
        this.properties = properties;
    }

    public void putProperty(String key, Object value) {
        this.properties.put(key, value);
    }

    public Object getProperty(String key) {
        return this.properties.get(key);
    }
}
