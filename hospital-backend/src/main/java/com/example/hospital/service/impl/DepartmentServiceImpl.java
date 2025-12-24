package com.example.hospital.service.impl;

import com.example.hospital.entity.Department;
import com.example.hospital.mapper.DepartmentMapper;
import com.example.hospital.service.DepartmentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 科室服务实现类
 */
@Service
public class DepartmentServiceImpl implements DepartmentService {

    @Autowired
    private DepartmentMapper departmentMapper;

    @Override
    public List<Department> getAllDepartments() {
        return departmentMapper.selectList(null);
    }
}

