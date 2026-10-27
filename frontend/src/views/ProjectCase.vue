<template>
  <div class="page">
    <div v-if="!currentProject" class="empty-project">
      <el-empty description="尚未选择项目，请先新建或切换项目">
        <el-button type="primary" size="small" @click="$router.push('/projects')">
          前往项目列表
        </el-button>
      </el-empty>
    </div>

    <el-card v-else shadow="never">
      <div class="toolbar">
        <div class="toolbar-title">
          <span>测试用例</span>
          <el-tag type="info" size="small">
            {{ currentProject.name }}（{{ currentProject.systemCode }}）
          </el-tag>
        </div>
        <div class="toolbar-right">
          <el-input
            v-model="keyword"
            size="small"
            placeholder="搜索用例名称/内容"
            prefix-icon="el-icon-search"
            clearable
            style="width: 240px"
            @keyup.enter.native="handleSearch"
            @clear="handleSearch"
          />
          <el-button size="small" icon="el-icon-refresh" @click="loadCases">刷新</el-button>
          <el-button type="primary" size="small" icon="el-icon-plus" @click="openCaseDialog()">
            新建用例
          </el-button>
        </div>
      </div>

      <el-table v-loading="loading" :data="cases" border stripe style="width: 100%">
        <el-table-column prop="id" label="ID" width="80" align="center" />
        <el-table-column prop="name" label="用例名称" min-width="180" show-overflow-tooltip />
        <el-table-column label="用例内容" min-width="260" show-overflow-tooltip>
          <template slot-scope="scope">
            <span>{{ contentSummary(scope.row.content) || '-' }}</span>
          </template>
        </el-table-column>
        <el-table-column label="创建时间" width="170" align="center">
          <template slot-scope="scope">{{ formatTime(scope.row.createTime) }}</template>
        </el-table-column>
        <el-table-column label="更新时间" width="170" align="center">
          <template slot-scope="scope">{{ formatTime(scope.row.updateTime) }}</template>
        </el-table-column>
        <el-table-column label="操作" width="150" align="center" fixed="right">
          <template slot-scope="scope">
            <el-button type="text" size="mini" @click="viewCase(scope.row)">查看</el-button>
            <el-button type="text" size="mini" @click="openCaseDialog(scope.row)">编辑</el-button>
            <el-button type="text" size="mini" style="color: #f56c6c" @click="removeCase(scope.row)">
              删除
            </el-button>
          </template>
        </el-table-column>
        <template slot="empty">
          <span>暂无用例数据</span>
        </template>
      </el-table>

      <el-pagination
        class="pagination"
        layout="total, sizes, prev, pager, next, jumper"
        :current-page.sync="pageNum"
        :page-size.sync="pageSize"
        :page-sizes="[10, 20, 50, 100]"
        :total="total"
        @size-change="handleSearch"
        @current-change="loadCases"
      />
    </el-card>

    <!-- 用例表单 -->
    <case-form-dialog
      :visible.sync="caseDialogVisible"
      :test-case="editingCase"
      :project-id="currentProject ? currentProject.id : ''"
      :projects="projects"
      @saved="handleCaseSaved"
    />

    <!-- 用例详情 -->
    <el-drawer title="用例详情" :visible.sync="detailVisible" direction="rtl" size="520px">
      <div v-if="detail" class="detail">
        <div class="detail-item">
          <div class="detail-label">用例名称</div>
          <div>{{ detail.name }}</div>
        </div>
        <div class="detail-item">
          <div class="detail-label">前置条件</div>
          <div class="detail-value">{{ detail.parsed.precondition || '-' }}</div>
        </div>
        <div class="detail-item">
          <div class="detail-label">测试步骤</div>
          <ol class="detail-steps">
            <li v-for="(s, i) in detail.parsed.steps" :key="i">{{ s }}</li>
          </ol>
        </div>
        <div class="detail-item">
          <div class="detail-label">预期结果</div>
          <div class="detail-value">{{ detail.parsed.expected || '-' }}</div>
        </div>
        <div class="detail-item">
          <div class="detail-label">原始 JSON</div>
          <pre class="detail-json">{{ detail.formatted }}</pre>
        </div>
      </div>
    </el-drawer>
  </div>
</template>

<script>
import { caseApi } from '@/api'
import { formatTime, contentSummary, parseContent } from '@/utils/case'
import { state } from '@/store/project'
import CaseFormDialog from '@/components/CaseFormDialog.vue'

export default {
  name: 'ProjectCase',
  components: { CaseFormDialog },
  data() {
    return {
      cases: [],
      total: 0,
      pageNum: 1,
      pageSize: 10,
      keyword: '',
      loading: false,
      caseDialogVisible: false,
      editingCase: null,
      detailVisible: false,
      detail: null
    }
  },
  computed: {
    currentProject() {
      return state.currentProject
    },
    projects() {
      return state.projects
    }
  },
  watch: {
    // 顶部下拉切换项目后自动刷新用例列表
    currentProject() {
      this.pageNum = 1
      this.loadCases()
    }
  },
  created() {
    this.loadCases()
  },
  methods: {
    formatTime,
    contentSummary,
    async loadCases() {
      if (!this.currentProject) {
        this.cases = []
        this.total = 0
        return
      }
      this.loading = true
      try {
        const res = await caseApi.list({
          projectId: this.currentProject.id,
          keyword: this.keyword || undefined,
          pageNum: this.pageNum,
          pageSize: this.pageSize
        })
        this.cases = res.data.list || []
        this.total = res.data.total || 0
      } catch (e) {
        this.$message.error(e.message || '用例列表加载失败')
      } finally {
        this.loading = false
      }
    },
    handleSearch() {
      this.pageNum = 1
      this.loadCases()
    },
    openCaseDialog(testCase) {
      this.editingCase = testCase || null
      this.caseDialogVisible = true
    },
    handleCaseSaved() {
      this.loadCases()
    },
    removeCase(row) {
      this.$confirm(`确认删除用例「${row.name}」？`, '删除确认', { type: 'warning' })
        .then(async () => {
          await caseApi.remove(row.id)
          this.$message.success('删除成功')
          if (this.cases.length === 1 && this.pageNum > 1) {
            this.pageNum -= 1
          }
          this.loadCases()
        })
        .catch(() => {})
    },
    viewCase(row) {
      const parsed = parseContent(row.content)
      let formatted = row.content
      try {
        formatted = JSON.stringify(JSON.parse(row.content), null, 2)
      } catch (e) {
        formatted = row.content || ''
      }
      this.detail = { ...row, parsed, formatted }
      this.detailVisible = true
    }
  }
}
</script>

<style scoped>
.page {
  min-height: 100%;
}

.empty-project {
  background-color: #fff;
  border-radius: 4px;
  padding: 60px 0;
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

.pagination {
  margin-top: 16px;
  text-align: right;
}

.detail {
  padding: 0 16px 16px;
}

.detail-item {
  margin-bottom: 16px;
}

.detail-label {
  font-weight: 600;
  margin-bottom: 6px;
  color: #606266;
}

.detail-value {
  white-space: pre-wrap;
  word-break: break-all;
}

.detail-steps {
  margin: 0;
  padding-left: 20px;
}

.detail-steps li {
  margin-bottom: 4px;
}

.detail-json {
  background-color: #f5f7fa;
  padding: 10px;
  border-radius: 4px;
  white-space: pre-wrap;
  word-break: break-all;
  margin: 0;
}
</style>
