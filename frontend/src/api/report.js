import request from '@/api'

/**
 * 报表接口封装
 */
export const reportApi = {
  systemOverview(params) {
    return request.get('/reports/system/overview', { params })
  },
  teamRows(params) {
    return request.get('/reports/teams', { params })
  },
  teamProjects(team, params) {
    return request.get(`/reports/teams/${encodeURIComponent(team)}/projects`, { params })
  },
  projectOverview(projectId, params) {
    return request.get(`/reports/projects/${projectId}/overview`, { params })
  },
  projectCases(projectId, params) {
    return request.get(`/reports/projects/${projectId}/cases`, { params })
  },
  topFailures(projectId, params) {
    return request.get(`/reports/projects/${projectId}/top-failures`, { params })
  },
  // 导出（blob 下载）
  exportTeams(params) {
    return download('/reports/system/overview/export', params, '团队报表')
  },
  exportTeamProjects(team, params) {
    return download(`/reports/teams/${encodeURIComponent(team)}/projects/export`, params,
      `项目报表_${team}`)
  },
  exportProjectCases(projectId, params) {
    return download(`/reports/projects/${projectId}/cases/export`, params, '用例明细')
  }
}

function download(url, params, namePrefix) {
  return request.get(url, {
    params,
    responseType: 'blob'
  }).then(blob => {
    const link = document.createElement('a')
    const startDate = (params && params.startDate) || ''
    const endDate = (params && params.endDate) || ''
    link.href = URL.createObjectURL(blob)
    link.download = `${namePrefix}_${startDate}_${endDate}.xlsx`
    document.body.appendChild(link)
    link.click()
    document.body.removeChild(link)
    URL.revokeObjectURL(link.href)
  })
}

export default reportApi
