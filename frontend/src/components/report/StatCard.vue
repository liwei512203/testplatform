<template>
  <el-card shadow="hover" class="stat-card">
    <div class="stat-value">
      {{ displayValue }}<span class="stat-suffix">{{ card.suffix }}</span>
    </div>
    <div class="stat-label">{{ card.label }}</div>
    <div v-if="card.changeRate !== null && card.changeRate !== undefined" class="stat-change" :class="changeClass">
      <i :class="arrowIcon" />
      {{ changeText }}%
      <span class="stat-prev">上期 {{ prevDisplay }}</span>
    </div>
    <div v-else class="stat-change stat-none">—</div>
  </el-card>
</template>

<script>
export default {
  name: 'StatCard',
  props: {
    card: { type: Object, required: true }
  },
  computed: {
    displayValue() {
      if (this.card.value === null || this.card.value === undefined) return '-'
      return Number(this.card.value).toLocaleString()
    },
    prevDisplay() {
      if (this.card.prevValue === null || this.card.prevValue === undefined) return '-'
      return Number(this.card.prevValue).toLocaleString()
    },
    changeText() {
      const v = Number(this.card.changeRate)
      return (v > 0 ? '+' : '') + v.toFixed(2)
    },
    isUp() {
      return Number(this.card.changeRate) > 0
    },
    // 环比方向语义：down-good（失败类）指标上升为劣化(红)；其余上升为改善(绿)
    changeClass() {
      if (Number(this.card.changeRate) === 0) return 'flat'
      const good = this.isUp ? this.card.direction === 'up-good' : this.card.direction !== 'up-good'
      return good ? 'good' : 'bad'
    },
    arrowIcon() {
      if (Number(this.card.changeRate) === 0) return 'el-icon-minus'
      return this.isUp ? 'el-icon-top' : 'el-icon-bottom'
    }
  }
}
</script>

<style scoped>
.stat-card {
  text-align: center;
}

.stat-value {
  font-size: 26px;
  font-weight: 600;
  color: #303133;
}

.stat-suffix {
  font-size: 14px;
  margin-left: 2px;
  color: #606266;
}

.stat-label {
  margin-top: 6px;
  font-size: 13px;
  color: #909399;
}

.stat-change {
  margin-top: 8px;
  font-size: 12px;
}

.stat-change.good {
  color: #67c23a;
}

.stat-change.bad {
  color: #f56c6c;
}

.stat-change.flat,
.stat-none {
  color: #c0c4cc;
}

.stat-prev {
  margin-left: 6px;
  color: #c0c4cc;
}
</style>
