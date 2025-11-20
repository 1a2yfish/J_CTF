package com.CTF.j_ctf.controller;

import com.CTF.j_ctf.entity.Team;
import com.CTF.j_ctf.entity.TeamApplication;
import com.CTF.j_ctf.service.TeamService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import jakarta.servlet.http.HttpServletRequest;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/teams")
public class TeamController {
    private final TeamService teamService;

    public TeamController(TeamService teamService) {
        this.teamService = teamService;
    }

    /**
     * 获取当前用户ID
     */
    private Integer getCurrentUserId(HttpServletRequest request) {
        Object userIdObj = request.getAttribute("userId");
        if (userIdObj != null) {
            return (Integer) userIdObj;
        }
        throw new SecurityException("用户未登录");
    }

    /**
     * 创建战队
     */
    @PostMapping
    public ResponseEntity<?> createTeam(@RequestBody Team team, HttpServletRequest request) {
        try {
            Integer userId = getCurrentUserId(request);
            // 这里需要设置队长用户对象
            // User captain = userRepository.findById(userId).orElseThrow(...);

            Team createdTeam = teamService.createTeam(team, new com.CTF.j_ctf.entity.User() {{ setUserID(userId); }});
            return ResponseEntity.ok(createSuccessResponse("创建战队成功", createdTeam));
        } catch (SecurityException e) {
            return ResponseEntity.status(401).body(createErrorResponse("用户未登录"));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(createErrorResponse(e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(createErrorResponse("创建战队失败"));
        }
    }

    /**
     * 更新战队信息
     */
    @PutMapping("/{teamId}")
    public ResponseEntity<?> updateTeam(@PathVariable Integer teamId,
                                        @RequestBody Team team,
                                        HttpServletRequest request) {
        try {
            Integer userId = getCurrentUserId(request);

            // 检查用户是否有权限修改该战队
            if (!teamService.isTeamCaptain(teamId, userId)) {
                return ResponseEntity.status(403).body(createErrorResponse("无权修改该战队"));
            }

            team.setTeamID(teamId);
            Team updatedTeam = teamService.updateTeam(team);
            return ResponseEntity.ok(createSuccessResponse("更新战队成功", updatedTeam));
        } catch (SecurityException e) {
            return ResponseEntity.status(401).body(createErrorResponse("用户未登录"));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(createErrorResponse(e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(createErrorResponse("更新战队失败"));
        }
    }

    /**
     * 获取战队详情
     */
    @GetMapping("/{teamId}")
    public ResponseEntity<?> getTeam(@PathVariable Integer teamId) {
        try {
            Optional<Team> teamOpt = teamService.getTeamById(teamId);
            if (teamOpt.isPresent()) {
                return ResponseEntity.ok(createSuccessResponse("获取成功", teamOpt.get()));
            } else {
                return ResponseEntity.notFound().build();
            }
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(createErrorResponse("获取战队详情失败"));
        }
    }

    /**
     * 获取战队列表
     */
    @GetMapping
    public ResponseEntity<?> getTeams(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "creationTime") String sort,
            @RequestParam(required = false) Integer competitionId,
            @RequestParam(required = false) String auditState,
            @RequestParam(required = false) String keyword,
            HttpServletRequest request) {
        try {
            Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, sort));
            Page<Team> teams;

            if (keyword != null && !keyword.trim().isEmpty()) {
                if (competitionId != null) {
                    teams = teamService.searchTeamsByCompetition(keyword, competitionId, pageable);
                } else {
                    teams = teamService.searchTeams(keyword, pageable);
                }
            } else if (competitionId != null) {
                teams = teamService.getTeamsByCompetition(competitionId, pageable);
            } else if (auditState != null) {
                teams = teamService.getTeamsByAuditState(auditState, pageable);
            } else {
                teams = teamService.getAllTeams(pageable);
            }

            Map<String, Object> response = new HashMap<>();
            response.put("teams", teams.getContent());
            response.put("totalPages", teams.getTotalPages());
            response.put("totalElements", teams.getTotalElements());
            response.put("currentPage", teams.getNumber());

            return ResponseEntity.ok(createSuccessResponse("获取成功", response));
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(createErrorResponse("获取战队列表失败"));
        }
    }

    /**
     * 申请加入战队
     */
    @PostMapping("/{teamId}/apply")
    public ResponseEntity<?> applyToJoinTeam(@PathVariable Integer teamId,
                                             @RequestBody Map<String, String> applyData,
                                             HttpServletRequest request) {
        try {
            Integer userId = getCurrentUserId(request);
            String remark = applyData.get("remark");

            TeamApplication application = teamService.applyToJoinTeam(teamId, userId, remark);
            return ResponseEntity.ok(createSuccessResponse("申请提交成功", application));
        } catch (SecurityException e) {
            return ResponseEntity.status(401).body(createErrorResponse("用户未登录"));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(createErrorResponse(e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(createErrorResponse("申请加入失败"));
        }
    }

    /**
     * 处理加入申请
     */
    @PostMapping("/applications/{applicationId}/process")
    public ResponseEntity<?> processApplication(@PathVariable Integer applicationId,
                                                @RequestBody Map<String, Object> processData,
                                                HttpServletRequest request) {
        try {
            Integer userId = getCurrentUserId(request);
            Boolean approved = (Boolean) processData.get("approved");
            String remark = (String) processData.get("remark");

            if (approved == null) {
                return ResponseEntity.badRequest().body(createErrorResponse("处理结果不能为空"));
            }

            TeamApplication application = teamService.processApplication(applicationId, approved, remark);
            String message = approved ? "已批准加入申请" : "已拒绝加入申请";
            return ResponseEntity.ok(createSuccessResponse(message, application));
        } catch (SecurityException e) {
            return ResponseEntity.status(401).body(createErrorResponse("用户未登录"));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(createErrorResponse(e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(createErrorResponse("处理申请失败"));
        }
    }

    /**
     * 获取战队申请列表
     */
    @GetMapping("/{teamId}/applications")
    public ResponseEntity<?> getTeamApplications(@PathVariable Integer teamId,
                                                 @RequestParam(defaultValue = "0") int page,
                                                 @RequestParam(defaultValue = "20") int size,
                                                 HttpServletRequest request) {
        try {
            Integer userId = getCurrentUserId(request);

            // 检查用户是否是队长
            if (!teamService.isTeamCaptain(teamId, userId)) {
                return ResponseEntity.status(403).body(createErrorResponse("无权查看申请列表"));
            }

            Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "applyTime"));
            Page<TeamApplication> applications = teamService.getTeamApplications(teamId, pageable);

            Map<String, Object> response = new HashMap<>();
            response.put("applications", applications.getContent());
            response.put("totalPages", applications.getTotalPages());
            response.put("totalElements", applications.getTotalElements());
            response.put("currentPage", applications.getNumber());

            return ResponseEntity.ok(createSuccessResponse("获取成功", response));
        } catch (SecurityException e) {
            return ResponseEntity.status(401).body(createErrorResponse("用户未登录"));
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(createErrorResponse("获取申请列表失败"));
        }
    }

    /**
     * 转让队长权限
     */
    @PostMapping("/{teamId}/transfer-captain")
    public ResponseEntity<?> transferCaptain(@PathVariable Integer teamId,
                                             @RequestBody Map<String, Integer> transferData,
                                             HttpServletRequest request) {
        try {
            Integer userId = getCurrentUserId(request);
            Integer newCaptainId = transferData.get("newCaptainId");

            if (newCaptainId == null) {
                return ResponseEntity.badRequest().body(createErrorResponse("新队长ID不能为空"));
            }

            boolean success = teamService.transferCaptain(teamId, newCaptainId, userId);
            if (success) {
                return ResponseEntity.ok(createSuccessResponse("队长权限转让成功"));
            } else {
                return ResponseEntity.badRequest().body(createErrorResponse("队长权限转让失败"));
            }
        } catch (SecurityException e) {
            return ResponseEntity.status(401).body(createErrorResponse("用户未登录"));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(createErrorResponse(e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(createErrorResponse("转让队长权限失败"));
        }
    }

    /**
     * 离开战队
     */
    @PostMapping("/{teamId}/leave")
    public ResponseEntity<?> leaveTeam(@PathVariable Integer teamId, HttpServletRequest request) {
        try {
            Integer userId = getCurrentUserId(request);

            boolean success = teamService.leaveTeam(teamId, userId);
            if (success) {
                return ResponseEntity.ok(createSuccessResponse("已成功离开战队"));
            } else {
                return ResponseEntity.badRequest().body(createErrorResponse("离开战队失败"));
            }
        } catch (SecurityException e) {
            return ResponseEntity.status(401).body(createErrorResponse("用户未登录"));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(createErrorResponse(e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(createErrorResponse("离开战队失败"));
        }
    }

    /**
     * 删除战队成员
     */
    @DeleteMapping("/{teamId}/members/{memberId}")
    public ResponseEntity<?> removeMember(@PathVariable Integer teamId,
                                          @PathVariable Integer memberId,
                                          HttpServletRequest request) {
        try {
            Integer userId = getCurrentUserId(request);

            // 检查用户是否是队长
            if (!teamService.isTeamCaptain(teamId, userId)) {
                return ResponseEntity.status(403).body(createErrorResponse("无权移除成员"));
            }

            boolean success = teamService.removeMember(teamId, memberId);
            if (success) {
                return ResponseEntity.ok(createSuccessResponse("成员移除成功"));
            } else {
                return ResponseEntity.badRequest().body(createErrorResponse("成员移除失败"));
            }
        } catch (SecurityException e) {
            return ResponseEntity.status(401).body(createErrorResponse("用户未登录"));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(createErrorResponse(e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(createErrorResponse("移除成员失败"));
        }
    }

    /**
     * 检查用户是否可以加入战队
     */
    @GetMapping("/{teamId}/can-join")
    public ResponseEntity<?> canJoinTeam(@PathVariable Integer teamId, HttpServletRequest request) {
        try {
            Integer userId = getCurrentUserId(request);
            boolean canJoin = teamService.canUserJoinTeam(teamId, userId);

            Map<String, Object> response = new HashMap<>();
            response.put("canJoin", canJoin);

            return ResponseEntity.ok(createSuccessResponse("获取成功", response));
        } catch (SecurityException e) {
            return ResponseEntity.status(401).body(createErrorResponse("用户未登录"));
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