<template>
  <div class="page">
    <el-card shadow="never" class="filter-card">
      <div class="filter-bar">
        <report-date-range v-model="dateRange" @change="handleRefresh" />
        <span class="data-through">数据截至：{{ dataThroughDate || '-' }}</span>
      </div>
    </el-card>

    <el-tabs v-model="activeTab" class="report-tabs">
      <el-tab-pane label="系统报表" name="system">
        <system-report v-if="activeTab === 'system'" :date-range="dateRange" @data-through="onDataThrough" />
      </el-tab-pane>
      <el-tab-pane label="团队报表" name="team">
        <team-report v-if="activeTab === 'team'" :date-range="dateRange" @data-through="onDataThrough" />
      </el-tab-pane>
      <el-tab-pane label="项目报表" name="project">
        <project-report v-if="activeTab === 'project'" :date-range="dateRange" @data-through="onDataThrough" />
      </el-tab-pane>
    </el-tabs>
  </div>
</template>

<script>
import ReportDateRange from '@/components/report/ReportDateRange.vue'
import SystemReport from '@/views/report/SystemReport.vue'
import TeamReport from '@/views/report/TeamReport.vue'
import ProjectReport from '@/views/report/ProjectReport.vue'

function fmt(d) {
  const pad = n => String(n).padStart(2, '0')
  return d.getFullYear() + '-' + pad(d.getMonth() + 1) + '-' + pad(d.getDate())
}

export default {
  name: 'ReportCenter',
  components: { ReportDateRange, SystemReport, TeamReport, ProjectReport },
  data() {
    return {
      dateRange: [
        fmt(new Date(Date.now() - 29 * 24 * 3600 * 1000)),
        fmt(new Date(Date.now() - 24 * 3600 * 1000))
      ],
      activeTab: 'system',
      dataThroughDate: ''
    }
  },
  methods: {
    onDataThrough(date) {
      if (date) this.dataThroughDate = date
    },
    handleRefresh() {
      // 日期变化后由各子组件 watch dateRange 自行刷新
    }
  }
}
</script>

<style scoped>
.page {
  min-height: 100%;
}

.filter-card {
  margin-bottom: 12px;
}

.filter-bar {
  display: flex;
  align-items: center;
  gap: 16px;
}

.data-through {
  font-size: 13px;
  color: #909399;
}

.report-tabs ::v-deep .el-tabs__header {
  margin-bottom: 12px;
  background: #fff;
  padding: 4px 16px 0;
  border-radius: 4px;
}

.report-tabs ::v-deep .el-tabs__content {
  padding: 0;
}
</style>
