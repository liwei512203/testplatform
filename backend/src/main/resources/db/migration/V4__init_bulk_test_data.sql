-- 批量测试数据：
-- 1）新增 20 个项目，按 4 个测试团队平均分配（每个团队 5 个项目）
-- 2）为每个项目生成 100 条测试用例（含演示项目在内，保证每个项目用例数 >= 100）

INSERT INTO `project` (`id`, `name`, `system_code`, `test_team`)
SELECT
  CONCAT('p-bulk-', LPAD(seq.n, 2, '0')),
  CONCAT(
    ELT(((seq.n - 1) MOD 4) + 1, '企金', '零售', '数据', '公共'),
    '-',
    ELT(((seq.n - 1) DIV 4) + 1, '信贷系统', '支付系统', '清算系统', '风控系统', '账务系统'),
    LPAD(seq.n, 2, '0')
  ),
  CONCAT('SYS', LPAD(seq.n, 3, '0')),
  ELT(((seq.n - 1) MOD 4) + 1, '企金测试团队', '零售测试团队', '数据测试团队', '公共测试团队')
FROM (
  SELECT (a.i - 1) * 5 + b.j AS n
  FROM
    (SELECT 1 AS i UNION ALL SELECT 2 UNION ALL SELECT 3 UNION ALL SELECT 4) a,
    (SELECT 1 AS j UNION ALL SELECT 2 UNION ALL SELECT 3 UNION ALL SELECT 4 UNION ALL SELECT 5) b
) seq
ORDER BY seq.n;

-- 每个项目 100 条用例
INSERT INTO `test_case` (`project_id`, `name`, `content`, `create_time`, `update_time`)
SELECT
  p.`id`,
  CONCAT(p.`name`, '-用例', LPAD(s.n, 3, '0')),
  CONCAT(
    '{"precondition":"测试环境已就绪，账号具备', p.`name`, '的访问权限",',
    '"steps":["1、登录系统并进入', p.`name`, '首页","2、打开第', s.n, '个用例对应的功能菜单","3、输入测试数据并提交","4、查看执行结果"],',
    '"expected":"', ELT((s.n MOD 4) + 1, '正常场景', '异常场景', '边界场景', '性能场景'),
    '：第', s.n, '个用例执行结果与预期一致"}'
  ),
  UNIX_TIMESTAMP() * 1000 - s.n * 60000,
  UNIX_TIMESTAMP() * 1000 - s.n * 60000
FROM `project` p,
  (
    SELECT (a.i - 1) * 10 + b.j AS n
    FROM
      (SELECT 1 AS i UNION ALL SELECT 2 UNION ALL SELECT 3 UNION ALL SELECT 4 UNION ALL SELECT 5
       UNION ALL SELECT 6 UNION ALL SELECT 7 UNION ALL SELECT 8 UNION ALL SELECT 9 UNION ALL SELECT 10) a,
      (SELECT 1 AS j UNION ALL SELECT 2 UNION ALL SELECT 3 UNION ALL SELECT 4 UNION ALL SELECT 5
       UNION ALL SELECT 6 UNION ALL SELECT 7 UNION ALL SELECT 8 UNION ALL SELECT 9 UNION ALL SELECT 10) b
  ) s;
