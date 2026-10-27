<template>
  <div class="layout">
    <el-container class="layout-container">
      <el-header class="header">
        <div class="header-left">
          <span class="logo">测试管理平台</span>
          <project-selector />
          <el-button size="small" icon="el-icon-s-grid" @click="$router.push('/projects')">
            项目列表
          </el-button>
          <el-tag v-if="currentProject" type="info" size="small" effect="plain">
            {{ currentProject.name }}（{{ currentProject.systemCode }}）·
            {{ currentProject.testTeam || '未分配团队' }}
          </el-tag>
          <el-tag v-else type="warning" size="small" effect="plain">未选择项目</el-tag>
        </div>
      </el-header>

      <el-container class="body">
        <el-aside width="200px"><side-menu /></el-aside>
        <el-main class="main"><router-view /></el-main>
      </el-container>
    </el-container>
  </div>
</template>

<script>
import ProjectSelector from '@/components/ProjectSelector.vue'
import SideMenu from '@/components/SideMenu.vue'
import { state, loadProjects } from '@/store/project'

export default {
  name: 'App',
  components: { ProjectSelector, SideMenu },
  computed: {
    currentProject() {
      return state.currentProject
    }
  },
  created() {
    loadProjects().catch(e => {
      this.$message.error(e.message || '项目列表加载失败')
    })
  }
}
</script>

<style scoped>
.layout {
  height: 100vh;
}

.layout-container {
  height: 100%;
}

.header {
  display: flex;
  align-items: center;
  background-color: #fff;
  border-bottom: 1px solid #ebeef5;
  box-shadow: 0 1px 4px rgba(0, 21, 41, 0.08);
  z-index: 10;
}

.header-left {
  display: flex;
  align-items: center;
  gap: 12px;
}

.logo {
  font-size: 17px;
  font-weight: 600;
  color: #303133;
}

.body {
  height: calc(100vh - 60px);
}

.main {
  padding: 16px;
  background-color: #f5f7fa;
  overflow-y: auto;
}
</style>
