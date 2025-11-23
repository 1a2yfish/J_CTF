package com.CTF.j_ctf.controller;

import com.CTF.j_ctf.entity.Competition;
import com.CTF.j_ctf.entity.Team;
import com.CTF.j_ctf.entity.User;
import com.CTF.j_ctf.service.AdminService;
import jakarta.servlet.http.HttpSession;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import jakarta.servlet.http.HttpServletRequest;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/admin")
public class AdminController {
    private final AdminService adminService;

    public AdminController(AdminService adminService) {
        this.adminService = adminService;
    }

    /**
     * 检查管理员权限
     */
    private void checkAdminPermission(HttpServletRequest request) {
        HttpSession session = request.getSession(false);
        if (session == null || !"ADMIN".equals(session.getAttribute("userRole"))) {
            throw new SecurityException("权限不足");
        }
    }

    // === 系统统计 ===

    @GetMapping("/dashboard")
    public ResponseEntity<?> getDashboard(HttpServletRequest request) {
        try {
            checkAdminPermission(request);
            Map<String, Object> stats = adminService.getSystemStatistics();
            return ResponseEntity.ok(createSuccessResponse("获取成功", stats));
        } catch (SecurityException e) {
            return ResponseEntity.status(403).body(createErrorResponse("权限不足"));
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(createErrorResponse("获取失败"));
        }
    }

    // === 用户管理 ===

    @GetMapping("/users")
    public ResponseEntity<?> getUsers(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "createTime") String sort,
            @RequestParam(required = false) String keyword,
            HttpServletRequest request) {
        try {
            checkAdminPermission(request);

            Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, sort));
            Page<User> users;

            if (keyword != null && !keyword.trim().isEmpty()) {
                users = adminService.searchUsers(keyword, pageable);
            } else {
                users = adminService.getAllUsers(pageable);
            }

            Map<String, Object> response = new HashMap<>();
            response.put("users", users.getContent());
            response.put("totalPages", users.getTotalPages());
            response.put("totalElements", users.getTotalElements());
            response.put("currentPage", users.getNumber());

            return ResponseEntity.ok(createSuccessResponse("获取成功", response));
        } catch (SecurityException e) {
            return ResponseEntity.status(403).body(createErrorResponse("权限不足"));
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(createErrorResponse("获取用户列表失败"));
        }
    }

    @GetMapping("/users/{userId}")
    public ResponseEntity<?> getUserDetails(@PathVariable Integer userId, HttpServletRequest request) {
        try {
            checkAdminPermission(request);
            User user = adminService.getUserDetails(userId);
            // 隐藏敏感信息
            user.setUserPassword(null);
            return ResponseEntity.ok(createSuccessResponse("获取成功", user));
        } catch (SecurityException e) {
            return ResponseEntity.status(403).body(createErrorResponse("权限不足"));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(createErrorResponse(e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(createErrorResponse("获取用户详情失败"));
        }
    }

    @PutMapping("/users/{userId}")
    public ResponseEntity<?> updateUser(@PathVariable Integer userId,
                                        @RequestBody User user,
                                        HttpServletRequest request) {
        try {
            checkAdminPermission(request);
            user.setUserID(userId); // 确保ID一致
            User updatedUser = adminService.updateUser(user);
            // 隐藏敏感信息
            updatedUser.setUserPassword(null);
            return ResponseEntity.ok(createSuccessResponse("更新成功", updatedUser));
        } catch (SecurityException e) {
            return ResponseEntity.status(403).body(createErrorResponse("权限不足"));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(createErrorResponse(e.getMessage()));
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.internalServerError().body(createErrorResponse("更新用户失败: " + e.getMessage()));
        }
    }

    @PostMapping("/users/{userId}/disable")
    public ResponseEntity<?> disableUser(@PathVariable Integer userId, HttpServletRequest request) {
        try {
            checkAdminPermission(request);
            boolean success = adminService.disableUser(userId);
            if (success) {
                return ResponseEntity.ok(createSuccessResponse("禁用用户成功"));
            } else {
                return ResponseEntity.badRequest().body(createErrorResponse("禁用用户失败"));
            }
        } catch (SecurityException e) {
            return ResponseEntity.status(403).body(createErrorResponse("权限不足"));
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(createErrorResponse("禁用用户失败"));
        }
    }

    @PostMapping("/users/{userId}/enable")
    public ResponseEntity<?> enableUser(@PathVariable Integer userId, HttpServletRequest request) {
        try {
            checkAdminPermission(request);
            boolean success = adminService.enableUser(userId);
            if (success) {
                return ResponseEntity.ok(createSuccessResponse("启用用户成功"));
            } else {
                return ResponseEntity.badRequest().body(createErrorResponse("启用用户失败"));
            }
        } catch (SecurityException e) {
            return ResponseEntity.status(403).body(createErrorResponse("权限不足"));
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(createErrorResponse("启用用户失败"));
        }
    }

    @DeleteMapping("/users/{userId}")
    public ResponseEntity<?> deleteUser(@PathVariable Integer userId, HttpServletRequest request) {
        try {
            checkAdminPermission(request);
            boolean success = adminService.deleteUser(userId);
            if (success) {
                return ResponseEntity.ok(createSuccessResponse("删除用户成功"));
            } else {
                return ResponseEntity.badRequest().body(createErrorResponse("删除用户失败：用户不存在"));
            }
        } catch (SecurityException e) {
            return ResponseEntity.status(403).body(createErrorResponse("权限不足"));
        } catch (RuntimeException e) {
            e.printStackTrace();
            return ResponseEntity.internalServerError().body(createErrorResponse("删除用户失败: " + e.getMessage()));
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.internalServerError().body(createErrorResponse("删除用户失败: " + e.getMessage()));
        }
    }

    // === 战队管理 ===

    @GetMapping("/teams")
    public ResponseEntity<?> getTeams(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(required = false) Integer competitionId,
            @RequestParam(required = false) String auditState,
            HttpServletRequest request) {
        try {
            checkAdminPermission(request);

            Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "creationTime"));
            Page<Team> teams;

            if (competitionId != null) {
                teams = adminService.getTeamsByCompetition(competitionId, pageable);
            } else if (auditState != null) {
                teams = adminService.getTeamsByAuditState(auditState, pageable);
            } else {
                teams = adminService.getAllTeams(pageable);
            }

            Map<String, Object> response = new HashMap<>();
            response.put("teams", teams.getContent());
            response.put("totalPages", teams.getTotalPages());
            response.put("totalElements", teams.getTotalElements());
            response.put("currentPage", teams.getNumber());

            return ResponseEntity.ok(createSuccessResponse("获取成功", response));
        } catch (SecurityException e) {
            return ResponseEntity.status(403).body(createErrorResponse("权限不足"));
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(createErrorResponse("获取战队列表失败"));
        }
    }

    @GetMapping("/teams/{teamId}")
    public ResponseEntity<?> getTeamDetails(@PathVariable Integer teamId, HttpServletRequest request) {
        try {
            checkAdminPermission(request);
            Team team = adminService.getTeamDetails(teamId);
            return ResponseEntity.ok(createSuccessResponse("获取成功", team));
        } catch (SecurityException e) {
            return ResponseEntity.status(403).body(createErrorResponse("权限不足"));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(createErrorResponse(e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(createErrorResponse("获取战队详情失败"));
        }
    }

    @PostMapping("/teams/{teamId}/audit")
    public ResponseEntity<?> auditTeam(@PathVariable Integer teamId,
                                       @RequestBody Map<String, String> auditData,
                                       HttpServletRequest request) {
        try {
            checkAdminPermission(request);
            String auditState = auditData.get("auditState");
            String auditRemark = auditData.get("auditRemark");

            if (auditState == null) {
                return ResponseEntity.badRequest().body(createErrorResponse("审核状态不能为空"));
            }

            Team team = adminService.updateTeamAuditState(teamId, auditState, auditRemark);
            return ResponseEntity.ok(createSuccessResponse("审核成功", team));
        } catch (SecurityException e) {
            return ResponseEntity.status(403).body(createErrorResponse("权限不足"));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(createErrorResponse(e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(createErrorResponse("审核战队失败"));
        }
    }

    @DeleteMapping("/teams/{teamId}")
    public ResponseEntity<?> deleteTeam(@PathVariable Integer teamId, HttpServletRequest request) {
        try {
            checkAdminPermission(request);
            boolean success = adminService.deleteTeam(teamId);
            if (success) {
                return ResponseEntity.ok(createSuccessResponse("删除战队成功"));
            } else {
                return ResponseEntity.badRequest().body(createErrorResponse("删除战队失败"));
            }
        } catch (SecurityException e) {
            return ResponseEntity.status(403).body(createErrorResponse("权限不足"));
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(createErrorResponse("删除战队失败"));
        }
    }

    @GetMapping("/competitions/{competitionId}/team-stats")
    public ResponseEntity<?> getTeamStatistics(@PathVariable Integer competitionId, HttpServletRequest request) {
        try {
            checkAdminPermission(request);
            Map<String, Object> stats = adminService.getTeamStatistics(competitionId);
            return ResponseEntity.ok(createSuccessResponse("获取成功", stats));
        } catch (SecurityException e) {
            return ResponseEntity.status(403).body(createErrorResponse("权限不足"));
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(createErrorResponse("获取统计失败"));
        }
    }

    // === 竞赛管理 ===

    @GetMapping("/competitions")
    public ResponseEntity<?> getCompetitions(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(required = false) String status,
            HttpServletRequest request) {
        try {
            checkAdminPermission(request);

            Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "publishTime"));
            Page<Competition> competitions;

            if (status != null) {
                competitions = adminService.getCompetitionsByStatus(status, pageable);
            } else {
                competitions = adminService.getAllCompetitions(pageable);
            }

            Map<String, Object> response = new HashMap<>();
            response.put("competitions", competitions.getContent());
            response.put("totalPages", competitions.getTotalPages());
            response.put("totalElements", competitions.getTotalElements());
            response.put("currentPage", competitions.getNumber());

            return ResponseEntity.ok(createSuccessResponse("获取成功", response));
        } catch (SecurityException e) {
            return ResponseEntity.status(403).body(createErrorResponse("权限不足"));
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(createErrorResponse("获取竞赛列表失败"));
        }
    }

    @GetMapping("/competitions/{competitionId}")
    public ResponseEntity<?> getCompetitionDetails(@PathVariable Integer competitionId, HttpServletRequest request) {
        try {
            checkAdminPermission(request);
            Competition competition = adminService.getCompetitionDetails(competitionId);
            return ResponseEntity.ok(createSuccessResponse("获取成功", competition));
        } catch (SecurityException e) {
            return ResponseEntity.status(403).body(createErrorResponse("权限不足"));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(createErrorResponse(e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(createErrorResponse("获取竞赛详情失败"));
        }
    }

    @PostMapping("/competitions")
    public ResponseEntity<?> createCompetition(@RequestBody Competition competition, HttpServletRequest request) {
        try {
            checkAdminPermission(request);
            Competition createdCompetition = adminService.createCompetition(competition);
            return ResponseEntity.ok(createSuccessResponse("创建竞赛成功", createdCompetition));
        } catch (SecurityException e) {
            return ResponseEntity.status(403).body(createErrorResponse("权限不足"));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(createErrorResponse(e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(createErrorResponse("创建竞赛失败"));
        }
    }

    @PutMapping("/competitions/{competitionId}")
    public ResponseEntity<?> updateCompetition(@PathVariable Integer competitionId,
                                               @RequestBody Competition competition,
                                               HttpServletRequest request) {
        try {
            checkAdminPermission(request);
            competition.setCompetitionID(competitionId); // 确保ID一致
            Competition updatedCompetition = adminService.updateCompetition(competition);
            return ResponseEntity.ok(createSuccessResponse("更新竞赛成功", updatedCompetition));
        } catch (SecurityException e) {
            return ResponseEntity.status(403).body(createErrorResponse("权限不足"));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(createErrorResponse(e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(createErrorResponse("更新竞赛失败"));
        }
    }

    @PostMapping("/competitions/{competitionId}/audit")
    public ResponseEntity<?> auditCompetition(@PathVariable Integer competitionId,
                                              @RequestBody Map<String, Object> auditData,
                                              HttpServletRequest request) {
        try {
            checkAdminPermission(request);
            Boolean approved = (Boolean) auditData.get("approved");
            String auditRemark = (String) auditData.get("auditRemark");

            if (approved == null) {
                return ResponseEntity.badRequest().body(createErrorResponse("审核结果不能为空"));
            }

            Competition competition = adminService.auditCompetition(competitionId, approved, auditRemark);
            return ResponseEntity.ok(createSuccessResponse("审核成功", competition));
        } catch (SecurityException e) {
            return ResponseEntity.status(403).body(createErrorResponse("权限不足"));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(createErrorResponse(e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(createErrorResponse("审核竞赛失败"));
        }
    }

    @DeleteMapping("/competitions/{competitionId}")
    public ResponseEntity<?> deleteCompetition(@PathVariable Integer competitionId, HttpServletRequest request) {
        try {
            checkAdminPermission(request);
            boolean success = adminService.deleteCompetition(competitionId);
            if (success) {
                return ResponseEntity.ok(createSuccessResponse("删除竞赛成功"));
            } else {
                return ResponseEntity.badRequest().body(createErrorResponse("删除竞赛失败"));
            }
        } catch (SecurityException e) {
            return ResponseEntity.status(403).body(createErrorResponse("权限不足"));
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(createErrorResponse("删除竞赛失败"));
        }
    }

    @GetMapping("/competitions/{competitionId}/stats")
    public ResponseEntity<?> getCompetitionStatistics(@PathVariable Integer competitionId, HttpServletRequest request) {
        try {
            checkAdminPermission(request);
            Map<String, Object> stats = adminService.getCompetitionStatistics(competitionId);
            return ResponseEntity.ok(createSuccessResponse("获取成功", stats));
        } catch (SecurityException e) {
            return ResponseEntity.status(403).body(createErrorResponse("权限不足"));
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(createErrorResponse("获取统计失败"));
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