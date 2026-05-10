-- MyBatis-Plus 自动填充审计字段 DDL
-- 为各表添加 create_by, create_time, update_by, update_time 列

-- sys_oss_file
ALTER TABLE sys_oss_file
    ADD COLUMN create_by BIGINT DEFAULT NULL COMMENT '创建人ID' AFTER content_type,
    ADD COLUMN create_time DATETIME DEFAULT NULL COMMENT '创建时间' AFTER create_by,
    ADD COLUMN update_by BIGINT DEFAULT NULL COMMENT '更新人ID' AFTER create_time,
    ADD COLUMN update_time DATETIME DEFAULT NULL COMMENT '更新时间' AFTER update_by;

-- sys_oss_config（已有 created_at/updatedAt，新增统一字段，后续可删除旧列）
ALTER TABLE sys_oss_config
    ADD COLUMN create_by BIGINT DEFAULT NULL COMMENT '创建人ID',
    ADD COLUMN create_time DATETIME DEFAULT NULL COMMENT '创建时间',
    ADD COLUMN update_by BIGINT DEFAULT NULL COMMENT '更新人ID',
    ADD COLUMN update_time DATETIME DEFAULT NULL COMMENT '更新时间';

-- sys_article
ALTER TABLE sys_article
    ADD COLUMN create_by BIGINT DEFAULT NULL COMMENT '创建人ID',
    ADD COLUMN create_time DATETIME DEFAULT NULL COMMENT '创建时间',
    ADD COLUMN update_by BIGINT DEFAULT NULL COMMENT '更新人ID',
    ADD COLUMN update_time DATETIME DEFAULT NULL COMMENT '更新时间';

-- sys_article_content
ALTER TABLE sys_article_content
    ADD COLUMN create_by BIGINT DEFAULT NULL COMMENT '创建人ID',
    ADD COLUMN create_time DATETIME DEFAULT NULL COMMENT '创建时间',
    ADD COLUMN update_by BIGINT DEFAULT NULL COMMENT '更新人ID',
    ADD COLUMN update_time DATETIME DEFAULT NULL COMMENT '更新时间';

-- sys_article_content_history
ALTER TABLE sys_article_content_history
    ADD COLUMN create_by BIGINT DEFAULT NULL COMMENT '创建人ID',
    ADD COLUMN create_time DATETIME DEFAULT NULL COMMENT '创建时间',
    ADD COLUMN update_by BIGINT DEFAULT NULL COMMENT '更新人ID',
    ADD COLUMN update_time DATETIME DEFAULT NULL COMMENT '更新时间';

-- sys_tag（顺便修复 id_deleted 列名为 is_deleted）
ALTER TABLE sys_tag
    CHANGE COLUMN id_deleted is_deleted VARCHAR(1) DEFAULT NULL COMMENT '删除标志',
    ADD COLUMN create_by BIGINT DEFAULT NULL COMMENT '创建人ID',
    ADD COLUMN create_time DATETIME DEFAULT NULL COMMENT '创建时间',
    ADD COLUMN update_by BIGINT DEFAULT NULL COMMENT '更新人ID',
    ADD COLUMN update_time DATETIME DEFAULT NULL COMMENT '更新时间';

-- sys_category
ALTER TABLE sys_category
    ADD COLUMN create_by BIGINT DEFAULT NULL COMMENT '创建人ID',
    ADD COLUMN create_time DATETIME DEFAULT NULL COMMENT '创建时间',
    ADD COLUMN update_by BIGINT DEFAULT NULL COMMENT '更新人ID',
    ADD COLUMN update_time DATETIME DEFAULT NULL COMMENT '更新时间';
