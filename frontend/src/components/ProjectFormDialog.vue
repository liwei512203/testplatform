<template>
  <el-dialog
    :title="isEdit ? '编辑项目' : '新建项目'"
    :visible.sync="innerVisible"
    width="440px"
    :close-on-click-modal="false"
    @closed="handleClosed"
  >
    <el-form ref="form" :model="form" :rules="rules" label-width="90px">
      <el-form-item label="项目名称" prop="name">
        <el-input v-model="form.name" maxlength="32" show-word-limit placeholder="请输入项目名称" />
      </el-form-item>
      <el-form-item label="系统编号" prop="systemCode">
        <el-input v-model="form.systemCode" maxlength="32" show-word-limit placeholder="如：TRADE" />
      </el-form-item>
      <el-form-item label="测试团队" prop="testTeam">
        <el-select v-model="form.testTeam" placeholder="请选择测试团队" style="width: 100%">
          <el-option v-for="team in TEST_TEAMS" :key="team" :label="team" :value="team" />
        </el-select>
      </el-form-item>
    </el-form>
    <div slot="footer">
      <el-button @click="innerVisible = false">取 消</el-button>
      <el-button type="primary" :loading="loading" @click="submit">确 定</el-button>
    </div>
  </el-dialog>
</template>

<script>
import { projectApi } from '@/api'
import { TEST_TEAMS, DEFAULT_TEST_TEAM } from '@/utils/constants'

export default {
  name: 'ProjectFormDialog',
  props: {
    visible: { type: Boolean, default: false },
    project: { type: Object, default: null }
  },
  data() {
    return {
      loading: false,
      TEST_TEAMS,
      form: { name: '', systemCode: '', testTeam: DEFAULT_TEST_TEAM },
      rules: {
        name: [{ required: true, message: '请输入项目名称', trigger: 'blur' }],
        systemCode: [{ required: true, message: '请输入系统编号', trigger: 'blur' }],
        testTeam: [{ required: true, message: '请选择测试团队', trigger: 'change' }]
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
      return !!this.project
    }
  },
  watch: {
    visible(val) {
      if (val) {
        this.form = {
          name: this.project ? this.project.name : '',
          systemCode: this.project ? this.project.systemCode : '',
          testTeam: this.project && this.project.testTeam
            ? this.project.testTeam
            : DEFAULT_TEST_TEAM
        }
        this.$nextTick(() => this.$refs.form && this.$refs.form.clearValidate())
      }
    }
  },
  methods: {
    handleClosed() {
      this.loading = false
    },
    submit() {
      this.$refs.form.validate(async valid => {
        if (!valid) return
        this.loading = true
        try {
          if (this.isEdit) {
            await projectApi.update(this.project.id, this.form)
            this.$message.success('修改成功')
          } else {
            await projectApi.create(this.form)
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
