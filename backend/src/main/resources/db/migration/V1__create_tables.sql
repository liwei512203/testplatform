-- 项目信息
CREATE TABLE `project` (
  `id` varchar(50) NOT NULL COMMENT '主键UUID',
  `name` varchar(32) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '项目名称',
  `system_code` varchar(32) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '系统编号',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='项目信息';

-- 测试用例信息
CREATE TABLE `test_case` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '用例ID',
  `project_id` varchar(50) NOT NULL COMMENT '所属项目ID',
  `name` varchar(32) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '用例名称',
  `content` text COMMENT '测试用例信息，包括前置条件，步骤，断言等，以一个json结构给出',
  `create_time` bigint NOT NULL COMMENT '创建时间',
  `update_time` bigint NOT NULL COMMENT '更新时间',
  PRIMARY KEY (`id`),
  INDEX `project_index`(`project_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='测试用例信息';
