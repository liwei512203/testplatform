-- =========================================================
-- 执行方式字段：手动执行 / 定时任务执行 / 流水线触发执行
-- 历史数据统一回填为「手动执行」
-- =========================================================

-- 1) 版本计划：计划级执行方式（创建时指定，执行时可覆盖）
ALTER TABLE `version_plan`
  ADD COLUMN `exec_type` varchar(16) COLLATE utf8mb4_unicode_ci NOT NULL DEFAULT '手动执行'
  COMMENT '执行方式：手动执行/定时任务执行/流水线触发执行' AFTER `name`;

-- 2) 计划用例状态表：记录最近一次执行的执行方式
ALTER TABLE `plan_case`
  ADD COLUMN `exec_type` varchar(16) COLLATE utf8mb4_unicode_ci NOT NULL DEFAULT '手动执行'
  COMMENT '最近一次执行方式' AFTER `result`;

-- 3) 执行日志：每条执行事实携带执行方式（报表统计事实来源）
ALTER TABLE `case_execution_log`
  ADD COLUMN `exec_type` varchar(16) COLLATE utf8mb4_unicode_ci NOT NULL DEFAULT '手动执行'
  COMMENT '执行方式' AFTER `result`;

-- 4) 项目日统计表：粒度调整为 天 x 项目 x 执行方式
--    主键相应调整（stat_date 仍为最左列，分区不受影响；历史行默认手动执行，唯一性保持）
ALTER TABLE `execution_daily_stat`
  ADD COLUMN `exec_type` varchar(16) COLLATE utf8mb4_unicode_ci NOT NULL DEFAULT '手动执行'
  COMMENT '执行方式' AFTER `test_team`,
  DROP PRIMARY KEY,
  ADD PRIMARY KEY (`stat_date`, `project_id`, `exec_type`);

-- 5) 用例日统计表：粒度调整为 天 x 用例 x 执行方式
ALTER TABLE `execution_case_daily_stat`
  ADD COLUMN `exec_type` varchar(16) COLLATE utf8mb4_unicode_ci NOT NULL DEFAULT '手动执行'
  COMMENT '执行方式' AFTER `test_team`,
  DROP PRIMARY KEY,
  ADD PRIMARY KEY (`stat_date`, `case_id`, `exec_type`);
