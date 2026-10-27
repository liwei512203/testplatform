<template>
  <div>
    <el-card shadow="never" class="project-select-card">
      <div class="project-bar">
        <span class="label">当前项目：</span>
        <el-select v-model="projectId" filterable size="small" placeholder="请选择项目" style="width: 300px" @change="onProjectChange">
          <el-option v-for="p in projects" :key="p.id" :label="p.name + '（' + p.systemCode + '）'" :value="p.id" />
        </el-select>
      </div>
    </el-card>

    <template v-if="projectId">
      <el-row :gutter="16" class="card-row">
        <el-col v-for="card in overview.cards" :key="card.key" :span="span">
          <stat-card :card="card" />
        </el-col>
      </el-row>

      <el-row :gutter="16">
        <el-col :span="14">
          <el-card shadow="never">
            <div slot="header">
              执行趋势
              <el-tag size="mini" type="info" style="margin-left: 8px">
                {{ overview.granularity === 'week' ? '按周聚合' : '按日' }}
              </el-tag>
            </div>
            <trend-chart
              :trend="overview.trend"
              :prev-trend="overview.prevTrend"
              :granularity="overview.granularity"
            />
          </el-card>
        </el-col>
        <el-col :span="10">
          <el-card shadow="never">
            <div slot="header">失败 TOP10 用例</div>
            <el-table :data="topFailures" size="small" height="320">
              <el-table-column type="index" label="#" width="40" align="center" />
              <el-table-column prop="caseName" label="用例名称" min-width="140" show-overflow-tooltip />
              <el-table-column prop="fail" label="失败" width="70" align="center">
                <template slot-scope="scope">
                  <span class="rate-bad">{{ scope.row.fail }}</span>
                </template>
              </el-table-column>
              <el-table-column label="成功率" width="80" align="center">
                <template slot-scope="scope">
                  {{ scope.row.successRate === null ? '-' : scope.row.successRate + '%' }}
                </template>
              </el-table-column>
            </el-table>
          </el-card>
        </el-col>
      </el-row>

      <el-row :gutter="16" class="type-row">
        <el-col :span="24">
          <el-card shadow="never">
            <div slot="header">分执行方式统计</div>
            <el-table :data="overview.typeStats || []" border stripe size="small">
              <el-table-column prop="execType" label="执行方式" min-width="140" align="center" />
              <el-table-column prop="executed" label="执行次数" width="110" align="center" />
              <el-table-column prop="success" label="成功次数" width="110" align="center" />
              <el-table-column prop="fail" label="失败次数" width="110" align="center" />
              <el-table-column label="成功率" width="110" align="center">
                <template slot-scope="scope">
                  <span :class="rateClass(scope.row.successRate)">
                    {{ scope.row.successRate === null || scope.row.successRate === undefined ? '-' : scope.row.successRate + '%' }}
                  </span>
                </template>
              </el-table-column>
              <template slot="empty">
                <span>暂无执行数据</span>
              </template>
            </el-table>
          </el-card>
        </el-col>
      </el-row>

      <el-card shadow="never" class="detail-card">
        <div class="toolbar">
          <el-button
            type="text"
            class="detail-toggle"
            @click="toggleDetail"
          >
            <i :class="detailExpanded ? 'el-icon-arrow-up' : 'el-icon-arrow-down'" />
            {{ detailExpanded ? '收起用例执行明细' : '查看用例执行明细' }}
            <span class="detail-toggle-tip">（明细查询数据量较大，按需展开）</span>
          </el-button>
          <div v-if="detailExpanded" class="toolbar-right">
            <el-input
              v-model="keyword"
              size="small"
              placeholder="搜索用例名称"
              prefix-icon="el-icon-search"
              clearable
              style="width: 220px"
              @keyup.enter.native="handleSearch"
              @clear="handleSearch"
            />
            <el-button size="small" icon="el-icon-download" :loading="exporting" @click="handleExport">
              导出Excel
            </el-button>
          </div>
        </div>

        <template v-if="detailExpanded">

        <el-table
          v-loading="loading"
          :data="caseRows"
          border
          stripe
          @sort-change="handleSortChange"
        >
          <el-table-column prop="caseId" label="用例ID" width="90" align="center" />
          <el-table-column prop="caseName" label="用例名称" min-width="180" show-overflow-tooltip />
          <el-table-column prop="executed" label="执行次数" width="110" align="center" sortable="custom" />
          <el-table-column prop="success" label="成功次数" width="110" align="center" />
          <el-table-column prop="fail" label="失败次数" width="110" align="center" sortable="custom" />
          <el-table-column label="成功率" width="130" align="center" sortable="custom" prop="successRate">
            <template slot="header">
              <span>成功率
                <el-tooltip v-if="caseTypeKeys.length" content="展开/收起各执行方式成功率" placement="top">
                  <i
                    :class="caseTypesExpanded ? 'el-icon-arrow-up' : 'el-icon-s-unfold'"
                    class="type-toggle"
                    @click="caseTypesExpanded = !caseTypesExpanded"
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
            v-for="type in caseTypesExpanded ? caseTypeKeys : []"
            :key="'c-' + type"
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
          <el-table-column prop="lastExecDate" label="最近执行日期" width="130" align="center" sortable="custom" />
          <template slot="empty">
            <span>该时间段内暂无用例执行数据</span>
          </template>
        </el-table>

        <el-pagination
          class="pagination"
          layout="total, sizes, prev, pager, next, jumper"
          :current-page.sync="pageNum"
          :page-size.sync="pageSize"
          :page-sizes="[20, 50, 100]"
          :total="total"
          @size-change="handleSearch"
          @current-change="loadCases"
        />
        </template>
      </el-card>
    </template>

    <el-empty v-else description="请先选择项目" />
  </div>
</template>

<script>
import { reportApi } from '@/api/report'
import { state } from '@/store/project'
import StatCard from '@/components/report/StatCard.vue'
import TrendChart from '@/components/report/TrendChart.vue'

export default {
  name: 'ProjectReport',
  components: { StatCard, TrendChart },
  props: {
    dateRange: { type: Array, required: true }
  },
  data() {
    return {
      projectId: '',
      overview: { cards: [], trend: [], prevTrend: [], typeStats: [], granularity: 'day', dataThroughDate: '' },
      topFailures: [],
      caseRows: [],
      total: 0,
      pageNum: 1,
      pageSize: 20,
      keyword: '',
      sortField: 'fail',
      sortOrder: 'desc',
      loading: false,
      exporting: false,
      // 明细默认收起：进入报表页不自动发起明细查询，用户点击展开才加载
      detailExpanded: false,
      // 各执行方式成功率列默认收起
      caseTypesExpanded: false
    }
  },
  computed: {
    projects() {
      return state.projects
    },
    span() {
      const n = (this.overview.cards || []).length || 4
      return Math.floor(24 / n)
    },
    // 明细表：动态执行方式列（当前页各行 typeRates key 并集）
    caseTypeKeys() {
      const keys = []
      const seen = {}
      this.caseRows.forEach(r => {
        const rates = r.typeRates || {}
        Object.keys(rates).forEach(k => {
          if (!seen[k]) {
            seen[k] = true
            keys.push(k)
          }
        })
      })
      return keys
    }
  },
  watch: {
    dateRange: {
      immediate: true,
      handler() {
        if (this.projectId) this.loadAll()
      }
    }
  },
  created() {
    // 默认选中当前项目
    if (state.currentProject) {
      this.projectId = state.currentProject.id
    }
    this.loadAll()
  },
  methods: {
    params() {
      return { startDate: this.dateRange[0], endDate: this.dateRange[1] }
    },
    loadAll() {
      if (!this.projectId) return
      this.loadOverview()
      this.loadTopFailures()
      // 明细仅在展开状态才随联动刷新
      if (this.detailExpanded) {
        this.handleSearch()
      }
    },
    toggleDetail() {
      this.detailExpanded = !this.detailExpanded
      if (this.detailExpanded && this.caseRows.length === 0) {
        this.handleSearch()
      }
    },
    onProjectChange() {
      // 切换项目：明细收起并清空，回到懒加载初始态
      this.detailExpanded = false
      this.caseTypesExpanded = false
      this.caseRows = []
      this.total = 0
      this.pageNum = 1
      this.keyword = ''
      this.loadAll()
    },
    async loadOverview() {
      try {
        const res = await reportApi.projectOverview(this.projectId, this.params())
        this.overview = res.data || this.overview
        this.$emit('data-through', this.overview.dataThroughDate)
      } catch (e) {
        this.$message.error(e.message || '项目总览加载失败')
      }
    },
    async loadTopFailures() {
      try {
        const res = await reportApi.topFailures(this.projectId, {
          startDate: this.dateRange[0],
          endDate: this.dateRange[1],
          limit: 10
        })
        this.topFailures = res.data || []
      } catch (e) {
        this.$message.error(e.message || '失败TOP加载失败')
      }
    },
    async loadCases() {
      if (!this.projectId) return
      this.loading = true
      try {
        const res = await reportApi.projectCases(this.projectId, {
          startDate: this.dateRange[0],
          endDate: this.dateRange[1],
          sortField: this.sortField,
          order: this.sortOrder,
          pageNum: this.pageNum,
          pageSize: this.pageSize,
          keyword: this.keyword || undefined
        })
        this.caseRows = res.data.list || []
        this.total = res.data.total || 0
      } catch (e) {
        this.$message.error(e.message || '用例明细加载失败')
      } finally {
        this.loading = false
      }
    },
    handleSearch() {
      this.pageNum = 1
      this.loadCases()
    },
    // 服务端排序映射
    handleSortChange({ prop, order }) {
      if (!order) {
        this.sortField = 'fail'
        this.sortOrder = 'desc'
      } else {
        const map = { fail: 'fail', successRate: 'rate', executed: 'executed', lastExecDate: 'lastDate' }
        this.sortField = map[prop] || 'fail'
        this.sortOrder = order === 'ascending' ? 'asc' : 'desc'
      }
      this.loadCases()
    },
    rateClass(rate) {
      if (rate === null || rate === undefined) return 'rate-none'
      if (rate >= 90) return 'rate-good'
      if (rate >= 75) return 'rate-mid'
      return 'rate-bad'
    },
    async handleExport() {
      this.exporting = true
      try {
        await reportApi.exportProjectCases(this.projectId, {
          startDate: this.dateRange[0],
          endDate: this.dateRange[1],
          keyword: this.keyword || undefined
        })
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
.project-select-card {
  margin-bottom: 12px;
}

.project-bar {
  display: flex;
  align-items: center;
}

.project-bar .label {
  font-size: 14px;
  color: #606266;
}

.card-row {
  margin-bottom: 16px;
}

.type-row {
  margin-bottom: 16px;
}

.detail-card {
  margin-top: 16px;
}

.detail-toggle {
  font-size: 15px;
  font-weight: 600;
  padding: 0;
}

.detail-toggle-tip {
  font-size: 12px;
  font-weight: 400;
  color: #c0c4cc;
}

.toolbar {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 16px;
  flex-wrap: wrap;
  gap: 8px;
}

.toolbar-title {
  font-size: 15px;
  font-weight: 600;
}

.toolbar-right {
  display: flex;
  align-items: center;
  gap: 8px;
}

.pagination {
  margin-top: 16px;
  text-align: right;
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
