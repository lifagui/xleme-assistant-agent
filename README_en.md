# Assistant Agent

[![License](https://img.shields.io/badge/License-Apache%202.0-blue.svg)](LICENSE)
[![Java](https://img.shields.io/badge/Java-17%2B-orange.svg)](https://openjdk.org/)
[![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.4-green.svg)](https://spring.io/projects/spring-boot)
[![Spring AI](https://img.shields.io/badge/Spring%20AI-1.1.0-blueviolet.svg)](https://spring.io/projects/spring-ai)
[![GraalVM](https://img.shields.io/badge/GraalVM-Polyglot-red.svg)](https://www.graalvm.org/)

## ✨ Technical Features

- 🚀 **Code-as-Action**: Agent generates and executes code to complete tasks, rather than just calling predefined tools
- 🔒 **Secure Sandbox**: AI-generated code runs safely in GraalVM polyglot sandbox with resource isolation
- 📊 **Multi-dimensional Evaluation**: Multi-layer intent recognition through Evaluation Graph, precisely guiding Agent behavior
- 🔄 **Dynamic Prompt Builder**: Dynamically inject context (experiences, knowledge, etc.) into prompts based on scenarios and evaluation results
- 🧠 **Experience Learning**: Automatically accumulates successful experiences to continuously improve performance on subsequent tasks
- ⚡ **Fast Response**: For familiar scenarios, skip LLM reasoning process and respond quickly based on experience

## 📖 Introduction

**Assistant Agent** is an enterprise-grade intelligent assistant framework built on [Spring AI Alibaba](https://github.com/alibaba/spring-ai-alibaba), adopting the Code-as-Action paradigm to orchestrate tools and complete tasks by generating and executing code. It's an intelligent assistant solution that **understands, acts, and learns**.

### What Can Assistant Agent Do?

Assistant Agent is a fully-featured intelligent assistant with the following core capabilities:

- 🔍 **Intelligent Q&A**: Supports unified retrieval architecture across multiple data sources (extensible via SPI for knowledge base, Web, etc.), providing accurate, traceable answers
- 🛠️ **Tool Invocation**: Supports MCP, HTTP API (OpenAPI) and other protocols, flexibly access massive tools, combine multiple tools to implement complex business workflows
- ⏰ **Proactive Service**: Supports scheduled tasks, delayed execution, event callbacks, letting the assistant proactively serve you
- 📬 **Multi-channel Delivery**: Built-in IDE reply, extensible to DingTalk, Feishu, WeCom, Webhook and other channels via SPI

### Why Choose Assistant Agent?

| Value | Description |
|-------|-------------|
| **Cost Reduction** | 24/7 intelligent customer service, significantly reducing manual support costs |
| **Quick Integration** | Business platforms can integrate with simple configuration, no extensive development required |
| **Flexible Customization** | Configure knowledge base, integrate enterprise tools, build your exclusive business assistant |
| **Continuous Optimization** | Automatically learns and accumulates experience, the assistant gets smarter with use |

### Use Cases

- **Intelligent Customer Service**: Connect to enterprise knowledge base, intelligently answer user inquiries
- **Operations Assistant**: Connect to monitoring and ticketing systems, automatically handle alerts, query status, execute operations
- **Business Assistant**: Connect to CRM, ERP and other business systems, assist employees in daily work


> 💡 The above are just typical scenario examples. By configuring knowledge base and integrating tools, Assistant Agent can adapt to more business scenarios. Feel free to explore.

![QA_comparison_en.png](images/QA_comparison_en.png)
![Tool_comparison_en.png](images/Tool_comparison_en.png)


### Overall Working Principle

Below is an end-to-end flow example of how Assistant Agent processes a complete request:

![workflow_en.png](images/workflow_en.png)

### Project Structure

```
assistant-agent/
├── assistant-agent-common          # Common tools, enums, constants
├── assistant-agent-core            # Core engine: GraalVM executor, tool registry
├── assistant-agent-extensions      # Extension modules:
│   ├── dynamic/               #   - Dynamic tools (MCP, HTTP API)
│   ├── experience/            #   - Experience management and FastIntent configuration
│   ├── learning/              #   - Learning extraction and storage
│   ├── search/                #   - Unified search capability
│   ├── reply/                 #   - Multi-channel reply
│   ├── trigger/               #   - Trigger mechanism
│   └── evaluation/            #   - Evaluation integration
├── assistant-agent-prompt-builder  # Prompt dynamic assembly
├── assistant-agent-evaluation      # Evaluation engine
├── assistant-agent-autoconfigure   # Spring Boot auto-configuration
└── assistant-agent-start           # Startup module
```

## 🚀 Quick Start

### Prerequisites

- Java 17+
- Maven 3.8+
- DashScope API Key

### 1. Clone and Build

```bash
git clone https://github.com/alibaba/assistant-agent.git
cd assistant-agent
mvn clean install -DskipTests
```

### 2. Configure API Key

```bash
export DASHSCOPE_API_KEY=your-api-key-here
```

### 3. Minimal Configuration

The project has built-in default configuration, just ensure the API Key is correct. For customization, edit `assistant-agent-start/src/main/resources/application.yml`:

```yaml
spring:
  ai:
    dashscope:
      api-key: ${DASHSCOPE_API_KEY}
      chat:
        options:
          model: qwen-max
```

### 4. Start the Application

```bash
cd assistant-agent-start
mvn spring-boot:run
```

All extension modules are enabled by default with sensible configurations; no additional configuration is required for a quick start.

### 5. Configure Knowledge Base (Connect to Business Knowledge)

> 💡 The framework provides a Mock knowledge base implementation by default for demonstration and testing. **Production environments need to connect to real knowledge sources** (such as vector databases, Elasticsearch, enterprise knowledge base APIs, etc.) so that the Agent can retrieve and answer business-related questions.

#### Option 1: Quick Experience (Using Built-in Mock Implementation)

The default configuration has knowledge base search enabled, you can experience it directly:

```yaml
spring:
  ai:
    alibaba:
      codeact:
        extension:
          search:
            enabled: true
            knowledge-search-enabled: true  # Enabled by default
```

#### Option 2: Connect to Real Knowledge Base (Recommended)

Implement the `SearchProvider` SPI interface to connect to your business knowledge sources:

```java
package com.example.knowledge;

import com.alibaba.assistant.agent.extension.search.spi.SearchProvider;
import com.alibaba.assistant.agent.extension.search.model.*;
import org.springframework.stereotype.Component;
import java.util.*;

@Component  // Add this annotation, Provider will be auto-registered
public class MyKnowledgeSearchProvider implements SearchProvider {

    @Override
    public boolean supports(SearchSourceType type) {
        return SearchSourceType.KNOWLEDGE == type;
    }

    @Override
    public List<SearchResultItem> search(SearchRequest request) {
        List<SearchResultItem> results = new ArrayList<>();
        
        // 1. Query from your knowledge source (vector database, ES, API, etc.)
        // Example: List<Doc> docs = vectorStore.similaritySearch(request.getQuery());
        
        // 2. Convert to SearchResultItem
        // for (Doc doc : docs) {
        //     SearchResultItem item = new SearchResultItem();
        //     item.setId(doc.getId());
        //     item.setSourceType(SearchSourceType.KNOWLEDGE);
        //     item.setTitle(doc.getTitle());
        //     item.setSnippet(doc.getSummary());
        //     item.setContent(doc.getContent());
        //     item.setScore(doc.getScore());
        //     results.add(item);
        // }
        
        return results;
    }

    @Override
    public String getName() {
        return "MyKnowledgeSearchProvider";
    }
}
```

#### Common Knowledge Source Integration Examples

| Knowledge Source Type | Integration Method |
|----------------------|-------------------|
| **Vector Database** (Alibaba Cloud AnalyticDB, Milvus, Pinecone) | Call vector similarity search API in `search()` method |
| **Elasticsearch** | Use ES client for full-text or vector search |
| **Enterprise Knowledge Base API** | Call internal knowledge base REST API |
| **Local Documents** | Read and index local Markdown/PDF files |

> 📖 For more details, refer to: [Knowledge Search Module Documentation](assistant-agent-extensions/src/main/java/com/alibaba/assistant/agent/extension/search/README.md)

## 🧩 Core Module Introduction

### Evaluation Module

**Role**: Multi-dimensional intent recognition framework that performs multi-layer trait recognition through Evaluation Graph.

```
┌──────────────────────────────────────────────────────────────────┐
│                    Evaluation Graph Example                      │
├──────────────────────────────────────────────────────────────────┤
│                                                                  │
│  User Input: "Query today's orders"                              │
│          │                                                       │
│          ▼                                                       │
│  ┌─────────────────────────────────────────────────────────┐     │
│  │ Layer 1 (parallel execution)                            │     │
│  │   ┌────────────┐         ┌────────────┐                 │     │
│  │   │ Is Vague?  │         │   Rewrite  │                 │     │
│  │   │ clear/vague│         │ (enhance)  │                 │     │
│  │   └─────┬──────┘         └─────┬──────┘                 │     │
│  └─────────┼──────────────────────┼────────────────────────┘     │
│            │                      │                              │
│            └──────────┬───────────┘                              │
│                       ▼                                          │
│  ┌─────────────────────────────────────────────────────────┐     │
│  │ Layer 2 (based on rewritten content, parallel)          │     │
│  │   ┌──────────┐   ┌───────────┐   ┌───────────┐          │     │
│  │   │Experience│   │ Tool      │   │ Knowledge │          │     │
│  │   │available │   │ Available │   │ Available │          │     │
│  │   │ yes/no   │   │  yes/no   │   │  yes/no   │          │     │ 
│  │   └──────────┘   └───────────┘   └───────────┘          │     │
│  └─────────────────────────────────────────────────────────┘     │
│                       │                                          │
│                       ▼                                          │
│            ┌────────────────────┐                                │
│            │ Integrate evaluation│                               │
│            │ results from        │                               │
│            │ different dimensions│                               │
│            │ → Pass to modules   │                               │
│            └────────────────────┘                                │
│                                                                  │
└──────────────────────────────────────────────────────────────────┘
```

**Core Capabilities**:
- **Dual Evaluation Engines**:
  - **LLM Evaluation**: Complex semantic judgment through large models. Users can fully customize evaluation prompts (`customPrompt`), or use default prompt assembly (supports `description`, `workingMechanism`, `fewShots` configurations)
  - **Rule-based Evaluation**: Implement rule logic through Java functions. Users can customize `Function<CriterionExecutionContext, CriterionResult>` to execute any rule judgment, suitable for threshold detection, format validation, exact matching, etc.
- **Custom Dependencies**: Evaluation items can declare dependencies via `dependsOn`. The system automatically builds an evaluation graph for topological execution - items without dependencies run in parallel, items with dependencies run sequentially. Subsequent evaluation items can access results from preceding items.
- **Evaluation Results**: Support `BOOLEAN`, `ENUM`, `SCORE`, `JSON`, `TEXT` and other types, passed to Prompt Builder to drive dynamic assembly

---

### Prompt Builder Module

**Role**: Dynamically assemble prompts sent to the model based on evaluation results and runtime context. Example:

```
┌─────────────────────────────────────────────────────────────────────────┐
│                   Prompt Builder - Conditional Dynamic Generation       │
├─────────────────────────────────────────────────────────────────────────┤
│                                                                         │
│  Evaluation Results Input:                                              │
│  ┌──────────────────────────────────────────────────────────┐           │
│  │ Vague: yes │ Experience: yes │ Tools: yes │ Knowledge: no│           │
│  └──────────────────────────────────────────────────────────┘           │
│                    │                                                    │
│                    ▼                                                    │
│  ┌─────────────────────────────────────────────────────────────────┐    │
│  │              Custom PromptBuilder Condition Matching            │    │
│  │                                                                 │    │
│  │   vague=yes ────▶ inject [Clarification Prompt]                 │    │
│  │   vague=no  ────▶ inject [Direct Execution Prompt]              │    │
│  │                                                                 │    │
│  │   experience=yes ──▶ inject [Historical Experience Reference]   │    │
│  │   tools=yes     ──▶ inject [Tool Usage Instructions]            │    │
│  │   knowledge=yes ──▶ inject [Relevant Knowledge Snippets]        │    │
│  │                                                                 │    │
│  │   Combo 1: vague + no tools + no knowledge ──▶ [Ask User Prompt]│    │
│  │   Combo 2: clear + tools + experience ──▶ [Fast Execute Prompt] │    │
│  └─────────────────────────────────────────────────────────────────┘    │
│                    │                                                    │
│                    ▼                                                    │
│  ┌─────────────────────────────────────────────────────────────────┐    │
│  │ Final Dynamic Prompt:                                           │    │
│  │ [System Prompt] + [Clarification Guide] + [Experience] +        │    │
│  │ [Tool Instructions] + [User Query]                              │    │
│  └─────────────────────────────────────────────────────────────────┘    │
│                    │                                                    │
│                    ▼                                                    │
│              ┌──────────┐                                               │
│              │  LLM     │                                               │
│              └──────────┘                                               │
│                                                                         │
└─────────────────────────────────────────────────────────────────────────┘
```

**Core Capabilities**:
- Multiple PromptBuilders execute in priority order
- Each Builder decides whether to contribute and what content based on evaluation results
- Support custom Builders for business-specific prompt logic
- Non-intrusive, intercepts at model invocation level

**Comparison with Traditional Approaches**:

| Comparison | Traditional Approach | Evaluation + PromptBuilder |
|------------|---------------------|---------------------------|
| **Prompt Length** | Need to enumerate handling instructions for various situations ("when encountering situation A..., when encountering situation B..."), prompts become bloated | Through pre-evaluation to identify scenarios, only inject context needed for current scenario, prompts are shorter and more precise |
| **Agent Behavior Controllability** | Relies on model's "understanding" of lengthy instructions, prone to misjudgment | Behavior driven by evaluation results, reducing model misjudgment, more controllable |
| **Extension Flexibility** | Adding new scenarios requires modifying prompts, difficult to maintain | Modify relevant evaluation items and PromptBuilder based on business needs |
| **Code Architecture** | Evaluation logic coupled with prompts | Evaluation logic decoupled from prompt templates, separation of concerns, independent maintenance and iteration |

---

### Learning Module

**Role**: Automatically extract and save valuable experiences from Agent execution history.

```
┌─────────────────────────────────────────────────────────────────────────┐
│                         Learning Module Workflow                        │
├─────────────────────────────────────────────────────────────────────────┤
│                                                                         │
│  ┌───────────────────────────────────────────────────────────────────┐  │
│  │                      Agent Execution Process                      │  │
│  │                                                                   │  │
│  │  Input ──▶ Reasoning ──▶ Code Gen ──▶ Execute ──▶ Output          │  │
│  │   │           │            │           │          │               │  │
│  │   └───────────┴────────────┴───────────┴──────────┘               │  │
│  │                            │                                      │  │
│  └────────────────────────────┼──────────────────────────────────────┘  │
│                               ▼                                         │
│              ┌──────────────────────────────┐                           │
│              │   Learning Context Capture   │                           │
│              │  - User Input                │                           │
│              │  - Reasoning Steps           │                           │
│              │  - Generated Code            │                           │
│              │  - Execution Result          │                           │
│              └───────────┬──────────────────┘                           │
│                          │                                              │
│                          ▼                                              │
│   ┌──────────────────────────────────────────────────────────────┐      │
│   │              Learning Extractors Analysis                    │      │
│   │                                                              │      │
│   │  ┌────────────┐  ┌────────────┐  ┌────────────┐              │      │
│   │  │ Experience │  │  Pattern   │  │   Error    │              │      │
│   │  │ Extractor  │  │ Extractor  │  │ Extractor  │              │      │
│   │  │Success Mode│  │Common Mode │  │Failure Mode│              │      │
│   │  └─────┬──────┘  └─────┬──────┘  └─────┬──────┘              │      │
│   └────────┼───────────────┼───────────────┼─────────────────────┘      │
│            │               │               │                            │
│            └───────────────┼───────────────┘                            │
│                            ▼                                            │
│                   ┌────────────────┐                                    │
│                   │   Persist &    │ ──▶ Available for future tasks     │
│                   │     Store      │                                    │
│                   └────────────────┘                                    │
│                                                                         │
└─────────────────────────────────────────────────────────────────────────┘
```

**Core Capabilities**:
- **After-Agent Learning**: Extract experiences after each Agent execution
- **After-Model Learning**: Extract experiences after each model call
- **Tool Interceptor**: Extract experiences from tool invocations
- **Offline Learning**: Batch analyze historical data to extract patterns
- **Learning Process**: Capture execution context → Extractor analysis and recognition → Generate experience records → Persist for subsequent reuse

---

### Experience Module

**Role**: Accumulate and reuse historical successful execution experiences.

```
┌─────────────────────────────────────────────────────────────────────────┐
│                       Experience Module Workflow                        │
├─────────────────────────────────────────────────────────────────────────┤
│                                                                         │
│  [Scenario 1: Experience Accumulation]                                  │
│                                                                         │
│   User: "Query order status"  ──▶  Agent Success  ──▶  ┌─────────────┐  │
│                                                        │ Save:       │  │
│                                                        │ - ReAct Exp │  │
│                                                        │ - Code Exp  │  │
│                                                        │ - Common Exp│  │
│                                                        └─────────────┘  │
│                                                              │          │
│                                                              ▼          │
│                                                     ┌────────────────┐  │
│                                                     │ Experience DB  │  │
│                                                     └────────────────┘  │
│                                                                         │
│  [Scenario 2: Experience Reuse]                              │          │
│                                                              │          │
│   User: "Query my order status" ◀── Match Similar ◀──────────┘          │
│            │                                                            │
│            ▼                                                            │
│   ┌─────────────────────────────────────────────────┐                   │
│   │ Agent references history, faster decision +     │                   │
│   │ generates correct code                          │                   │
│   └─────────────────────────────────────────────────┘                   │
│                                                                         │
│  [Scenario 3: FastIntent Quick Response]                                │
│                                                                         │
│   ┌─────────────────────────────────────────────────────────────────┐   │
│   │ Experience DB                                                   │   │
│   │   ┌─────────────────────┐       ┌────────────────────────────┐  │   │
│   │   │ Experience A        │       │ Experience B               │  │   │
│   │   │ (Normal)            │       │ (✓ FastIntent configured)  │  │   │
│   │   │ No FastIntent config│       │ Condition: prefix "View    │  │   │
│   │   │ → Inject to prompt  │       │             *sales"        │  │   │
│   │   │   for LLM reference │       │   Action: Call sales API   │  │   │
│   │   └─────────────────────┘       └───────────┬────────────────┘  │   │
│   └─────────────────────────────────────────────┼───────────────────┘   │
│                                                 │ Condition matched     │
│                                                 ▼                       │
│   User: "View today's sales" ──▶ Match Exp B ──▶ Skip LLM, execute      │
│                                   FastIntent     directly               │
│                                                                         │
└─────────────────────────────────────────────────────────────────────────┘
```

**Core Capabilities**:
- **Multiple Experience Types**: Code generation experience, ReAct decision experience, common sense experience, providing historical reference for similar tasks
- **Flexible Reuse**: Experiences can be injected into prompts or used for FastIntent matching
- **Lifecycle Management**: Support experience creation, update, and deletion
- **FastIntent Quick Response**:
  - Experience must explicitly configure `fastIntentConfig` to enable
  - When matching configured conditions, skip full LLM reasoning and directly execute pre-recorded tool calls or code
- Support multi-condition matching: message prefix, regex, metadata, state, etc.

---

### Trigger Module

**Role**: Create and manage scheduled tasks or event-triggered Agent executions.

```
┌─────────────────────────────────────────────────────────────────────────┐
│                        Trigger Module Capabilities                      │
├─────────────────────────────────────────────────────────────────────────┤
│                                                                         │
│  [Scheduled Trigger]                                                    │
│                                                                         │
│   User: "Send me daily sales report at 9am"                             │
│            │                                                            │
│            ▼                                                            │
│   ┌─────────────────┐     ┌─────────────────┐     ┌─────────────────┐   │
│   │  Agent creates  │     │   Scheduler     │     │  Auto Execute   │   │
│   │  Cron trigger   │────▶│  0 9 * * *      │────▶│  Generate report│   │
│   │  (self-schedule)│     │                 │     │  Send notify    │   │
│   └─────────────────┘     └─────────────────┘     └─────────────────┘   │
│                                                                         │
│  [Delayed Trigge]                                                       │
│                                                                         │
│   User: "Remind me about the meeting in 30 minutes"                     │
│            │                                                            │
│            ▼                                                            │
│   ┌──────────────────┐     ┌─────────────────┐     ┌─────────────────┐  │
│   │  Agent creates   │     │   After 30min   │     │  Send reminder  │  │
│   │  one-time trigger│────▶│   fire          │────▶│  "Time to meet" │  │
│   └──────────────────┘     └─────────────────┘     └─────────────────┘  │
│                                                                         │
│  [Callback Trigger]                                                     │
│                                                                         │
│   User: "Help me with xx when xx condition is met"                      │
│                                                                         │
│   External System: Send event to Webhook                                │
│            │                                                            │
│            ▼                                                            │
│   ┌─────────────────┐     ┌─────────────────┐     ┌─────────────────┐   │
│   │  Receive        │     │  Trigger Agent  │     │  Process event  │   │
│   │  Webhook event  │────▶│  execute task   │────▶│  Return response│   │
│   └─────────────────┘     └─────────────────┘     └─────────────────┘   │
│                                                                         │
└─────────────────────────────────────────────────────────────────────────┘
```

**Core Capabilities**:
- `TIME_CRON` Trigger: Support Cron expression for scheduled task triggers
- `TIME_ONCE` Trigger: Support one-time delayed trigger
- `CALLBACK` Trigger: Support callback event trigger
- Agent can autonomously create triggers through tools, achieving "self-scheduling"

---

### Reply Channel Module

**Role**: Provide flexible message reply capability, supporting multiple output channels.

```
┌─────────────────────────────────────────────────────────────────────────┐
│                     Reply Channel Module Capabilities                   │
├─────────────────────────────────────────────────────────────────────────┤
│                                                                         │
│   Agent needs to reply to user                                          │
│            │                                                            │
│            ▼                                                            │
│   ┌─────────────────────────────────────────────────────────────────┐   │
│   │                    Channel Router                               │   │
│   └─────────────────────────────────────────────────────────────────┘   │
│            │                                                            │
│            ├──────────────┬──────────────┬──────────────┐               │
│            ▼              ▼              ▼              ▼               │
│   ┌────────────┐  ┌─────────────┐  ┌─────────────┐  ┌────────────┐      │
│   │ DEFAULT    │  │  IDE_CARD   │  │ IM_NOTIFY   │  │  WEBHOOK   │      │
│   │ Text Reply │  │ Card Display│  │ Push Notify │  │ JSON Push  │      │
│   └─────┬──────┘  └─────┬───────┘  └─────┬───────┘  └─────┬──────┘      │
│         │               │                │                │             │
│         ▼               ▼                ▼                ▼             │
│   ┌──────────┐    ┌───────────┐    ┌────────────┐    ┌──────────┐       │
│   │ Console  │    │   IDE     │    │   IM       │    │ External │       │
│   │ Terminal │    │ Rich Card │    │(Extendable)│    │  System  │       │
│   └──────────┘    └───────────┘    └────────────┘    └──────────┘       │
│                                                                         │
│  [Usage Example]                                                        │
│                                                                         │
│   User: "Send results after analysis"                                   │
│            │                                                            │
│            ▼                                                            │
│   Agent: send_message(text="Analysis results...")                       │
│            │                                                            │
│            ▼                                                            │
│   User receives: "📊 Analysis Results: ..."                             │
│                                                                         │
└─────────────────────────────────────────────────────────────────────────┘
```

**Core Capabilities**:
- **Multi-channel Routing**: Agent can choose different channels to reply based on scenario
- **Configuration-driven**: Dynamically generate reply tools, no coding required
- **Sync/Async Support**: Support both synchronous and asynchronous reply modes
- **Unified Interface**: Shield underlying implementation differences
- **Built-in Demo Channel**: `IDE_TEXT` (for demonstration)
- **Extendable Channels** (by implementing `ReplyChannelDefinition` SPI): e.g. `IDE_CARD`, `IM_NOTIFICATION` (DingTalk/Feishu/WeCom), `WEBHOOK_JSON`, etc. - requires custom implementation

---

### Dynamic Tools Module

**Role**: Provide highly extensible tool system, enabling Agent to call various external tools to complete tasks.

```
┌─────────────────────────────────────────────────────────────────────────┐
│                        Tool Extension Architecture                      │
├─────────────────────────────────────────────────────────────────────────┤
│                                                                         │
│   Agent needs to execute operation                                      │
│            │                                                            │
│            ▼                                                            │
│   ┌──────────────────────────────────────────────────────────────────┐  │
│   │                   CodeactTool System                             │  │
│   └──────────────────────────────────────────────────────────────────┘  │
│            │                                                            │
│            ├─────────────┬─────────────┬─────────────┬──────────────┐   │
│            ▼             ▼             ▼             ▼              ▼   │
│   ┌────────────┐ ┌────────────┐ ┌────────────┐ ┌────────────┐ ┌──────┐  │
│   │   MCP      │ │   HTTP     │ │  Search    │ │  Trigger   │ │Custom│  │
│   │   Tools    │ │   API      │ │  Tools     │ │  Tools     │ │Tools │  │
│   │            │ │   Tools    │ │            │ │            │ │      │  │
│   └─────┬──────┘ └─────┬──────┘ └─────┬──────┘ └─────┬──────┘ └──┬───┘  │
│         │              │              │              │           │      │
│         ▼              ▼              ▼              ▼           ▼      │
│   ┌──────────┐   ┌──────────┐   ┌───────────┐   ┌──────────┐  ┌─────┐   │
│   │ Any MCP  │   │ REST API │   │ Knowledge │   │ Scheduled│  │ ... │   │
│   │ Server   │   │ OpenAPI  │   │ Search    │   │ Tasks    │  │     │   │
│   └──────────┘   └──────────┘   │ Project   │   │ Callbacks│  └─────┘   │
│                                 │ Search    │   └──────────┘            │
│                                 └───────────┘                           │
│                                                                         │
└─────────────────────────────────────────────────────────────────────────┘
```

**Core Capabilities**:
- **MCP Tool Support**: One-click integration with any MCP Server, reuse MCP tool ecosystem
- **HTTP API Support**: Integrate REST APIs through OpenAPI specification, call existing enterprise interfaces
- **Built-in Tool Types**: Search, Reply, Trigger, Learning, etc.
- **Custom Tool SPI**: Implement `CodeactTool` interface to easily extend new tools

---

### Knowledge Search Module

**Role**: Multi-source unified search engine, providing knowledge support for Agent Q&A and decision-making.

```
┌─────────────────────────────────────────────────────────────────────────┐
│                    Multi-Source Search Architecture                     │
├─────────────────────────────────────────────────────────────────────────┤
│                                                                         │
│  User Question: "How to configure database connection pool?"            │
│            │                                                            │
│            ▼                                                            │
│  ┌─────────────────────────────────────────────────────────────────┐    │
│  │                 Unified Search Interface                        │    │
│  └─────────────────────────────────────────────────────────────────┘    │
│            │                                                            │
│            ├────────────────┬────────────────┬────────────────┐         │
│            ▼                ▼                ▼                ▼         │
│   ┌──────────────┐  ┌──────────────┐  ┌──────────────┐  ┌────────┐      │
│   │  Knowledge   │  │   Project    │  │     Web      │  │ Custom │      │
│   │   Provider   │  │   Provider   │  │   Provider   │  │Provider│      │
│   │  (Primary)   │  │  (Optional)  │  │  (Optional)  │  │ (SPI)  │      │
│   └──────┬───────┘  └──────┬───────┘  └──────┬───────┘  └───┬────┘      │
│          │                 │                 │              │           │
│          ▼                 ▼                 ▼              ▼           │
│   ┌──────────────┐  ┌──────────────┐  ┌──────────────┐  ┌────────┐      │
│   │ FAQ / Docs   │  │ Source Code  │  │ Web Articles │  │  ...   │      │
│   │ Q&A History  │  │ Config Files │  │ Tech Forums  │  │        │      │
│   │ Team Notes   │  │ Logs         │  │              │  │        │      │
│   └──────────────┘  └──────────────┘  └──────────────┘  └────────┘      │
│          │                 │                 │              │           │
│          └─────────────────┴─────────────────┴──────────────┘           │
│                            │                                            │
│                            ▼                                            │
│               ┌────────────────────────┐                                │
│               │ Aggregate & Rank       │                                │
│               │ → Inject into Prompt   │                                │
│               └────────────────────────┘                                │
│                                                                         │
└─────────────────────────────────────────────────────────────────────────┘
```

**Core Capabilities**:
- **Unified Search Interface**: `SearchProvider` SPI, supports pluggable data sources
- **Demo Providers**: Built-in Mock implementations for Knowledge, Project, Web (for demonstration and testing only)
- **Custom Extension**: Implement `SearchProvider` interface to connect any data source (databases, vector stores, APIs)
- **Result Aggregation**: Support configurable ranking strategies
- **Business Value**: Connect enterprise knowledge base to provide accurate answers, support answer traceability, reduce manual customer service pressure

**Configuration Example**:

```yaml
spring:
  ai:
    alibaba:
      codeact:
        extension:
          search:
            enabled: true
            knowledge-search-enabled: true   # Knowledge base (Mock implementation by default)
            project-search-enabled: false    # Project code (Mock implementation by default)
            web-search-enabled: false        # Web search (Mock implementation by default)
            default-top-k: 5
            search-timeout-ms: 5000
```

> 💡 The above search features provide Mock implementations by default for demonstration and testing. For production use, implement `SearchProvider` SPI to connect actual data sources.

---

## 📚 Reference Documentation

- [Full Configuration Reference](assistant-agent-start/src/main/resources/application-reference.yml)
- [Spring AI Alibaba Documentation](https://github.com/alibaba/spring-ai-alibaba)

## 🤝 Contributing

We welcome contributions! Please see [CONTRIBUTING.md](CONTRIBUTING.md) for guidelines.

## 📄 License

This project is licensed under the Apache License 2.0 - see the [LICENSE](LICENSE) file for details.

## 🙏 Acknowledgments

- [Spring AI](https://github.com/spring-projects/spring-ai)
- [Spring AI Alibaba](https://github.com/alibaba/spring-ai-alibaba)
- [GraalVM](https://www.graalvm.org/)
