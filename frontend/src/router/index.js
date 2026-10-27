import Vue from 'vue'
import VueRouter from 'vue-router'
import ProjectCase from '@/views/ProjectCase.vue'
import ProjectList from '@/views/ProjectList.vue'
import VersionPlan from '@/views/VersionPlan.vue'
import ReportCenter from '@/views/report/ReportCenter.vue'

Vue.use(VueRouter)

const routes = [
  { path: '/', redirect: '/cases' },
  { path: '/cases', name: 'cases', component: ProjectCase },
  { path: '/projects', name: 'projects', component: ProjectList },
  { path: '/version-plans', name: 'versionPlans', component: VersionPlan },
  { path: '/reports', name: 'reports', component: ReportCenter }
]

export default new VueRouter({
  routes
})
