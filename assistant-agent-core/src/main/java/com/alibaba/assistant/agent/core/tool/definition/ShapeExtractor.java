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

import com.alibaba.assistant.agent.common.tools.definition.ArrayShapeNode;
import com.alibaba.assistant.agent.common.tools.definition.ObjectShapeNode;
import com.alibaba.assistant.agent.common.tools.definition.PrimitiveShapeNode;
import com.alibaba.assistant.agent.common.tools.definition.PrimitiveType;
import com.alibaba.assistant.agent.common.tools.definition.ShapeNode;
import com.alibaba.assistant.agent.common.tools.definition.UnknownShapeNode;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Iterator;
import java.util.Map;

/**
 * Shape 提取器 - 从 JSON 字符串提取 ShapeNode。
 *
 * @author Assistant Agent Team
 * @since 1.0.0
 */
public class ShapeExtractor {

	private static final Logger logger = LoggerFactory.getLogger(ShapeExtractor.class);

	private static final ObjectMapper objectMapper = new ObjectMapper();

	private ShapeExtractor() {
		// 工具类，禁止实例化
	}

	/**
	 * 从 JSON 字符串提取 ShapeNode。
	 * @param json JSON 字符串
	 * @return 提取的 ShapeNode
	 */
	public static ShapeNode extract(String json) {
		if (json == null || json.isBlank()) {
			logger.debug("ShapeExtractor#extract - reason=JSON为空，返回UnknownShapeNode");
			return new UnknownShapeNode();
		}

		try {
			JsonNode rootNode = objectMapper.readTree(json);
			return extractFromJsonNode(rootNode);
		}
		catch (Exception e) {
			logger.warn("ShapeExtractor#extract - reason=解析JSON失败，返回UnknownShapeNode, error={}", e.getMessage());
			return new UnknownShapeNode();
		}
	}

	/**
	 * 从 JsonNode 提取 ShapeNode。
	 * @param node JSON 节点
	 * @return 提取的 ShapeNode
	 */
	public static ShapeNode extractFromJsonNode(JsonNode node) {
		if (node == null || node.isNull()) {
			return new PrimitiveShapeNode(PrimitiveType.NULL);
		}

		if (node.isTextual()) {
			return new PrimitiveShapeNode(PrimitiveType.STRING);
		}

		if (node.isInt() || node.isLong()) {
			return new PrimitiveShapeNode(PrimitiveType.INTEGER);
		}

		if (node.isDouble() || node.isFloat() || node.isNumber()) {
			return new PrimitiveShapeNode(PrimitiveType.NUMBER);
		}

		if (node.isBoolean()) {
			return new PrimitiveShapeNode(PrimitiveType.BOOLEAN);
		}

		if (node.isArray()) {
			return extractArrayShape(node);
		}

		if (node.isObject()) {
			return extractObjectShape(node);
		}

		return new UnknownShapeNode();
	}

	/**
	 * 从数组节点提取 ArrayShapeNode。
	 * @param arrayNode 数组节点
	 * @return ArrayShapeNode
	 */
	private static ArrayShapeNode extractArrayShape(JsonNode arrayNode) {
		if (arrayNode.isEmpty()) {
			// 空数组，元素类型未知
			return new ArrayShapeNode(new UnknownShapeNode());
		}

		// 从第一个元素推断元素类型
		JsonNode firstElement = arrayNode.get(0);
		ShapeNode itemShape = extractFromJsonNode(firstElement);

		// 如果有多个元素，尝试合并它们的 shape
		if (arrayNode.size() > 1) {
			for (int i = 1; i < arrayNode.size(); i++) {
				ShapeNode otherShape = extractFromJsonNode(arrayNode.get(i));
				itemShape = ReturnSchemaMerger.mergeShapes(itemShape, otherShape);
			}
		}

		return new ArrayShapeNode(itemShape);
	}

	/**
	 * 从对象节点提取 ObjectShapeNode。
	 * @param objectNode 对象节点
	 * @return ObjectShapeNode
	 */
	private static ObjectShapeNode extractObjectShape(JsonNode objectNode) {
		ObjectShapeNode shapeNode = new ObjectShapeNode();

		Iterator<Map.Entry<String, JsonNode>> fields = objectNode.fields();
		while (fields.hasNext()) {
			Map.Entry<String, JsonNode> field = fields.next();
			String fieldName = field.getKey();
			JsonNode fieldValue = field.getValue();

			ShapeNode fieldShape = extractFromJsonNode(fieldValue);
			shapeNode.putField(fieldName, fieldShape);
		}

		return shapeNode;
	}

}

