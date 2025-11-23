package com.CTF.j_ctf.controller;

import com.CTF.j_ctf.entity.Competition;
import com.CTF.j_ctf.entity.Team;
import com.CTF.j_ctf.entity.TeamApplication;
import com.CTF.j_ctf.entity.User;
import com.CTF.j_ctf.repository.CompetitionRepository;
import com.CTF.j_ctf.service.TeamService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/teams")
public class TeamController {
    private final TeamService teamService;
    private final CompetitionRepository competitionRepository;

    public TeamController(TeamService teamService, CompetitionRepository competitionRepository) {
        this.teamService = teamService;
        this.competitionRepository = competitionRepository;
    }

    /**
     * 获取当前用户信息和权限
     */
    private Map<String, Object> getCurrentUserInfo(HttpServletRequest request) {
        HttpSession session = request.getSession(false);
        if (session == null) {
            throw new SecurityException("用户未登录");
        }

        Map<String, Object> userInfo = new HashMap<>();
        userInfo.put("userId", session.getAttribute("userId"));
        userInfo.put("userRole", session.getAttribute("userRole"));
        userInfo.put("userName", session.getAttribute("userName"));

        return userInfo;
    }

    /**
     * 检查管理员权限
     */
    private void checkAdminPermission(HttpServletRequest request) {
        Map<String, Object> userInfo = getCurrentUserInfo(request);
        String userRole = (String) userInfo.get("userRole");
        if (!"ADMIN".equals(userRole)) {
            throw new SecurityException("权限不足，需要管理员权限");
        }
    }

    /**
     * 检查竞赛创建者或管理员权限
     */
    private void checkCompetitionCreatorOrAdminPermission(Integer competitionId, HttpServletRequest request) {
        Map<String, Object> userInfo = getCurrentUserInfo(request);
        Integer userId = (Integer) userInfo.get("userId");
        String userRole = (String) userInfo.get("userRole");

        if ("ADMIN".equals(userRole)) {
            return; // 管理员有所有权限
        }

        // 检查用户是否是竞赛创建者
        boolean isCreator = teamService.isUserCompetitionCreator(competitionId, userId);
        if (!isCreator) {
            throw new SecurityException("权限不足，需要竞赛创建者或管理员权限");
        }
    }

    /**
     * 创建战队
     */
    @PostMapping
    public ResponseEntity<?> createTeam(@RequestBody Map<String, Object> requestData, HttpServletRequest request) {
        try {
            Map<String, Object> userInfo = getCurrentUserInfo(request);
            Integer userId = (Integer) userInfo.get("userId");

            // 创建用户对象
            User captain = new User();
            captain.setUserID(userId);

            // 从请求中获取数据
            String teamName = (String) requestData.get("teamName");
            Object competitionIdObj = requestData.get("competitionID");
            if (competitionIdObj == null) {
                competitionIdObj = requestData.get("competitionId");
            }
            
            if (teamName == null || teamName.trim().isEmpty()) {
                return ResponseEntity.badRequest().body(createErrorResponse("战队名称不能为空"));
            }
            
            if (competitionIdObj == null) {
                return ResponseEntity.badRequest().body(createErrorResponse("必须指定竞赛"));
            }
            
            Integer competitionId;
            if (competitionIdObj instanceof Integer) {
                competitionId = (Integer) competitionIdObj;
            } else if (competitionIdObj instanceof Number) {
                competitionId = ((Number) competitionIdObj).intValue();
            } else {
                return ResponseEntity.badRequest().body(createErrorResponse("竞赛ID格式错误"));
            }
            
            // 获取竞赛对象
            Optional<Competition> competitionOpt = competitionRepository.findById(competitionId);
            if (competitionOpt.isEmpty()) {
                return ResponseEntity.badRequest().body(createErrorResponse("竞赛不存在"));
            }
            
            // 创建 Team 对象
            Team team = new Team();
            team.setTeamName(teamName);
            team.setCompetition(competitionOpt.get());
            
            // 处理可选字段
            if (requestData.containsKey("description")) {
                team.setDescription((String) requestData.get("description"));
            }
            if (requestData.containsKey("inviteCode")) {
                // inviteCode 可以用于后续功能，暂时不处理
            }

            Team createdTeam = teamService.createTeam(team, captain);
            return ResponseEntity.ok(createSuccessResponse("创建战队成功", createdTeam));
        } catch (SecurityException e) {
            return ResponseEntity.status(401).body(createErrorResponse("用户未登录"));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(createErrorResponse(e.getMessage()));
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.internalServerError().body(createErrorResponse("创建战队失败: " + e.getMessage()));
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
            Map<String, Object> userInfo = getCurrentUserInfo(request);
            Integer userId = (Integer) userInfo.get("userId");
            String userRole = (String) userInfo.get("userRole");

            // 检查权限：管理员或战队队长
            if (!"ADMIN".equals(userRole) && !teamService.isTeamCaptain(teamId, userId)) {
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
     * 解散战队
     */
    @DeleteMapping("/{teamId}")
    public ResponseEntity<?> disbandTeam(@PathVariable Integer teamId, HttpServletRequest request) {
        try {
            Map<String, Object> userInfo = getCurrentUserInfo(request);
            Integer userId = (Integer) userInfo.get("userId");
            String userRole = (String) userInfo.get("userRole");

            // 检查权限：管理员或战队队长
            if (!"ADMIN".equals(userRole) && !teamService.isTeamCaptain(teamId, userId)) {
                return ResponseEntity.status(403).body(createErrorResponse("无权解散该战队"));
            }

            boolean success = teamService.disbandTeam(teamId);
            if (success) {
                return ResponseEntity.ok(createSuccessResponse("解散战队成功"));
            } else {
                return ResponseEntity.badRequest().body(createErrorResponse("解散战队失败"));
            }
        } catch (SecurityException e) {
            return ResponseEntity.status(401).body(createErrorResponse("用户未登录"));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(createErrorResponse(e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(createErrorResponse("解散战队失败"));
        }
    }

    /**
     * 获取战队详情
     */
    @GetMapping("/{teamId}")
    public ResponseEntity<?> getTeam(@PathVariable Integer teamId) {
        try {
            // 使用带成员的方法，避免 LAZY 加载问题
            Optional<Team> teamOpt = teamService.getTeamByIdWithMembers(teamId);
            if (teamOpt.isPresent()) {
                Team team = teamOpt.get();
                // 转换为 DTO 以避免 LAZY 加载序列化问题
                Map<String, Object> teamData = new HashMap<>();
                teamData.put("teamID", team.getTeamID());
                teamData.put("teamName", team.getTeamName());
                teamData.put("description", team.getDescription());
                teamData.put("auditState", team.getAuditState());
                teamData.put("auditRemark", team.getAuditRemark());
                teamData.put("creationTime", team.getCreationTime());
                
                // 队长信息
                if (team.getCaptain() != null) {
                    Map<String, Object> captainMap = new HashMap<>();
                    captainMap.put("userID", team.getCaptain().getUserID());
                    captainMap.put("userName", team.getCaptain().getUserName());
                    teamData.put("captain", captainMap);
                }
                
                // 竞赛信息
                if (team.getCompetition() != null) {
                    Map<String, Object> competitionMap = new HashMap<>();
                    competitionMap.put("competitionID", team.getCompetition().getCompetitionID());
                    competitionMap.put("title", team.getCompetition().getTitle());
                    teamData.put("competition", competitionMap);
                }
                
                // 成员列表
                List<Map<String, Object>> membersList = new ArrayList<>();
                Integer captainId = team.getCaptain() != null ? team.getCaptain().getUserID() : null;
                if (team.getTeamMembers() != null) {
                    for (com.CTF.j_ctf.entity.TeamMember member : team.getTeamMembers()) {
                        if (member.getUser() != null) {
                            Map<String, Object> memberMap = new HashMap<>();
                            Integer memberUserId = member.getUser().getUserID();
                            memberMap.put("userID", memberUserId);
                            memberMap.put("userName", member.getUser().getUserName());
                            // 标记是否为队长（通过 captain 字段判断）
                            memberMap.put("isCaptain", captainId != null && captainId.equals(memberUserId));
                            // User 实体的邮箱字段是 userEmail
                            if (member.getUser().getUserEmail() != null) {
                                memberMap.put("email", member.getUser().getUserEmail());
                            }
                            if (member.getUser().getPhoneNumber() != null) {
                                memberMap.put("phoneNumber", member.getUser().getPhoneNumber());
                            }
                            membersList.add(memberMap);
                        }
                    }
                }
                teamData.put("members", membersList);
                
                return ResponseEntity.ok(createSuccessResponse("获取成功", teamData));
            } else {
                return ResponseEntity.notFound().build();
            }
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.internalServerError().body(createErrorResponse("获取战队详情失败: " + e.getMessage()));
        }
    }

    /**
     * 获取当前用户所在的战队
     */
    @GetMapping("/my-team")
    public ResponseEntity<?> getMyTeam(@RequestParam(required = false) Integer competitionId,
                                       HttpServletRequest request) {
        try {
            Map<String, Object> userInfo = getCurrentUserInfo(request);
            Integer userId = (Integer) userInfo.get("userId");

            if (userId == null) {
                return ResponseEntity.status(401).body(createErrorResponse("用户未登录"));
            }

            Optional<Team> teamOpt;
            if (competitionId != null) {
                teamOpt = teamService.getUserTeamInCompetition(userId, competitionId);
            } else {
                teamOpt = teamService.getUserCurrentTeam(userId);
            }

            if (teamOpt.isPresent()) {
                return ResponseEntity.ok(createSuccessResponse("获取成功", teamOpt.get()));
            } else {
                Map<String, Object> response = new HashMap<>();
                response.put("hasTeam", false);
                return ResponseEntity.ok(createSuccessResponse("用户当前未加入任何战队", response));
            }
        } catch (SecurityException e) {
            return ResponseEntity.status(401).body(createErrorResponse("用户未登录"));
        } catch (Exception e) {
            e.printStackTrace(); // 打印异常堆栈以便调试
            return ResponseEntity.internalServerError().body(createErrorResponse("获取战队信息失败: " + e.getMessage()));
        }
    }

    /**
     * 获取当前用户加入的所有战队
     */
    @GetMapping("/my-teams")
    public ResponseEntity<?> getMyTeams(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            HttpServletRequest request) {
        try {
            Map<String, Object> userInfo = getCurrentUserInfo(request);
            Integer userId = (Integer) userInfo.get("userId");

            if (userId == null) {
                return ResponseEntity.status(401).body(createErrorResponse("用户未登录"));
            }

            Pageable pageable = PageRequest.of(page, size);
            Page<Team> teams = teamService.getTeamsByMember(userId, pageable);

            // 转换为DTO以避免LAZY加载序列化问题
            List<Map<String, Object>> teamDTOs = new ArrayList<>();
            for (Team team : teams.getContent()) {
                Map<String, Object> teamData = new HashMap<>();
                teamData.put("teamID", team.getTeamID());
                teamData.put("id", team.getTeamID()); // 兼容字段
                teamData.put("teamName", team.getTeamName());
                teamData.put("name", team.getTeamName()); // 兼容字段
                teamData.put("description", team.getDescription());
                teamData.put("auditState", team.getAuditState());
                teamData.put("auditRemark", team.getAuditRemark());
                teamData.put("creationTime", team.getCreationTime());
                
                // 队长信息
                if (team.getCaptain() != null) {
                    Map<String, Object> captainMap = new HashMap<>();
                    captainMap.put("userID", team.getCaptain().getUserID());
                    captainMap.put("userName", team.getCaptain().getUserName());
                    teamData.put("captain", captainMap);
                    teamData.put("captainName", team.getCaptain().getUserName());
                }
                
                // 竞赛信息
                if (team.getCompetition() != null) {
                    Map<String, Object> competitionMap = new HashMap<>();
                    competitionMap.put("competitionID", team.getCompetition().getCompetitionID());
                    competitionMap.put("id", team.getCompetition().getCompetitionID()); // 兼容字段
                    competitionMap.put("title", team.getCompetition().getTitle());
                    competitionMap.put("Title", team.getCompetition().getTitle()); // 兼容字段
                    teamData.put("competition", competitionMap);
                    teamData.put("competitionID", team.getCompetition().getCompetitionID());
                    teamData.put("competitionId", team.getCompetition().getCompetitionID()); // 兼容字段
                    teamData.put("competitionName", team.getCompetition().getTitle());
                }
                
                // 成员数量 - 使用安全的方式获取，避免LAZY加载异常
                try {
                    List<Map<String, Object>> membersList = new ArrayList<>();
                    if (team.getTeamMembers() != null && !team.getTeamMembers().isEmpty()) {
                        for (com.CTF.j_ctf.entity.TeamMember member : team.getTeamMembers()) {
                            try {
                                if (member != null && member.getUser() != null) {
                                    Map<String, Object> memberMap = new HashMap<>();
                                    memberMap.put("userID", member.getUser().getUserID());
                                    memberMap.put("userName", member.getUser().getUserName());
                                    memberMap.put("email", member.getUser().getUserEmail());
                                    memberMap.put("phoneNumber", member.getUser().getPhoneNumber());
                                    membersList.add(memberMap);
                                }
                            } catch (Exception e) {
                                // 忽略单个成员加载失败
                                System.err.println("加载团队成员失败: " + e.getMessage());
                            }
                        }
                    }
                    teamData.put("members", membersList);
                } catch (Exception e) {
                    // 如果无法加载成员列表，设置为空列表
                    System.err.println("加载团队成员列表失败: " + e.getMessage());
                    teamData.put("members", new ArrayList<>());
                }
                
                teamDTOs.add(teamData);
            }

            Map<String, Object> response = new HashMap<>();
            response.put("teams", teamDTOs);
            response.put("totalPages", teams.getTotalPages());
            response.put("totalElements", teams.getTotalElements());
            response.put("currentPage", teams.getNumber());
            response.put("pageSize", teams.getSize());

            return ResponseEntity.ok(createSuccessResponse("获取成功", response));
        } catch (SecurityException e) {
            return ResponseEntity.status(401).body(createErrorResponse("用户未登录"));
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.internalServerError().body(createErrorResponse("获取我的战队列表失败"));
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
            response.put("pageSize", teams.getSize());

            return ResponseEntity.ok(createSuccessResponse("获取成功", response));
        } catch (Exception e) {
            e.printStackTrace(); // 打印堆栈跟踪以便调试
            return ResponseEntity.internalServerError()
                    .body(createErrorResponse("获取战队列表失败: " + e.getMessage()));
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
            Map<String, Object> userInfo = getCurrentUserInfo(request);
            Integer userId = (Integer) userInfo.get("userId");
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
            Map<String, Object> userInfo = getCurrentUserInfo(request);
            Integer userId = (Integer) userInfo.get("userId");

            Boolean approved = (Boolean) processData.get("approved");
            String remark = (String) processData.get("remark");

            if (approved == null) {
                return ResponseEntity.badRequest().body(createErrorResponse("处理结果不能为空"));
            }

            // 检查权限：必须是战队队长或管理员
            Optional<TeamApplication> applicationOpt = teamService.getApplicationById(applicationId);
            if (applicationOpt.isEmpty()) {
                return ResponseEntity.badRequest().body(createErrorResponse("申请不存在"));
            }

            TeamApplication application = applicationOpt.get();
            String userRole = (String) userInfo.get("userRole");
            boolean isCaptain = teamService.isTeamCaptain(application.getTeam().getTeamID(), userId);

            if (!"ADMIN".equals(userRole) && !isCaptain) {
                return ResponseEntity.status(403).body(createErrorResponse("无权处理该申请"));
            }

            TeamApplication processedApplication = teamService.processApplication(applicationId, approved, remark);
            String message = approved ? "已批准加入申请" : "已拒绝加入申请";
            return ResponseEntity.ok(createSuccessResponse(message, processedApplication));
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
                                                 @RequestParam(required = false) String status,
                                                 HttpServletRequest request) {
        try {
            Map<String, Object> userInfo = getCurrentUserInfo(request);
            Integer userId = (Integer) userInfo.get("userId");
            String userRole = (String) userInfo.get("userRole");

            // 检查权限：必须是战队队长或管理员
            if (userId == null) {
                return ResponseEntity.status(401).body(createErrorResponse("用户未登录"));
            }
            if (!"ADMIN".equals(userRole) && !teamService.isTeamCaptain(teamId, userId)) {
                return ResponseEntity.status(403).body(createErrorResponse("无权查看申请列表"));
            }

            Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "applyTime"));
            Page<TeamApplication> applications;

            if (status != null) {
                applications = teamService.getTeamApplicationsByStatus(teamId, status, pageable);
            } else {
                applications = teamService.getTeamApplications(teamId, pageable);
            }

            Map<String, Object> response = new HashMap<>();
            response.put("applications", applications.getContent());
            response.put("totalPages", applications.getTotalPages());
            response.put("totalElements", applications.getTotalElements());
            response.put("currentPage", applications.getNumber());
            response.put("pageSize", applications.getSize());

            return ResponseEntity.ok(createSuccessResponse("获取成功", response));
        } catch (SecurityException e) {
            return ResponseEntity.status(401).body(createErrorResponse("用户未登录"));
        } catch (Exception e) {
            e.printStackTrace(); // 打印异常堆栈以便调试
            return ResponseEntity.internalServerError().body(createErrorResponse("获取申请列表失败: " + e.getMessage()));
        }
    }

    /**
     * 获取用户申请记录
     */
    @GetMapping("/my-applications")
    public ResponseEntity<?> getMyApplications(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            HttpServletRequest request) {
        try {
            Map<String, Object> userInfo = getCurrentUserInfo(request);
            Integer userId = (Integer) userInfo.get("userId");

            Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "applyTime"));
            Page<TeamApplication> applications = teamService.getUserApplications(userId, pageable);

            Map<String, Object> response = new HashMap<>();
            response.put("applications", applications.getContent());
            response.put("totalPages", applications.getTotalPages());
            response.put("totalElements", applications.getTotalElements());
            response.put("currentPage", applications.getNumber());
            response.put("pageSize", applications.getSize());

            return ResponseEntity.ok(createSuccessResponse("获取成功", response));
        } catch (SecurityException e) {
            return ResponseEntity.status(401).body(createErrorResponse("用户未登录"));
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(createErrorResponse("获取申请记录失败"));
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
            Map<String, Object> userInfo = getCurrentUserInfo(request);
            Integer userId = (Integer) userInfo.get("userId");
            Integer newCaptainId = transferData.get("newCaptainId");

            if (newCaptainId == null) {
                return ResponseEntity.badRequest().body(createErrorResponse("新队长ID不能为空"));
            }

            // 检查权限：必须是当前队长
            if (!teamService.isTeamCaptain(teamId, userId)) {
                return ResponseEntity.status(403).body(createErrorResponse("无权转让队长权限"));
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
            Map<String, Object> userInfo = getCurrentUserInfo(request);
            Integer userId = (Integer) userInfo.get("userId");

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
            Map<String, Object> userInfo = getCurrentUserInfo(request);
            Integer userId = (Integer) userInfo.get("userId");
            String userRole = (String) userInfo.get("userRole");

            // 检查权限：管理员或战队队长
            if (!"ADMIN".equals(userRole) && !teamService.isTeamCaptain(teamId, userId)) {
                return ResponseEntity.status(403).body(createErrorResponse("无权移除成员"));
            }

            // 队长不能移除自己
            if (teamService.isTeamCaptain(teamId, memberId)) {
                return ResponseEntity.badRequest().body(createErrorResponse("不能移除队长"));
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
     * 审核战队（管理员功能）
     */
    @PostMapping("/{teamId}/audit")
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

            Team team = teamService.auditTeam(teamId, auditState, auditRemark);
            return ResponseEntity.ok(createSuccessResponse("审核成功", team));
        } catch (SecurityException e) {
            return ResponseEntity.status(403).body(createErrorResponse(e.getMessage()));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(createErrorResponse(e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(createErrorResponse("审核战队失败"));
        }
    }

    /**
     * 检查用户是否可以加入战队
     */
    @GetMapping("/{teamId}/can-join")
    public ResponseEntity<?> canJoinTeam(@PathVariable Integer teamId, HttpServletRequest request) {
        try {
            Map<String, Object> userInfo = getCurrentUserInfo(request);
            Integer userId = (Integer) userInfo.get("userId");

            boolean canJoin = teamService.canUserJoinTeam(teamId, userId);
            String reason = canJoin ? "可以加入" : teamService.getJoinRestrictionReason(teamId, userId);

            Map<String, Object> response = new HashMap<>();
            response.put("canJoin", canJoin);
            response.put("reason", reason);

            return ResponseEntity.ok(createSuccessResponse("获取成功", response));
        } catch (SecurityException e) {
            return ResponseEntity.status(401).body(createErrorResponse("用户未登录"));
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(createErrorResponse("检查失败"));
        }
    }

    /**
     * 获取战队统计信息
     */
    @GetMapping("/{teamId}/stats")
    public ResponseEntity<?> getTeamStatistics(@PathVariable Integer teamId, HttpServletRequest request) {
        try {
            Map<String, Object> userInfo = getCurrentUserInfo(request);
            Integer userId = (Integer) userInfo.get("userId");
            String userRole = (String) userInfo.get("userRole");

            // 检查权限：管理员或战队成员
            boolean isMember = teamService.isTeamMember(teamId, userId);
            if (!"ADMIN".equals(userRole) && !isMember) {
                return ResponseEntity.status(403).body(createErrorResponse("无权查看统计信息"));
            }

            Map<String, Object> stats = teamService.getTeamStatistics(teamId);
            return ResponseEntity.ok(createSuccessResponse("获取成功", stats));
        } catch (SecurityException e) {
            return ResponseEntity.status(401).body(createErrorResponse("用户未登录"));
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(createErrorResponse("获取统计信息失败"));
        }
    }

    /**
     * 邀请用户加入战队
     */
    @PostMapping("/{teamId}/invite")
    public ResponseEntity<?> inviteUser(@PathVariable Integer teamId,
                                        @RequestBody Map<String, Integer> inviteData,
                                        HttpServletRequest request) {
        try {
            Map<String, Object> userInfo = getCurrentUserInfo(request);
            Integer userId = (Integer) userInfo.get("userId");
            Integer targetUserId = inviteData.get("targetUserId");

            if (targetUserId == null) {
                return ResponseEntity.badRequest().body(createErrorResponse("目标用户ID不能为空"));
            }

            // 检查权限：必须是战队队长
            if (!teamService.isTeamCaptain(teamId, userId)) {
                return ResponseEntity.status(403).body(createErrorResponse("无权邀请用户"));
            }

            TeamApplication invitation = teamService.inviteUser(teamId, targetUserId, userId);
            return ResponseEntity.ok(createSuccessResponse("邀请发送成功", invitation));
        } catch (SecurityException e) {
            return ResponseEntity.status(401).body(createErrorResponse("用户未登录"));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(createErrorResponse(e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(createErrorResponse("发送邀请失败"));
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