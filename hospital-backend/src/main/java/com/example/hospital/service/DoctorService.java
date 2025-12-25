package com.example.hospital.service;

import com.example.hospital.dto.DoctorProfileResponse;
import com.example.hospital.dto.DoctorProfileUpdateRequest;
import com.example.hospital.entity.Doctor;

/**
 * 医生服务接口
 */
public interface DoctorService {

    /**
     * 根据用户ID获取医生信息
     * @param userId 用户ID
     * @return 医生实体
     */
    Doctor getDoctorByUserId(String userId);

    /**
     * 根据用户ID获取医生完整资料（包含医院和科室信息）
     * @param userId 用户ID
     * @return 医生资料响应对象
     */
    DoctorProfileResponse getDoctorProfile(String userId);

    /**
     * 更新医生个人资料
     * @param userId 用户ID
     * @param request 更新请求体
     * @return 更新后的医生实体
     */
    Doctor updateDoctorProfile(String userId, DoctorProfileUpdateRequest request);
}
