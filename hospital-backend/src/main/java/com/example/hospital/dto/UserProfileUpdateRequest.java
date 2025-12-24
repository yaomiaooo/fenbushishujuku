package com.example.hospital.dto;

import com.example.hospital.common.Result;
import lombok.Data;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * 用户个人资料更新请求的数据传输对象
 */
@Data
public class UserProfileUpdateRequest {
    private String realName; // 真实姓名
    private String phone;    // 手机号码

    @RestController
    @RequestMapping("/api/hospital")
    @CrossOrigin // ★★★ 这一行就是允许前端连接后端的关键 ★★★
    public static class HospitalController {

        // 1. 获取科室列表接口
        @GetMapping("/departments")
        public Result<List<Map<String, Object>>> getDepartments() {
            System.out.println("前端正在请求科室列表...");

            List<Map<String, Object>> depts = new ArrayList<>();
            // 模拟数据库数据
            depts.add(Map.of("id", 1, "name", "心血管内科", "desc", "Cardiology", "icon", "❤️"));
            depts.add(Map.of("id", 2, "name", "神经外科", "desc", "Neurosurgery", "icon", "🧠"));
            depts.add(Map.of("id", 3, "name", "骨科中心", "desc", "Orthopedics", "icon", "🦴"));
            depts.add(Map.of("id", 4, "name", "儿科", "desc", "Pediatrics", "icon", "👶"));
            depts.add(Map.of("id", 5, "name", "妇产科", "desc", "Obstetrics", "icon", "🤰"));

            return Result.success(depts);
        }

        // 2. 模拟预约提交接口
        @PostMapping("/appointment")
        public Result<String> createAppointment(@RequestBody Map<String, Object> payload) {
            System.out.println("收到预约请求: " + payload);
            return Result.success("预约成功，请留意短信通知！");
        }
    }
}

