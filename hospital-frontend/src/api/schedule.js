// 假设项目已存在基础请求工具（如axios实例）
import request from '../utils/request.js'

/**
 * 科室列表接口
 * 后端接口示例：GET /api/departments
 */
export const getDepartments = () => {
  return request({
    url: '/api/departments',
    method: 'GET'
  })
}

/**
 * 医生排班查询接口
 * 后端接口示例：GET /api/schedules?departmentId=xxx&date=xxx
 * @param {Object} params - 查询参数
 * @param {string} params.departmentId - 科室ID（可选）
 * @param {string} params.date - 日期（格式：yyyy-MM-dd，必选）
 */
export const getDoctorSchedules = (params) => {
  return request({
    url: '/api/schedules',
    method: 'GET',
    params // 自动拼接为查询字符串
  })
}

/**
 * 提交预约接口
 * 后端接口示例：POST /api/reservations
 * @param {Object} data - 预约信息
 * @param {string} data.scheduleId - 排班ID
 * @param {string} data.patientId - 患者ID
 * @param {string} data.patientName - 患者姓名
 * @param {string} data.phone - 联系电话
 */
export const submitReservation = (data) => {
  return request({
    url: '/api/reservations',
    method: 'POST',
    data // 请求体数据
  })
}