package com.CTF.j_ctf.controller;

import com.CTF.j_ctf.entity.User;
import com.CTF.j_ctf.service.AuthService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/admin/auth")
public class AdminAuthController {
    private final AuthService authService;

    public AdminAuthController(AuthService authService) {
        this.authService = authService;
    }

    /**
     * 管理员登录
     */
    @PostMapping("/login")
    public ResponseEntity<?> adminLogin(@RequestBody Map<String, String> loginData,
                                        HttpServletRequest request) {
        try {
            String account = loginData.get("account");
            String password = loginData.get("password");

            if (account == null || account.trim().isEmpty()) {
                return ResponseEntity.badRequest().body(createErrorResponse("账号不能为空"));
            }

            if (password == null || password.trim().isEmpty()) {
                return ResponseEntity.badRequest().body(createErrorResponse("密码不能为空"));
            }

            // 验证管理员登录
            Optional<User> admin = authService.adminLogin(account, password);

            if (admin.isPresent()) {
                User adminUser = admin.get();
                // 登录成功，设置session
                HttpSession session = request.getSession();
                session.setAttribute("userId", adminUser.getUserID()); // 统一使用userId
                session.setAttribute("adminId", adminUser.getUserID()); // 保留adminId以兼容
                session.setAttribute("userRole", "ADMIN");
                session.setAttribute("userName", adminUser.getUserName());
                session.setMaxInactiveInterval(30 * 60); // 30分钟超时

                Map<String, Object> response = new HashMap<>();
                response.put("userId", adminUser.getUserID());
                response.put("adminId", adminUser.getUserID());
                response.put("userName", adminUser.getUserName());
                response.put("userRole", "ADMIN");
                response.put("adminRole", adminUser.getAdminRole());
                response.put("message", "登录成功");

                return ResponseEntity.ok(createSuccessResponse("登录成功", response));
            } else {
                return ResponseEntity.badRequest().body(createErrorResponse("账号或密码错误"));
            }
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(createErrorResponse("登录失败"));
        }
    }

    /**
     * 管理员登出
     */
    @PostMapping("/logout")
    public ResponseEntity<?> adminLogout(HttpServletRequest request) {
        try {
            HttpSession session = request.getSession(false);
            if (session != null) {
                session.invalidate();
            }
            return ResponseEntity.ok(createSuccessResponse("登出成功"));
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(createErrorResponse("登出失败"));
        }
    }

    /**
     * 检查管理员登录状态
     */
    @GetMapping("/check")
    public ResponseEntity<?> checkAdminStatus(HttpServletRequest request) {
        try {
            HttpSession session = request.getSession(false);
            Map<String, Object> status = new HashMap<>();

            if (session != null && "ADMIN".equals(session.getAttribute("userRole"))) {
                status.put("isLoggedIn", true);
                status.put("adminId", session.getAttribute("adminId"));
                status.put("userName", session.getAttribute("userName"));
                status.put("userRole", "ADMIN");
                return ResponseEntity.ok(createSuccessResponse("已登录", status));
            } else {
                status.put("isLoggedIn", false);
                return ResponseEntity.ok(createSuccessResponse("未登录", status));
            }
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(createErrorResponse("检查状态失败"));
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