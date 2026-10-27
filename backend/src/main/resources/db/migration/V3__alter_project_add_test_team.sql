-- 项目新增「测试团队」字段
ALTER TABLE `project`
  ADD COLUMN `test_team` varchar(32) COLLATE utf8mb4_unicode_ci NOT NULL DEFAULT '公共测试团队'
  COMMENT '所属测试团队：企金测试团队/零售测试团队/数据测试团队/公共测试团队' AFTER `system_code`;

-- 历史数据回填
UPDATE `project` SET `test_team` = '企金测试团队' WHERE `system_code` = 'TRADE';
UPDATE `project` SET `test_team` = '零售测试团队' WHERE `system_code` = 'PAY';
