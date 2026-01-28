-- ============================================================================
-- Assistant Agent 数据库初始化脚本
-- 版本: V1
-- 描述: 创建会话、消息、提醒、触发器相关表结构
-- ============================================================================

-- ============================================================================
-- 公共函数：自动更新 updated_at 字段
-- ============================================================================
CREATE OR REPLACE FUNCTION update_updated_at_column()
RETURNS TRIGGER AS $$
BEGIN
    NEW.updated_at = CURRENT_TIMESTAMP;
    RETURN NEW;
END;
$$ language 'plpgsql';

-- ============================================================================
-- 会话表
-- ============================================================================
CREATE TABLE IF NOT EXISTS conversation (
    id              VARCHAR(36) PRIMARY KEY,
    tenant_id       VARCHAR(64) NOT NULL,          -- 租户ID，区分不同业务方
    user_id         VARCHAR(64) NOT NULL,
    thread_id       VARCHAR(36) NOT NULL,          -- Agent 线程ID
    title           VARCHAR(255),
    status          VARCHAR(20) DEFAULT 'ACTIVE',  -- ACTIVE/ARCHIVED/DELETED
    created_at      TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at      TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

COMMENT ON TABLE conversation IS '会话表';
COMMENT ON COLUMN conversation.tenant_id IS '租户ID，区分不同业务方';
COMMENT ON COLUMN conversation.user_id IS '用户ID';
COMMENT ON COLUMN conversation.thread_id IS 'Agent线程ID，用于多轮对话上下文保持';
COMMENT ON COLUMN conversation.title IS '会话标题';
COMMENT ON COLUMN conversation.status IS '状态：ACTIVE/ARCHIVED/DELETED';

-- 索引：tenant_id 在前，便于数据隔离和未来分库分表
CREATE INDEX IF NOT EXISTS idx_conversation_tenant_user ON conversation(tenant_id, user_id);
CREATE INDEX IF NOT EXISTS idx_conversation_tenant_user_updated ON conversation(tenant_id, user_id, updated_at DESC);

-- 自动更新 updated_at 触发器
DROP TRIGGER IF EXISTS update_conversation_updated_at ON conversation;
CREATE TRIGGER update_conversation_updated_at
    BEFORE UPDATE ON conversation
    FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();

-- ============================================================================
-- 消息表
-- ============================================================================
CREATE TABLE IF NOT EXISTS message (
    id              VARCHAR(36) PRIMARY KEY,
    tenant_id       VARCHAR(64) NOT NULL,          -- 租户ID
    conversation_id VARCHAR(36) NOT NULL,          -- 逻辑关联 conversation.id
    role            VARCHAR(20) NOT NULL,          -- USER/ASSISTANT/SYSTEM
    content_type    VARCHAR(20) DEFAULT 'TEXT',    -- TEXT/JSON
    content         TEXT NOT NULL,
    metadata        JSONB,
    created_at      TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at      TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

COMMENT ON TABLE message IS '消息表';
COMMENT ON COLUMN message.tenant_id IS '租户ID';
COMMENT ON COLUMN message.conversation_id IS '会话ID，逻辑关联 conversation.id';
COMMENT ON COLUMN message.role IS '角色：USER/ASSISTANT/SYSTEM';
COMMENT ON COLUMN message.content_type IS '内容类型：TEXT/JSON';
COMMENT ON COLUMN message.content IS '消息内容';
COMMENT ON COLUMN message.metadata IS '元数据（JSONB）';

-- 索引：按业务方+会话ID查询消息列表
CREATE INDEX IF NOT EXISTS idx_message_tenant_conversation ON message(tenant_id, conversation_id);
CREATE INDEX IF NOT EXISTS idx_message_tenant_conversation_created ON message(tenant_id, conversation_id, created_at);

-- 自动更新 updated_at 触发器
DROP TRIGGER IF EXISTS update_message_updated_at ON message;
CREATE TRIGGER update_message_updated_at
    BEFORE UPDATE ON message
    FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();

-- ============================================================================
-- 提醒表
-- ============================================================================
CREATE TABLE IF NOT EXISTS reminder (
    id              VARCHAR(36) PRIMARY KEY,
    tenant_id       VARCHAR(64) NOT NULL,          -- 租户ID
    user_id         VARCHAR(64) NOT NULL,          -- 创建者用户ID
    target_user_id  VARCHAR(64) NOT NULL,          -- 目标用户ID（支持给别人设置提醒）
    trigger_id      VARCHAR(64),                   -- 逻辑关联 trigger_definition.trigger_id
    type            VARCHAR(30) NOT NULL,          -- DRINK_WATER/MEDICINE/SEDENTARY/MEAL/SLEEP/CUSTOM/RELAY
    content         JSONB NOT NULL,                -- 提醒内容及上下文信息
    status          VARCHAR(20) DEFAULT 'ACTIVE',  -- ACTIVE/CANCELLED/DELETED
    created_at      TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at      TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

COMMENT ON TABLE reminder IS '提醒表';
COMMENT ON COLUMN reminder.tenant_id IS '租户ID';
COMMENT ON COLUMN reminder.user_id IS '创建者用户ID';
COMMENT ON COLUMN reminder.target_user_id IS '目标用户ID（支持传话筒功能）';
COMMENT ON COLUMN reminder.trigger_id IS '触发器ID，逻辑关联 trigger_definition';
COMMENT ON COLUMN reminder.type IS '提醒类型：DRINK_WATER/MEDICINE/SEDENTARY/MEAL/SLEEP/CUSTOM/RELAY';
COMMENT ON COLUMN reminder.content IS '提醒内容及上下文信息（JSONB）';
COMMENT ON COLUMN reminder.status IS '状态：ACTIVE/CANCELLED/DELETED';

-- 索引：所有查询都带 tenant_id 条件
CREATE INDEX IF NOT EXISTS idx_reminder_tenant_user ON reminder(tenant_id, user_id);
CREATE INDEX IF NOT EXISTS idx_reminder_tenant_target_user ON reminder(tenant_id, target_user_id);
CREATE INDEX IF NOT EXISTS idx_reminder_tenant_trigger ON reminder(tenant_id, trigger_id);
CREATE INDEX IF NOT EXISTS idx_reminder_tenant_user_status ON reminder(tenant_id, user_id, status);
CREATE INDEX IF NOT EXISTS idx_reminder_tenant_target_status ON reminder(tenant_id, target_user_id, status);

-- 自动更新 updated_at 触发器
DROP TRIGGER IF EXISTS update_reminder_updated_at ON reminder;
CREATE TRIGGER update_reminder_updated_at
    BEFORE UPDATE ON reminder
    FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();

-- ============================================================================
-- 提醒执行记录表
-- ============================================================================
CREATE TABLE IF NOT EXISTS reminder_log (
    id                      VARCHAR(36) PRIMARY KEY,
    tenant_id               VARCHAR(64) NOT NULL,      -- 租户ID
    reminder_id             VARCHAR(36) NOT NULL,      -- 逻辑关联 reminder.id
    trigger_execution_id    VARCHAR(36),               -- 逻辑关联 trigger_execution_record.execution_id
    scheduled_time          TIMESTAMP NOT NULL,
    actual_time             TIMESTAMP,
    channel                 VARCHAR(20) NOT NULL,      -- SMS/IN_APP
    status                  VARCHAR(20) NOT NULL,      -- PENDING/SENT/DELIVERED/COMPLETED/SKIPPED/SNOOZED/FAILED
    user_feedback           TEXT,                      -- 用户反馈
    created_at              TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at              TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

COMMENT ON TABLE reminder_log IS '提醒执行记录表';
COMMENT ON COLUMN reminder_log.tenant_id IS '租户ID';
COMMENT ON COLUMN reminder_log.reminder_id IS '提醒ID，逻辑关联 reminder';
COMMENT ON COLUMN reminder_log.trigger_execution_id IS '触发器执行ID，逻辑关联 trigger_execution_record';
COMMENT ON COLUMN reminder_log.scheduled_time IS '计划执行时间';
COMMENT ON COLUMN reminder_log.actual_time IS '实际执行时间';
COMMENT ON COLUMN reminder_log.channel IS '通知渠道：SMS/IN_APP';
COMMENT ON COLUMN reminder_log.status IS '状态：PENDING/SENT/DELIVERED/COMPLETED/SKIPPED/SNOOZED/FAILED';
COMMENT ON COLUMN reminder_log.user_feedback IS '用户反馈';

-- 索引：按业务方+提醒ID查询、按状态查询
CREATE INDEX IF NOT EXISTS idx_reminder_log_tenant_reminder ON reminder_log(tenant_id, reminder_id);
CREATE INDEX IF NOT EXISTS idx_reminder_log_tenant_trigger_exec ON reminder_log(tenant_id, trigger_execution_id);
CREATE INDEX IF NOT EXISTS idx_reminder_log_tenant_scheduled ON reminder_log(tenant_id, scheduled_time);
CREATE INDEX IF NOT EXISTS idx_reminder_log_tenant_status ON reminder_log(tenant_id, status);

-- 自动更新 updated_at 触发器
DROP TRIGGER IF EXISTS update_reminder_log_updated_at ON reminder_log;
CREATE TRIGGER update_reminder_log_updated_at
    BEFORE UPDATE ON reminder_log
    FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();

-- ============================================================================
-- 触发器定义表
-- ============================================================================
CREATE TABLE IF NOT EXISTS trigger_definition (
    trigger_id          VARCHAR(64) PRIMARY KEY,
    tenant_id           VARCHAR(64) NOT NULL,          -- 租户ID
    name                VARCHAR(100) NOT NULL,
    description         VARCHAR(500),
    source_type         VARCHAR(20) NOT NULL,          -- USER/GROUP/GLOBAL
    source_id           VARCHAR(64) NOT NULL,
    schedule_mode       VARCHAR(20) NOT NULL,          -- CRON/FIXED_DELAY/FIXED_RATE/ONE_TIME/TRIGGER
    schedule_value      VARCHAR(100),
    execute_function    VARCHAR(100) NOT NULL,
    condition_function  VARCHAR(100),
    abandon_function    VARCHAR(100),
    function_code_snapshot JSONB,
    parameters          JSONB,
    status              VARCHAR(20) DEFAULT 'ACTIVE',  -- ACTIVE/CANCELLED/DELETED
    expire_at           TIMESTAMP,
    created_by          VARCHAR(64),
    created_at          TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at          TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

COMMENT ON TABLE trigger_definition IS '触发器定义表';
COMMENT ON COLUMN trigger_definition.tenant_id IS '租户ID';
COMMENT ON COLUMN trigger_definition.name IS '触发器名称';
COMMENT ON COLUMN trigger_definition.description IS '触发器描述';
COMMENT ON COLUMN trigger_definition.source_type IS '来源类型：USER/GROUP/GLOBAL';
COMMENT ON COLUMN trigger_definition.source_id IS '来源ID';
COMMENT ON COLUMN trigger_definition.schedule_mode IS '调度模式：CRON/FIXED_DELAY/FIXED_RATE/ONE_TIME/TRIGGER';
COMMENT ON COLUMN trigger_definition.schedule_value IS '调度值（cron表达式或间隔值）';
COMMENT ON COLUMN trigger_definition.execute_function IS '执行函数名';
COMMENT ON COLUMN trigger_definition.condition_function IS '条件函数名';
COMMENT ON COLUMN trigger_definition.abandon_function IS '放弃条件函数名';
COMMENT ON COLUMN trigger_definition.function_code_snapshot IS '函数代码快照（JSONB）';
COMMENT ON COLUMN trigger_definition.parameters IS '执行参数（JSONB）';
COMMENT ON COLUMN trigger_definition.status IS '状态：ACTIVE/CANCELLED/DELETED';
COMMENT ON COLUMN trigger_definition.expire_at IS '过期时间';
COMMENT ON COLUMN trigger_definition.created_by IS '创建者';

-- 索引：所有查询都带 tenant_id 条件
CREATE INDEX IF NOT EXISTS idx_trigger_tenant_source ON trigger_definition(tenant_id, source_type, source_id);
CREATE INDEX IF NOT EXISTS idx_trigger_tenant_status ON trigger_definition(tenant_id, status);
CREATE INDEX IF NOT EXISTS idx_trigger_tenant_created_by ON trigger_definition(tenant_id, created_by);
CREATE INDEX IF NOT EXISTS idx_trigger_tenant_expire_at ON trigger_definition(tenant_id, expire_at) WHERE expire_at IS NOT NULL;

-- 自动更新 updated_at 触发器
DROP TRIGGER IF EXISTS update_trigger_definition_updated_at ON trigger_definition;
CREATE TRIGGER update_trigger_definition_updated_at
    BEFORE UPDATE ON trigger_definition
    FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();

-- ============================================================================
-- 触发器执行记录表
-- ============================================================================
CREATE TABLE IF NOT EXISTS trigger_execution_record (
    execution_id    VARCHAR(36) PRIMARY KEY,
    tenant_id       VARCHAR(64) NOT NULL,              -- 租户ID
    trigger_id      VARCHAR(64) NOT NULL,              -- 逻辑关联 trigger_definition.trigger_id
    scheduled_time  TIMESTAMP,
    start_time      TIMESTAMP,
    end_time        TIMESTAMP,
    status          VARCHAR(20) NOT NULL,              -- PENDING/RUNNING/SUCCESS/FAILED/SKIPPED/TIMEOUT
    error_message   TEXT,
    output_summary  TEXT,
    retry_count     INT DEFAULT 0,
    created_at      TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at      TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

COMMENT ON TABLE trigger_execution_record IS '触发器执行记录表';
COMMENT ON COLUMN trigger_execution_record.tenant_id IS '租户ID';
COMMENT ON COLUMN trigger_execution_record.trigger_id IS '触发器ID，逻辑关联 trigger_definition';
COMMENT ON COLUMN trigger_execution_record.scheduled_time IS '计划执行时间';
COMMENT ON COLUMN trigger_execution_record.start_time IS '开始执行时间';
COMMENT ON COLUMN trigger_execution_record.end_time IS '结束执行时间';
COMMENT ON COLUMN trigger_execution_record.status IS '状态：PENDING/RUNNING/SUCCESS/FAILED/SKIPPED/TIMEOUT';
COMMENT ON COLUMN trigger_execution_record.error_message IS '错误信息';
COMMENT ON COLUMN trigger_execution_record.output_summary IS '输出摘要';
COMMENT ON COLUMN trigger_execution_record.retry_count IS '重试次数';

-- 索引：所有查询都带 tenant_id 条件
CREATE INDEX IF NOT EXISTS idx_trigger_exec_tenant_trigger ON trigger_execution_record(tenant_id, trigger_id);
CREATE INDEX IF NOT EXISTS idx_trigger_exec_tenant_status ON trigger_execution_record(tenant_id, status);
CREATE INDEX IF NOT EXISTS idx_trigger_exec_tenant_scheduled ON trigger_execution_record(tenant_id, scheduled_time);
CREATE INDEX IF NOT EXISTS idx_trigger_exec_tenant_trigger_status ON trigger_execution_record(tenant_id, trigger_id, status);

-- 自动更新 updated_at 触发器
DROP TRIGGER IF EXISTS update_trigger_execution_record_updated_at ON trigger_execution_record;
CREATE TRIGGER update_trigger_execution_record_updated_at
    BEFORE UPDATE ON trigger_execution_record
    FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();

-- ============================================================================
-- 完成
-- ============================================================================
