package com.example.hospital.service.impl;

import com.example.hospital.entity.User;
import com.example.hospital.service.UserService;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.time.LocalDateTime;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

/**
 * UserServiceImpl 的集成测试
 */
@SpringBootTest // 启动完整的Spring Boot应用进行测试
class UserServiceImplTest {

    @Autowired
    private UserService userService;

    private User testUser;

    // 在每个测试方法运行前执行
    @BeforeEach
    void setUp() {
        // 1. 创建一个临时的测试用户
        testUser = new User();
        testUser.setUserId(UUID.randomUUID().toString().substring(0, 10)); // 创建一个随机ID
        testUser.setUserPhone("19999999999");
        testUser.setUserPassword("testpassword");
        testUser.setRole("user");
        testUser.setCreatedAt(LocalDateTime.now());

        // 注意：这里直接调用 userService 来创建一个用户，如果注册功能还没实现，可以先用 UserMapper
        // 为了测试独立，我们假设有一个方法可以插入用户，这里我们直接调用 userMapper
        // 但由于我们没有注入 userMapper，所以这里先省略插入，假设数据库已存在一个已知用户
        // 在实际项目中，通常会使用 @Sql 注解或在 @BeforeEach 中用 mapper 插入数据
        
        // 简化：我们假设一个已知的用户ID存在于数据库中，例如 'test-user-id'
        // 在运行测试前，请确保数据库中有这样一条记录。
        // 为了让测试可独立运行，我们将在后续实现注册功能后完善这里。
    }

    @AfterEach
    void tearDown() {
        // 4. 测试结束后清理数据 (同样，需要mapper来操作)
    }

    @Test
    void testGetUserById_WhenUserExists() {
        // --- 准备阶段 ---
        // 假设数据库中存在一个ID为 "user001" 的用户
        // 您需要手动在数据库的 `user` 表中插入一条数据，例如：
        // INSERT INTO `user` (`user_id`, `user_phone`, `user_password`, `role`) 
        // VALUES ('user001', '13800138000', '123456', 'user');
        String existingUserId = "user001";

        // --- 执行阶段 ---
        User foundUser = userService.getUserById(existingUserId);

        // --- 验证阶段 ---
        assertNotNull(foundUser, "用户不应该为null");
        assertEquals(existingUserId, foundUser.getUserId(), "用户ID应该匹配");
        assertEquals("13800138000", foundUser.getUserPhone(), "用户手机号应该匹配");
        assertNull(foundUser.getUserPassword(), "返回的用户密码应该是null（脱敏）");
    }

    @Test
    void testGetUserById_WhenUserNotExists() {
        // --- 准备阶段 ---
        String nonExistingUserId = "non-existent-id";

        // --- 执行和验证阶段 ---
        // 验证当用户不存在时，是否会抛出我们预期的 BusinessException
        Exception exception = assertThrows(com.example.hospital.common.BusinessException.class, () -> {
            userService.getUserById(nonExistingUserId);
        });

        // 验证异常信息是否正确
        assertEquals("用户不存在", exception.getMessage());
    }
}

