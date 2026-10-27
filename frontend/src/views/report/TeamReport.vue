<template>
  <div>
    <!-- 团队一览 -->
    <el-card v-if="!drillTeam" shadow="never">
      <div class="toolbar">
        <span class="toolbar-title">团队执行情况</span>
        <el-button
          size="small"
          icon="el-icon-download"
          :loading="exporting"
          @click="handleExportTeams"
        >
          导出Excel
        </el-button>
      </div>

      <el-table v-loading="loading" :data="teamRows" border stripe>
        <el-table-column prop="testTeam" label="测试团队" min-width="140" />
        <el-table-column prop="projectCount" label="项目数" width="90" align="center" sortable />
        <el-table-column prop="executed" label="执行次数" width="110" align="center" sortable />
        <el-table-column prop="covered" label="覆盖用例数" width="110" align="center" sortable />
        <el-table-column prop="totalCases" label="总用例数" width="100" align="center" />
        <el-table-column label="执行率" width="100" align="center" sortable :sort-by="row => row.executionRate || 0">
          <template slot-scope="scope">
            <span :class="rateClass(scope.row.executionRate)">
              {{ scope.row.executionRate === null || scope.row.executionRate === undefined ? '-' : scope.row.executionRate + '%' }}
            </span>
          </template>
        </el-table-column>
        <el-table-column prop="success" label="成功次数" width="110" align="center" />
        <el-table-column prop="fail" label="失败次数" width="110" align="center" sortable />
        <el-table-column prop="successRate" label="成功率" width="130" align="center" sortable :sort-by="row => row.successRate || 0">
          <template slot="header">
            <span>成功率
              <el-tooltip v-if="teamTypeKeys.length" content="展开/收起各执行方式成功率" placement="top">
                <i
                  :class="teamTypesExpanded ? 'el-icon-arrow-up' : 'el-icon-s-unfold'"
                  class="type-toggle"
                  @click="teamTypesExpanded = !teamTypesExpanded"
                />
              </el-tooltip>
            </span>
          </template>
          <template slot-scope="scope">
            <span :class="rateClass(scope.row.successRate)">
              {{ scope.row.successRate === null ? '-' : scope.row.successRate + '%' }}
            </span>
          </template>
        </el-table-column>
        <el-table-column
          v-for="type in teamTypesExpanded ? teamTypeKeys : []"
          :key="'t-' + type"
          :label="type + '成功率'"
          width="115"
          align="center"
        >
          <template slot-scope="scope">
            <span :class="rateClass(scope.row.typeRates ? scope.row.typeRates[type] : null)">
              {{ scope.row.typeRates && scope.row.typeRates[type] !== undefined && scope.row.typeRates[type] !== null ? scope.row.typeRates[type] + '%' : '-' }}
            </span>
          </template>
        </el-table-column>
        <el-table-column label="执行环比" width="150" align="center">
          <template slot-scope="scope">
            <span v-if="scope.row.executedChangeRate !== null && scope.row.executedChangeRate !== undefined"
              :class="scope.row.executedChangeRate >= 0 ? 'rate-good' : 'rate-bad'">
              {{ scope.row.executedChangeRate >= 0 ? '+' : '' }}{{ scope.row.executedChangeRate.toFixed(2) }}%
            </span>
            <span v-else class="rate-none">—</span>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="110" align="center" fixed="right">
          <template slot-scope="scope">
            <el-button type="text" size="mini" @click="drillInto(scope.row)">项目下钻</el-button>
          </template>
        </el-table-column>
        <template slot="empty">
          <span>所选时间范围内暂无执行数据</span>
        </template>
      </el-table>
    </el-card>

    <!-- 团队下钻：项目列表 -->
    <el-card v-else shadow="never">
      <div class="toolbar">
        <div class="toolbar-title">
          <el-button size="mini" icon="el-icon-back" @click="drillTeam = null">返回</el-button>
          <span style="margin-left: 8px">{{ drillTeam }} - 项目执行情况</span>
        </div>
        <el-button
          size="small"
          icon="el-icon-download"
          :loading="exporting"
          @click="handleExportProjects"
        >
          导出Excel
        </el-button>
      </div>

      <el-table v-loading="loading" :data="projectRows" border stripe key="drill-project-table">
        <el-table-column prop="projectName" label="项目名称" min-width="170" show-overflow-tooltip />
        <el-table-column prop="systemCode" label="系统编号" width="120" align="center" />
        <el-table-column prop="executed" label="执行次数" width="110" align="center" sortable />
        <el-table-column prop="covered" label="覆盖用例数" width="110" align="center" />
        <el-table-column prop="totalCases" label="总用例数" width="100" align="center" />
        <el-table-column label="执行率" width="100" align="center">
          <template slot-scope="scope">
            <span :class="rateClass(scope.row.executionRate)">
              {{ scope.row.executionRate === null || scope.row.executionRate === undefined ? '-' : scope.row.executionRate + '%' }}
            </span>
          </template>
        </el-table-column>
        <el-table-column prop="success" label="成功次数" width="110" align="center" />
        <el-table-column prop="fail" label="失败次数" width="110" align="center" sortable />
        <el-table-column prop="successRate" label="成功率" width="130" align="center">
          <template slot="header">
            <span>成功率
              <el-tooltip v-if="projectTypeKeys.length" content="展开/收起各执行方式成功率" placement="top">
                <i
                  :class="projectTypesExpanded ? 'el-icon-arrow-up' : 'el-icon-s-unfold'"
                  class="type-toggle"
                  @click="projectTypesExpanded = !projectTypesExpanded"
                />
              </el-tooltip>
            </span>
          </template>
          <template slot-scope="scope">
            <span :class="rateClass(scope.row.successRate)">
              {{ scope.row.successRate === null ? '-' : scope.row.successRate + '%' }}
            </span>
          </template>
        </el-table-column>
        <el-table-column
          v-for="type in projectTypesExpanded ? projectTypeKeys : []"
          :key="'p-' + type"
          :label="type + '成功率'"
          width="115"
          align="center"
        >
          <template slot-scope="scope">
            <span :class="rateClass(scope.row.typeRates ? scope.row.typeRates[type] : null)">
              {{ scope.row.typeRates && scope.row.typeRates[type] !== undefined && scope.row.typeRates[type] !== null ? scope.row.typeRates[type] + '%' : '-' }}
            </span>
          </template>
        </el-table-column>
        <el-table-column label="执行环比" width="150" align="center">
          <template slot-scope="scope">
            <span v-if="scope.row.executedChangeRate !== null && scope.row.executedChangeRate !== undefined"
              :class="scope.row.executedChangeRate >= 0 ? 'rate-good' : 'rate-bad'">
              {{ scope.row.executedChangeRate >= 0 ? '+' : '' }}{{ scope.row.executedChangeRate.toFixed(2) }}%
            </span>
            <span v-else class="rate-none">—</span>
          </template>
        </el-table-column>
        <template slot="empty">
          <span>该团队暂无项目执行数据</span>
        </template>
      </el-table>
    </el-card>
  </div>
</template>

<script>
import { reportApi } from '@/api/report'

export default {
  name: 'TeamReport',
  props: {
    dateRange: { type: Array, required: true }
  },
  data() {
    return {
      teamRows: [],
      projectRows: [],
      drillTeam: null,
      loading: false,
      exporting: false,
      // 各执行方式成功率列默认收起，点击成功率列头按钮展开
      teamTypesExpanded: false,
      projectTypesExpanded: false
    }
  },
  computed: {
    // 团队一览：动态执行方式列（各行 typeRates key 并集，保持出现顺序）
    teamTypeKeys() {
      return this.collectTypeKeys(this.teamRows)
    },
    projectTypeKeys() {
      return this.collectTypeKeys(this.projectRows)
    }
  },
  watch: {
    dateRange: {
      immediate: true,
      handler() {
        this.load()
      }
    },
    drillTeam() {
      this.load()
    }
  },
  methods: {
    collectTypeKeys(rows) {
      const keys = []
      const seen = {}
      rows.forEach(r => {
        const rates = r.typeRates || {}
        Object.keys(rates).forEach(k => {
          if (!seen[k]) {
            seen[k] = true
            keys.push(k)
          }
        })
      })
      return keys
    },
    params() {
      return { startDate: this.dateRange[0], endDate: this.dateRange[1] }
    },
    async load() {
      if (!this.dateRange || this.dateRange.length !== 2) return
      this.loading = true
      try {
        if (this.drillTeam) {
          const res = await reportApi.teamProjects(this.drillTeam, this.params())
          this.projectRows = res.data || []
        } else {
          const res = await reportApi.teamRows(this.params())
          this.teamRows = res.data || []
        }
      } catch (e) {
        this.$message.error(e.message || '报表加载失败')
      } finally {
        this.loading = false
      }
    },
    drillInto(row) {
      this.drillTeam = row.testTeam
    },
    rateClass(rate) {
      if (rate === null || rate === undefined) return 'rate-none'
      if (rate >= 90) return 'rate-good'
      if (rate >= 75) return 'rate-mid'
      return 'rate-bad'
    },
    async handleExportTeams() {
      this.exporting = true
      try {
        await reportApi.exportTeams(this.params())
        this.$message.success('导出成功')
      } catch (e) {
        this.$message.error(e.message || '导出失败')
      } finally {
        this.exporting = false
      }
    },
    async handleExportProjects() {
      this.exporting = true
      try {
        await reportApi.exportTeamProjects(this.drillTeam, this.params())
        this.$message.success('导出成功')
      } catch (e) {
        this.$message.error(e.message || '导出失败')
      } finally {
        this.exporting = false
      }
    }
  }
}
</script>

<style scoped>
.toolbar {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 16px;
}

.toolbar-title {
  display: flex;
  align-items: center;
  font-size: 15px;
  font-weight: 600;
}

.rate-good {
  color: #67c23a;
}

.rate-mid {
  color: #e6a23c;
}

.rate-bad {
  color: #f56c6c;
}

.rate-none {
  color: #c0c4cc;
}

.type-toggle {
  margin-left: 4px;
  color: #409eff;
  cursor: pointer;
  font-size: 14px;
}

.type-toggle:hover {
  opacity: 0.75;
}
</style>
