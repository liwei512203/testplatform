-- =========================================================
-- 执行方式字典表：值域数据化，新增执行方式只需 INSERT 一行，
-- 前后端无需改代码（报表列/执行弹窗选项均由字典与数据驱动）
-- =========================================================
CREATE TABLE `exec_type` (
  `name` varchar(16) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '执行方式名称',
  `sort` int NOT NULL DEFAULT 0 COMMENT '展示排序',
  PRIMARY KEY (`name`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='执行方式字典';

INSERT INTO `exec_type` (`name`, `sort`) VALUES
  ('手动执行', 1),
  ('定时任务执行', 2),
  ('流水线触发执行', 3);
