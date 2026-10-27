<template>
  <div class="page">
    <el-card shadow="never">
      <div class="toolbar">
        <div class="toolbar-title">
          <span>项目列表</span>
          <el-tag size="small" type="info">共 {{ filteredProjects.length }} 个项目</el-tag>
        </div>
        <div class="toolbar-right">
          <el-select
            v-model="testTeam"
            size="small"
            clearable
            placeholder="全部测试团队"
            style="width: 150px"
          >
            <el-option v-for="team in TEST_TEAMS" :key="team" :label="team" :value="team" />
          </el-select>
          <el-input
            v-model="keyword"
            size="small"
            placeholder="搜索项目名称/系统编号"
            prefix-icon="el-icon-search"
            clearable
            style="width: 240px"
          />
          <el-button size="small" icon="el-icon-refresh" @click="loadProjects">刷新</el-button>
          <el-button type="primary" size="small" icon="el-icon-plus" @click="openDialog()">
            新建项目
          </el-button>
        </div>
      </div>

      <el-table v-loading="loading" :data="filteredProjects" border stripe style="width: 100%">
        <el-table-column prop="name" label="项目名称" min-width="200" show-overflow-tooltip />
        <el-table-column prop="systemCode" label="系统编号" min-width="140" show-overflow-tooltip />
        <el-table-column label="测试团队" width="140" align="center">
          <template slot-scope="scope">
            <el-tag :type="teamTagType(scope.row.testTeam)" size="small">
              {{ scope.row.testTeam || '-' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="状态" width="120" align="center">
          <template slot-scope="scope">
            <el-tag
              v-if="currentProject && currentProject.id === scope.row.id"
              type="success"
              size="small"
            >
              当前项目
            </el-tag>
            <span v-else style="color: #909399">-</span>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="260" align="center" fixed="right">
          <template slot-scope="scope">
            <el-button
              type="text"
              size="mini"
              :disabled="currentProject && currentProject.id === scope.row.id"
              @click="switchProject(scope.row)"
            >
              切换项目
            </el-button>
            <el-button type="text" size="mini" @click="openDialog(scope.row)">编辑</el-button>
            <el-button
              type="text"
              size="mini"
              style="color: #f56c6c"
              @click="removeProject(scope.row)"
            >
              删除
            </el-button>
          </template>
        </el-table-column>
        <template slot="empty">
          <span>暂无项目，请先新建项目</span>
        </template>
      </el-table>
    </el-card>

    <project-form-dialog
      :visible.sync="dialogVisible"
      :project="editingProject"
      @saved="handleSaved"
    />
  </div>
</template>

<script>
import { projectApi } from '@/api'
import { state, setCurrentProject, refreshCurrentProject } from '@/store/project'
import { TEST_TEAMS, TEST_TEAM_TAG_TYPE } from '@/utils/constants'
import ProjectFormDialog from '@/components/ProjectFormDialog.vue'

export default {
  name: 'ProjectList',
  components: { ProjectFormDialog },
  data() {
    return {
      loading: false,
      keyword: '',
      testTeam: '',
      TEST_TEAMS,
      dialogVisible: false,
      editingProject: null
    }
  },
  computed: {
    currentProject() {
      return state.currentProject
    },
    filteredProjects() {
      const kw = this.keyword.trim().toLowerCase()
      return state.projects.filter(p => {
        const matchTeam = !this.testTeam || p.testTeam === this.testTeam
        const matchKeyword =
          !kw ||
          p.name.toLowerCase().includes(kw) ||
          p.systemCode.toLowerCase().includes(kw)
        return matchTeam && matchKeyword
      })
    }
  },
  created() {
    this.loadProjects()
  },
  methods: {
    teamTagType(team) {
      return TEST_TEAM_TAG_TYPE[team] || 'info'
    },
    async loadProjects() {
      this.loading = true
      try {
        const res = await projectApi.list()
        state.projects = res.data || []
        refreshCurrentProject()
      } catch (e) {
        this.$message.error(e.message || '项目列表加载失败')
      } finally {
        this.loading = false
      }
    },
    openDialog(project) {
      this.editingProject = project || null
      this.dialogVisible = true
    },
    handleSaved() {
      this.loadProjects()
    },
    switchProject(project) {
      setCurrentProject(project)
      this.$message.success(`已切换到项目「${project.name}」`)
      this.$router.push('/cases')
    },
    removeProject(project) {
      this.$confirm(
        `删除项目「${project.name}」会同时删除该项目下的所有业务数据，是否继续？`,
        '删除确认',
        { type: 'warning' }
      )
        .then(async () => {
          await projectApi.remove(project.id)
          this.$message.success('删除成功')
          await this.loadProjects()
          if (this.currentProject && this.currentProject.id === project.id) {
            setCurrentProject(state.projects[0] || null)
          }
        })
        .catch(() => {})
    }
  }
}
</script>

<style scoped>
.page {
  height: 100%;
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
  display: flex;
  align-items: center;
  gap: 8px;
  font-size: 16px;
  font-weight: 600;
}

.toolbar-right {
  display: flex;
  align-items: center;
  gap: 8px;
}
</style>
