package com.CTF.j_ctf.controller;

import com.CTF.j_ctf.entity.Challenge;
import com.CTF.j_ctf.entity.ChallengeHint;
import com.CTF.j_ctf.entity.FlagSubmission;
import com.CTF.j_ctf.service.ChallengeService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/challenges")
public class ChallengeController {
    private final ChallengeService challengeService;

    public ChallengeController(ChallengeService challengeService) {
        this.challengeService = challengeService;
    }

    /**
     * 获取当前用户ID和权限
     */
    private Map<String, Object> getCurrentUserInfo(HttpServletRequest request) {
        HttpSession session = request.getSession(false);
        if (session == null) {
            throw new SecurityException("用户未登录");
        }

        Integer userId = (Integer) session.getAttribute("userId");
        String userRole = (String) session.getAttribute("userRole");
        String userName = (String) session.getAttribute("userName");

        if (userId == null || userRole == null) {
            throw new SecurityException("用户信息不完整");
        }

        Map<String, Object> userInfo = new HashMap<>();
        userInfo.put("userId", userId);
        userInfo.put("userRole", userRole);
        userInfo.put("userName", userName);

        return userInfo;
    }

    /**
     * 检查管理员权限
     */
    private void checkAdminPermission(HttpServletRequest request) {
        Map<String, Object> userInfo = getCurrentUserInfo(request);
        if (!"ADMIN".equals(userInfo.get("userRole"))) {
            throw new SecurityException("权限不足，需要管理员权限");
        }
    }

    /**
     * 检查编辑权限（管理员或题目创建者）
     */
    private void checkEditPermission(Integer challengeId, HttpServletRequest request) {
        Map<String, Object> userInfo = getCurrentUserInfo(request);

        // 管理员有所有权限
        if ("ADMIN".equals(userInfo.get("userRole"))) {
            return;
        }

        // 检查是否是题目创建者
        Integer userId = (Integer) userInfo.get("userId");
        boolean isCreator = challengeService.isChallengeCreator(challengeId, userId);
        if (!isCreator) {
            throw new SecurityException("权限不足，只能编辑自己创建的题目");
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

        String xRealIp = request.getHeader("X-Real-IP");
        if (xRealIp != null && !xRealIp.isEmpty()) {
            return xRealIp;
        }

        return request.getRemoteAddr();
    }

    /**
     * 创建题目
     */
    @PostMapping
    public ResponseEntity<?> createChallenge(@RequestBody Challenge challenge, HttpServletRequest request) {
        try {
            checkAdminPermission(request);

            // 验证必要字段
            if (challenge.getTitle() == null || challenge.getTitle().trim().isEmpty()) {
                return ResponseEntity.badRequest().body(createErrorResponse("题目标题不能为空"));
            }
            if (challenge.getFlag() == null || challenge.getFlag().trim().isEmpty()) {
                return ResponseEntity.badRequest().body(createErrorResponse("Flag不能为空"));
            }
            if (challenge.getPoints() == null || challenge.getPoints() <= 0) {
                return ResponseEntity.badRequest().body(createErrorResponse("题目分值必须大于0"));
            }

            Map<String, Object> userInfo = getCurrentUserInfo(request);
            challenge.setCreatorId((Integer) userInfo.get("userId"));

            Challenge createdChallenge = challengeService.createChallenge(challenge);

            // 返回时移除敏感信息
            createdChallenge.setFlag(null);

            return ResponseEntity.ok(createSuccessResponse("创建题目成功", createdChallenge));
        } catch (SecurityException e) {
            return ResponseEntity.status(403).body(createErrorResponse(e.getMessage()));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(createErrorResponse(e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(createErrorResponse("创建题目失败"));
        }
    }

    /**
     * 更新题目
     */
    @PutMapping("/{challengeId}")
    public ResponseEntity<?> updateChallenge(@PathVariable Integer challengeId,
                                             @RequestBody Challenge challenge,
                                             HttpServletRequest request) {
        try {
            checkEditPermission(challengeId, request);

            // 验证题目是否存在
            Optional<Challenge> existingChallenge = challengeService.getChallengeById(challengeId);
            if (existingChallenge.isEmpty()) {
                return ResponseEntity.notFound().build();
            }

            challenge.setChallengeID(challengeId);
            Challenge updatedChallenge = challengeService.updateChallenge(challenge);

            // 返回时移除敏感信息
            updatedChallenge.setFlag(null);

            return ResponseEntity.ok(createSuccessResponse("更新题目成功", updatedChallenge));
        } catch (SecurityException e) {
            return ResponseEntity.status(403).body(createErrorResponse(e.getMessage()));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(createErrorResponse(e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(createErrorResponse("更新题目失败"));
        }
    }

    /**
     * 删除题目
     */
    @DeleteMapping("/{challengeId}")
    public ResponseEntity<?> deleteChallenge(@PathVariable Integer challengeId, HttpServletRequest request) {
        try {
            checkEditPermission(challengeId, request);

            boolean success = challengeService.deleteChallenge(challengeId);
            if (success) {
                return ResponseEntity.ok(createSuccessResponse("删除题目成功"));
            } else {
                return ResponseEntity.badRequest().body(createErrorResponse("删除题目失败"));
            }
        } catch (SecurityException e) {
            return ResponseEntity.status(403).body(createErrorResponse(e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(createErrorResponse("删除题目失败"));
        }
    }

    /**
     * 获取题目详情
     */
    @GetMapping("/{challengeId}")
    public ResponseEntity<?> getChallenge(@PathVariable Integer challengeId, HttpServletRequest request) {
        try {
            Map<String, Object> userInfo = getCurrentUserInfo(request);
            Integer userId = (Integer) userInfo.get("userId");

            Optional<Challenge> challengeOpt = challengeService.getChallengeById(challengeId);
            if (challengeOpt.isEmpty()) {
                return ResponseEntity.notFound().build();
            }

            Challenge challenge = challengeOpt.get();

            // 检查用户是否已解决该题目
            boolean solved = challengeService.hasUserSolvedChallenge(challengeId, userId);

            // 对于非管理员，隐藏flag和其他敏感信息
            boolean isAdmin = "ADMIN".equals(userInfo.get("userRole"));
            if (!isAdmin) {
                challenge.setFlag(null);
                // 如果用户已经解决，可以显示flag
                if (solved) {
                    challenge.setFlag("[已解决]");
                }
            }

            Map<String, Object> response = new HashMap<>();
            response.put("challenge", challenge);
            response.put("solved", solved);
            response.put("isAdmin", isAdmin);

            return ResponseEntity.ok(createSuccessResponse("获取成功", response));
        } catch (SecurityException e) {
            return ResponseEntity.status(403).body(createErrorResponse(e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(createErrorResponse("获取题目详情失败"));
        }
    }

    /**
     * 获取题目列表
     */
    @GetMapping
    public ResponseEntity<?> getChallenges(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "createTime") String sort,
            @RequestParam(required = false) Integer competitionId,
            @RequestParam(required = false) String category,
            @RequestParam(required = false) String difficulty,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) Boolean solved, // 新增：筛选已解决/未解决
            HttpServletRequest request) {
        try {
            Map<String, Object> userInfo = getCurrentUserInfo(request);
            Integer userId = (Integer) userInfo.get("userId");

            Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, sort));
            Page<Challenge> challenges;

            // 使用多条件筛选方法，支持同时应用多个筛选条件
            if (keyword != null && !keyword.trim().isEmpty()) {
                // 如果有关键词，使用搜索方法
                if (competitionId != null) {
                    challenges = challengeService.searchChallengesByCompetition(keyword, competitionId, pageable);
                } else {
                    challenges = challengeService.searchChallenges(keyword, pageable);
                }
                // 注意：搜索方法可能不支持category和difficulty筛选，需要后续优化
            } else {
                // 使用多条件筛选，支持同时应用competitionId、category、difficulty
                challenges = challengeService.getChallengesByMultipleConditions(
                    competitionId, category, difficulty, pageable);
            }

            // 处理每个题目，移除敏感信息并标记解决状态
            List<Challenge> safeChallenges = challenges.getContent().stream().peek(challenge -> {
                // 移除flag
                challenge.setFlag(null);

                // 标记用户解决状态
                boolean isSolved = challengeService.hasUserSolvedChallenge(challenge.getChallengeID(), userId);
                challenge.setSolved(isSolved); // 假设Challenge实体有solved字段，或者使用DTO

            }).toList();

            Map<String, Object> response = new HashMap<>();
            response.put("challenges", safeChallenges);
            response.put("totalPages", challenges.getTotalPages());
            response.put("totalElements", challenges.getTotalElements());
            response.put("currentPage", challenges.getNumber());
            response.put("userId", userId);

            return ResponseEntity.ok(createSuccessResponse("获取成功", response));
        } catch (SecurityException e) {
            return ResponseEntity.status(403).body(createErrorResponse(e.getMessage()));
        } catch (Exception e) {
            e.printStackTrace(); // 打印堆栈跟踪以便调试
            return ResponseEntity.internalServerError()
                    .body(createErrorResponse("获取题目列表失败: " + e.getMessage()));
        }
    }

    /**
     * 提交Flag
     */
    @PostMapping("/{challengeId}/submit")
    public ResponseEntity<?> submitFlag(@PathVariable Integer challengeId,
                                        @RequestBody Map<String, String> submitData,
                                        HttpServletRequest request) {
        try {
            Map<String, Object> userInfo = getCurrentUserInfo(request);
            Integer userId = (Integer) userInfo.get("userId");
            String submittedFlag = submitData.get("flag");
            String ipAddress = getClientIpAddress(request);

            if (submittedFlag == null || submittedFlag.trim().isEmpty()) {
                return ResponseEntity.badRequest().body(createErrorResponse("Flag不能为空"));
            }

            // 检查题目是否存在
            Optional<Challenge> challengeOpt = challengeService.getChallengeById(challengeId);
            if (challengeOpt.isEmpty()) {
                return ResponseEntity.badRequest().body(createErrorResponse("题目不存在"));
            }

            // 检查是否已经解决
            boolean alreadySolved = challengeService.hasUserSolvedChallenge(challengeId, userId);
            if (alreadySolved) {
                Map<String, Object> response = new HashMap<>();
                response.put("isCorrect", true);
                response.put("message", "您已经解决了这道题目！");
                response.put("alreadySolved", true);
                return ResponseEntity.ok(createSuccessResponse("提交成功", response));
            }

            FlagSubmission submission = challengeService.submitFlag(challengeId, userId, submittedFlag, ipAddress);

            Map<String, Object> response = new HashMap<>();
            response.put("isCorrect", submission.getIsCorrect());
            response.put("message", submission.getIsCorrect() ?
                    "Flag正确！恭喜您解决了这道题目！" : "Flag错误，请重新尝试！");
            response.put("submissionId", submission.getSubmissionID());
            response.put("submitTime", submission.getSubmitTime());
            response.put("alreadySolved", false);

            return ResponseEntity.ok(createSuccessResponse("提交成功", response));
        } catch (SecurityException e) {
            return ResponseEntity.status(403).body(createErrorResponse(e.getMessage()));
        } catch (IllegalArgumentException e) {
            // IllegalArgumentException 通常是业务逻辑错误（如未加入团队、已解答等），返回400错误
            return ResponseEntity.badRequest().body(createErrorResponse(e.getMessage()));
        } catch (Exception e) {
            e.printStackTrace(); // 打印异常堆栈以便调试
            // 系统错误不返回详细错误信息给前端，避免暴露内部实现
            return ResponseEntity.internalServerError().body(createErrorResponse("提交Flag失败，请稍后重试"));
        }
    }

    /**
     * 获取题目提示
     */
    @GetMapping("/{challengeId}/hints")
    public ResponseEntity<?> getHints(@PathVariable Integer challengeId, HttpServletRequest request) {
        try {
            Map<String, Object> userInfo = getCurrentUserInfo(request);
            Integer userId = (Integer) userInfo.get("userId");

            // 检查用户是否已解决题目或是否有权限查看提示
            boolean solved = challengeService.hasUserSolvedChallenge(challengeId, userId);
            boolean isAdmin = "ADMIN".equals(userInfo.get("userRole"));

            if (!solved && !isAdmin) {
                // 对于未解决的题目，可能需要其他条件才能查看提示
                // 这里可以添加逻辑，比如需要消耗积分等
            }

            List<ChallengeHint> hints = challengeService.getHintsByChallenge(challengeId);
            return ResponseEntity.ok(createSuccessResponse("获取成功", hints));
        } catch (SecurityException e) {
            return ResponseEntity.status(403).body(createErrorResponse(e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(createErrorResponse("获取提示失败"));
        }
    }

    /**
     * 添加提示
     */
    @PostMapping("/{challengeId}/hints")
    public ResponseEntity<?> addHint(@PathVariable Integer challengeId,
                                     @RequestBody ChallengeHint hint,
                                     HttpServletRequest request) {
        try {
            checkEditPermission(challengeId, request);

            if (hint.getHintContent() == null || hint.getHintContent().trim().isEmpty()) {
                return ResponseEntity.badRequest().body(createErrorResponse("提示内容不能为空"));
            }

            ChallengeHint createdHint = challengeService.addHint(challengeId, hint);
            return ResponseEntity.ok(createSuccessResponse("添加提示成功", createdHint));
        } catch (SecurityException e) {
            return ResponseEntity.status(403).body(createErrorResponse(e.getMessage()));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(createErrorResponse(e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(createErrorResponse("添加提示失败"));
        }
    }

    /**
     * 更新提示
     */
    @PutMapping("/hints/{hintId}")
    public ResponseEntity<?> updateHint(@PathVariable Integer hintId,
                                        @RequestBody ChallengeHint hint,
                                        HttpServletRequest request) {
        try {
            // 检查权限
            Integer challengeId = challengeService.getChallengeIdByHintId(hintId);
            checkEditPermission(challengeId, request);

            hint.setHintID(hintId);
            ChallengeHint updatedHint = challengeService.updateHint(hint);
            return ResponseEntity.ok(createSuccessResponse("更新提示成功", updatedHint));
        } catch (SecurityException e) {
            return ResponseEntity.status(403).body(createErrorResponse(e.getMessage()));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(createErrorResponse(e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(createErrorResponse("更新提示失败"));
        }
    }

    /**
     * 删除提示
     */
    @DeleteMapping("/hints/{hintId}")
    public ResponseEntity<?> removeHint(@PathVariable Integer hintId, HttpServletRequest request) {
        try {
            // 检查权限
            Integer challengeId = challengeService.getChallengeIdByHintId(hintId);
            checkEditPermission(challengeId, request);

            boolean success = challengeService.removeHint(hintId);
            if (success) {
                return ResponseEntity.ok(createSuccessResponse("删除提示成功"));
            } else {
                return ResponseEntity.badRequest().body(createErrorResponse("删除提示失败"));
            }
        } catch (SecurityException e) {
            return ResponseEntity.status(403).body(createErrorResponse(e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(createErrorResponse("删除提示失败"));
        }
    }

    /**
     * 获取题目提交记录
     */
    @GetMapping("/{challengeId}/submissions")
    public ResponseEntity<?> getSubmissions(@PathVariable Integer challengeId,
                                            @RequestParam(defaultValue = "0") int page,
                                            @RequestParam(defaultValue = "20") int size,
                                            HttpServletRequest request) {
        try {
            Map<String, Object> userInfo = getCurrentUserInfo(request);
            Integer userId = (Integer) userInfo.get("userId");
            boolean isAdmin = "ADMIN".equals(userInfo.get("userRole"));

            // 非管理员只能查看自己的提交记录
            Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "submitTime"));
            Page<FlagSubmission> submissions;

            if (isAdmin) {
                submissions = challengeService.getSubmissionsByChallenge(challengeId, pageable);
            } else {
                submissions = challengeService.getUserSubmissionsByChallenge(challengeId, userId, pageable);
            }

            Map<String, Object> response = new HashMap<>();
            response.put("submissions", submissions.getContent());
            response.put("totalPages", submissions.getTotalPages());
            response.put("totalElements", submissions.getTotalElements());
            response.put("currentPage", submissions.getNumber());
            response.put("isAdmin", isAdmin);

            return ResponseEntity.ok(createSuccessResponse("获取成功", response));
        } catch (SecurityException e) {
            return ResponseEntity.status(403).body(createErrorResponse(e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(createErrorResponse("获取提交记录失败"));
        }
    }

    /**
     * 获取题目统计信息
     */
    @GetMapping("/competitions/{competitionId}/stats")
    public ResponseEntity<?> getChallengeStatistics(@PathVariable Integer competitionId, HttpServletRequest request) {
        try {
            checkAdminPermission(request);

            Map<String, Object> stats = challengeService.getChallengeStatistics(competitionId);
            return ResponseEntity.ok(createSuccessResponse("获取成功", stats));
        } catch (SecurityException e) {
            return ResponseEntity.status(403).body(createErrorResponse(e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(createErrorResponse("获取统计信息失败"));
        }
    }

    /**
     * 获取竞赛题目类别
     */
    @GetMapping("/competitions/{competitionId}/categories")
    public ResponseEntity<?> getCategories(@PathVariable Integer competitionId, HttpServletRequest request) {
        try {
            getCurrentUserInfo(request); // 验证登录

            List<String> categories = challengeService.getCategoriesByCompetition(competitionId);
            return ResponseEntity.ok(createSuccessResponse("获取成功", categories));
        } catch (SecurityException e) {
            return ResponseEntity.status(403).body(createErrorResponse(e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(createErrorResponse("获取类别失败"));
        }
    }

    /**
     * 检查用户是否已解决题目
     */
    @GetMapping("/{challengeId}/solved")
    public ResponseEntity<?> checkSolved(@PathVariable Integer challengeId, HttpServletRequest request) {
        try {
            Map<String, Object> userInfo = getCurrentUserInfo(request);
            Integer userId = (Integer) userInfo.get("userId");

            boolean solved = challengeService.hasUserSolvedChallenge(challengeId, userId);

            Map<String, Object> response = new HashMap<>();
            response.put("solved", solved);
            response.put("challengeId", challengeId);
            response.put("userId", userId);

            return ResponseEntity.ok(createSuccessResponse("获取成功", response));
        } catch (SecurityException e) {
            return ResponseEntity.status(403).body(createErrorResponse(e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(createErrorResponse("检查失败"));
        }
    }

    /**
     * 获取用户解决的所有题目
     */
    @GetMapping("/solved")
    public ResponseEntity<?> getSolvedChallenges(HttpServletRequest request) {
        try {
            Map<String, Object> userInfo = getCurrentUserInfo(request);
            Integer userId = (Integer) userInfo.get("userId");

            List<Integer> solvedChallengeIds = challengeService.getSolvedChallengeIdsByUser(userId);

            Map<String, Object> response = new HashMap<>();
            response.put("solvedChallengeIds", solvedChallengeIds);
            response.put("count", solvedChallengeIds.size());

            return ResponseEntity.ok(createSuccessResponse("获取成功", response));
        } catch (SecurityException e) {
            return ResponseEntity.status(403).body(createErrorResponse(e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(createErrorResponse("获取已解决题目失败"));
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