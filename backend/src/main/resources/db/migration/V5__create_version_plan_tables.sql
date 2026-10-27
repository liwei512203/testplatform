-- 版本计划
CREATE TABLE `version_plan` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '版本计划ID',
  `project_id` varchar(50) NOT NULL COMMENT '所属项目ID',
  `name` varchar(32) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '版本名称（迭代轮次，如 V1.0）',
  `create_time` bigint NOT NULL COMMENT '计划创建时间（用户指定，毫秒时间戳）',
  `update_time` bigint NOT NULL COMMENT '记录更新时间',
  PRIMARY KEY (`id`),
  INDEX `project_index`(`project_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='版本计划';

-- 版本计划关联用例（创建计划时快照项目下全部用例）
CREATE TABLE `plan_case` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键',
  `plan_id` bigint NOT NULL COMMENT '版本计划ID',
  `case_id` bigint NOT NULL COMMENT '测试用例ID',
  `result` varchar(16) COLLATE utf8mb4_unicode_ci NOT NULL DEFAULT '未执行' COMMENT '执行结果：未执行/成功/失败',
  `execute_time` bigint NULL COMMENT '执行时间（与版本计划创建时间保持一致）',
  PRIMARY KEY (`id`),
  INDEX `plan_index`(`plan_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='版本计划关联用例';
