<template>
  <div class="side-menu">
    <el-menu
      :default-active="activePath"
      router
      class="menu"
    >
      <el-menu-item index="/cases">
        <i class="el-icon-s-order" />
        <span slot="title">测试用例</span>
      </el-menu-item>
      <el-menu-item index="/version-plans">
        <i class="el-icon-s-flag" />
        <span slot="title">版本计划</span>
      </el-menu-item>
      <el-menu-item index="/reports">
        <i class="el-icon-s-data" />
        <span slot="title">数据报表</span>
      </el-menu-item>
    </el-menu>
  </div>
</template>

<script>
export default {
  name: 'SideMenu',
  computed: {
    activePath() {
      return this.$route.path
    }
  }
}
</script>

<style scoped>
.side-menu {
  height: 100%;
  background-color: #fff;
  border-right: 1px solid #ebeef5;
  overflow-y: auto;
}

.menu {
  border-right: none;
}
</style>
