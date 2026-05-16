-- ----------------------------
-- Table structure for sys_config
-- ----------------------------
DROP TABLE IF EXISTS `sys_config`;
CREATE TABLE `sys_config`  (
  `id` bigint NOT NULL COMMENT '主键ID',
  `config_key` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_german2_ci NOT NULL COMMENT '配置键',
  `config_value` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_german2_ci NULL DEFAULT '' COMMENT '配置值',
  `create_by` bigint NULL DEFAULT NULL COMMENT '创建人ID',
  `create_time` datetime NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_by` bigint NULL DEFAULT NULL COMMENT '更新人ID',
  `update_time` datetime NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `is_deleted` tinyint NULL DEFAULT 0 COMMENT '删除标志(0正常 1删除)',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_config_key`(`config_key` ASC) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_german2_ci COMMENT = '站点配置表' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of sys_config
-- ----------------------------
INSERT INTO `sys_config` VALUES (1, 'site_name', '我的博客', NULL, '2026-05-16 00:00:00', NULL, '2026-05-16 00:00:00', 0);
INSERT INTO `sys_config` VALUES (2, 'copyright', '© 2026 xxx', NULL, '2026-05-16 00:00:00', NULL, '2026-05-16 00:00:00', 0);
INSERT INTO `sys_config` VALUES (3, 'github', 'https://github.com/xxx', NULL, '2026-05-16 00:00:00', NULL, '2026-05-16 00:00:00', 0);
INSERT INTO `sys_config` VALUES (4, 'beian', '京ICP备xxxxx号', NULL, '2026-05-16 00:00:00', NULL, '2026-05-16 00:00:00', 0);
