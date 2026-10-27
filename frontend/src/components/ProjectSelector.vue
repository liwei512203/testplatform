<template>
  <el-select
    v-model="selectedId"
    filterable
    size="small"
    placeholder="请选择项目"
    class="project-select"
    @change="handleChange"
  >
    <el-option v-for="p in projects" :key="p.id" :label="p.name" :value="p.id">
      <span class="option-name">{{ p.name }}</span>
      <span class="option-code">{{ p.systemCode }}</span>
    </el-option>
    <!-- 下拉框底部的“项目列表”入口 -->
    <el-option :value="MANAGE_VALUE" label="项目列表" class="manage-option">
      <i class="el-icon-s-grid" />
      <span>项目列表</span>
    </el-option>
  </el-select>
</template>

<script>
import { state, setCurrentProject } from '@/store/project'

const MANAGE_VALUE = '__manage__'

export default {
  name: 'ProjectSelector',
  data() {
    return {
      MANAGE_VALUE,
      selectedId: ''
    }
  },
  computed: {
    projects() {
      return state.projects
    },
    currentProject() {
      return state.currentProject
    }
  },
  watch: {
    currentProject: {
      immediate: true,
      handler(val) {
        this.selectedId = val ? val.id : ''
      }
    }
  },
  methods: {
    handleChange(val) {
      if (val === MANAGE_VALUE) {
        // 还原下拉框显示值为当前项目，避免选中“项目列表”
        this.$nextTick(() => {
          this.selectedId = this.currentProject ? this.currentProject.id : ''
        })
        this.$router.push('/projects')
        return
      }
      const project = this.projects.find(p => p.id === val)
      if (project) {
        setCurrentProject(project)
        this.$emit('change', project)
      }
    }
  }
}
</script>

<style scoped>
.project-select {
  width: 260px;
}

.option-name {
  float: left;
}

.option-code {
  float: right;
  color: #8492a6;
  font-size: 12px;
  margin-left: 12px;
}

.manage-option {
  border-top: 1px solid #ebeef5;
  color: #409eff;
}
</style>
