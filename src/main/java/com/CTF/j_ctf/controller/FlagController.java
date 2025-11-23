package com.CTF.j_ctf.controller;

import com.CTF.j_ctf.entity.Flag;
import com.CTF.j_ctf.entity.FlagSubmission;
import com.CTF.j_ctf.entity.User;
import com.CTF.j_ctf.service.FlagService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import java.time.LocalDateTime;
import java.util.*;

@RestController
@RequestMapping("/api/flags")
public class FlagController {
    private final FlagService flagService;

    public FlagController(FlagService flagService) {
        this.flagService = flagService;
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
        boolean isCreator = flagService.isUserCompetitionCreator(competitionId, userId);
        if (!isCreator) {
            throw new SecurityException("权限不足，需要竞赛创建者或管理员权限");
        }
    }

    /**
     * 获取客户端IP地址
     */
    private String getClientIpAddress(HttpServletRequest request) {
        String xForwardedFor = request.getHeader("X-Forwarded-For");
        if (xForwardedFor != null && !xForwardedFor.isEmpty()) {
            return xForwardedFor.split(",")[0].trim();
        }
        return request.getRemoteAddr();
    }

    /**
     * 获取客户端User-Agent
     */
    private String getClientUserAgent(HttpServletRequest request) {
        return request.getHeader("User-Agent");
    }

    /**
     * 创建Flag
     */
    @PostMapping
    public ResponseEntity<?> createFlag(@RequestBody Flag flag, HttpServletRequest request) {
        try {
            // 检查权限，只有管理员或竞赛创建者可以创建Flag
            checkCompetitionCreatorOrAdminPermission(flag.getCompetition().getCompetitionID(), request);

            Flag createdFlag = flagService.createFlag(flag);
            return ResponseEntity.ok(createSuccessResponse("创建Flag成功", createdFlag));
        } catch (SecurityException e) {
            return ResponseEntity.status(403).body(createErrorResponse(e.getMessage()));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(createErrorResponse(e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(createErrorResponse("创建Flag失败"));
        }
    }

    /**
     * 更新Flag
     */
    @PutMapping("/{flagId}")
    public ResponseEntity<?> updateFlag(@PathVariable Integer flagId,
                                        @RequestBody Flag flag,
                                        HttpServletRequest request) {
        try {
            // 获取Flag对应的竞赛ID
            Optional<Flag> existingFlagOpt = flagService.getFlagById(flagId);
            if (existingFlagOpt.isEmpty()) {
                return ResponseEntity.badRequest().body(createErrorResponse("Flag不存在"));
            }

            Integer competitionId = existingFlagOpt.get().getCompetition().getCompetitionID();
            checkCompetitionCreatorOrAdminPermission(competitionId, request);

            flag.setFlagID(flagId);
            Flag updatedFlag = flagService.updateFlag(flag);
            return ResponseEntity.ok(createSuccessResponse("更新Flag成功", updatedFlag));
        } catch (SecurityException e) {
            return ResponseEntity.status(403).body(createErrorResponse(e.getMessage()));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(createErrorResponse(e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(createErrorResponse("更新Flag失败"));
        }
    }

    /**
     * 删除Flag
     */
    @DeleteMapping("/{flagId}")
    public ResponseEntity<?> deleteFlag(@PathVariable Integer flagId, HttpServletRequest request) {
        try {
            // 获取Flag对应的竞赛ID
            Optional<Flag> existingFlagOpt = flagService.getFlagById(flagId);
            if (existingFlagOpt.isEmpty()) {
                return ResponseEntity.badRequest().body(createErrorResponse("Flag不存在"));
            }

            Integer competitionId = existingFlagOpt.get().getCompetition().getCompetitionID();
            checkCompetitionCreatorOrAdminPermission(competitionId, request);

            boolean success = flagService.deleteFlag(flagId);
            if (success) {
                return ResponseEntity.ok(createSuccessResponse("删除Flag成功"));
            } else {
                return ResponseEntity.badRequest().body(createErrorResponse("删除Flag失败"));
            }
        } catch (SecurityException e) {
            return ResponseEntity.status(403).body(createErrorResponse(e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(createErrorResponse("删除Flag失败"));
        }
    }

    /**
     * 获取Flag详情
     */
    @GetMapping("/{flagId}")
    public ResponseEntity<?> getFlag(@PathVariable Integer flagId, HttpServletRequest request) {
        try {
            Map<String, Object> userInfo = getCurrentUserInfo(request);
            Integer userId = (Integer) userInfo.get("userId");
            String userRole = (String) userInfo.get("userRole");

            Optional<Flag> flagOpt = flagService.getFlagById(flagId);
            if (flagOpt.isPresent()) {
                Flag flag = flagOpt.get();

                // 对于非管理员和非创建者，隐藏Flag值
                boolean canViewFlag = "ADMIN".equals(userRole) ||
                        flagService.isUserCompetitionCreator(flag.getCompetition().getCompetitionID(), userId);

                if (!canViewFlag) {
                    flag.setFlagValue("***"); // 隐藏真实Flag值
                }

                return ResponseEntity.ok(createSuccessResponse("获取成功", flag));
            } else {
                return ResponseEntity.notFound().build();
            }
        } catch (SecurityException e) {
            return ResponseEntity.status(401).body(createErrorResponse("用户未登录"));
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(createErrorResponse("获取Flag详情失败"));
        }
    }

    /**
     * 获取Flag列表
     */
    @GetMapping
    public ResponseEntity<?> getFlags(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "createTime") String sort,
            @RequestParam(required = false) Integer competitionId,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String keyword,
            HttpServletRequest request) {
        try {
            Map<String, Object> userInfo = getCurrentUserInfo(request);
            Integer userId = (Integer) userInfo.get("userId");
            String userRole = (String) userInfo.get("userRole");

            Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, sort));
            Page<Flag> flags;

            if (keyword != null && !keyword.trim().isEmpty()) {
                flags = flagService.searchFlags(keyword, pageable);
            } else if (competitionId != null) {
                flags = flagService.getFlagsByCompetition(competitionId, pageable);
            } else if (status != null) {
                flags = flagService.getFlagsByStatus(Integer.valueOf(status), pageable);
            } else {
                flags = flagService.getAllFlags(pageable);
            }

            // 对于非管理员和非创建者，隐藏Flag值
            boolean isAdmin = "ADMIN".equals(userRole);
            for (Flag flag : flags.getContent()) {
                boolean canViewFlag = isAdmin ||
                        flagService.isUserCompetitionCreator(flag.getCompetition().getCompetitionID(), userId);

                if (!canViewFlag) {
                    flag.setFlagValue("***"); // 隐藏真实Flag值
                }
            }

            Map<String, Object> response = new HashMap<>();
            response.put("flags", flags.getContent());
            response.put("totalPages", flags.getTotalPages());
            response.put("totalElements", flags.getTotalElements());
            response.put("currentPage", flags.getNumber());
            response.put("pageSize", flags.getSize());

            return ResponseEntity.ok(createSuccessResponse("获取成功", response));
        } catch (SecurityException e) {
            return ResponseEntity.status(401).body(createErrorResponse("用户未登录"));
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(createErrorResponse("获取Flag列表失败"));
        }
    }

    /**
     * 提交Flag（个人）
     */
    @PostMapping("/{flagId}/submit")
    public ResponseEntity<?> submitFlag(@PathVariable Integer flagId,
                                        @RequestBody Map<String, String> submitData,
                                        HttpServletRequest request) {
        try {
            Map<String, Object> userInfo = getCurrentUserInfo(request);
            Integer userId = (Integer) userInfo.get("userId");

            String submittedValue = submitData.get("value");
            String ipAddress = getClientIpAddress(request);
            String userAgent = getClientUserAgent(request);

            if (submittedValue == null || submittedValue.trim().isEmpty()) {
                return ResponseEntity.badRequest().body(createErrorResponse("Flag值不能为空"));
            }

            // 检查用户是否可以提交
            if (!flagService.canUserSubmitFlag(flagId, userId)) {
                return ResponseEntity.badRequest().body(createErrorResponse("当前无法提交Flag"));
            }

            FlagSubmission submission = flagService.submitFlag(flagId, userId, submittedValue, ipAddress, userAgent);

            Map<String, Object> response = new HashMap<>();
            response.put("isCorrect", submission.getIsCorrect());
            response.put("pointsAwarded", submission.getPointsAwarded());
            response.put("submissionId", submission.getSubmissionID());
            response.put("submitTime", submission.getSubmitTime());
            response.put("message", submission.getIsCorrect() ?
                    "Flag正确！恭喜您获得了 " + submission.getPointsAwarded() + " 分！" :
                    "Flag错误，请重新尝试！");

            return ResponseEntity.ok(createSuccessResponse("提交成功", response));
        } catch (SecurityException e) {
            return ResponseEntity.status(401).body(createErrorResponse("用户未登录"));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(createErrorResponse(e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(createErrorResponse("提交Flag失败"));
        }
    }

    /**
     * 提交Flag（战队）
     */
    @PostMapping("/{flagId}/submit-team")
    public ResponseEntity<?> submitFlagForTeam(@PathVariable Integer flagId,
                                               @RequestBody Map<String, String> submitData,
                                               HttpServletRequest request) {
        try {
            Map<String, Object> userInfo = getCurrentUserInfo(request);
            Integer userId = (Integer) userInfo.get("userId");

            String teamIdStr = submitData.get("teamId");
            String submittedValue = submitData.get("value");
            String ipAddress = getClientIpAddress(request);
            String userAgent = getClientUserAgent(request);

            if (submittedValue == null || submittedValue.trim().isEmpty()) {
                return ResponseEntity.badRequest().body(createErrorResponse("Flag值不能为空"));
            }

            if (teamIdStr == null || teamIdStr.trim().isEmpty()) {
                return ResponseEntity.badRequest().body(createErrorResponse("战队ID不能为空"));
            }

            Integer teamId = Integer.parseInt(teamIdStr);

            // 检查用户是否可以提交
            if (!flagService.canTeamSubmitFlag(flagId, teamId)) {
                return ResponseEntity.badRequest().body(createErrorResponse("当前无法提交Flag"));
            }

            FlagSubmission submission = flagService.submitFlagForTeam(flagId, teamId, submittedValue, ipAddress, userAgent);

            Map<String, Object> response = new HashMap<>();
            response.put("isCorrect", submission.getIsCorrect());
            response.put("pointsAwarded", submission.getPointsAwarded());
            response.put("submissionId", submission.getSubmissionID());
            response.put("submitTime", submission.getSubmitTime());
            response.put("message", submission.getIsCorrect() ?
                    "Flag正确！恭喜战队获得了 " + submission.getPointsAwarded() + " 分！" :
                    "Flag错误，请重新尝试！");

            return ResponseEntity.ok(createSuccessResponse("提交成功", response));
        } catch (SecurityException e) {
            return ResponseEntity.status(401).body(createErrorResponse("用户未登录"));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(createErrorResponse(e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(createErrorResponse("提交Flag失败"));
        }
    }

    /**
     * 获取提交记录列表
     */
    @GetMapping("/submissions")
    @org.springframework.transaction.annotation.Transactional(readOnly = true)
    public ResponseEntity<?> getSubmissions(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "submitTime") String sort,
            @RequestParam(required = false) Integer competitionId,
            @RequestParam(required = false) Integer flagId,
            @RequestParam(required = false) Integer userId,
            HttpServletRequest request) {
        try {
            Map<String, Object> currentUserInfo = getCurrentUserInfo(request);
            String userRole = (String) currentUserInfo.get("userRole");

            // 非管理员只能查看自己的提交记录
            if (userId != null && !"ADMIN".equals(userRole)) {
                Integer currentUserId = (Integer) currentUserInfo.get("userId");
                if (!currentUserId.equals(userId)) {
                    return ResponseEntity.status(403).body(createErrorResponse("无权查看其他用户的提交记录"));
                }
            }

            Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, sort));
            Page<FlagSubmission> submissions;

            if (competitionId != null) {
                submissions = flagService.getSubmissionsByCompetition(competitionId, pageable);
            } else if (flagId != null) {
                submissions = flagService.getSubmissionsByFlag(flagId, pageable);
            } else if (userId != null) {
                submissions = flagService.getSubmissionsByUser(userId, pageable);
            } else {
                // 非管理员只能查看自己的提交记录
                if (!"ADMIN".equals(userRole)) {
                    Integer currentUserId = (Integer) currentUserInfo.get("userId");
                    submissions = flagService.getSubmissionsByUser(currentUserId, pageable);
                } else {
                    submissions = flagService.getAllSubmissions(pageable);
                }
            }

            // 转换为 DTO 以避免 LAZY 加载序列化问题
            List<Map<String, Object>> submissionList = new ArrayList<>();
            if (submissions != null && submissions.getContent() != null) {
                for (FlagSubmission submission : submissions.getContent()) {
                    Map<String, Object> subMap = new HashMap<>();
                    subMap.put("submissionID", submission.getSubmissionID());
                    subMap.put("submittedContent", submission.getSubmittedContent());
                    subMap.put("isCorrect", submission.getIsCorrect());
                    subMap.put("submitTime", submission.getSubmitTime());
                    subMap.put("ipAddress", submission.getIpAddress());
                    subMap.put("userAgent", submission.getUserAgent());
                    subMap.put("pointsAwarded", submission.getPointsAwarded());
                    
                    // 安全地获取关联对象的信息
                    if (submission.getChallenge() != null) {
                        Map<String, Object> challengeMap = new HashMap<>();
                        challengeMap.put("challengeID", submission.getChallenge().getChallengeID());
                        challengeMap.put("title", submission.getChallenge().getTitle());
                        subMap.put("challenge", challengeMap);
                    }
                    
                    if (submission.getCompetition() != null) {
                        Map<String, Object> competitionMap = new HashMap<>();
                        competitionMap.put("competitionID", submission.getCompetition().getCompetitionID());
                        competitionMap.put("title", submission.getCompetition().getTitle());
                        subMap.put("competition", competitionMap);
                    }
                    
                    if (submission.getUser() != null) {
                        Map<String, Object> userMap = new HashMap<>();
                        userMap.put("userID", submission.getUser().getUserID());
                        userMap.put("userName", submission.getUser().getUserName());
                        // User 实体有 getUserEmail() 方法
                        userMap.put("userEmail", submission.getUser().getUserEmail());
                        subMap.put("user", userMap);
                    }
                    
                    if (submission.getTeam() != null) {
                        Map<String, Object> teamMap = new HashMap<>();
                        teamMap.put("teamID", submission.getTeam().getTeamID());
                        teamMap.put("teamName", submission.getTeam().getTeamName());
                        subMap.put("team", teamMap);
                    }
                    
                    submissionList.add(subMap);
                }
            }

            Map<String, Object> response = new HashMap<>();
            response.put("submissions", submissionList);
            if (submissions != null) {
                response.put("totalPages", submissions.getTotalPages());
                response.put("totalElements", submissions.getTotalElements());
                response.put("currentPage", submissions.getNumber());
                response.put("pageSize", submissions.getSize());
            } else {
                response.put("totalPages", 0);
                response.put("totalElements", 0);
                response.put("currentPage", page);
                response.put("pageSize", size);
            }

            return ResponseEntity.ok(createSuccessResponse("获取成功", response));
        } catch (SecurityException e) {
            return ResponseEntity.status(401).body(createErrorResponse("用户未登录"));
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(createErrorResponse("获取提交记录失败"));
        }
    }

    /**
     * 获取用户提交记录
     */
    @GetMapping("/my-submissions")
    @org.springframework.transaction.annotation.Transactional(readOnly = true)
    public ResponseEntity<?> getMySubmissions(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(required = false) Integer competitionId,
            HttpServletRequest request) {
        try {
            Map<String, Object> userInfo = getCurrentUserInfo(request);
            Integer userId = (Integer) userInfo.get("userId");

            if (userId == null) {
                return ResponseEntity.status(401).body(createErrorResponse("用户未登录"));
            }

            Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "submitTime"));
            Page<FlagSubmission> submissions;

            if (competitionId != null) {
                submissions = flagService.getUserSubmissionsByCompetition(userId, competitionId, pageable);
            } else {
                submissions = flagService.getSubmissionsByUser(userId, pageable);
            }

            // 转换为 DTO 以避免 LAZY 加载序列化问题
            // 注意：必须在事务内访问关联对象
            List<Map<String, Object>> submissionList = new ArrayList<>();
            if (submissions != null && submissions.getContent() != null) {
                for (FlagSubmission submission : submissions.getContent()) {
                    Map<String, Object> subMap = new HashMap<>();
                    subMap.put("submissionID", submission.getSubmissionID());
                    subMap.put("submittedContent", submission.getSubmittedContent());
                    subMap.put("isCorrect", submission.getIsCorrect());
                    subMap.put("submitTime", submission.getSubmitTime());
                    subMap.put("ipAddress", submission.getIpAddress());
                    subMap.put("userAgent", submission.getUserAgent());
                    subMap.put("pointsAwarded", submission.getPointsAwarded());
                    
                    // 安全地获取关联对象的信息
                    if (submission.getChallenge() != null) {
                        Map<String, Object> challengeMap = new HashMap<>();
                        challengeMap.put("challengeID", submission.getChallenge().getChallengeID());
                        challengeMap.put("title", submission.getChallenge().getTitle());
                        subMap.put("challenge", challengeMap);
                    }
                    
                    if (submission.getCompetition() != null) {
                        Map<String, Object> competitionMap = new HashMap<>();
                        competitionMap.put("competitionID", submission.getCompetition().getCompetitionID());
                        competitionMap.put("title", submission.getCompetition().getTitle());
                        subMap.put("competition", competitionMap);
                    }
                    
                    if (submission.getUser() != null) {
                        Map<String, Object> userMap = new HashMap<>();
                        userMap.put("userID", submission.getUser().getUserID());
                        userMap.put("userName", submission.getUser().getUserName());
                        userMap.put("userEmail", submission.getUser().getUserEmail());
                        subMap.put("user", userMap);
                    }
                    
                    if (submission.getTeam() != null) {
                        Map<String, Object> teamMap = new HashMap<>();
                        teamMap.put("teamID", submission.getTeam().getTeamID());
                        teamMap.put("teamName", submission.getTeam().getTeamName());
                        subMap.put("team", teamMap);
                    }
                    
                    submissionList.add(subMap);
                }
            }

            Map<String, Object> response = new HashMap<>();
            response.put("submissions", submissionList);
            response.put("totalPages", submissions != null ? submissions.getTotalPages() : 0);
            response.put("totalElements", submissions != null ? submissions.getTotalElements() : 0);
            response.put("currentPage", submissions != null ? submissions.getNumber() : page);
            response.put("pageSize", submissions != null ? submissions.getSize() : size);

            return ResponseEntity.ok(createSuccessResponse("获取成功", response));
        } catch (SecurityException e) {
            return ResponseEntity.status(401).body(createErrorResponse("用户未登录"));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(createErrorResponse(e.getMessage()));
        } catch (Exception e) {
            e.printStackTrace(); // 打印异常堆栈以便调试
            String errorMessage = e.getMessage() != null ? e.getMessage() : "获取提交记录失败";
            // 如果是嵌套异常，获取根原因
            Throwable cause = e.getCause();
            if (cause != null && cause.getMessage() != null) {
                errorMessage = cause.getMessage();
            }
            return ResponseEntity.internalServerError().body(createErrorResponse("获取提交记录失败: " + errorMessage));
        }
    }

    /**
     * 检查用户是否可以提交Flag
     */
    @GetMapping("/{flagId}/can-submit")
    public ResponseEntity<?> canSubmitFlag(@PathVariable Integer flagId, HttpServletRequest request) {
        try {
            Map<String, Object> userInfo = getCurrentUserInfo(request);
            Integer userId = (Integer) userInfo.get("userId");

            boolean canSubmit = flagService.canUserSubmitFlag(flagId, userId);
            String reason = canSubmit ? "可以提交" : flagService.getSubmitRestrictionReason(flagId, userId);

            Map<String, Object> response = new HashMap<>();
            response.put("canSubmit", canSubmit);
            response.put("reason", reason);

            return ResponseEntity.ok(createSuccessResponse("获取成功", response));
        } catch (SecurityException e) {
            return ResponseEntity.status(401).body(createErrorResponse("用户未登录"));
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(createErrorResponse("检查失败"));
        }
    }

    /**
     * 检查用户是否已解决Flag
     */
    @GetMapping("/{flagId}/solved")
    public ResponseEntity<?> checkSolved(@PathVariable Integer flagId, HttpServletRequest request) {
        try {
            Map<String, Object> userInfo = getCurrentUserInfo(request);
            Integer userId = (Integer) userInfo.get("userId");

            boolean solved = flagService.hasUserSolvedFlag(flagId, userId);

            Map<String, Object> response = new HashMap<>();
            response.put("solved", solved);

            return ResponseEntity.ok(createSuccessResponse("获取成功", response));
        } catch (SecurityException e) {
            return ResponseEntity.status(401).body(createErrorResponse("用户未登录"));
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(createErrorResponse("检查失败"));
        }
    }

    /**
     * 重新生成Flag
     */
    @PostMapping("/{flagId}/regenerate")
    public ResponseEntity<?> regenerateFlag(@PathVariable Integer flagId, HttpServletRequest request) {
        try {
            // 获取Flag对应的竞赛ID
            Optional<Flag> existingFlagOpt = flagService.getFlagById(flagId);
            if (existingFlagOpt.isEmpty()) {
                return ResponseEntity.badRequest().body(createErrorResponse("Flag不存在"));
            }

            Integer competitionId = existingFlagOpt.get().getCompetition().getCompetitionID();
            checkCompetitionCreatorOrAdminPermission(competitionId, request);

            Flag regeneratedFlag = flagService.regenerateFlag(flagId);
            return ResponseEntity.ok(createSuccessResponse("重新生成Flag成功", regeneratedFlag));
        } catch (SecurityException e) {
            return ResponseEntity.status(403).body(createErrorResponse(e.getMessage()));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(createErrorResponse(e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(createErrorResponse("重新生成Flag失败"));
        }
    }

    /**
     * 批量生成Flag
     */
    @PostMapping("/generate-batch")
    public ResponseEntity<?> generateFlags(@RequestBody Map<String, Object> generateData, HttpServletRequest request) {
        try {
            Integer competitionId = (Integer) generateData.get("competitionId");
            if (competitionId == null) {
                return ResponseEntity.badRequest().body(createErrorResponse("竞赛ID不能为空"));
            }

            checkCompetitionCreatorOrAdminPermission(competitionId, request);

            Integer count = (Integer) generateData.get("count");
            Integer points = (Integer) generateData.get("points");
            String expireTimeStr = (String) generateData.get("expireTime");

            if (count == null || points == null) {
                return ResponseEntity.badRequest().body(createErrorResponse("参数不完整"));
            }

            LocalDateTime expireTime = expireTimeStr != null ? LocalDateTime.parse(expireTimeStr) : null;

            List<Flag> flags = flagService.generateFlagsForCompetition(competitionId, count, points, expireTime);

            return ResponseEntity.ok(createSuccessResponse("生成Flag成功", flags));
        } catch (SecurityException e) {
            return ResponseEntity.status(403).body(createErrorResponse(e.getMessage()));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(createErrorResponse(e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(createErrorResponse("生成Flag失败"));
        }
    }

    /**
     * 获取Flag统计信息
     */
    @GetMapping("/competitions/{competitionId}/stats")
    public ResponseEntity<?> getFlagStatistics(@PathVariable Integer competitionId, HttpServletRequest request) {
        try {
            Map<String, Object> userInfo = getCurrentUserInfo(request);
            String userRole = (String) userInfo.get("userRole");

            // 检查权限：管理员或竞赛创建者
            if (!"ADMIN".equals(userRole)) {
                Integer userId = (Integer) userInfo.get("userId");
                boolean isCreator = flagService.isUserCompetitionCreator(competitionId, userId);
                if (!isCreator) {
                    return ResponseEntity.status(403).body(createErrorResponse("无权查看统计信息"));
                }
            }

            Map<String, Object> stats = flagService.getFlagStatistics(competitionId);
            return ResponseEntity.ok(createSuccessResponse("获取成功", stats));
        } catch (SecurityException e) {
            return ResponseEntity.status(401).body(createErrorResponse("用户未登录"));
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(createErrorResponse("获取统计信息失败"));
        }
    }

    /**
     * 获取用户Flag统计信息
     */
    @GetMapping("/my-stats")
    public ResponseEntity<?> getMyFlagStatistics(@RequestParam Integer competitionId, HttpServletRequest request) {
        try {
            Map<String, Object> userInfo = getCurrentUserInfo(request);
            Integer userId = (Integer) userInfo.get("userId");

            Map<String, Object> stats = flagService.getUserFlagStatistics(userId, competitionId);
            return ResponseEntity.ok(createSuccessResponse("获取成功", stats));
        } catch (SecurityException e) {
            return ResponseEntity.status(401).body(createErrorResponse("用户未登录"));
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(createErrorResponse("获取统计信息失败"));
        }
    }

    /**
     * 获取排行榜
     */
    @GetMapping("/competitions/{competitionId}/leaderboard")
    public ResponseEntity<?> getLeaderboard(@PathVariable Integer competitionId,
                                            @RequestParam(defaultValue = "0") int page,
                                            @RequestParam(defaultValue = "20") int size,
                                            HttpServletRequest request) {
        try {
            Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "totalScore"));
            Page<Map<String, Object>> leaderboard = flagService.getCompetitionLeaderboard(competitionId, pageable);

            Map<String, Object> response = new HashMap<>();
            response.put("leaderboard", leaderboard != null ? leaderboard.getContent() : new ArrayList<>());
            response.put("totalPages", leaderboard != null ? leaderboard.getTotalPages() : 0);
            response.put("totalElements", leaderboard != null ? leaderboard.getTotalElements() : 0);
            response.put("currentPage", leaderboard != null ? leaderboard.getNumber() : page);
            response.put("pageSize", leaderboard != null ? leaderboard.getSize() : size);

            return ResponseEntity.ok(createSuccessResponse("获取成功", response));
        } catch (Exception e) {
            e.printStackTrace(); // 打印异常堆栈以便调试
            return ResponseEntity.internalServerError().body(createErrorResponse("获取排行榜失败: " + e.getMessage()));
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