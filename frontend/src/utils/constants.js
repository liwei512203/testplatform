/**
 * 测试团队值域（与后端 enums.TestTeam 保持一致）
 */
export const TEST_TEAMS = [
  '企金测试团队',
  '零售测试团队',
  '数据测试团队',
  '公共测试团队'
]

export const DEFAULT_TEST_TEAM = '公共测试团队'

/**
 * 团队标签颜色
 */
export const TEST_TEAM_TAG_TYPE = {
  企金测试团队: 'primary',
  零售测试团队: 'success',
  数据测试团队: 'warning',
  公共测试团队: 'info'
}

/**
 * 执行方式本地兜底值域：正式值域由后端字典接口 /api/dicts/exec-types 下发，
 * 本常量仅在字典接口不可用时兜底展示
 */
export const EXEC_TYPES = ['手动执行', '定时任务执行', '流水线触发执行']

export const DEFAULT_EXEC_TYPE = '手动执行'

/**
 * 执行方式标签颜色
 */
export const EXEC_TYPE_TAG_TYPE = {
  手动执行: 'primary',
  定时任务执行: 'warning',
  流水线触发执行: 'success'
}
