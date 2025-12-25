package com.example.hospital.controller;

import com.example.hospital.common.Result;
import com.example.hospital.common.UserLoginToken;
import com.example.hospital.dto.DoctorProfileResponse;
import com.example.hospital.dto.DoctorProfileUpdateRequest;
import com.example.hospital.entity.Doctor;
import com.example.hospital.service.DoctorService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import jakarta.servlet.http.HttpServletRequest;

/**
 * 医生控制器
 */
@RestController
@RequestMapping("/api/doctor")
public class DoctorController {

    @Autowired
    private DoctorService doctorService;

    /**
     * 获取当前登录医生的个人资料
     * @param request HttpServletRequest，用于获取拦截器中存入的用户ID
     * @return 医生完整资料（包含医院和科室信息）
     */
    @UserLoginToken // 需要Token验证
    @GetMapping("/profile")
    public Result<DoctorProfileResponse> getDoctorProfile(HttpServletRequest request) {
        // 从request中获取由拦截器注入的用户ID
        String userId = (String) request.getAttribute("userId");
        DoctorProfileResponse doctorProfile = doctorService.getDoctorProfile(userId);
        return Result.success(doctorProfile);
    }

    /**
     * 更新当前登录医生的个人资料
     * @param request HttpServletRequest
     * @param updateRequest 包含更新信息的数据体
     * @return 更新后的医生完整资料
     */
    @UserLoginToken // 需要Token验证
    @PutMapping("/profile")
    public Result<DoctorProfileResponse> updateDoctorProfile(HttpServletRequest request, @RequestBody DoctorProfileUpdateRequest updateRequest) {
        String userId = (String) request.getAttribute("userId");
        Doctor updatedDoctor = doctorService.updateDoctorProfile(userId, updateRequest);
        // 返回更新后的完整资料
        DoctorProfileResponse doctorProfile = doctorService.getDoctorProfile(userId);
        return Result.success(doctorProfile);
    }
}
