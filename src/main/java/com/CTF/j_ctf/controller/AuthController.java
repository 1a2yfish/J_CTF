package com.CTF.j_ctf.controller;

import com.CTF.j_ctf.entity.OrdinaryUser;
import com.CTF.j_ctf.entity.User;
import com.CTF.j_ctf.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

/**
 * 用户模块控制器
 * 路由前缀：/api/users
 */
@RestController
@RequestMapping("/api/users")
public class AuthController {

    @Autowired
    private UserService userService;

    /**
     * 通用用户注册接口
     * 请求方式：POST
     * 路由：/api/users/register
     * 请求体：User对象的JSON数据
     */
    @PostMapping("/register")
    public ResponseEntity<?> register(@RequestBody User user) {
        try {
            User registeredUser = userService.register(user);
            return ResponseEntity.ok(createSuccessResponse("注册成功", registeredUser));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(createErrorResponse("注册失败：" + e.getMessage()));
        }
    }

    /**
     * 普通用户注册接口
     * 请求方式：POST
     * 路由：/api/users/register/ordinary
     * 请求参数：通过URL参数或表单传递
     */
    @PostMapping("/register/ordinary")
    public ResponseEntity<?> registerOrdinaryUser(
            @RequestParam String userPassword,
            @RequestParam String phoneNumber,
            @RequestParam String userEmail,
            @RequestParam String userName,
            @RequestParam String gender,
            @RequestParam String schoolWorkunit) {
        try {
            OrdinaryUser ordinaryUser = userService.registerOrdinaryUser(
                    userPassword, phoneNumber, userEmail, userName, gender, schoolWorkunit);
            return ResponseEntity.ok(createSuccessResponse("普通用户注册成功", ordinaryUser));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(createErrorResponse("普通用户注册失败：" + e.getMessage()));
        }
    }

    /**
     * 用户登录接口
     * 请求方式：POST
     * 路由：/api/users/login
     * 请求参数：账号（account）、密码（password）
     */
    @PostMapping("/login")
    public ResponseEntity<?> login(
            @RequestParam String account,
            @RequestParam String password) {
        try {
            Optional<User> userOptional = userService.login(account, password);
            if (userOptional.isPresent()) {
                return ResponseEntity.ok(createSuccessResponse("登录成功", userOptional.get()));
            } else {
                return ResponseEntity.badRequest().body(createErrorResponse("账号或密码错误"));
            }
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(createErrorResponse("登录失败：" + e.getMessage()));
        }
    }

    /**
     * 根据ID查询用户
     * 请求方式：GET
     * 路由：/api/users/{id}
     * 路径参数：用户ID
     */
    @GetMapping("/{id}")
    public ResponseEntity<?> findById(@PathVariable Integer id) {
        try {
            Optional<User> userOptional = userService.findById(id);
            if (userOptional.isPresent()) {
                return ResponseEntity.ok(createSuccessResponse("获取用户成功", userOptional.get()));
            } else {
                return ResponseEntity.badRequest().body(createErrorResponse("未找到ID为" + id + "的用户"));
            }
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(createErrorResponse("获取用户失败：" + e.getMessage()));
        }
    }

    /**
     * 根据账号查询用户
     * 请求方式：GET
     * 路由：/api/users/account/{account}
     * 路径参数：用户账号
     */
    @GetMapping("/account/{account}")
    public ResponseEntity<?> findByAccount(@PathVariable String account) {
        try {
            Optional<User> userOptional = userService.findByAccount(account);
            if (userOptional.isPresent()) {
                return ResponseEntity.ok(createSuccessResponse("获取用户成功", userOptional.get()));
            } else {
                return ResponseEntity.badRequest().body(createErrorResponse("未找到账号为" + account + "的用户"));
            }
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(createErrorResponse("获取用户失败：" + e.getMessage()));
        }
    }

    /**
     * 用户注销接口
     * 请求方式：POST
     * 路由：/api/users/logout
     */
    @PostMapping("/logout")
    public ResponseEntity<?> logout() {
        try {
            // 这里应该实现注销逻辑，比如清除session或token
            return ResponseEntity.ok(createSuccessResponse("注销成功"));
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(createErrorResponse("注销失败：" + e.getMessage()));
        }
    }

    /**
     * 创建成功响应
     */
    private Map<String, Object> createSuccessResponse(String message) {
        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("message", message);
        return response;
    }

    /**
     * 创建成功响应（带数据）
     */
    private Map<String, Object> createSuccessResponse(String message, Object data) {
        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("message", message);
        response.put("data", data);
        return response;
    }

    /**
     * 创建错误响应
     */
    private Map<String, Object> createErrorResponse(String message) {
        Map<String, Object> response = new HashMap<>();
        response.put("success", false);
        response.put("message", message);
        return response;
    }
}