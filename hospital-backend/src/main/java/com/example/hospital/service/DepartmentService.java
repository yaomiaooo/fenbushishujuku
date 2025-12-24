package com.example.hospital.service;

import com.example.hospital.entity.Department;

import java.util.List;

/**
 * 科室服务接口
 */
public interface DepartmentService {

    /**
     * 获取所有科室列表
     * @return 科室列表
     */
    List<Department> getAllDepartments();
}

