<template>
  <el-dialog
    :title="isEdit ? '编辑用例' : '新建用例'"
    :visible.sync="innerVisible"
    width="680px"
    :close-on-click-modal="false"
    @closed="handleClosed"
  >
    <el-form ref="form" :model="form" :rules="rules" label-width="90px">
      <el-form-item label="所属项目" prop="projectId">
        <el-select v-model="form.projectId" placeholder="请选择项目" style="width: 100%">
          <el-option
            v-for="p in projects"
            :key="p.id"
            :label="`${p.name}（${p.systemCode}）`"
            :value="p.id"
          />
        </el-select>
      </el-form-item>
      <el-form-item label="用例名称" prop="name">
        <el-input v-model="form.name" maxlength="32" show-word-limit placeholder="请输入用例名称" />
      </el-form-item>
      <el-form-item label="前置条件">
        <el-input
          v-model="form.precondition"
          type="textarea"
          :rows="2"
          placeholder="请输入前置条件"
        />
      </el-form-item>
      <el-form-item label="测试步骤">
        <div v-for="(step, index) in form.steps" :key="index" class="step-row">
          <span class="step-index">{{ index + 1 }}</span>
          <el-input v-model="form.steps[index]" placeholder="请输入测试步骤" />
          <el-button
            type="danger"
            icon="el-icon-delete"
            circle
            plain
            :disabled="form.steps.length === 1"
            @click="removeStep(index)"
          />
        </div>
        <el-button type="text" icon="el-icon-plus" @click="addStep">添加步骤</el-button>
      </el-form-item>
      <el-form-item label="预期结果">
        <el-input v-model="form.expected" type="textarea" :rows="2" placeholder="请输入预期结果/断言" />
      </el-form-item>
      <el-form-item label="JSON内容">
        <el-input v-model="jsonPreview" type="textarea" :rows="5" readonly />
      </el-form-item>
    </el-form>
    <div slot="footer">
      <el-button @click="innerVisible = false">取 消</el-button>
      <el-button type="primary" :loading="loading" @click="submit">确 定</el-button>
    </div>
  </el-dialog>
</template>

<script>
import { caseApi } from '@/api'
import { parseContent, buildContent } from '@/utils/case'

export default {
  name: 'CaseFormDialog',
  props: {
    visible: { type: Boolean, default: false },
    testCase: { type: Object, default: null },
    projectId: { type: String, default: '' },
    projects: { type: Array, default: () => [] }
  },
  data() {
    return {
      loading: false,
      form: {
        projectId: '',
        name: '',
        precondition: '',
        steps: [''],
        expected: ''
      },
      rules: {
        projectId: [{ required: true, message: '请选择所属项目', trigger: 'change' }],
        name: [{ required: true, message: '请输入用例名称', trigger: 'blur' }]
      }
    }
  },
  computed: {
    innerVisible: {
      get() {
        return this.visible
      },
      set(val) {
        this.$emit('update:visible', val)
      }
    },
    isEdit() {
      return !!this.testCase
    },
    jsonPreview() {
      return buildContent(this.form)
    }
  },
  watch: {
    visible(val) {
      if (!val) return
      if (this.testCase) {
        const parsed = parseContent(this.testCase.content)
        this.form = {
          projectId: this.testCase.projectId,
          name: this.testCase.name,
          precondition: parsed.precondition,
          steps: parsed.steps.slice(),
          expected: parsed.expected
        }
      } else {
        this.form = {
          projectId: this.projectId,
          name: '',
          precondition: '',
          steps: [''],
          expected: ''
        }
      }
      this.$nextTick(() => this.$refs.form && this.$refs.form.clearValidate())
    }
  },
  methods: {
    handleClosed() {
      this.loading = false
    },
    addStep() {
      this.form.steps.push('')
    },
    removeStep(index) {
      this.form.steps.splice(index, 1)
      if (!this.form.steps.length) this.form.steps.push('')
    },
    submit() {
      this.$refs.form.validate(async valid => {
        if (!valid) return
        this.loading = true
        try {
          const payload = {
            projectId: this.form.projectId,
            name: this.form.name,
            content: buildContent(this.form)
          }
          if (this.isEdit) {
            await caseApi.update(this.testCase.id, payload)
            this.$message.success('修改成功')
          } else {
            await caseApi.create(payload)
            this.$message.success('新建成功')
          }
          this.innerVisible = false
          this.$emit('saved')
        } catch (e) {
          this.$message.error(e.message || '操作失败')
        } finally {
          this.loading = false
        }
      })
    }
  }
}
</script>

<style scoped>
.step-row {
  display: flex;
  align-items: center;
  margin-bottom: 8px;
}

.step-index {
  width: 24px;
  color: #909399;
  text-align: center;
}

.step-row .el-input {
  flex: 1;
  margin: 0 8px;
}
</style>
