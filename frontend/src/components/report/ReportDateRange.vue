<template>
  <el-date-picker
    :value="value"
    type="daterange"
    value-format="yyyy-MM-dd"
    range-separator="至"
    start-placeholder="开始日期"
    end-placeholder="结束日期"
    :picker-options="pickerOptions"
    :clearable="false"
    style="width: 280px"
    @input="handleChange"
  />
</template>

<script>
const MAX_RANGE_DAYS = 366

export default {
  name: 'ReportDateRange',
  props: {
    value: { type: Array, default: () => [] }
  },
  data() {
    return {
      pickerOptions: {
        // 禁用未来日期 + 选择第一个日期后限制 366 天跨度
        onPick: ({ minDate }) => {
          this.pickerMinDate = minDate
        },
        disabledDate: time => {
          if (time.getTime() > Date.now()) return true
          if (this.pickerMinDate) {
            const oneDay = 24 * 3600 * 1000
            const minTime = this.pickerMinDate.getTime()
            return (
              time.getTime() > minTime + (MAX_RANGE_DAYS - 1) * oneDay ||
              time.getTime() < minTime - (MAX_RANGE_DAYS - 1) * oneDay
            )
          }
          return false
        }
      },
      pickerMinDate: null
    }
  },
  methods: {
    handleChange(val) {
      if (val && val[0] && val[1]) {
        const days =
          (new Date(val[1]).getTime() - new Date(val[0]).getTime()) / (24 * 3600 * 1000) + 1
        if (days > MAX_RANGE_DAYS) {
          this.$message.warning(`时间范围不能超过 ${MAX_RANGE_DAYS} 天`)
          return
        }
      }
      this.pickerMinDate = null
      this.$emit('input', val)
      this.$emit('change', val)
    }
  }
}
</script>
