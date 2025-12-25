<template>
  <div>
    <div class="stats-row">
      <div class="stat-card blue-gradient">
        <div class="stat-info">
          <h3>待接诊</h3>
          <span class="num">12</span>
        </div>
        <Icon icon="mdi:account-clock" class="bg-icon" />
      </div>

      <div class="stat-card green-gradient">
        <div class="stat-info">
          <h3>已完成</h3>
          <span class="num">28</span>
        </div>
        <Icon icon="mdi:account-check" class="bg-icon" />
      </div>

      <div class="stat-card orange-gradient">
        <div class="stat-info">
          <h3>今日挂号</h3>
          <span class="num">45</span>
        </div>
        <Icon icon="mdi:calendar-today" class="bg-icon" />
      </div>

      <div class="stat-card purple-gradient">
        <div class="stat-info">
          <h3>好评率</h3>
          <span class="num">98%</span>
        </div>
        <Icon icon="mdi:star-face" class="bg-icon" />
      </div>
    </div>

    <div class="panel-container">
      <div class="panel-header">
        <h3>候诊队列 <small>Patient Queue</small></h3>
        <div class="actions">
          <button class="btn-refresh" @click="refresh"><Icon icon="mdi:refresh" /> 刷新</button>
          <button class="btn-call-next" @click="callNext">
            <Icon icon="mdi:bullhorn-outline" /> 叫号下一位
          </button>
        </div>
      </div>

      <div class="table-wrapper">
        <table>
          <thead>
          <tr>
            <th>就诊号</th>
            <th>姓名</th>
            <th>性别</th>
            <th>年龄</th>
            <th>挂号类型</th>
            <th>等待时间</th>
            <th>状态</th>
            <th>操作</th>
          </tr>
          </thead>
          <tbody>
          <tr v-for="p in patientQueue" :key="p.id">
            <td class="id-col">#{{ p.id }}</td>
            <td class="name-col">{{ p.name }}</td>
            <td>{{ p.gender }}</td>
            <td>{{ p.age }}</td>
            <td>
              <span class="type-badge" :class="p.typeClass">{{ p.type }}</span>
            </td>
            <td>{{ p.waitTime }} mins</td>
            <td>
              <span class="status-dot" :class="p.statusColor"></span>
              {{ p.status }}
            </td>
            <td>
              <button class="btn-action primary" @click="handleDiagnose(p)">接诊</button>
              <button class="btn-action text">查看档案</button>
            </td>
          </tr>
          </tbody>
        </table>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref } from 'vue'
import { Icon } from '@iconify/vue'

const patientQueue = ref([
  { id: '202501', name: '张伟', gender: '男', age: 35, type: '普通号', typeClass: 'normal', waitTime: 5, status: '候诊中', statusColor: 'blue' },
  { id: '202502', name: '李秀英', gender: '女', age: 62, type: '专家号', typeClass: 'expert', waitTime: 12, status: '候诊中', statusColor: 'blue' },
  { id: '202503', name: '王强', gender: '男', age: 28, type: '急诊', typeClass: 'emergency', waitTime: 1, status: '准备中', statusColor: 'orange' },
  { id: '202504', name: '陈静', gender: '女', age: 45, type: '普通号', typeClass: 'normal', waitTime: 25, status: '过号', statusColor: 'gray' },
])

function refresh() {
  alert('模拟刷新队列（后续可接后端接口）')
}
function callNext() {
  alert('模拟叫号下一位（后续可接后端接口）')
}
function handleDiagnose(p) {
  alert(`开始接诊患者：${p.name} (ID: ${p.id})`)
}
</script>

<style scoped>
/* 复制你原 DoctorDashboard 的卡片/表格风格，确保一致 */
.stats-row {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 20px;
  margin-bottom: 25px;
}
.stat-card {
  padding: 20px 25px;
  border-radius: 12px;
  color: white;
  display: flex;
  justify-content: space-between;
  align-items: center;
  box-shadow: 0 5px 15px rgba(0,0,0,0.1);
  position: relative;
  overflow: hidden;
  transition: transform 0.2s;
}
.stat-card:hover { transform: translateY(-3px); }
.stat-info h3 { margin: 0; font-size: 0.9rem; opacity: 0.9; font-weight: normal; }
.stat-info .num { font-size: 2rem; font-weight: bold; display: block; margin-top: 5px; }
.bg-icon { font-size: 4rem; opacity: 0.2; position: absolute; right: -10px; bottom: -10px; transform: rotate(-15deg); }

.blue-gradient { background: linear-gradient(135deg, #3b82f6, #0056b3); }
.green-gradient { background: linear-gradient(135deg, #42e695, #3bb2b8); }
.orange-gradient { background: linear-gradient(135deg, #f093fb, #f5576c); }
.purple-gradient { background: linear-gradient(135deg, #5ee7df, #b490ca); }

.panel-container {
  background: white;
  border-radius: 12px;
  padding: 20px;
  box-shadow: 0 5px 20px rgba(0,0,0,0.03);
  display: flex;
  flex-direction: column;
}
.panel-header {
  display: flex; justify-content: space-between; align-items: center; margin-bottom: 20px;
}
.panel-header h3 { margin: 0; font-size: 1.2rem; color: #333; }
.panel-header small { color: #999; font-weight: normal; margin-left: 8px; }

.actions { display: flex; gap: 10px; }
.btn-refresh { border: 1px solid #ddd; background: white; padding: 8px 15px; border-radius: 6px; cursor: pointer; color: #666; display: flex; align-items: center; gap: 5px; }
.btn-call-next { background: #004ea2; color: white; border: none; padding: 8px 20px; border-radius: 6px; cursor: pointer; display: flex; align-items: center; gap: 5px; font-weight: bold; transition: 0.2s; }
.btn-call-next:hover { background: #003d80; box-shadow: 0 4px 10px rgba(0,78,162,0.3); }

.table-wrapper { flex: 1; overflow-y: auto; }
table { width: 100%; border-collapse: collapse; }
th { text-align: left; padding: 15px; color: #999; font-size: 0.85rem; border-bottom: 1px solid #eee; font-weight: 500; }
td { padding: 15px; border-bottom: 1px solid #f9f9f9; color: #333; font-size: 0.95rem; }
tr:hover { background: #f0f7ff; }

.id-col { font-family: monospace; color: #004ea2; font-weight: bold; }
.name-col { font-weight: 600; }

.type-badge { padding: 4px 8px; border-radius: 4px; font-size: 0.8rem; }
.type-badge.normal { background: #e6f7ff; color: #004ea2; }
.type-badge.expert { background: #fff7e6; color: #fa8c16; border: 1px solid #ffd591; }
.type-badge.emergency { background: #fff1f0; color: #f5222d; font-weight: bold; }

.status-dot { display: inline-block; width: 6px; height: 6px; border-radius: 50%; margin-right: 5px; vertical-align: middle; }
.status-dot.blue { background: #1890ff; }
.status-dot.orange { background: #fa8c16; }
.status-dot.gray { background: #d9d9d9; }

.btn-action { border: none; background: none; cursor: pointer; font-size: 0.9rem; margin-right: 10px; }
.btn-action.primary { color: #004ea2; font-weight: bold; }
.btn-action.text { color: #999; }
.btn-action:hover { text-decoration: underline; }
</style>
