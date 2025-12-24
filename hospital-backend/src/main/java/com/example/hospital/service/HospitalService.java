package com.example.hospital.service;

import com.example.hospital.entity.Hospital;

import java.util.List;

/**
 * 医院/院区服务接口
 */
public interface HospitalService {

    /**
     * 获取所有医院/院区列表
     * @return 医院列表
     */
    List<Hospital> getAllHospitals();
}

