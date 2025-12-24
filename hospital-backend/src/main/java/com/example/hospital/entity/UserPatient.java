package com.example.hospital.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

@Data
@TableName("user_patient")
public class UserPatient {
    private String userId;       // varchar(32), 联合主键
    private String patientId;    // varchar(32)
    private String relationType; // '本人'/'父母'/'子女'/'配偶'/'其他'
}