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
          <span>版本计划</span>
          <el-tag type="info" size="small">
            {{ currentProject.name }}（{{ currentProject.systemCode }}）
          </el-tag>
        </div>
        <div class="toolbar-right">
          <el-button size="small" icon="el-icon-refresh" @click="loadPlans">刷新</el-button>
          <el-button type="primary" size="small" icon="el-icon-plus" @click="openPlanDialog()">
            新建版本计划
          </el-button>
        </div>
      </div>

      <el-table v-loading="loading" :data="plans" border stripe style="width: 100%">
        <el-table-column prop="id" label="ID" width="70" align="center" />
        <el-table-column prop="name" label="版本名称" min-width="140" show-overflow-tooltip />
        <el-table-column label="计划时间" width="170" align="center">
          <template slot-scope="scope">{{ formatTime(scope.row.createTime) }}</template>
        </el-table-column>
        <el-table-column label="用例总数" width="100" align="center">
          <template slot-scope="scope">{{ scope.row.totalCases }}</template>
        </el-table-column>
        <el-table-column label="执行统计" width="230" align="center">
          <template slot-scope="scope">
            <el-tag type="success" size="small">成功 {{ scope.row.successCount }}</el-tag>
            <el-tag type="danger" size="small" class="stat-tag">失败 {{ scope.row.failCount }}</el-tag>
            <el-tag type="info" size="small" class="stat-tag">
              未执行 {{ scope.row.unexecutedCount }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="250" align="center" fixed="right">
          <template slot-scope="scope">
            <el-button type="text" size="mini" @click="viewCases(scope.row)">查看用例</el-button>
            <el-button
              type="text"
              size="mini"
              :disabled="scope.row.totalCases === 0"
              @click="executePlan(scope.row)"
            >
              一键执行
            </el-button>
            <el-button type="text" size="mini" @click="openPlanDialog(scope.row)">编辑</el-button>
            <el-button type="text" size="mini" style="color: #f56c6c" @click="removePlan(scope.row)">
              删除
            </el-button>
          </template>
        </el-table-column>
        <template slot="empty">
          <el-empty description="暂无版本计划，请新建" :image-size="80" />
        </template>
      </el-table>

      <el-pagination
        class="pagination"
        layout="total, sizes, prev, pager, next, jumper"
        :current-page.sync="pageNum"
        :page-size.sync="pageSize"
        :page-sizes="[10, 20, 50]"
        :total="total"
        @size-change="handleSearch"
        @current-change="loadPlans"
      />
    </el-card>

    <!-- 新建/编辑版本计划 -->
    <el-dialog
      :title="isEdit ? '编辑版本计划' : '新建版本计划'"
      :visible.sync="dialogVisible"
      width="460px"
      :close-on-click-modal="false"
    >
      <el-form ref="form" :model="form" :rules="rules" label-width="90px">
        <el-form-item label="版本名称" prop="name">
          <el-input
            v-model="form.name"
            maxlength="32"
            show-word-limit
            placeholder="请输入迭代轮次，如：V1.0"
          />
        </el-form-item>
        <el-form-item label="计划时间" prop="createTime">
          <el-date-picker
            v-model="form.createTime"
            type="datetime"
            value-format="timestamp"
            placeholder="选择计划时间"
            style="width: 100%"
          />
        </el-form-item>
        <el-alert
          v-if="!isEdit"
          title="创建后将自动继承当前项目下的全部测试用例"
          type="info"
          :closable="false"
        />
      </el-form>
      <div slot="footer">
        <el-button @click="dialogVisible = false">取 消</el-button>
        <el-button type="primary" :loading="saving" @click="submitPlan">确 定</el-button>
      </div>
    </el-dialog>

    <!-- 一键执行：选择执行方式 -->
    <el-dialog
      title="一键执行"
      :visible.sync="executeDialogVisible"
      width="440px"
      :close-on-click-modal="false"
    >
      <div v-if="executeTargetPlan" class="exec-tip">
        将模拟执行「{{ executeTargetPlan.name }}」下全部
        <b>{{ executeTargetPlan.totalCases }}</b> 条用例，随机分配成功与失败，
        执行时间与计划时间保持一致。
      </div>
      <el-form label-width="90px" style="margin-top: 12px">
        <el-form-item label="执行方式">
          <el-radio-group v-model="executeForm.execType">
            <el-radio v-for="t in EXEC_TYPES" :key="t" :label="t">{{ t }}</el-radio>
          </el-radio-group>
        </el-form-item>
      </el-form>
      <div slot="footer">
        <el-button @click="executeDialogVisible = false">取 消</el-button>
        <el-button type="primary" :loading="executing" @click="confirmExecute">执 行</el-button>
      </div>
    </el-dialog>

    <!-- 计划用例列表 -->
    <el-drawer
      :title="`计划用例 - ${currentPlan ? currentPlan.name : ''}`"
      :visible.sync="caseDrawerVisible"
      direction="rtl"
      size="65%"
    >
      <div class="drawer-body">
        <div class="drawer-toolbar">
          <el-tag v-if="currentPlan" type="info" size="small">
            计划时间：{{ formatTime(currentPlan.createTime) }}
          </el-tag>
          <el-tag type="success" size="small">
            成功 {{ currentPlan ? currentPlan.successCount : 0 }}
          </el-tag>
          <el-tag type="danger" size="small">
            失败 {{ currentPlan ? currentPlan.failCount : 0 }}
          </el-tag>
          <el-tag type="info" size="small">
            未执行 {{ currentPlan ? currentPlan.unexecutedCount : 0 }}
          </el-tag>
        </div>
        <el-table
          v-loading="caseLoading"
          :data="planCases"
          border
          stripe
          height="calc(100vh - 220px)"
          style="width: 100%"
        >
          <el-table-column prop="caseId" label="用例ID" width="80" align="center" />
          <el-table-column prop="caseName" label="用例名称" min-width="180" show-overflow-tooltip />
          <el-table-column label="执行结果" width="110" align="center">
            <template slot-scope="scope">
              <el-tag :type="resultTagType(scope.row.result)" size="small">
                {{ scope.row.result }}
              </el-tag>
            </template>
          </el-table-column>
          <el-table-column label="执行方式" width="120" align="center">
            <template slot-scope="scope">
              <el-tag
                v-if="scope.row.result !== '未执行'"
                :type="execTypeTagType(scope.row.execType)"
                size="small"
              >
                {{ scope.row.execType || '手动执行' }}
              </el-tag>
              <span v-else style="color: #c0c4cc">-</span>
            </template>
          </el-table-column>
          <el-table-column label="执行时间" width="170" align="center">
            <template slot-scope="scope">
              {{ scope.row.executeTime ? formatTime(scope.row.executeTime) : '-' }}
            </template>
          </el-table-column>
          <template slot="empty">
            <span>该计划下没有测试用例</span>
          </template>
        </el-table>
        <el-pagination
          class="pagination"
          layout="total, prev, pager, next"
          :current-page.sync="casePageNum"
          :page-size="casePageSize"
          :total="caseTotal"
          @current-change="loadPlanCases"
        />
      </div>
    </el-drawer>
  </div>
</template>

<script>
import { planApi, dictApi } from '@/api'
import { formatTime } from '@/utils/case'
import { state } from '@/store/project'
import { EXEC_TYPES, DEFAULT_EXEC_TYPE, EXEC_TYPE_TAG_TYPE } from '@/utils/constants'

export default {
  name: 'VersionPlan',
  data() {
    return {
      plans: [],
      total: 0,
      pageNum: 1,
      pageSize: 10,
      loading: false,
      // 计划表单
      dialogVisible: false,
      saving: false,
      editingPlan: null,
      // 执行方式选项由字典接口驱动，本地常量仅作加载失败兜底
      EXEC_TYPES,
      form: { name: '', createTime: '' },
      rules: {
        name: [{ required: true, message: '请输入版本名称', trigger: 'blur' }],
        createTime: [{ required: true, message: '请选择计划时间', trigger: 'change' }]
      },
      // 一键执行弹窗（选择执行方式）
      executeDialogVisible: false,
      executing: false,
      executeTargetPlan: null,
      executeForm: { execType: DEFAULT_EXEC_TYPE },
      // 计划用例抽屉
      caseDrawerVisible: false,
      caseLoading: false,
      currentPlan: null,
      planCases: [],
      caseTotal: 0,
      casePageNum: 1,
      casePageSize: 10
    }
  },
  computed: {
    currentProject() {
      return state.currentProject
    },
    isEdit() {
      return !!this.editingPlan
    }
  },
  watch: {
    currentProject() {
      this.pageNum = 1
      this.loadPlans()
    }
  },
  created() {
    this.loadPlans()
  },
  methods: {
    formatTime,
    resultTagType(result) {
      if (result === '成功') return 'success'
      if (result === '失败') return 'danger'
      return 'info'
    },
    execTypeTagType(type) {
      return EXEC_TYPE_TAG_TYPE[type] || 'primary'
    },
    async loadPlans() {
      if (!this.currentProject) {
        this.plans = []
        this.total = 0
        return
      }
      this.loading = true
      try {
        const res = await planApi.list({
          projectId: this.currentProject.id,
          pageNum: this.pageNum,
          pageSize: this.pageSize
        })
        this.plans = res.data.list || []
        this.total = res.data.total || 0
      } catch (e) {
        this.$message.error(e.message || '版本计划加载失败')
      } finally {
        this.loading = false
      }
    },
    handleSearch() {
      this.pageNum = 1
      this.loadPlans()
    },
    openPlanDialog(plan) {
      this.editingPlan = plan || null
      this.form = {
        name: plan ? plan.name : '',
        createTime: plan ? plan.createTime : ''
      }
      this.dialogVisible = true
      this.$nextTick(() => this.$refs.form && this.$refs.form.clearValidate())
    },
    submitPlan() {
      this.$refs.form.validate(async valid => {
        if (!valid) return
        this.saving = true
        try {
          const payload = {
            projectId: this.currentProject.id,
            name: this.form.name,
            createTime: this.form.createTime
          }
          if (this.isEdit) {
            await planApi.update(this.editingPlan.id, payload)
            this.$message.success('修改成功')
          } else {
            await planApi.create(payload)
            this.$message.success('创建成功，已继承项目下全部测试用例')
          }
          this.dialogVisible = false
          this.loadPlans()
        } catch (e) {
          this.$message.error(e.message || '操作失败')
        } finally {
          this.saving = false
        }
      })
    },
    removePlan(plan) {
      this.$confirm(
        `确认删除版本计划「${plan.name}」？其关联的用例执行记录将一并删除。`,
        '删除确认',
        { type: 'warning' }
      )
        .then(async () => {
          await planApi.remove(plan.id)
          this.$message.success('删除成功')
          if (this.plans.length === 1 && this.pageNum > 1) {
            this.pageNum -= 1
          }
          this.loadPlans()
        })
        .catch(() => {})
    },
    async executePlan(plan) {
      // 执行方式是每次执行的属性：弹窗选择（选项来自字典，默认手动执行）
      this.executeTargetPlan = plan
      this.executeForm.execType = DEFAULT_EXEC_TYPE
      this.executeDialogVisible = true
      try {
        const res = await dictApi.execTypes()
        const types = res.data || []
        if (types.length) {
          this.EXEC_TYPES = types
          if (!types.includes(this.executeForm.execType)) {
            this.executeForm.execType = types[0]
          }
        }
      } catch (e) {
        // 字典加载失败时保留本地常量兜底
      }
    },
    async confirmExecute() {
      const plan = this.executeTargetPlan
      if (!plan) return
      this.executing = true
      try {
        const res = await planApi.execute(plan.id, { execType: this.executeForm.execType })
        const { success, fail } = res.data
        this.$message.success(
          `执行完成（${this.executeForm.execType}）：成功 ${success} 条，失败 ${fail} 条`
        )
        this.executeDialogVisible = false
        this.loadPlans()
        if (this.caseDrawerVisible && this.currentPlan && this.currentPlan.id === plan.id) {
          this.loadPlanCases()
        }
      } catch (e) {
        this.$message.error(e.message || '执行失败')
      } finally {
        this.executing = false
      }
    },
    async viewCases(plan) {
      this.currentPlan = { ...plan }
      this.caseDrawerVisible = true
      this.casePageNum = 1
      this.loadPlanCases()
    },
    async loadPlanCases() {
      if (!this.currentPlan) return
      this.caseLoading = true
      try {
        const res = await planApi.cases(this.currentPlan.id, {
          pageNum: this.casePageNum,
          pageSize: this.casePageSize
        })
        this.planCases = res.data.list || []
        this.caseTotal = res.data.total || 0
      } catch (e) {
        this.$message.error(e.message || '计划用例加载失败')
      } finally {
        this.caseLoading = false
      }
    }
  }
}
</script>

<style scoped>
.page {
  min-height: 100%;
}

.exec-tip {
  font-size: 13px;
  color: #606266;
  line-height: 1.8;
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

.stat-tag {
  margin-left: 4px;
}

.pagination {
  margin-top: 16px;
  text-align: right;
}

.drawer-body {
  padding: 0 16px 16px;
}

.drawer-toolbar {
  display: flex;
  align-items: center;
  gap: 8px;
  margin-bottom: 12px;
  flex-wrap: wrap;
}
</style>
