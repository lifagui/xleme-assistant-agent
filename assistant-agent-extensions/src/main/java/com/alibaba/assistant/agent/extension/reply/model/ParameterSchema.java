package com.alibaba.assistant.agent.extension.reply.model;

import java.util.*;

/**
 * 参数Schema定义
 * 用于描述回复渠道支持的参数结构
 *
 * @author Assistant Agent Team
 */
public class ParameterSchema {

	private final List<ParameterDef> parameters;

	public ParameterSchema(List<ParameterDef> parameters) {
		this.parameters = parameters != null ? new ArrayList<>(parameters) : new ArrayList<>();
	}

	public List<ParameterDef> getParameters() {
		return new ArrayList<>(parameters);
	}

	/**
	 * 验证参数是否符合schema定义
	 */
	public void validate(Map<String, Object> params) {
		if (params == null) {
			params = Collections.emptyMap();
		}

		// 检查必填参数
		for (ParameterDef param : parameters) {
			if (param.isRequired() && !params.containsKey(param.getName())) {
				throw new IllegalArgumentException(
						String.format("Required parameter '%s' is missing", param.getName()));
			}
		}

		// 检查参数类型和枚举值
		for (Map.Entry<String, Object> entry : params.entrySet()) {
			String paramName = entry.getKey();
			Object value = entry.getValue();

			ParameterDef paramDef = findParameter(paramName);
			if (paramDef != null) {
				// 验证枚举值
				if (paramDef.getEnumValues() != null && !paramDef.getEnumValues().isEmpty()) {
					String strValue = value != null ? value.toString() : null;
					if (strValue != null && !paramDef.getEnumValues().contains(strValue)) {
						throw new IllegalArgumentException(String.format(
								"Parameter '%s' value '%s' is not in allowed enum values: %s", paramName, strValue,
								paramDef.getEnumValues()));
					}
				}
			}
		}
	}

	private ParameterDef findParameter(String name) {
		return parameters.stream().filter(p -> p.getName().equals(name)).findFirst().orElse(null);
	}

	/**
	 * 转换为JSON Schema字符串（简化版）
	 */
	public String toJsonSchema() {
		StringBuilder sb = new StringBuilder();
		sb.append("{\"type\":\"object\",\"properties\":{");

		for (int i = 0; i < parameters.size(); i++) {
			if (i > 0) {
				sb.append(",");
			}
			ParameterDef param = parameters.get(i);
			sb.append("\"").append(param.getName()).append("\":{");
			sb.append("\"type\":\"").append(param.getType().getJsonType()).append("\"");
			if (param.getDescription() != null) {
				sb.append(",\"description\":\"").append(param.getDescription()).append("\"");
			}
			if (param.getEnumValues() != null && !param.getEnumValues().isEmpty()) {
				sb.append(",\"enum\":[");
				for (int j = 0; j < param.getEnumValues().size(); j++) {
					if (j > 0)
						sb.append(",");
					sb.append("\"").append(param.getEnumValues().get(j)).append("\"");
				}
				sb.append("]");
			}
			sb.append("}");
		}

		sb.append("},\"required\":[");
		boolean first = true;
		for (ParameterDef param : parameters) {
			if (param.isRequired()) {
				if (!first)
					sb.append(",");
				sb.append("\"").append(param.getName()).append("\"");
				first = false;
			}
		}
		sb.append("]}");

		return sb.toString();
	}

	public static Builder builder() {
		return new Builder();
	}

	/**
	 * 参数定义
	 */
	public static class ParameterDef {

		private String name;

		private ParameterType type;

		private boolean required;

		private String description;

		private Object defaultValue;

		private List<String> enumValues;

		public ParameterDef() {
		}

		public ParameterDef(String name, ParameterType type, boolean required, String description) {
			this.name = name;
			this.type = type;
			this.required = required;
			this.description = description;
		}

		// Getters and Setters
		public String getName() {
			return name;
		}

		public void setName(String name) {
			this.name = name;
		}

		public ParameterType getType() {
			return type;
		}

		public void setType(ParameterType type) {
			this.type = type;
		}

		public boolean isRequired() {
			return required;
		}

		public void setRequired(boolean required) {
			this.required = required;
		}

		public String getDescription() {
			return description;
		}

		public void setDescription(String description) {
			this.description = description;
		}

		public Object getDefaultValue() {
			return defaultValue;
		}

		public void setDefaultValue(Object defaultValue) {
			this.defaultValue = defaultValue;
		}

		public List<String> getEnumValues() {
			return enumValues;
		}

		public void setEnumValues(List<String> enumValues) {
			this.enumValues = enumValues;
		}

	}

	/**
	 * 参数类型枚举
	 */
	public enum ParameterType {

		STRING("string"), INTEGER("integer"), BOOLEAN("boolean"), OBJECT("object"), ARRAY("array");

		private final String jsonType;

		ParameterType(String jsonType) {
			this.jsonType = jsonType;
		}

		public String getJsonType() {
			return jsonType;
		}

	}

	/**
	 * Builder for ParameterSchema
	 */
	public static class Builder {

		private final List<ParameterDef> parameters = new ArrayList<>();

		public Builder parameter(String name, ParameterType type, boolean required, String description) {
			ParameterDef def = new ParameterDef(name, type, required, description);
			parameters.add(def);
			return this;
		}

		public Builder parameter(ParameterDef def) {
			parameters.add(def);
			return this;
		}

		public ParameterSchema build() {
			return new ParameterSchema(parameters);
		}

	}

}

