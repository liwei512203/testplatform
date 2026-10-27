import Vue from 'vue'
import { projectApi } from '@/api'

const STORAGE_KEY = 'tp_current_project_id'

/**
 * 全局项目状态（轻量 store）：
 * - projects：所有项目，用于顶部下拉选择
 * - currentProject：当前所在项目，所有业务数据都归属于它
 */
export const state = Vue.observable({
  projects: [],
  currentProject: null
})

/**
 * 加载项目列表，并按本地缓存恢复上次选择的项目
 */
export async function loadProjects() {
  const res = await projectApi.list()
  state.projects = res.data || []
  const savedId = localStorage.getItem(STORAGE_KEY)
  const current =
    state.projects.find(p => p.id === savedId) || state.projects[0] || null
  setCurrentProject(current)
  return state.projects
}

/**
 * 切换当前项目（同时刷新本地缓存，刷新页面后仍保持）
 */
export function setCurrentProject(project) {
  state.currentProject = project || null
  if (project) {
    localStorage.setItem(STORAGE_KEY, project.id)
  } else {
    localStorage.removeItem(STORAGE_KEY)
  }
}

/**
 * 项目变更后同步 store 中的项目信息（新增/修改/删除后调用）
 */
export function refreshCurrentProject() {
  if (!state.currentProject) return
  const updated = state.projects.find(p => p.id === state.currentProject.id)
  setCurrentProject(updated || null)
}
