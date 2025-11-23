package com.CTF.j_ctf.controller;

import com.CTF.j_ctf.entity.User;
import com.CTF.j_ctf.repository.UserRepository;
import com.CTF.j_ctf.service.AuthService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

/**
 * 普通用户认证控制器
 * 路由前缀：/api/users
 */
@RestController
@RequestMapping("/api/users")
public class AuthController {

    @Autowired
    private AuthService authService;

    @Autowired
    private UserRepository userRepository;

    /**
     * 普通用户注册接口
     * 请求方式：POST
     * 路由：/api/users/register
     */
    @PostMapping("/register")
    public ResponseEntity<?> registerOrdinaryUser(@RequestBody Map<String, String> userData) {
        try {
            // 验证必要字段
            String userPassword = userData.get("userPassword");
            String phoneNumber = userData.get("phoneNumber");
            String userEmail = userData.get("userEmail");
            String userName = userData.get("userName");
            String gender = userData.get("gender");
            String schoolWorkunit = userData.get("schoolWorkunit");

            // 验证必填字段
            if (userPassword == null || userPassword.trim().isEmpty()) {
                return ResponseEntity.badRequest().body(createErrorResponse("密码不能为空"));
            }
            if (phoneNumber == null || phoneNumber.trim().isEmpty()) {
                return ResponseEntity.badRequest().body(createErrorResponse("手机号不能为空"));
            }
            if (userEmail == null || userEmail.trim().isEmpty()) {
                return ResponseEntity.badRequest().body(createErrorResponse("邮箱不能为空"));
            }
            if (userName == null || userName.trim().isEmpty()) {
                return ResponseEntity.badRequest().body(createErrorResponse("用户名不能为空"));
            }

            User registeredUser = authService.registerOrdinaryUser(
                    userPassword, phoneNumber, userEmail, userName, gender, schoolWorkunit);

            // 返回时移除敏感信息
            registeredUser.setUserPassword(null);

            return ResponseEntity.ok(createSuccessResponse("普通用户注册成功", registeredUser));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(createErrorResponse(e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(createErrorResponse("普通用户注册失败：系统内部错误"));
        }
    }

    /**
     * 普通用户登录接口
     * 请求方式：POST
     * 路由：/api/users/login
     */
    @PostMapping("/login")
    public ResponseEntity<?> login(
            @RequestParam String account,
            @RequestParam String password,
            HttpServletRequest request) {
        try {
            // 验证输入
            if (account == null || account.trim().isEmpty()) {
                return ResponseEntity.badRequest().body(createErrorResponse("账号不能为空"));
            }
            if (password == null || password.trim().isEmpty()) {
                return ResponseEntity.badRequest().body(createErrorResponse("密码不能为空"));
            }

            Optional<User> userOptional = authService.login(account, password);

            if (userOptional.isPresent()) {
                User user = userOptional.get();

                // 根据用户类型设置角色
                String userRole = user.getUserType() == User.UserType.ADMIN ? "ADMIN" : "USER";

                // 设置session
                HttpSession session = request.getSession();
                session.setAttribute("userId", user.getUserID());
                session.setAttribute("userRole", userRole);
                session.setAttribute("userName", user.getUserName());
                session.setMaxInactiveInterval(30 * 60); // 30分钟超时

                // 准备返回数据（移除敏感信息）
                Map<String, Object> userInfo = new HashMap<>();
                userInfo.put("userId", user.getUserID());
                userInfo.put("userRole", userRole);
                userInfo.put("userName", user.getUserName());
                userInfo.put("userEmail", user.getUserEmail());
                userInfo.put("phoneNumber", user.getPhoneNumber());
                userInfo.put("userType", user.getUserType());

                return ResponseEntity.ok(createSuccessResponse("登录成功", userInfo));
            } else {
                return ResponseEntity.badRequest().body(createErrorResponse("账号或密码错误"));
            }
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(createErrorResponse("登录失败：系统内部错误"));
        }
    }

    /**
     * 检查登录状态
     * 请求方式：GET
     * 路由：/api/users/check
     */
    @GetMapping("/check")
    public ResponseEntity<?> checkLoginStatus(HttpServletRequest request) {
        try {
            HttpSession session = request.getSession(false);
            Map<String, Object> status = new HashMap<>();

            if (session != null && session.getAttribute("userRole") != null) {
                status.put("isLoggedIn", true);
                status.put("userId", session.getAttribute("userId"));
                status.put("userRole", session.getAttribute("userRole"));
                status.put("userName", session.getAttribute("userName"));
                return ResponseEntity.ok(createSuccessResponse("用户已登录", status));
            } else {
                status.put("isLoggedIn", false);
                return ResponseEntity.ok(createSuccessResponse("用户未登录", status));
            }
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(createErrorResponse("检查登录状态失败"));
        }
    }

    /**
     * 获取当前登录用户信息
     * 请求方式：GET
     * 路由：/api/users/profile
     */
    @GetMapping("/profile")
    public ResponseEntity<?> getProfile(HttpServletRequest request) {
        try {
            HttpSession session = request.getSession(false);
            if (session == null || session.getAttribute("userRole") == null) {
                return ResponseEntity.status(401).body(createErrorResponse("用户未登录"));
            }

            Integer userId = (Integer) session.getAttribute("userId");
            Optional<User> userOptional = authService.findById(userId);

            if (userOptional.isPresent()) {
                User user = userOptional.get();
                // 移除敏感信息
                user.setUserPassword(null);
                return ResponseEntity.ok(createSuccessResponse("获取用户信息成功", user));
            } else {
                return ResponseEntity.badRequest().body(createErrorResponse("用户不存在"));
            }
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(createErrorResponse("获取用户信息失败"));
        }
    }

    /**
     * 更新用户信息
     * 请求方式：PUT
     * 路由：/api/users/profile
     */
    @PutMapping("/profile")
    public ResponseEntity<?> updateProfile(@RequestBody Map<String, Object> updateData, HttpServletRequest request) {
        try {
            HttpSession session = request.getSession(false);
            if (session == null || session.getAttribute("userRole") == null) {
                return ResponseEntity.status(401).body(createErrorResponse("用户未登录"));
            }

            Integer userId = (Integer) session.getAttribute("userId");
            Optional<User> userOptional = authService.findById(userId);

            if (userOptional.isPresent()) {
                User user = userOptional.get();

                // 只能更新普通用户信息
                if (!user.isOrdinaryUser()) {
                    return ResponseEntity.badRequest().body(createErrorResponse("只能更新普通用户信息"));
                }

                // 更新允许修改的字段
                if (updateData.containsKey("gender")) {
                    user.setGender((String) updateData.get("gender"));
                }
                if (updateData.containsKey("schoolWorkunit")) {
                    user.setSchoolWorkunit((String) updateData.get("schoolWorkunit"));
                }
                if (updateData.containsKey("userEmail")) {
                    String newEmail = (String) updateData.get("userEmail");
                    if (newEmail != null && !newEmail.trim().isEmpty()) {
                        user.setUserEmail(newEmail);
                    }
                }
                if (updateData.containsKey("userName")) {
                    String newUserName = (String) updateData.get("userName");
                    if (newUserName != null && !newUserName.trim().isEmpty()) {
                        // 检查用户名是否已被其他用户使用
                        Optional<User> existingUser = userRepository.findByUserName(newUserName);
                        if (existingUser.isPresent() && !existingUser.get().getUserID().equals(userId)) {
                            return ResponseEntity.badRequest().body(createErrorResponse("用户名已被使用"));
                        }
                        user.setUserName(newUserName);
                    }
                }
                if (updateData.containsKey("phoneNumber")) {
                    String newPhoneNumber = (String) updateData.get("phoneNumber");
                    if (newPhoneNumber != null && !newPhoneNumber.trim().isEmpty()) {
                        // 检查手机号是否已被其他用户使用（仅普通用户）
                        Optional<User> existingUser = userRepository.findOrdinaryUserByPhoneNumber(User.UserType.ORDINARY, newPhoneNumber);
                        if (existingUser.isPresent() && !existingUser.get().getUserID().equals(userId)) {
                            return ResponseEntity.badRequest().body(createErrorResponse("手机号已被使用"));
                        }
                        user.setPhoneNumber(newPhoneNumber);
                    }
                }

                // 直接保存更新，不使用 register 方法（避免密码加密和验证逻辑）
                User updatedUser = userRepository.save(user);

                // 移除敏感信息
                updatedUser.setUserPassword(null);

                return ResponseEntity.ok(createSuccessResponse("更新用户信息成功", updatedUser));
            } else {
                return ResponseEntity.badRequest().body(createErrorResponse("用户不存在"));
            }
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(createErrorResponse("更新用户信息失败"));
        }
    }

    /**
     * 用户注销接口
     * 请求方式：POST
     * 路由：/api/users/logout
     */
    @PostMapping("/logout")
    public ResponseEntity<?> logout(HttpServletRequest request) {
        try {
            HttpSession session = request.getSession(false);
            if (session != null) {
                session.invalidate();
            }
            return ResponseEntity.ok(createSuccessResponse("注销成功"));
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(createErrorResponse("注销失败：系统内部错误"));
        }
    }

    /**
     * 创建成功响应
     */
    private Map<String, Object> createSuccessResponse(String message) {
        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("message", message);
        response.put("timestamp", System.currentTimeMillis());
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
        response.put("timestamp", System.currentTimeMillis());
        return response;
    }

    /**
     * 创建错误响应
     */
    private Map<String, Object> createErrorResponse(String message) {
        Map<String, Object> response = new HashMap<>();
        response.put("success", false);
        response.put("message", message);
        response.put("timestamp", System.currentTimeMillis());
        return response;
    }
}