import axios from 'axios'

const request = axios.create({
  baseURL: '/api',
  timeout: 10000
})

request.interceptors.response.use(
  response => {
    // blob（文件下载）直接透传
    if (response.config && response.config.responseType === 'blob') {
      return response.data
    }
    const res = response.data
    if (res && res.code !== undefined && res.code !== 200) {
      return Promise.reject(new Error(res.message || '请求失败'))
    }
    return res
  },
  error => Promise.reject(error)
)

export const projectApi = {
  list(params) {
    return request.get('/projects', { params })
  },
  create(data) {
    return request.post('/projects', data)
  },
  update(id, data) {
    return request.put(`/projects/${id}`, data)
  },
  remove(id) {
    return request.delete(`/projects/${id}`)
  }
}

export const planApi = {
  list(params) {
    return request.get('/plans', { params })
  },
  create(data) {
    return request.post('/plans', data)
  },
  update(id, data) {
    return request.put(`/plans/${id}`, data)
  },
  remove(id) {
    return request.delete(`/plans/${id}`)
  },
  cases(id, params) {
    return request.get(`/plans/${id}/cases`, { params })
  },
  execute(id, params) {
    return request.post(`/plans/${id}/execute`, null, { params })
  }
}

export const dictApi = {
  execTypes() {
    return request.get('/dicts/exec-types')
  }
}

export const caseApi = {
  list(params) {
    return request.get('/cases', { params })
  },
  detail(id) {
    return request.get(`/cases/${id}`)
  },
  create(data) {
    return request.post('/cases', data)
  },
  update(id, data) {
    return request.put(`/cases/${id}`, data)
  },
  remove(id) {
    return request.delete(`/cases/${id}`)
  }
}

export default request
