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
package com.alibaba.assistant.agent.core.tool.definition;

import com.alibaba.assistant.agent.common.tools.definition.ParameterNode;
import com.alibaba.assistant.agent.common.tools.definition.ParameterTree;
import com.alibaba.assistant.agent.common.tools.definition.ParameterType;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * 工具定义解析器 - 从 inputSchema JSON 解析出 ParameterTree。
 *
 * @author Assistant Agent Team
 * @since 1.0.0
 */
public class ToolDefinitionParser {

	private static final Logger logger = LoggerFactory.getLogger(ToolDefinitionParser.class);

	private static final ObjectMapper objectMapper = new ObjectMapper();

	private ToolDefinitionParser() {
		// 工具类，禁止实例化
	}

	/**
	 * 从 inputSchema JSON 解析出 ParameterTree。
	 * @param inputSchema JSON Schema 字符串
	 * @return 解析后的 ParameterTree
	 */
	public static ParameterTree parse(String inputSchema) {
		if (inputSchema == null || inputSchema.isBlank()) {
			logger.debug("ToolDefinitionParser#parse - reason=inputSchema为空，返回空ParameterTree");
			return ParameterTree.empty();
		}

		try {
			JsonNode rootNode = objectMapper.readTree(inputSchema);
			return parseFromJsonNode(rootNode, inputSchema);
		}
		catch (Exception e) {
			logger.warn("ToolDefinitionParser#parse - reason=解析inputSchema失败，返回空ParameterTree, error={}", e.getMessage());
			return ParameterTree.builder().rawInputSchema(inputSchema).build();
		}
	}

	/**
	 * 从 JsonNode 解析 ParameterTree。
	 * @param rootNode 根 JSON 节点
	 * @param rawInputSchema 原始 schema 字符串
	 * @return 解析后的 ParameterTree
	 */
	public static ParameterTree parseFromJsonNode(JsonNode rootNode, String rawInputSchema) {
		ParameterTree.Builder builder = ParameterTree.builder().rawInputSchema(rawInputSchema);

		// 解析 required 字段
		Set<String> requiredNames = new HashSet<>();
		JsonNode requiredNode = rootNode.get("required");
		if (requiredNode != null && requiredNode.isArray()) {
			for (JsonNode reqName : requiredNode) {
				requiredNames.add(reqName.asText());
			}
		}
		builder.requiredNames(requiredNames);

		// 解析 properties 字段
		JsonNode propertiesNode = rootNode.get("properties");
		if (propertiesNode != null && propertiesNode.isObject()) {
			Iterator<Map.Entry<String, JsonNode>> fields = propertiesNode.fields();
			while (fields.hasNext()) {
				Map.Entry<String, JsonNode> field = fields.next();
				String paramName = field.getKey();
				JsonNode paramNode = field.getValue();

				ParameterNode parameterNode = parseParameterNode(paramName, paramNode, requiredNames.contains(paramName));
				builder.addParameter(parameterNode);
			}
		}

		logger.debug("ToolDefinitionParser#parseFromJsonNode - reason=成功解析ParameterTree, parameterCount={}",
				builder.build().getParameterCount());
		return builder.build();
	}

	/**
	 * 解析单个参数节点。
	 * @param name 参数名
	 * @param node JSON 节点
	 * @param required 是否必填
	 * @return 解析后的 ParameterNode
	 */
	private static ParameterNode parseParameterNode(String name, JsonNode node, boolean required) {
		ParameterNode.Builder builder = ParameterNode.builder().name(name).required(required);

		// 解析类型
		String typeStr = getTextValue(node, "type");
		ParameterType type = ParameterType.fromJsonSchemaType(typeStr);
		builder.type(type);

		// 解析描述
		String description = getTextValue(node, "description");
		builder.description(description);

		// 解析格式
		String format = getTextValue(node, "format");
		builder.format(format);

		// 解析默认值
		JsonNode defaultNode = node.get("default");
		if (defaultNode != null && !defaultNode.isNull()) {
			builder.defaultValue(extractValue(defaultNode));
		}

		// 解析枚举值
		JsonNode enumNode = node.get("enum");
		if (enumNode != null && enumNode.isArray()) {
			List<Object> enumValues = new ArrayList<>();
			for (JsonNode enumValue : enumNode) {
				enumValues.add(extractValue(enumValue));
			}
			builder.enumValues(enumValues);
		}

		// 解析约束
		StringBuilder constraint = new StringBuilder();
		appendConstraint(constraint, node, "minimum", "min");
		appendConstraint(constraint, node, "maximum", "max");
		appendConstraint(constraint, node, "minLength", "minLen");
		appendConstraint(constraint, node, "maxLength", "maxLen");
		appendConstraint(constraint, node, "pattern", "pattern");
		if (constraint.length() > 0) {
			builder.constraint(constraint.toString());
		}

		// 解析数组元素类型
		if (type == ParameterType.ARRAY) {
			JsonNode itemsNode = node.get("items");
			if (itemsNode != null) {
				ParameterNode itemsParameterNode = parseParameterNode("item", itemsNode, false);
				builder.items(itemsParameterNode);
			}
		}

		// 解析嵌套对象属性
		if (type == ParameterType.OBJECT) {
			JsonNode propertiesNode = node.get("properties");
			if (propertiesNode != null && propertiesNode.isObject()) {
				Set<String> nestedRequired = new HashSet<>();
				JsonNode nestedRequiredNode = node.get("required");
				if (nestedRequiredNode != null && nestedRequiredNode.isArray()) {
					for (JsonNode reqName : nestedRequiredNode) {
						nestedRequired.add(reqName.asText());
					}
				}

				List<ParameterNode> properties = new ArrayList<>();
				Iterator<Map.Entry<String, JsonNode>> fields = propertiesNode.fields();
				while (fields.hasNext()) {
					Map.Entry<String, JsonNode> field = fields.next();
					String propName = field.getKey();
					JsonNode propNode = field.getValue();
					properties.add(parseParameterNode(propName, propNode, nestedRequired.contains(propName)));
				}
				builder.properties(properties);
			}
		}

		// 解析 oneOf/anyOf
		JsonNode oneOfNode = node.get("oneOf");
		if (oneOfNode == null) {
			oneOfNode = node.get("anyOf");
		}
		if (oneOfNode != null && oneOfNode.isArray()) {
			List<ParameterNode> variants = new ArrayList<>();
			int index = 0;
			for (JsonNode variantNode : oneOfNode) {
				variants.add(parseParameterNode("variant" + index++, variantNode, false));
			}
			builder.unionVariants(variants);
		}

		return builder.build();
	}

	private static String getTextValue(JsonNode node, String fieldName) {
		JsonNode fieldNode = node.get(fieldName);
		return fieldNode != null && !fieldNode.isNull() ? fieldNode.asText() : null;
	}

	private static Object extractValue(JsonNode node) {
		if (node.isTextual()) {
			return node.asText();
		}
		else if (node.isInt()) {
			return node.asInt();
		}
		else if (node.isLong()) {
			return node.asLong();
		}
		else if (node.isDouble() || node.isFloat()) {
			return node.asDouble();
		}
		else if (node.isBoolean()) {
			return node.asBoolean();
		}
		else if (node.isNull()) {
			return null;
		}
		else {
			return node.toString();
		}
	}

	private static void appendConstraint(StringBuilder sb, JsonNode node, String fieldName, String label) {
		JsonNode fieldNode = node.get(fieldName);
		if (fieldNode != null && !fieldNode.isNull()) {
			if (sb.length() > 0) {
				sb.append(", ");
			}
			sb.append(label).append("=").append(fieldNode.asText());
		}
	}

}

