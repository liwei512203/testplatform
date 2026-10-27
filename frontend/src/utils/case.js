/**
 * 用例内容（content 字段）为 JSON 字符串，前端按如下结构编辑：
 * {
 *   "precondition": "前置条件",
 *   "steps": ["步骤1", "步骤2"],
 *   "expected": "预期结果"
 * }
 */

export function parseContent(content) {
  if (!content) {
    return { precondition: '', steps: [''], expected: '' }
  }
  try {
    const obj = typeof content === 'string' ? JSON.parse(content) : content
    const steps = Array.isArray(obj.steps) && obj.steps.length ? obj.steps : ['']
    return {
      precondition: obj.precondition || '',
      steps,
      expected: obj.expected || ''
    }
  } catch (e) {
    return { precondition: '', steps: [''], expected: '' }
  }
}

export function buildContent(form) {
  const steps = (form.steps || []).map(s => (s || '').trim()).filter(s => s !== '')
  return JSON.stringify(
    {
      precondition: form.precondition || '',
      steps,
      expected: form.expected || ''
    },
    null,
    2
  )
}

export function formatTime(ms) {
  if (!ms) return '-'
  const d = new Date(Number(ms))
  if (Number.isNaN(d.getTime())) return '-'
  const pad = n => String(n).padStart(2, '0')
  return `${d.getFullYear()}-${pad(d.getMonth() + 1)}-${pad(d.getDate())} ${pad(d.getHours())}:${pad(d.getMinutes())}:${pad(d.getSeconds())}`
}

export function contentSummary(content) {
  const { steps, expected } = parseContent(content)
  const firstStep = steps[0] || ''
  const text = firstStep || expected || ''
  return text.length > 40 ? text.slice(0, 40) + '...' : text
}
