<template>
  <div ref="chart" class="trend-chart" />
</template>

<script>
import * as echarts from 'echarts/core'
import { LineChart, BarChart } from 'echarts/charts'
import {
  GridComponent,
  TooltipComponent,
  LegendComponent
} from 'echarts/components'
import { CanvasRenderer } from 'echarts/renderers'

echarts.use([LineChart, BarChart, GridComponent, TooltipComponent, LegendComponent, CanvasRenderer])

/**
 * 双轴趋势图：柱=执行次数（左轴），线=成功率（右轴）；
 * 支持上一周期对照（浅色柱/虚线，按日期对齐，无对应日期为 null）
 */
export default {
  name: 'TrendChart',
  props: {
    trend: { type: Array, default: () => [] },
    prevTrend: { type: Array, default: () => [] },
    granularity: { type: String, default: 'day' }
  },
  data() {
    return { chart: null }
  },
  watch: {
    trend() {
      this.render()
    },
    prevTrend() {
      this.render()
    }
  },
  mounted() {
    this.chart = echarts.init(this.$refs.chart)
    this.render()
    window.addEventListener('resize', this.handleResize)
    this.$once('hook:beforeDestroy', () => {
      window.removeEventListener('resize', this.handleResize)
      if (this.chart) {
        this.chart.dispose()
        this.chart = null
      }
    })
  },
  methods: {
    handleResize() {
      if (this.chart) this.chart.resize()
    },
    render() {
      if (!this.chart) return
      const labels = this.trend.map(p => p.date)
      const executed = this.trend.map(p => p.executed)
      const rates = this.trend.map(p => (p.successRate === null ? null : p.successRate))
      const hasPrev = this.prevTrend && this.prevTrend.length > 0

      // 上期数据按日期与当前横轴对齐（日期不存在则该点为 null）
      const prevMap = {}
      this.prevTrend.forEach(p => {
        prevMap[p.date] = p
      })
      const prevExecAligned = labels.map(d => (prevMap[d] ? prevMap[d].executed : null))
      const prevRateAligned = labels.map(
        d => (prevMap[d] && prevMap[d].successRate !== null ? prevMap[d].successRate : null)
      )

      const series = [
        {
          name: '执行次数',
          type: 'bar',
          data: executed,
          barMaxWidth: 24,
          itemStyle: { color: '#409eff' }
        },
        {
          name: '成功率',
          type: 'line',
          yAxisIndex: 1,
          data: rates,
          smooth: true,
          connectNulls: true,
          itemStyle: { color: '#67c23a' }
        }
      ]
      const legend = ['执行次数', '成功率']
      if (hasPrev) {
        series.push(
          {
            name: '上期执行',
            type: 'bar',
            data: prevExecAligned,
            barMaxWidth: 24,
            barGap: '-100%',
            itemStyle: { color: '#a0cfff', opacity: 0.5 },
            silent: true
          },
          {
            name: '上期成功率',
            type: 'line',
            yAxisIndex: 1,
            data: prevRateAligned,
            smooth: true,
            connectNulls: true,
            lineStyle: { type: 'dashed', color: '#b3e19d' },
            itemStyle: { color: '#b3e19d' },
            symbol: 'none'
          }
        )
        legend.push('上期执行', '上期成功率')
      }

      this.chart.setOption(
        {
          tooltip: { trigger: 'axis' },
          legend: { data: legend },
          grid: { left: 56, right: 56, top: 40, bottom: 30 },
          xAxis: {
            type: 'category',
            data: labels,
            axisLabel: {
              rotate: labels.length > 20 ? 45 : 0,
              formatter: v => v.slice(5)
            }
          },
          yAxis: [
            {
              type: 'value',
              name: '执行次数',
              minInterval: 1
            },
            {
              type: 'value',
              name: '成功率',
              min: 0,
              max: 100,
              axisLabel: { formatter: '{value}%' }
            }
          ],
          series
        },
        true
      )
    }
  }
}
</script>

<style scoped>
.trend-chart {
  width: 100%;
  height: 320px;
}
</style>
