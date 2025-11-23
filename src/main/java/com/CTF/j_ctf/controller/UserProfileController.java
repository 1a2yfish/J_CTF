package com.CTF.j_ctf.controller;

import com.CTF.j_ctf.entity.User;
import com.CTF.j_ctf.service.UserProfileService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/user-profile")
public class UserProfileController {
    private final UserProfileService userProfileService;

    public UserProfileController(UserProfileService userProfileService) {
        this.userProfileService = userProfileService;
    }

    /**
     * 获取当前用户ID（从Session中获取）
     */
    private Integer getCurrentUserId(HttpServletRequest request) {
        HttpSession session = request.getSession(false);
        if (session == null || !"USER".equals(session.getAttribute("userRole"))) {
            throw new SecurityException("用户未登录");
        }
        return (Integer) session.getAttribute("userId");
    }

    /**
     * 查看个人信息
     */
    @GetMapping
    public ResponseEntity<?> getProfile(HttpServletRequest request) {
        try {
            Integer userId = getCurrentUserId(request);
            Optional<User> userProfile = userProfileService.getUserProfile(userId);

            if (userProfile.isPresent()) {
                // 隐藏敏感信息
                User profile = userProfile.get();
                profile.setUserPassword(null);
                return ResponseEntity.ok(createSuccessResponse("获取个人信息成功", profile));
            } else {
                return ResponseEntity.badRequest().body(createErrorResponse("用户不存在或不是普通用户"));
            }
        } catch (SecurityException e) {
            return ResponseEntity.status(401).body(createErrorResponse("用户未登录"));
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(createErrorResponse("获取个人信息失败"));
        }
    }

    /**
     * 修改个人信息
     */
    @PutMapping
    public ResponseEntity<?> updateProfile(@RequestBody User updatedProfile,
                                           HttpServletRequest request) {
        try {
            Integer userId = getCurrentUserId(request);
            User result = userProfileService.updateUserProfile(userId, updatedProfile);
            result.setUserPassword(null);
            return ResponseEntity.ok(createSuccessResponse("更新个人信息成功", result));
        } catch (SecurityException e) {
            return ResponseEntity.status(401).body(createErrorResponse("用户未登录"));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(createErrorResponse(e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(createErrorResponse("更新个人信息失败"));
        }
    }

    /**
     * 修改密码
     */
    @PostMapping("/change-password")
    public ResponseEntity<?> changePassword(@RequestBody Map<String, String> passwordData,
                                            HttpServletRequest request) {
        try {
            Integer userId = getCurrentUserId(request);
            String oldPassword = passwordData.get("oldPassword");
            String newPassword = passwordData.get("newPassword");

            if (oldPassword == null || newPassword == null) {
                return ResponseEntity.badRequest().body(createErrorResponse("参数不完整"));
            }

            boolean success = userProfileService.changePassword(userId, oldPassword, newPassword);

            if (success) {
                return ResponseEntity.ok(createSuccessResponse("密码修改成功"));
            } else {
                return ResponseEntity.badRequest().body(createErrorResponse("密码修改失败"));
            }
        } catch (SecurityException e) {
            return ResponseEntity.status(401).body(createErrorResponse("用户未登录"));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(createErrorResponse(e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(createErrorResponse("密码修改失败"));
        }
    }

    /**
     * 注销账户
     */
    @PostMapping("/deactivate")
    public ResponseEntity<?> deactivateAccount(@RequestBody Map<String, String> requestData,
                                               HttpServletRequest request) {
        try {
            Integer userId = getCurrentUserId(request);
            String password = requestData.get("password");

            if (password == null) {
                return ResponseEntity.badRequest().body(createErrorResponse("请输入密码"));
            }

            boolean success = userProfileService.deactivateAccount(userId, password);

            if (success) {
                // 注销成功后清除session
                HttpSession session = request.getSession(false);
                if (session != null) {
                    session.invalidate();
                }
                return ResponseEntity.ok(createSuccessResponse("账户注销成功"));
            } else {
                return ResponseEntity.badRequest().body(createErrorResponse("账户注销失败"));
            }
        } catch (SecurityException e) {
            return ResponseEntity.status(401).body(createErrorResponse("用户未登录"));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(createErrorResponse(e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(createErrorResponse("账户注销失败"));
        }
    }

    /**
     * 发送密码重置验证码
     */
    @PostMapping("/forgot-password/send-code")
    public ResponseEntity<?> sendPasswordResetCode(@RequestBody Map<String, String> requestData) {
        try {
            String identifier = requestData.get("identifier");

            if (identifier == null || identifier.trim().isEmpty()) {
                return ResponseEntity.badRequest().body(createErrorResponse("请输入用户名、邮箱或手机号"));
            }

            boolean success = userProfileService.sendPasswordResetCode(identifier);

            // 出于安全考虑，无论用户是否存在都返回成功
            return ResponseEntity.ok(createSuccessResponse("验证码已发送"));
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(createErrorResponse("发送验证码失败"));
        }
    }

    /**
     * 重置密码
     */
    @PostMapping("/forgot-password/reset")
    public ResponseEntity<?> resetPassword(@RequestBody Map<String, String> requestData) {
        try {
            String identifier = requestData.get("identifier");
            String verificationCode = requestData.get("verificationCode");
            String newPassword = requestData.get("newPassword");

            if (identifier == null || verificationCode == null || newPassword == null) {
                return ResponseEntity.badRequest().body(createErrorResponse("参数不完整"));
            }

            boolean success = userProfileService.resetPassword(identifier, verificationCode, newPassword);

            if (success) {
                return ResponseEntity.ok(createSuccessResponse("密码重置成功"));
            } else {
                return ResponseEntity.badRequest().body(createErrorResponse("密码重置失败"));
            }
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(createErrorResponse(e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(createErrorResponse("密码重置失败"));
        }
    }

    /**
     * 检查用户名是否可用
     */
    @GetMapping("/check-username")
    public ResponseEntity<?> checkUsername(@RequestParam String username) {
        try {
            boolean available = userProfileService.isUsernameAvailable(username);
            Map<String, Object> response = new HashMap<>();
            response.put("available", available);
            response.put("username", username);
            return ResponseEntity.ok(createSuccessResponse("检查完成", response));
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(createErrorResponse("检查失败"));
        }
    }

    /**
     * 检查邮箱是否可用
     */
    @GetMapping("/check-email")
    public ResponseEntity<?> checkEmail(@RequestParam String email) {
        try {
            boolean available = userProfileService.isEmailAvailable(email);
            Map<String, Object> response = new HashMap<>();
            response.put("available", available);
            response.put("email", email);
            return ResponseEntity.ok(createSuccessResponse("检查完成", response));
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(createErrorResponse("检查失败"));
        }
    }

    /**
     * 检查手机号是否可用
     */
    @GetMapping("/check-phone")
    public ResponseEntity<?> checkPhone(@RequestParam String phone) {
        try {
            boolean available = userProfileService.isPhoneAvailable(phone);
            Map<String, Object> response = new HashMap<>();
            response.put("available", available);
            response.put("phone", phone);
            return ResponseEntity.ok(createSuccessResponse("检查完成", response));
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(createErrorResponse("检查失败"));
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