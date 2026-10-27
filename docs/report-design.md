# 测试管理平台 —— 执行报表中心设计文档

> 版本：v1.0
> 日期：2026-10-26
> 状态：设计已评审锁定（Q1~Q6 决策见附录）
> 范围：数据报表菜单整体升级为报表中心；后端报表查询接口与 Excel 导出；前端三页签报表 UI（Vue2 + ECharts）

---

## 1. 背景与目标

测试管理平台已具备项目、测试用例、版本计划（含一键执行模拟）能力，执行事实统一记录在 `case_execution_log`，并由每日跑批聚合为两张日统计表。本设计在此之上交付三级报表体系，服务三类角色：

| 角色 | 关注点 | 主用页面 |
|---|---|---|
| 测试总监/测管管理者 | 全平台吞吐与质量总貌、团队横向对比 | 系统报表 |
| 测试经理（团队） | 本团队各项目执行与质量、劣化项目定位 | 团队报表 → 项目下钻 |
| 测试负责人（项目） | 单项目执行明细、失败用例定位 | 项目报表 |

核心指标口径（全报表统一）：
- **执行次数**：`SUM(executed_count)`，重复执行累计（工作量口径）；
- **覆盖用例数**：`COUNT(DISTINCT case_id)`，去重用例（覆盖面口径）；
- **成功率**：`SUM(success_count)/SUM(executed_count)*100%`，保留两位小数；
- **数据新鲜度**：T+1（每日 01:00 跑批重算最近 2 天），报表页显示「数据截至 {T-1}」。

## 2. 整体结构

```
报表中心（左侧菜单「数据报表」）
+------------------------------------------------------------------+
| [Tab: 系统报表 | 团队报表 | 项目报表]                                |
| 时间范围(默认近30天,上限366天) [导出Excel]      数据截至: T-1       |
+------------------------------------------------------------------+
| 系统报表：五卡片(带环比) + 每日执行量/成功率双轴折线(带周期对比)      |
| 团队报表：团队一览表(每队一行,含环比) → 行点击下钻项目列表           |
| 项目报表：项目卡片+趋势(单项目) + 失败TOP10 + 用例执行明细表          |
+------------------------------------------------------------------+
```

## 3. 页面与功能明细

### 3.1 系统报表
- **卡片**：项目总数（取当前项目表 count，不受时间过滤）、执行次数、覆盖用例数、整体成功率、失败次数；每卡片右上角环比值。
- **趋势图**：每日执行次数（柱/线，左轴）+ 每日成功率（折线，右轴）；≤60 天按日、61~366 天按周聚合；图下方并列展示上一周期同形状曲线（浅色）。
- 无团队/项目筛选（全系统视角）。

### 3.2 团队报表
- **团队一览表**：团队 | 项目数 | 执行次数(环比) | 覆盖用例数 | 成功次数 | 失败次数 | 成功率 | 环比；
  - 仅列出当前存在项目的团队（值域四团队）；
  - 项目数取自项目表（不受时间过滤），执行类指标受时间过滤。
- **下钻**：点击团队行 → 展示该团队项目列表（列同 3.3 项目列表），返回上一级按钮。
- 导出：团队一览、团队项目列表。

### 3.3 项目报表
- **入口**：Tab 直达（顶部项目选择器联动默认项目）或团队下钻进入。
- **项目卡片**：执行次数、覆盖用例数、成功率、失败次数（环比）；不含项目总数。
- **趋势图**：同系统报表（限定单项目），含周期对比。
- **失败 TOP10**：时间段内失败次数前十的用例（用例名 + 失败次数 + 成功率），点击用例跳明细表并定位。
- **用例执行明细表**：
  - 列：用例ID | 用例名称 | 执行次数 | 成功次数 | 失败次数 | 成功率 | 最近执行日期；
  - 服务端排序（失败次数 desc 默认 / 成功率 asc / 执行次数 desc / 最近执行日期 desc）；
  - 服务端分页（默认 20/页）；用例名称关键字搜索（模糊）；
  - 导出 Excel（上限 10 万行，超限报错提示收窄时间范围）。

## 4. 接口设计（统一前缀 /api/reports）

| 接口 | 方法 | 参数 | 返回 |
|---|---|---|---|
| `/api/reports/system/overview` | GET | startDate,endDate | 卡片指标+环比、趋势点列、数据截至日期 |
| `/api/reports/teams` | GET | startDate,endDate | 团队行列表（含环比） |
| `/api/reports/teams/{team}/projects` | GET | startDate,endDate | 团队下项目行列表 |
| `/api/reports/projects/{id}/overview` | GET | startDate,endDate | 项目卡片+环比、趋势点列 |
| `/api/reports/projects/{id}/cases` | GET | startDate,endDate,sortField,pageNum,pageSize,keyword | 明细分页（total,list） |
| `/api/reports/projects/{id}/top-failures` | GET | startDate,endDate,limit=10 | 失败TOP用例列表 |
| `/api/reports/system/overview/export` | GET | startDate,endDate | Excel 文件流 |
| `/api/reports/teams/export` | GET | startDate,endDate | Excel（团队一览） |
| `/api/reports/teams/{team}/projects/export` | GET | startDate,endDate | Excel（团队项目列表） |
| `/api/reports/projects/{id}/cases/export` | GET | startDate,endDate,keyword | Excel（用例明细，≤10万行） |

通用规则：
- `startDate/endDate` 必填（yyyy-MM-dd），跨度 ≤ 366 天，后端校验返回 400；
- 环比区间 = 所选区间向前平移等长天数（例 10-01~10-31 → 08-31~09-30）；对照区间无数据或超出保留期显示「—」；
- 返回体统一 `Result<T>`；`dataThroughDate` 随每个查询返回（= 跑批最后成功重算日，默认 T-1）。

## 5. 数据访问设计（SQL 要点）

| 查询 | 数据源 | 说明 |
|---|---|---|
| 系统/团队/项目卡片 | `execution_daily_stat` | 日期区间聚合；覆盖用例数由 `execution_case_daily_stat COUNT(DISTINCT case_id)` 补充 |
| 趋势（日粒度） | `execution_daily_stat` | `GROUP BY stat_date`，区间 ≤ 60 天 |
| 趋势（周粒度） | `execution_daily_stat` | `GROUP BY 周起始日(WEEKDAY函数)`，61~366 天 |
| 团队一览 | `execution_daily_stat` | `GROUP BY test_team`，项目数来自 `project` |
| 项目列表（团队下） | `execution_daily_stat` | `WHERE test_team=?`（+project 表 outer join 补无执行项目） |
| 用例明细 | `execution_case_daily_stat` JOIN `test_case` | `WHERE project_id=? AND stat_date BETWEEN`，排序字段映射到聚合列 |
| 失败TOP10 | `execution_case_daily_stat` JOIN `test_case` | `ORDER BY fail_count 合计 desc LIMIT 10` |
| 环比 | 上述各 SQL 复用 | 同一查询传对照区间再跑一次 |

性能要点：
- 所有查询走 `stat_date` 前缀（分区裁剪）与 `(project_id, stat_date)`、`(test_team, stat_date)` 索引；用例明细 366 天最坏扫描 ≈ 366 × 项目用例数百~千行，毫秒~秒级；
- 无需修改现有表结构与索引；
- 不提供系统级用例排行（数据量不可控），排行仅到项目粒度。

## 6. Excel 导出（后端 EasyExcel）

- 依赖：`com.alibaba:easyexcel`（3.3.x，流式写出防 OOM）；
- 导出与查询同一 Service，导出接口复用查询逻辑（去分页、上限校验）；
- 文件名：`{报表名}_{起始日}_{截止日}.xlsx`（如 `团队报表_20260901_20260930.xlsx`），UTF-8 编码处理中文；
- 前端以 blob 接收（`responseType: 'blob'`），a 标签下载；后端设置 `Content-Disposition`。

## 7. 前端设计（Vue2 + Element UI + ECharts 5）

- 依赖新增：`echarts@5`（按需引入 LineChart/BarChart 组件与 tooltip/grid/legend）；
- 页面结构：`views/report/ReportCenter.vue`（Tab 容器）+ `SystemReport.vue` / `TeamReport.vue` / `ProjectReport.vue`；
- 公共组件：`ReportDateRange.vue`（366 天限制 + disabledDate）、`StatCard.vue`（指标卡+环比角标，方向语义：成功率升=绿、失败升=红）、`TrendChart.vue`（双轴折线+周期对比浅色线）；
- 明细表排序/分页走服务端（`@sort-change` 事件映射 sortField）；
- 时间范围变更后所有卡片/图表/表格联动刷新（单一数据加载入口）；
- 图表 resize 监听（窗口与 Tab 切换）。

## 8. 兼容与约束

- 不改变既有版本计划/执行/跑批逻辑，报表为纯读侧新增；
- `Report.vue`（旧占位页）由 `views/report/ReportCenter.vue` 替换，路由 `/reports` 不变；
- 后端新增代码不触碰既有 Mapper/Service，全部落在 `report` 包下（ReportController/ReportService/ReportMapper(+XML)），导出依赖加入 pom；
- 明细导出 10 万行上限与 366 天范围上限均为配置项（`report.export.max-rows`、`report.range.max-days`），便于后续调整。

## 9. 实施顺序（Tasks）

1. 后端：pom 加 easyexcel；ReportMapper + XML（卡片/趋势/团队/明细/TOP 查询，含环比复用）；
2. 后端：ReportService（参数校验、区间与环比区间计算、周粒度切换、dataThroughDate）+ ReportController（查询 6 接口 + 导出 4 接口）；
3. 前端：依赖安装 echarts；公共组件（日期范围/指标卡/趋势图）；ReportCenter 三页签页面；路由替换旧 Report.vue；
4. 联调与数据核对（与 `case_execution_log` 聚合对账）。

## 附录：已锁定决策记录

| # | 问题 | 决策 | 时间 |
|---|---|---|---|
| Q1 | 图表程度 | B：趋势折线图（ECharts 5），团队对比用表格 | 2026-10-26 |
| Q2 | Excel 导出 | 需要（后端 EasyExcel 流式，范围：团队一览/团队项目列表/用例明细） | 2026-10-26 |
| Q3 | 时间范围上限 | 366 天（前后端双重校验） | 2026-10-26 |
| Q4 | 环比对照 | 要（等长周期平移对照，卡片级展示，趋势图并列浅色曲线） | 2026-10-26 |
| Q5 | 导出实现 | 后端 EasyExcel，明细上限 10 万行 | 2026-10-26 |
| Q6 | 趋势粒度 | ≤60 天按日、61~366 天按周 | 2026-10-26 |
