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
package com.alibaba.assistant.agent.extension.dynamic.http;

import com.alibaba.assistant.agent.common.tools.CodeactToolMetadata;
import com.alibaba.assistant.agent.extension.dynamic.tool.AbstractDynamicCodeactTool;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.chat.model.ToolContext;
import org.springframework.ai.tool.definition.ToolDefinition;
import org.springframework.lang.Nullable;

import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.HashMap;
import java.util.Map;
import java.util.StringJoiner;

/**
 * HTTP 动态 CodeAct 工具。
 *
 * <p>基于 OpenAPI 操作定义，执行 HTTP 请求。
 *
 * @author Assistant Agent Team
 * @since 1.0.0
 */
public class HttpDynamicCodeactTool extends AbstractDynamicCodeactTool {

	private static final Logger logger = LoggerFactory.getLogger(HttpDynamicCodeactTool.class);

	private final String method;

	private final String pathTemplate;

	private final String baseUrl;

	private final Map<String, String> globalHeaders;

	private final long timeoutMs;

	private final HttpClient httpClient;

	/**
	 * 构造 HTTP 动态工具。
	 *
	 * @param objectMapper ObjectMapper 实例
	 * @param toolDefinition 工具定义
	 * @param codeactMetadata CodeAct 元数据
	 * @param method HTTP 方法
	 * @param pathTemplate URL 路径模板
	 * @param baseUrl 基础 URL
	 * @param globalHeaders 全局请求头
	 * @param timeoutMs 超时时间
	 */
	public HttpDynamicCodeactTool(ObjectMapper objectMapper, ToolDefinition toolDefinition,
			CodeactToolMetadata codeactMetadata, String method, String pathTemplate,
			String baseUrl, Map<String, String> globalHeaders, long timeoutMs) {
		super(objectMapper, toolDefinition, codeactMetadata);
		this.method = method.toUpperCase();
		this.pathTemplate = pathTemplate;
		this.baseUrl = baseUrl;
		this.globalHeaders = globalHeaders != null ? new HashMap<>(globalHeaders) : new HashMap<>();
		this.timeoutMs = timeoutMs;
		this.httpClient = HttpClient.newBuilder()
				.connectTimeout(Duration.ofMillis(timeoutMs))
				.build();
	}

	@Override
	protected String doCall(Map<String, Object> args, @Nullable ToolContext toolContext) throws Exception {
		logger.info("HttpDynamicCodeactTool#doCall - reason=开始调用HTTP接口, method={}, path={}",
				method, pathTemplate);

		// 解析嵌套参数（处理 null 值）
		@SuppressWarnings("unchecked")
		Map<String, Object> pathParams = args.get("path") instanceof Map
				? (Map<String, Object>) args.get("path")
				: new HashMap<>();

		@SuppressWarnings("unchecked")
		Map<String, Object> queryParams = args.get("query") instanceof Map
				? (Map<String, Object>) args.get("query")
				: new HashMap<>();

		@SuppressWarnings("unchecked")
		Map<String, Object> headerParams = args.get("headers") instanceof Map
				? (Map<String, Object>) args.get("headers")
				: new HashMap<>();

		Object bodyParam = args.get("body");

		// 构建 URL
		String url = buildUrl(pathParams, queryParams);

		// 构建请求
		HttpRequest.Builder requestBuilder = HttpRequest.newBuilder()
				.uri(URI.create(url))
				.timeout(Duration.ofMillis(timeoutMs));

		// 添加全局请求头
		for (Map.Entry<String, String> header : globalHeaders.entrySet()) {
			requestBuilder.header(header.getKey(), header.getValue());
		}

		// 添加参数中的请求头
		for (Map.Entry<String, Object> header : headerParams.entrySet()) {
			requestBuilder.header(header.getKey(), String.valueOf(header.getValue()));
		}

		// 设置请求方法和 body
		HttpRequest request = buildRequest(requestBuilder, bodyParam);

		long startTime = System.currentTimeMillis();

		// 执行请求
		HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

		long costMs = System.currentTimeMillis() - startTime;

		int statusCode = response.statusCode();
		String responseBody = response.body();

		logger.info("HttpDynamicCodeactTool#doCall - reason=HTTP接口调用完成, status={}, costMs={}, resultLength={}",
				statusCode, costMs, responseBody != null ? responseBody.length() : 0);

		// 处理响应
		if (statusCode >= 200 && statusCode < 300) {
			return responseBody;
		}
		else {
			return buildErrorResponse("http_status=" + statusCode + ", body=" + responseBody);
		}
	}

	/**
	 * 构建完整 URL。
	 */
	private String buildUrl(Map<String, Object> pathParams, Map<String, Object> queryParams) {
		// 替换路径参数
		String path = pathTemplate;
		for (Map.Entry<String, Object> param : pathParams.entrySet()) {
			path = path.replace("{" + param.getKey() + "}", String.valueOf(param.getValue()));
		}

		// 构建完整 URL
		String url = baseUrl;
		if (!url.endsWith("/") && !path.startsWith("/")) {
			url += "/";
		}
		else if (url.endsWith("/") && path.startsWith("/")) {
			url = url.substring(0, url.length() - 1);
		}
		url += path;

		// 添加查询参数（处理 null 情况）
		if (queryParams != null && !queryParams.isEmpty()) {
			StringJoiner joiner = new StringJoiner("&");
			for (Map.Entry<String, Object> param : queryParams.entrySet()) {
				if (param.getValue() != null) {
					String encodedValue = URLEncoder.encode(String.valueOf(param.getValue()), StandardCharsets.UTF_8);
					joiner.add(param.getKey() + "=" + encodedValue);
				}
			}
			if (joiner.length() > 0) {
				url += (url.contains("?") ? "&" : "?") + joiner.toString();
			}
		}

		return url;
	}

	/**
	 * 构建 HTTP 请求。
	 */
	private HttpRequest buildRequest(HttpRequest.Builder builder, Object bodyParam) throws Exception {
		switch (method) {
			case "GET":
				return builder.GET().build();
			case "DELETE":
				return builder.DELETE().build();
			case "POST":
				return buildRequestWithBody(builder, "POST", bodyParam);
			case "PUT":
				return buildRequestWithBody(builder, "PUT", bodyParam);
			case "PATCH":
				return buildRequestWithBody(builder, "PATCH", bodyParam);
			default:
				return builder.method(method, HttpRequest.BodyPublishers.noBody()).build();
		}
	}

	/**
	 * 构建带 body 的请求。
	 */
	private HttpRequest buildRequestWithBody(HttpRequest.Builder builder, String method, Object bodyParam)
			throws Exception {
		String bodyJson;
		if (bodyParam == null) {
			bodyJson = "{}";
		}
		else if (bodyParam instanceof String) {
			bodyJson = (String) bodyParam;
		}
		else {
			bodyJson = objectMapper.writeValueAsString(bodyParam);
		}

		builder.header("Content-Type", "application/json");

		return builder.method(method, HttpRequest.BodyPublishers.ofString(bodyJson)).build();
	}

	public String getMethod() {
		return method;
	}

	public String getPathTemplate() {
		return pathTemplate;
	}

	public String getBaseUrl() {
		return baseUrl;
	}

}

