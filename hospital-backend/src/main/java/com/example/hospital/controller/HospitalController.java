package com.example.hospital.controller;

import com.example.hospital.common.PassToken;
import com.example.hospital.common.Result;
import com.example.hospital.entity.Hospital;
import com.example.hospital.service.HospitalService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * 医院/院区控制器
 */
@RestController
@RequestMapping("/api/hospitals")
public class HospitalController {

    @Autowired
    private HospitalService hospitalService;

    /**
     * 获取所有医院/院区列表
     * @return 医院列表
     */
    @PassToken // 无需登录即可访问
    @GetMapping
    public Result<List<Hospital>> getAllHospitals() {
        List<Hospital> hospitals = hospitalService.getAllHospitals();
        return Result.success(hospitals);
    }

    // 未来可以添加获取单个医院详情、获取某医院下所有科室等接口
}

