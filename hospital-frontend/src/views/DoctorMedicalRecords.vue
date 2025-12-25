<template>
  <div class="page">
    <!-- 筛选区 -->
    <div class="card filter-card">
      <div class="filter-row">
        <div class="field">
          <label>月份</label>
          <input type="month" v-model="filters.month" />
        </div>

        <div class="field">
          <label>关键词</label>
          <input
              type="text"
              v-model.trim="filters.keyword"
              placeholder="患者姓名 / 手机号 / 预约号（可选）"
              @keyup.enter="loadRecords"
          />
        </div>

        <div class="field actions">
          <button class="btn primary" @click="loadRecords" :disabled="loading">
            {{ loading ? '加载中...' : '查询' }}
          </button>
          <button class="btn" @click="resetFilters" :disabled="loading">重置</button>
        </div>
      </div>

      <div class="hint">
        按月查询医生历史就诊记录，可用于病历回溯与随访。若接口异常将显示示例数据（仅用于前端联调）。
      </div>
    </div>

    <!-- 概览区 -->
    <div class="stats">
      <div class="stat">
        <div class="k">记录条数</div>
        <div class="v">{{ total }}</div>
      </div>
      <div class="stat">
        <div class="k">本月已完成</div>
        <div class="v">{{ finishedCount }}</div>
      </div>
      <div class="stat">
        <div class="k">本月取消/爽约</div>
        <div class="v">{{ cancelledCount }}</div>
      </div>
      <div class="stat">
        <div class="k">完成率</div>
        <div class="v">{{ completionRate }}%</div>
      </div>
    </div>

    <!-- 表格区 -->
    <div class="card table-card">
      <div class="table-head">
        <h3>病历档案 <small>Visit Records</small></h3>
        <div class="right">
          <span v-if="error" class="error">{{ error }}</span>
        </div>
      </div>

      <div class="table-wrapper">
        <table>
          <thead>
          <tr>
            <th style="width: 120px;">日期</th>
            <th style="width: 110px;">时间段</th>
            <th style="width: 140px;">患者</th>
            <th style="width: 140px;">就诊号/预约号</th>
            <th style="width: 120px;">科室</th>
            <th style="width: 120px;">诊断/主诉</th>
            <th style="width: 110px;">状态</th>
            <th style="width: 140px;">操作</th>
          </tr>
          </thead>

          <tbody>
          <tr v-if="!loading && records.length === 0">
            <td colspan="8" class="empty">暂无数据</td>
          </tr>

          <tr v-for="r in records" :key="r.record_id">
            <td class="mono">{{ r.visit_date }}</td>
            <td><span class="badge">{{ r.time_slot || '-' }}</span></td>
            <td>
              <div class="patient">
                <div class="pname">{{ r.patient_name }}</div>
                <div class="psub">{{ r.patient_phone || '' }}</div>
              </div>
            </td>
            <td class="mono">{{ r.order_no || r.record_no || '-' }}</td>
            <td>{{ r.department_name || '-' }}</td>
            <td class="ellipsis" :title="r.summary || ''">{{ r.summary || '-' }}</td>
            <td>
              <span class="status" :class="statusClass(r.status)">{{ statusText(r.status) }}</span>
            </td>
            <td>
              <button class="btn-action primary" @click="openDetail(r)">查看详情</button>
              <button class="btn-action text" @click="openPatient(r)">患者档案</button>
            </td>
          </tr>
          </tbody>
        </table>
      </div>

      <!-- 分页 -->
      <div class="pager">
        <div class="tip">第 {{ page }} / {{ totalPages }} 页</div>
        <div class="controls">
          <button class="btn" @click="prevPage" :disabled="loading || page <= 1">上一页</button>
          <button class="btn" @click="nextPage" :disabled="loading || page >= totalPages">下一页</button>
        </div>
      </div>
    </div>

    <!-- 详情弹窗（轻量版：不依赖三方 UI 框架） -->
    <div v-if="detailVisible" class="modal-mask" @click.self="detailVisible=false">
      <div class="modal">
        <div class="modal-head">
          <h3>就诊详情</h3>
          <button class="x" @click="detailVisible=false">×</button>
        </div>
        <div class="modal-body">
          <div class="kv">
            <div><span class="k">日期</span><span class="v">{{ detail.visit_date }}</span></div>
            <div><span class="k">时间段</span><span class="v">{{ detail.time_slot || '-' }}</span></div>
            <div><span class="k">患者</span><span class="v">{{ detail.patient_name }}</span></div>
            <div><span class="k">就诊号/预约号</span><span class="v">{{ detail.order_no || detail.record_no || '-' }}</span></div>
            <div><span class="k">科室</span><span class="v">{{ detail.department_name || '-' }}</span></div>
            <div><span class="k">状态</span><span class="v">{{ statusText(detail.status) }}</span></div>
          </div>

          <div class="block">
            <div class="block-title">诊断/主诉摘要</div>
            <div class="block-text">{{ detail.summary || '—' }}</div>
          </div>

          <div class="block">
            <div class="block-title">处置/医嘱</div>
            <div class="block-text">{{ detail.advice || '—' }}</div>
          </div>
        </div>
        <div class="modal-foot">
          <button class="btn primary" @click="detailVisible=false">关闭</button>
        </div>
      </div>
    </div>

  </div>
</template>

<script setup>
import { computed, onMounted, ref } from 'vue'

const loading = ref(false)
const error = ref('')
const records = ref([])

const page = ref(1)
const pageSize = ref(10)
const total = ref(0)

const filters = ref({
  month: '',   // YYYY-MM
  keyword: ''
})

const detailVisible = ref(false)
const detail = ref({})

function pad(n) { return String(n).padStart(2, '0') }

function initDefaultMonth() {
  const now = new Date()
  filters.value.month = `${now.getFullYear()}-${pad(now.getMonth() + 1)}`
}

function buildUrl() {
  const base = (import.meta?.env?.VITE_API_BASE || '') + '/api/doctor/visit-records'
  const qs = new URLSearchParams()
  if (filters.value.month) qs.set('month', filters.value.month)
  if (filters.value.keyword) qs.set('keyword', filters.value.keyword)
  qs.set('page', String(page.value))
  qs.set('pageSize', String(pageSize.value))
  return `${base}?${qs.toString()}`
}

function normalize(list) {
  return (list || []).map(x => ({
    record_id: x.record_id ?? x.id ?? `${x.visit_date || ''}-${x.order_no || ''}-${Math.random()}`,
    visit_date: (x.visit_date || x.visitDate || '').slice(0, 10),
    time_slot: x.time_slot || x.timeSlot || '',
    patient_name: x.patient_name || x.patientName || '未知',
    patient_phone: x.patient_phone || x.patientPhone || '',
    order_no: x.order_no || x.orderNo || '',
    record_no: x.record_no || x.recordNo || '',
    department_name: x.department_name || x.departmentName || '',
    summary: x.summary || x.diagnosis || x.complaint || '',
    advice: x.advice || x.doctorAdvice || '',
    status: x.status ?? x.visit_status ?? x.visitStatus ?? 'FINISHED'
  }))
}

async function loadRecords() {
  loading.value = true
  error.value = ''
  try {
    const url = buildUrl()
    const token = localStorage.getItem('token')
    const resp = await fetch(url, {
      method: 'GET',
      headers: {
        'Content-Type': 'application/json',
        ...(token ? { Authorization: `Bearer ${token}` } : {})
      }
    })
    if (!resp.ok) throw new Error(`HTTP ${resp.status}`)

    const json = await resp.json()

    // 兼容常见分页结构：
    // A) { data: { list: [], total: 100 } }
    // B) { data: [], total: 100 }
    // C) { list: [], total: 100 }
    // D) 直接 []
    let list = []
    let t = 0

    if (Array.isArray(json)) {
      list = json
      t = json.length
    } else if (Array.isArray(json?.data)) {
      list = json.data
      t = Number(json.total ?? json?.dataTotal ?? list.length)
    } else if (Array.isArray(json?.list)) {
      list = json.list
      t = Number(json.total ?? list.length)
    } else if (Array.isArray(json?.data?.list)) {
      list = json.data.list
      t = Number(json.data.total ?? json.total ?? list.length)
    }

    records.value = normalize(list)
    total.value = t
  } catch (e) {
    error.value = '接口不可用，已展示示例数据（仅用于前端联调）'
    const mock = mockData()
    total.value = mock.total
    records.value = normalize(mock.list)
  } finally {
    loading.value = false
  }
}

function mockData() {
  // 简单 mock：按页返回
  const baseDate = filters.value.month ? `${filters.value.month}-` : '2025-12-'
  const all = [
    { record_id: 'r1', visit_date: baseDate + '03', time_slot: '08:00-10:00', patient_name: '张伟', patient_phone: '138****1234', order_no: 'A20251203001', department_name: '心血管内科', summary: '胸闷、心悸，建议进一步检查', advice: '心电图/血脂/随访', status: 'FINISHED' },
    { record_id: 'r2', visit_date: baseDate + '07', time_slot: '13:30-17:00', patient_name: '李秀英', patient_phone: '139****9876', order_no: 'A20251207012', department_name: '心血管内科', summary: '高血压复诊，调整用药', advice: '低盐饮食，2周复诊', status: 'FINISHED' },
    { record_id: 'r3', visit_date: baseDate + '09', time_slot: '19:00-21:00', patient_name: '王强', patient_phone: '137****2222', order_no: 'A20251209005', department_name: '心血管内科', summary: '胸痛待查，建议急查', advice: '必要时急诊处理', status: 'CANCELLED' },
    { record_id: 'r4', visit_date: baseDate + '18', time_slot: '08:00-10:00', patient_name: '陈静', patient_phone: '136****3344', order_no: 'A20251218009', department_name: '心血管内科', summary: '药物不良反应评估', advice: '调整剂量，观察', status: 'NOSHOW' },
  ]

  const start = (page.value - 1) * pageSize.value
  const end = start + pageSize.value
  return { total: all.length, list: all.slice(start, end) }
}

function resetFilters() {
  initDefaultMonth()
  filters.value.keyword = ''
  page.value = 1
  loadRecords()
}

const totalPages = computed(() => Math.max(1, Math.ceil(total.value / pageSize.value)))

function prevPage() {
  if (page.value > 1) {
    page.value--
    loadRecords()
  }
}

function nextPage() {
  if (page.value < totalPages.value) {
    page.value++
    loadRecords()
  }
}

function openDetail(r) {
  detail.value = r
  detailVisible.value = true
}

function openPatient(r) {
  alert(`这里可跳转到患者档案页（待你后续实现）：${r.patient_name}`)
}

function statusText(s) {
  const v = String(s || '').toUpperCase()
  if (v === 'FINISHED' || v === 'DONE') return '已完成'
  if (v === 'CANCELLED') return '已取消'
  if (v === 'NOSHOW') return '爽约'
  if (v === 'IN_PROGRESS') return '进行中'
  return '未知'
}

function statusClass(s) {
  const v = String(s || '').toUpperCase()
  if (v === 'FINISHED' || v === 'DONE') return 'ok'
  if (v === 'CANCELLED') return 'danger'
  if (v === 'NOSHOW') return 'warn'
  if (v === 'IN_PROGRESS') return 'info'
  return 'default'
}

const finishedCount = computed(() =>
    records.value.filter(x => ['FINISHED','DONE'].includes(String(x.status || '').toUpperCase())).length
)
const cancelledCount = computed(() =>
    records.value.filter(x => ['CANCELLED','NOSHOW'].includes(String(x.status || '').toUpperCase())).length
)
const completionRate = computed(() => {
  if (total.value <= 0) return 0
  return Math.round((finishedCount.value / total.value) * 100)
})

onMounted(() => {
  initDefaultMonth()
  loadRecords()
})
</script>

<style scoped>
.page { display: flex; flex-direction: column; gap: 16px; }

.card {
  background: #fff;
  border-radius: 12px;
  box-shadow: 0 5px 20px rgba(0,0,0,0.03);
  padding: 16px 18px;
}

.filter-row {
  display: grid;
  grid-template-columns: 200px 1fr 220px;
  gap: 14px;
  align-items: end;
}

.field label {
  display: block;
  font-size: 0.85rem;
  color: #666;
  margin-bottom: 6px;
}

.field input {
  width: 100%;
  padding: 10px 10px;
  border: 1px solid #e6e6e6;
  border-radius: 8px;
  outline: none;
}
.field input:focus {
  border-color: #004ea2;
  box-shadow: 0 0 0 3px rgba(0, 78, 162, 0.10);
}

.actions { display: flex; justify-content: flex-end; gap: 10px; }
.btn {
  padding: 10px 16px;
  border-radius: 8px;
  cursor: pointer;
  border: 1px solid #ddd;
  background: #fff;
  color: #666;
}
.btn.primary {
  border: none;
  background: #004ea2;
  color: #fff;
  font-weight: 700;
}
.btn.primary:hover { background: #003d80; }
.btn:disabled { opacity: 0.6; cursor: not-allowed; }

.hint { margin-top: 10px; font-size: 0.82rem; color: #999; }

.stats { display: grid; grid-template-columns: repeat(4, 1fr); gap: 12px; }
.stat {
  background: #fff;
  border-radius: 12px;
  box-shadow: 0 5px 20px rgba(0,0,0,0.03);
  padding: 14px 16px;
}
.stat .k { font-size: 0.85rem; color: #999; }
.stat .v { margin-top: 6px; font-size: 1.4rem; font-weight: 800; color: #004ea2; }

.table-head { display: flex; align-items: center; justify-content: space-between; margin-bottom: 12px; }
.table-head h3 { margin: 0; font-size: 1.1rem; color: #333; }
.table-head small { color: #999; font-weight: normal; margin-left: 8px; }
.error { color: #ff4d4f; font-size: 0.85rem; }

.table-wrapper { overflow: auto; }
table { width: 100%; border-collapse: collapse; }
th {
  text-align: left;
  padding: 14px 12px;
  color: #999;
  font-size: 0.85rem;
  border-bottom: 1px solid #eee;
  font-weight: 600;
}
td {
  padding: 14px 12px;
  border-bottom: 1px solid #f7f7f7;
  color: #333;
}
tr:hover { background: #f0f7ff; }

.mono {
  font-family: ui-monospace, SFMono-Regular, Menlo, Monaco, Consolas, "Liberation Mono", "Courier New", monospace;
  color: #004ea2;
  font-weight: 700;
}

.badge {
  display: inline-block;
  padding: 4px 10px;
  border-radius: 999px;
  background: #e6f7ff;
  color: #004ea2;
  font-size: 0.85rem;
  font-weight: 700;
}

.patient .pname { font-weight: 700; }
.patient .psub { font-size: 0.8rem; color: #999; margin-top: 2px; }

.ellipsis {
  max-width: 260px;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

.status {
  display: inline-block;
  padding: 4px 10px;
  border-radius: 999px;
  font-size: 0.85rem;
  font-weight: 800;
}
.status.ok { background: #f6ffed; color: #52c41a; border: 1px solid #b7eb8f; }
.status.warn { background: #fff7e6; color: #fa8c16; border: 1px solid #ffd591; }
.status.danger { background: #fff1f0; color: #ff4d4f; border: 1px solid #ffccc7; }
.status.info { background: #e6f7ff; color: #1890ff; border: 1px solid #91d5ff; }
.status.default { background: #fafafa; color: #999; border: 1px solid #eee; }

.btn-action { border: none; background: none; cursor: pointer; font-size: 0.9rem; margin-right: 10px; }
.btn-action.primary { color: #004ea2; font-weight: 800; }
.btn-action.text { color: #999; }
.btn-action:hover { text-decoration: underline; }

.empty { text-align: center; color: #999; padding: 18px 0; }

.pager {
  margin-top: 12px;
  display: flex;
  align-items: center;
  justify-content: space-between;
  color: #999;
  font-size: 0.85rem;
}
.pager .controls { display: flex; gap: 10px; }

.modal-mask {
  position: fixed;
  inset: 0;
  background: rgba(0,0,0,0.35);
  display: flex;
  align-items: center;
  justify-content: center;
  z-index: 9999;
}
.modal {
  width: min(720px, calc(100vw - 40px));
  background: #fff;
  border-radius: 14px;
  box-shadow: 0 20px 60px rgba(0,0,0,0.2);
  overflow: hidden;
}
.modal-head {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 14px 16px;
  border-bottom: 1px solid #f0f0f0;
}
.modal-head h3 { margin: 0; font-size: 1.05rem; }
.modal-head .x {
  border: none; background: none; cursor: pointer;
  font-size: 22px; line-height: 22px; color: #999;
}
.modal-body { padding: 14px 16px; }
.kv { display: grid; grid-template-columns: 1fr 1fr; gap: 10px 16px; }
.k { color: #999; margin-right: 8px; }
.v { color: #333; font-weight: 700; }

.block { margin-top: 12px; }
.block-title { color: #666; font-weight: 800; margin-bottom: 6px; }
.block-text { background: #fafafa; border: 1px solid #f0f0f0; border-radius: 10px; padding: 10px; color: #333; }

.modal-foot {
  padding: 12px 16px;
  border-top: 1px solid #f0f0f0;
  display: flex;
  justify-content: flex-end;
}
</style>
