<template>
  <div>
    <el-row :gutter="16" class="card-row">
      <el-col v-for="card in overview.cards" :key="card.key" :span="span">
        <stat-card :card="card" />
      </el-col>
    </el-row>

    <el-card shadow="never">
      <div slot="header">
        每日执行趋势
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

    <el-card shadow="never" class="type-card">
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
  </div>
</template>

<script>
import { reportApi } from '@/api/report'
import StatCard from '@/components/report/StatCard.vue'
import TrendChart from '@/components/report/TrendChart.vue'

export default {
  name: 'SystemReport',
  components: { StatCard, TrendChart },
  props: {
    dateRange: { type: Array, required: true }
  },
  data() {
    return {
      overview: { cards: [], trend: [], prevTrend: [], typeStats: [], granularity: 'day', dataThroughDate: '' },
      loading: false
    }
  },
  computed: {
    span() {
      const n = (this.overview.cards || []).length || 5
      return Math.floor(24 / n)
    }
  },
  watch: {
    dateRange: {
      immediate: true,
      handler() {
        this.load()
      }
    }
  },
  methods: {
    rateClass(rate) {
      if (rate === null || rate === undefined) return 'rate-none'
      if (rate >= 90) return 'rate-good'
      if (rate >= 75) return 'rate-mid'
      return 'rate-bad'
    },
    async load() {
      if (!this.dateRange || this.dateRange.length !== 2) return
      this.loading = true
      try {
        const res = await reportApi.systemOverview({
          startDate: this.dateRange[0],
          endDate: this.dateRange[1]
        })
        this.overview = res.data || this.overview
        this.$emit('data-through', this.overview.dataThroughDate)
      } catch (e) {
        this.$message.error(e.message || '报表加载失败')
      } finally {
        this.loading = false
      }
    }
  }
}
</script>

<style scoped>
.card-row {
  margin-bottom: 16px;
}

.type-card {
  margin-top: 16px;
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
</style>
