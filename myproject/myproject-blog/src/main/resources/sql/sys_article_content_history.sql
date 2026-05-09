CREATE TABLE `sys_article_content_history` (
  `id` bigint NOT NULL COMMENT '主键',
  `article_id` bigint NOT NULL COMMENT '文章ID',
  `oss_id` bigint NOT NULL COMMENT '关联OSS文件ID（被替换的旧版本）',
  `version` int NOT NULL COMMENT '版本号，每篇文章独立自增',
  `replaced_by` varchar(64) DEFAULT NULL COMMENT '操作人',
  `replaced_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '替换时间',
  `remark` varchar(255) DEFAULT NULL COMMENT '备注',
  PRIMARY KEY (`id`),
  KEY `idx_article_id` (`article_id`),
  KEY `idx_article_version` (`article_id`, `version`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='文章内容版本历史表';
