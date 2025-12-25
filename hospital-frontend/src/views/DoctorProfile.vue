<template>
  <div class="page">
    <!-- 顶部操作条 -->
    <div class="card head-card">
      <div class="left">
        <h3>个人信息维护 <small>Profile</small></h3>
        <div class="sub">用于医生端资料展示与预约信息关联。请确保联系方式与执业信息准确。</div>
      </div>

      <div class="right">
        <span v-if="error" class="error">{{ error }}</span>
        <button v-if="!editing" class="btn primary" @click="startEdit">编辑</button>
        <button v-else class="btn primary" @click="save" :disabled="loading">{{ loading ? '保存中...' : '保存' }}</button>
        <button v-if="editing" class="btn" @click="cancelEdit" :disabled="loading">取消</button>
      </div>
    </div>

    <!-- 信息卡 -->
    <div class="card form-card">
      <div class="form-grid">
        <div class="field">
          <label>医生编号</label>
          <input type="text" v-model="form.doctor_id" disabled />
        </div>

        <div class="field">
          <label>姓名</label>
          <input type="text" v-model.trim="form.name" :disabled="!editing" />
        </div>

        <div class="field">
          <label>性别</label>
          <select v-model="form.gender" :disabled="!editing">
            <option value="男">男</option>
            <option value="女">女</option>
            <option value="未知">未知</option>
          </select>
        </div>

        <div class="field">
          <label>电话</label>
          <input type="text" v-model.trim="form.phone" :disabled="!editing" placeholder="例如：13800001234" />
          <div v-if="editing && phoneError" class="warn">{{ phoneError }}</div>
        </div>

        <div class="field">
          <label>邮箱</label>
          <input type="text" v-model.trim="form.email" :disabled="!editing" placeholder="例如：xx@hospital.com" />
          <div v-if="editing && emailError" class="warn">{{ emailError }}</div>
        </div>

        <div class="field">
          <label>所属院区</label>
          <input type="text" v-model.trim="form.hospital_name" :disabled="!editing" placeholder="例如：朝晖院区" />
        </div>

        <div class="field">
          <label>所属科室</label>
          <input type="text" v-model.trim="form.department_name" :disabled="!editing" placeholder="例如：心血管内科" />
        </div>

        <div class="field">
          <label>职称</label>
          <select v-model="form.title" :disabled="!editing">
            <option value="住院医师">住院医师</option>
            <option value="主治医师">主治医师</option>
            <option value="副主任医师">副主任医师</option>
            <option value="主任医师">主任医师</option>
          </select>
        </div>

        <div class="field">
          <label>擅长领域</label>
          <input type="text" v-model.trim="form.specialty" :disabled="!editing" placeholder="例如：冠心病/心律失常" />
        </div>

        <div class="field full">
          <label>个人简介</label>
          <textarea v-model.trim="form.bio" :disabled="!editing" rows="4" placeholder="用于患者端展示的医生简介"></textarea>
        </div>
      </div>

      <div class="foot">
        <div class="meta">
          <div>最后更新时间：<span class="mono">{{ form.updated_at || '-' }}</span></div>
          <div>提示：医生编号、院区/科室若由管理员维护，可在后端设为只读。</div>
        </div>
      </div>
    </div>

    <!-- 快捷信息卡（可选） -->
    <div class="stats">
      <div class="stat">
        <div class="k">当前科室</div>
        <div class="v">{{ form.department_name || '-' }}</div>
      </div>
      <div class="stat">
        <div class="k">职称</div>
        <div class="v">{{ form.title || '-' }}</div>
      </div>
      <div class="stat">
        <div class="k">联系方式</div>
        <div class="v">{{ form.phone || '-' }}</div>
      </div>
      <div class="stat">
        <div class="k">展示状态</div>
        <div class="v">{{ editing ? '编辑中' : '只读' }}</div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted } from 'vue'

/* ======================
 * 状态
 * ====================== */
const loading = ref(false)
const editing = ref(false)
const error = ref('')

/* ======================
 * 页面表单（UI 使用）
 * ====================== */
const form = reactive({
  doctor_id: '',
  name: '',
  gender: '',
  phone: '',
  email: '',
  title: '',
  bio: '',
  hospital_id: '',
  updated_at: ''
})

let snapshot = null

/* ======================
 * 工具函数
 * ====================== */
function nowText() {
  const d = new Date()
  const yyyy = d.getFullYear()
  const mm = String(d.getMonth() + 1).padStart(2, '0')
  const dd = String(d.getDate()).padStart(2, '0')
  const hh = String(d.getHours()).padStart(2, '0')
  const mi = String(d.getMinutes()).padStart(2, '0')
  return `${yyyy}-${mm}-${dd} ${hh}:${mi}`
}

function apiUrl() {
  return (import.meta.env.VITE_API_BASE || '') + '/api/doctor/profile'
}

/* ======================
 * 后端 → 前端 映射
 * ====================== */
function applyFromApi(data) {
  form.doctor_id = data.doctorId || ''
  form.name = data.doctorName || ''
  form.gender = data.doctorGender || ''
  form.phone = data.doctorPhone || ''
  form.email = data.doctorEmail || ''
  form.title = data.title || ''
  form.bio = data.doctorIntro || ''
  form.hospital_id = data.hospitalId || ''
  form.updated_at = nowText()
}

/* ======================
 * 加载医生信息
 * ====================== */
async function load() {
  loading.value = true
  error.value = ''

  try {
    const token = localStorage.getItem('token')
    const resp = await fetch(apiUrl(), {
      headers: {
        'Content-Type': 'application/json',
        ...(token ? { Authorization: `Bearer ${token}` } : {})
      }
    })

    if (!resp.ok) throw new Error(`HTTP ${resp.status}`)

    const json = await resp.json()
    if (json.code !== 0) throw new Error(json.message)

    applyFromApi(json.data)
  } catch (e) {
    console.error(e)
    error.value = '医生信息加载失败，请稍后重试'
  } finally {
    loading.value = false
  }
}

/* ======================
 * 编辑 / 取消
 * ====================== */
function startEdit() {
  snapshot = JSON.parse(JSON.stringify(form))
  editing.value = true
}

function cancelEdit() {
  if (snapshot) Object.assign(form, snapshot)
  editing.value = false
}

/* ======================
 * 校验
 * ====================== */
const phoneError = computed(() => {
  if (!editing.value) return ''
  if (!form.phone) return '电话不能为空'
  return /^\d{11}$/.test(form.phone) ? '' : '电话格式不正确'
})

const emailError = computed(() => {
  if (!editing.value) return ''
  if (!form.email) return ''
  return /^[^\s@]+@[^\s@]+\.[^\s@]+$/.test(form.email) ? '' : '邮箱格式不正确'
})

function validate() {
  if (!form.name) return '姓名不能为空'
  if (phoneError.value) return phoneError.value
  if (emailError.value) return emailError.value
  return ''
}

/* ======================
 * 保存（只提交允许修改的字段）
 * ====================== */
async function save() {
  const msg = validate()
  if (msg) {
    alert(msg)
    return
  }

  loading.value = true
  error.value = ''

  try {
    const token = localStorage.getItem('token')

    const payload = {
      doctorPhone: form.phone,
      doctorEmail: form.email,
      doctorIntro: form.bio
    }

    const resp = await fetch(apiUrl(), {
      method: 'PUT',
      headers: {
        'Content-Type': 'application/json',
        ...(token ? { Authorization: `Bearer ${token}` } : {})
      },
      body: JSON.stringify(payload)
    })

    if (!resp.ok) throw new Error(`HTTP ${resp.status}`)

    const json = await resp.json()
    if (json.code !== 0) throw new Error(json.message)

    applyFromApi(json.data)
    editing.value = false
    alert('保存成功')
  } catch (e) {
    console.error(e)
    error.value = '保存失败，请稍后重试'
  } finally {
    loading.value = false
  }
}

/* ======================
 * 生命周期
 * ====================== */
onMounted(load)
</script>


<style scoped>
.page { display: flex; flex-direction: column; gap: 16px; }

.card {
  background: #fff;
  border-radius: 12px;
  box-shadow: 0 5px 20px rgba(0,0,0,0.03);
  padding: 16px 18px;
}

.head-card {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  gap: 14px;
}
.head-card h3 { margin: 0; font-size: 1.1rem; color: #333; }
.head-card small { color: #999; font-weight: normal; margin-left: 8px; }
.sub { margin-top: 6px; color: #999; font-size: 0.85rem; }

.right { display: flex; align-items: center; gap: 10px; }
.error { color: #ff4d4f; font-size: 0.85rem; max-width: 360px; }

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
  font-weight: 800;
}
.btn.primary:hover { background: #003d80; }
.btn:disabled { opacity: 0.6; cursor: not-allowed; }

.form-grid {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: 14px;
}

.field label {
  display: block;
  font-size: 0.85rem;
  color: #666;
  margin-bottom: 6px;
}

.field input,
.field select,
.field textarea {
  width: 100%;
  padding: 10px 10px;
  border: 1px solid #e6e6e6;
  border-radius: 8px;
  outline: none;
  background: #fff;
}

.field textarea { resize: vertical; }

.field input:focus,
.field select:focus,
.field textarea:focus {
  border-color: #004ea2;
  box-shadow: 0 0 0 3px rgba(0, 78, 162, 0.10);
}

.field input:disabled,
.field select:disabled,
.field textarea:disabled {
  background: #fafafa;
  color: #666;
}

.field.full { grid-column: 1 / -1; }

.warn {
  margin-top: 6px;
  font-size: 0.82rem;
  color: #fa8c16;
}

.foot {
  margin-top: 14px;
  border-top: 1px solid #f0f0f0;
  padding-top: 12px;
  color: #999;
  font-size: 0.85rem;
}
.meta { display: flex; justify-content: space-between; gap: 10px; flex-wrap: wrap; }
.mono {
  font-family: ui-monospace, SFMono-Regular, Menlo, Monaco, Consolas, "Liberation Mono", "Courier New", monospace;
  color: #004ea2;
  font-weight: 800;
}

.stats { display: grid; grid-template-columns: repeat(4, 1fr); gap: 12px; }
.stat {
  background: #fff;
  border-radius: 12px;
  box-shadow: 0 5px 20px rgba(0,0,0,0.03);
  padding: 14px 16px;
}
.stat .k { font-size: 0.85rem; color: #999; }
.stat .v { margin-top: 6px; font-size: 1.2rem; font-weight: 900; color: #004ea2; }
</style>
