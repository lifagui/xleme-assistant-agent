package com.alibaba.assistant.agent.extension.reply.config;

import java.util.*;

/**
 * 回复工具配置模型
 *
 * @author Assistant Agent Team
 */
public class ReplyToolConfig {

	private String toolName;

	private String channelCode;

	private String description;

	private boolean reactEnabled = true;

	private boolean codeActEnabled = true;

	private List<ParameterConfig> parameters;

	private Map<String, String> parameterMapping;

	private Map<String, Object> defaultParameters;

	public ReplyToolConfig() {
	}

	// Getters and Setters
	public String getToolName() {
		return toolName;
	}

	public void setToolName(String toolName) {
		this.toolName = toolName;
	}

	public String getChannelCode() {
		return channelCode;
	}

	public void setChannelCode(String channelCode) {
		this.channelCode = channelCode;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public boolean isReactEnabled() {
		return reactEnabled;
	}

	public void setReactEnabled(boolean reactEnabled) {
		this.reactEnabled = reactEnabled;
	}

	public boolean isCodeActEnabled() {
		return codeActEnabled;
	}

	public void setCodeActEnabled(boolean codeActEnabled) {
		this.codeActEnabled = codeActEnabled;
	}

	public List<ParameterConfig> getParameters() {
		return parameters;
	}

	public void setParameters(List<ParameterConfig> parameters) {
		this.parameters = parameters;
	}

	public Map<String, String> getParameterMapping() {
		return parameterMapping;
	}

	public void setParameterMapping(Map<String, String> parameterMapping) {
		this.parameterMapping = parameterMapping;
	}

	public Map<String, Object> getDefaultParameters() {
		return defaultParameters;
	}

	public void setDefaultParameters(Map<String, Object> defaultParameters) {
		this.defaultParameters = defaultParameters;
	}

	public static Builder builder() {
		return new Builder();
	}

	/**
	 * 参数配置
	 */
	public static class ParameterConfig {

		private String name;

		private String type;

		private boolean required;

		private String description;

		private Object defaultValue;

		private List<String> enumValues;

		public ParameterConfig() {
		}

		// Getters and Setters
		public String getName() {
			return name;
		}

		public void setName(String name) {
			this.name = name;
		}

		public String getType() {
			return type;
		}

		public void setType(String type) {
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

		public static Builder builder() {
			return new Builder();
		}

		public static class Builder {

			private final ParameterConfig config = new ParameterConfig();

			public Builder name(String name) {
				config.setName(name);
				return this;
			}

			public Builder type(String type) {
				config.setType(type);
				return this;
			}

			public Builder required(boolean required) {
				config.setRequired(required);
				return this;
			}

			public Builder description(String description) {
				config.setDescription(description);
				return this;
			}

			public Builder defaultValue(Object defaultValue) {
				config.setDefaultValue(defaultValue);
				return this;
			}

			public Builder enumValues(List<String> enumValues) {
				config.setEnumValues(enumValues);
				return this;
			}

			public ParameterConfig build() {
				return config;
			}

		}

	}

	/**
	 * Builder for ReplyToolConfig
	 */
	public static class Builder {

		private final ReplyToolConfig config = new ReplyToolConfig();

		public Builder toolName(String toolName) {
			config.setToolName(toolName);
			return this;
		}

		public Builder channelCode(String channelCode) {
			config.setChannelCode(channelCode);
			return this;
		}

		public Builder description(String description) {
			config.setDescription(description);
			return this;
		}

		public Builder reactEnabled(boolean reactEnabled) {
			config.setReactEnabled(reactEnabled);
			return this;
		}

		public Builder codeActEnabled(boolean codeActEnabled) {
			config.setCodeActEnabled(codeActEnabled);
			return this;
		}

		public Builder parameters(List<ParameterConfig> parameters) {
			config.setParameters(parameters);
			return this;
		}

		public Builder parameterMapping(Map<String, String> parameterMapping) {
			config.setParameterMapping(parameterMapping);
			return this;
		}

		public Builder defaultParameters(Map<String, Object> defaultParameters) {
			config.setDefaultParameters(defaultParameters);
			return this;
		}

		public ReplyToolConfig build() {
			return config;
		}

	}

}

