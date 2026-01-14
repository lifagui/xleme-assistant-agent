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
package com.alibaba.assistant.agent.autoconfigure.subagent;

import com.alibaba.assistant.agent.common.enums.Language;
import com.alibaba.assistant.agent.common.tools.CodeactTool;
import com.alibaba.assistant.agent.core.tool.schema.ReturnSchemaRegistry;
import com.alibaba.assistant.agent.autoconfigure.subagent.node.CodeGeneratorNode;
import com.alibaba.cloud.ai.graph.StateGraph;
import com.alibaba.cloud.ai.graph.agent.hook.Hook;
import com.alibaba.cloud.ai.graph.agent.interceptor.ModelInterceptor;
import com.alibaba.cloud.ai.graph.exception.GraphStateException;
import com.alibaba.cloud.ai.graph.internal.node.Node;
import org.springframework.ai.chat.model.ChatModel;

import java.util.List;

/**
 * CodeGeneratorSubAgent - 代码生成子Agent（参考ReactAgent，但基于BaseAgent）
 *
 * <p>实现代码生成的业务逻辑，图结构为：START -> code_gen -> END
 * <p>不需要多轮推理，只负责一次性代码生成
 *
 * @author Assistant Agent Team
 * @since 1.0.0
 */
public class CodeGeneratorSubAgent extends CodeactSubAgent {

	private final CodeGeneratorNode codeGenNode;

	public CodeGeneratorSubAgent(CodeGeneratorNode codeGenNode, Builder builder) {
		super(builder.name,
			  builder.description,
			  builder.hooks,
			  builder.modelInterceptors,
			  "generated_code");

		this.codeGenNode = codeGenNode;

		// 设置拦截器到节点（参考ReactAgent的做法）
		if (this.modelInterceptors != null && !this.modelInterceptors.isEmpty()) {
			this.codeGenNode.setModelInterceptors(this.modelInterceptors);
		}
	}

	@Override
	protected StateGraph initGraph() throws GraphStateException {
		StateGraph graph = new StateGraph();

		// 添加代码生成节点 (wrap NodeActionWithConfig as AsyncNodeActionWithConfig)
		graph.addNode("code_gen", com.alibaba.cloud.ai.graph.action.AsyncNodeActionWithConfig.node_async(codeGenNode));

		// 简单的线性流程：START -> code_gen -> END
		graph.addEdge(StateGraph.START, "code_gen");
		graph.addEdge("code_gen", StateGraph.END);

		logger.info("CodeGeneratorSubAgent#initGraph 图结构初始化完成");
		return graph;
	}

    @Override
    public Node asNode(boolean includeContents, boolean returnReasoningContents) {
        throw new UnsupportedOperationException("CodeGeneratorSubAgent does not support asNode()");
    }

    /**
	 * Builder for CodeGeneratorSubAgent（参考ReactAgent.Builder）
	 */
	public static class Builder {
		private String name = "code-generator";
		private String description = "Generate function code based on requirements";
		private ChatModel chatModel;
		private Language language = Language.PYTHON;
		private List<CodeactTool> codeactTools;
		private List<? extends Hook> hooks;
		private List<ModelInterceptor> modelInterceptors;
		private boolean isCondition = false;
		private String customSystemPrompt;
		private ReturnSchemaRegistry returnSchemaRegistry;

		public Builder name(String name) {
			this.name = name;
			return this;
		}

		public Builder description(String description) {
			this.description = description;
			return this;
		}

		public Builder chatModel(ChatModel chatModel) {
			this.chatModel = chatModel;
			return this;
		}

		public Builder language(Language language) {
			this.language = language;
			return this;
		}

		public Builder codeactTools(List<CodeactTool> codeactTools) {
			this.codeactTools = codeactTools;
			return this;
		}

		public Builder hooks(List<? extends Hook> hooks) {
			this.hooks = hooks;
			return this;
		}

		public Builder modelInterceptors(List<ModelInterceptor> modelInterceptors) {
			this.modelInterceptors = modelInterceptors;
			return this;
		}

		public Builder isCondition(boolean isCondition) {
			this.isCondition = isCondition;
			return this;
		}

		public Builder returnSchemaRegistry(ReturnSchemaRegistry returnSchemaRegistry) {
			this.returnSchemaRegistry = returnSchemaRegistry;
			return this;
		}

		public Builder customSystemPrompt(String customSystemPrompt) {
			this.customSystemPrompt = customSystemPrompt;
			return this;
		}

		public CodeGeneratorSubAgent build() {
			if (chatModel == null) {
				throw new IllegalArgumentException("ChatModel is required");
			}

			// 创建CodeGeneratorNode，传入 returnSchemaRegistry
			CodeGeneratorNode codeGenNode = new CodeGeneratorNode(
					chatModel,
					language,
					codeactTools,
					modelInterceptors,
					"generated_code",
					isCondition,
					customSystemPrompt,
					returnSchemaRegistry
			);

			return new CodeGeneratorSubAgent(codeGenNode, this);
		}
	}

	/**
	 * Create a builder
	 */
	public static Builder builder() {
		return new Builder();
	}
}

