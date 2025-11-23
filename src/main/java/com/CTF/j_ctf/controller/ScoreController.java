package com.CTF.j_ctf.controller;

import com.CTF.j_ctf.entity.Score;
import com.CTF.j_ctf.entity.ScoreSummary;
import com.CTF.j_ctf.entity.User;
import com.CTF.j_ctf.service.ScoreService;
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
@RequestMapping("/api/scores")
public class ScoreController {
    private final ScoreService scoreService;

    public ScoreController(ScoreService scoreService) {
        this.scoreService = scoreService;
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
        boolean isCreator = scoreService.isUserCompetitionCreator(competitionId, userId);
        if (!isCreator) {
            throw new SecurityException("权限不足，需要竞赛创建者或管理员权限");
        }
    }

    /**
     * 检查用户权限（只能访问自己的数据，除非是管理员）
     */
    private void checkUserPermission(Integer targetUserId, HttpServletRequest request) {
        Map<String, Object> userInfo = getCurrentUserInfo(request);
        Integer currentUserId = (Integer) userInfo.get("userId");
        String userRole = (String) userInfo.get("userRole");

        if (currentUserId == null) {
            throw new SecurityException("用户未登录");
        }

        if (!"ADMIN".equals(userRole) && !currentUserId.equals(targetUserId)) {
            throw new SecurityException("权限不足，只能访问自己的数据");
        }
    }

    /**
     * 创建分数记录
     */
    @PostMapping
    public ResponseEntity<?> createScore(@RequestBody Score score, HttpServletRequest request) {
        try {
            // 检查权限，只有管理员可以手动创建分数记录
            checkAdminPermission(request);

            Score createdScore = scoreService.createScore(score);
            return ResponseEntity.ok(createSuccessResponse("创建分数记录成功", createdScore));
        } catch (SecurityException e) {
            return ResponseEntity.status(403).body(createErrorResponse(e.getMessage()));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(createErrorResponse(e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(createErrorResponse("创建分数记录失败"));
        }
    }

    /**
     * 更新分数记录
     */
    @PutMapping("/{scoreId}")
    public ResponseEntity<?> updateScore(@PathVariable Integer scoreId,
                                         @RequestBody Score score,
                                         HttpServletRequest request) {
        try {
            // 检查权限
            checkAdminPermission(request);

            score.setChangeID(scoreId);
            Score updatedScore = scoreService.updateScore(score);
            return ResponseEntity.ok(createSuccessResponse("更新分数记录成功", updatedScore));
        } catch (SecurityException e) {
            return ResponseEntity.status(403).body(createErrorResponse(e.getMessage()));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(createErrorResponse(e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(createErrorResponse("更新分数记录失败"));
        }
    }

    /**
     * 删除分数记录
     */
    @DeleteMapping("/{scoreId}")
    public ResponseEntity<?> deleteScore(@PathVariable Integer scoreId, HttpServletRequest request) {
        try {
            // 检查权限
            checkAdminPermission(request);

            boolean success = scoreService.deleteScore(scoreId);
            if (success) {
                return ResponseEntity.ok(createSuccessResponse("删除分数记录成功"));
            } else {
                return ResponseEntity.badRequest().body(createErrorResponse("删除分数记录失败"));
            }
        } catch (SecurityException e) {
            return ResponseEntity.status(403).body(createErrorResponse(e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(createErrorResponse("删除分数记录失败"));
        }
    }

    /**
     * 标记分数无效
     */
    @PostMapping("/{scoreId}/invalidate")
    public ResponseEntity<?> invalidateScore(@PathVariable Integer scoreId, HttpServletRequest request) {
        try {
            // 检查权限
            checkAdminPermission(request);

            boolean success = scoreService.invalidateScore(scoreId);
            if (success) {
                return ResponseEntity.ok(createSuccessResponse("标记分数无效成功"));
            } else {
                return ResponseEntity.badRequest().body(createErrorResponse("标记分数无效失败"));
            }
        } catch (SecurityException e) {
            return ResponseEntity.status(403).body(createErrorResponse(e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(createErrorResponse("标记分数无效失败"));
        }
    }

    /**
     * 恢复分数
     */
    @PostMapping("/{scoreId}/restore")
    public ResponseEntity<?> restoreScore(@PathVariable Integer scoreId, HttpServletRequest request) {
        try {
            // 检查权限
            checkAdminPermission(request);

            boolean success = scoreService.restoreScore(scoreId);
            if (success) {
                return ResponseEntity.ok(createSuccessResponse("恢复分数成功"));
            } else {
                return ResponseEntity.badRequest().body(createErrorResponse("恢复分数失败"));
            }
        } catch (SecurityException e) {
            return ResponseEntity.status(403).body(createErrorResponse(e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(createErrorResponse("恢复分数失败"));
        }
    }

    /**
     * 获取分数记录详情
     */
    @GetMapping("/{scoreId}")
    public ResponseEntity<?> getScore(@PathVariable Integer scoreId) {
        try {
            Optional<Score> scoreOpt = scoreService.getScoreById(scoreId);
            if (scoreOpt.isPresent()) {
                return ResponseEntity.ok(createSuccessResponse("获取成功", scoreOpt.get()));
            } else {
                return ResponseEntity.notFound().build();
            }
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(createErrorResponse("获取分数记录详情失败"));
        }
    }

    /**
     * 获取分数记录列表
     */
    @GetMapping
    public ResponseEntity<?> getScores(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "createTime") String sort,
            @RequestParam(required = false) Integer userId,
            @RequestParam(required = false) Integer teamId,
            @RequestParam(required = false) Integer competitionId,
            @RequestParam(required = false) Boolean isValid,
            HttpServletRequest request) {
        try {
            Map<String, Object> userInfo = getCurrentUserInfo(request);
            String userRole = (String) userInfo.get("userRole");

            // 非管理员只能查看自己的分数记录
            if (userId != null && !"ADMIN".equals(userRole)) {
                Integer currentUserId = (Integer) userInfo.get("userId");
                if (!currentUserId.equals(userId)) {
                    return ResponseEntity.status(403).body(createErrorResponse("无权查看其他用户的分数记录"));
                }
            }

            Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, sort));
            Page<Score> scores;

            if (userId != null) {
                scores = scoreService.getScoresByUser(userId, pageable);
            } else if (teamId != null) {
                scores = scoreService.getScoresByTeam(teamId, pageable);
            } else if (competitionId != null) {
                scores = scoreService.getScoresByCompetition(competitionId, pageable);
            } else if (isValid != null) {
                scores = scoreService.getScoresByValidity(isValid, pageable);
            } else {
                // 非管理员只能查看自己的分数记录
                if (!"ADMIN".equals(userRole)) {
                    Integer currentUserId = (Integer) userInfo.get("userId");
                    scores = scoreService.getScoresByUser(currentUserId, pageable);
                } else {
                    scores = scoreService.getAllScores(pageable);
                }
            }

            Map<String, Object> response = new HashMap<>();
            response.put("scores", scores.getContent());
            response.put("totalPages", scores.getTotalPages());
            response.put("totalElements", scores.getTotalElements());
            response.put("currentPage", scores.getNumber());
            response.put("pageSize", scores.getSize());

            return ResponseEntity.ok(createSuccessResponse("获取成功", response));
        } catch (SecurityException e) {
            return ResponseEntity.status(401).body(createErrorResponse("用户未登录"));
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(createErrorResponse("获取分数记录列表失败"));
        }
    }

    /**
     * 获取用户个人分数记录
     */
    @GetMapping("/my-scores")
    public ResponseEntity<?> getMyScores(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(required = false) Integer competitionId,
            HttpServletRequest request) {
        try {
            Map<String, Object> userInfo = getCurrentUserInfo(request);
            Integer userId = (Integer) userInfo.get("userId");

            Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createTime"));
            Page<Score> scores;

            if (competitionId != null) {
                scores = scoreService.getUserScoresByCompetition(userId, competitionId, pageable);
            } else {
                scores = scoreService.getScoresByUser(userId, pageable);
            }

            Map<String, Object> response = new HashMap<>();
            response.put("scores", scores.getContent());
            response.put("totalPages", scores.getTotalPages());
            response.put("totalElements", scores.getTotalElements());
            response.put("currentPage", scores.getNumber());
            response.put("pageSize", scores.getSize());

            return ResponseEntity.ok(createSuccessResponse("获取成功", response));
        } catch (SecurityException e) {
            return ResponseEntity.status(401).body(createErrorResponse("用户未登录"));
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(createErrorResponse("获取个人分数记录失败"));
        }
    }

    /**
     * 获取用户排行榜（分页版）
     */
    @GetMapping("/rankings/users")
    public ResponseEntity<?> getUserRankings(
            @RequestParam(required = false) Integer competitionId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        try {
            Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "totalScore"));
            Page<ScoreSummary> rankings;

            if (competitionId != null) {
                rankings = scoreService.getUserRankingByCompetition(competitionId, pageable);
            } else {
                rankings = scoreService.getOverallUserRanking(pageable);
            }

            Map<String, Object> response = new HashMap<>();
            response.put("rankings", rankings.getContent());
            response.put("totalPages", rankings.getTotalPages());
            response.put("totalElements", rankings.getTotalElements());
            response.put("currentPage", rankings.getNumber());
            response.put("pageSize", rankings.getSize());

            return ResponseEntity.ok(createSuccessResponse("获取成功", response));
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(createErrorResponse("获取用户排行榜失败"));
        }
    }

    /**
     * 获取战队排行榜（分页版）
     */
    @GetMapping("/rankings/teams")
    public ResponseEntity<?> getTeamRankings(
            @RequestParam(required = false) Integer competitionId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        try {
            Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "totalScore"));
            Page<ScoreSummary> rankings;

            if (competitionId != null) {
                rankings = scoreService.getTeamRankingByCompetition(competitionId, pageable);
            } else {
                rankings = scoreService.getOverallTeamRanking(pageable);
            }

            Map<String, Object> response = new HashMap<>();
            response.put("rankings", rankings.getContent());
            response.put("totalPages", rankings.getTotalPages());
            response.put("totalElements", rankings.getTotalElements());
            response.put("currentPage", rankings.getNumber());
            response.put("pageSize", rankings.getSize());

            return ResponseEntity.ok(createSuccessResponse("获取成功", response));
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(createErrorResponse("获取战队排行榜失败"));
        }
    }

    /**
     * 获取用户总分数
     */
    @GetMapping("/users/{userId}/total")
    public ResponseEntity<?> getUserTotalScore(@PathVariable Integer userId,
                                               @RequestParam Integer competitionId,
                                               HttpServletRequest request) {
        try {
            // 检查权限
            checkUserPermission(userId, request);

            Integer totalScore = scoreService.getTotalScoreByUserAndCompetition(userId, competitionId);

            Map<String, Object> response = new HashMap<>();
            response.put("userId", userId);
            response.put("competitionId", competitionId);
            response.put("totalScore", totalScore);

            return ResponseEntity.ok(createSuccessResponse("获取成功", response));
        } catch (SecurityException e) {
            return ResponseEntity.status(403).body(createErrorResponse(e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(createErrorResponse("获取用户总分数失败"));
        }
    }

    /**
     * 获取当前用户总分数
     */
    @GetMapping("/my-total")
    public ResponseEntity<?> getMyTotalScore(@RequestParam Integer competitionId,
                                             HttpServletRequest request) {
        try {
            Map<String, Object> userInfo = getCurrentUserInfo(request);
            Integer userId = (Integer) userInfo.get("userId");

            Integer totalScore = scoreService.getTotalScoreByUserAndCompetition(userId, competitionId);

            Map<String, Object> response = new HashMap<>();
            response.put("userId", userId);
            response.put("competitionId", competitionId);
            response.put("totalScore", totalScore);

            return ResponseEntity.ok(createSuccessResponse("获取成功", response));
        } catch (SecurityException e) {
            return ResponseEntity.status(401).body(createErrorResponse("用户未登录"));
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(createErrorResponse("获取用户总分数失败"));
        }
    }

    /**
     * 获取战队总分数
     */
    @GetMapping("/teams/{teamId}/total")
    public ResponseEntity<?> getTeamTotalScore(@PathVariable Integer teamId,
                                               @RequestParam Integer competitionId) {
        try {
            Integer totalScore = scoreService.getTotalScoreByTeamAndCompetition(teamId, competitionId);

            Map<String, Object> response = new HashMap<>();
            response.put("teamId", teamId);
            response.put("competitionId", competitionId);
            response.put("totalScore", totalScore);

            return ResponseEntity.ok(createSuccessResponse("获取成功", response));
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(createErrorResponse("获取战队总分数失败"));
        }
    }

    /**
     * 获取用户分数统计
     */
    @GetMapping("/users/{userId}/stats")
    public ResponseEntity<?> getUserScoreStatistics(@PathVariable Integer userId,
                                                    @RequestParam Integer competitionId,
                                                    HttpServletRequest request) {
        try {
            // 检查权限
            checkUserPermission(userId, request);

            Map<String, Object> stats = scoreService.getUserScoreStatistics(userId, competitionId);
            return ResponseEntity.ok(createSuccessResponse("获取成功", stats));
        } catch (SecurityException e) {
            return ResponseEntity.status(403).body(createErrorResponse(e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(createErrorResponse("获取用户分数统计失败"));
        }
    }

    /**
     * 获取当前用户分数统计
     */
    @GetMapping("/my-stats")
    public ResponseEntity<?> getMyScoreStatistics(@RequestParam Integer competitionId,
                                                  HttpServletRequest request) {
        try {
            Map<String, Object> userInfo = getCurrentUserInfo(request);
            Integer userId = (Integer) userInfo.get("userId");

            Map<String, Object> stats = scoreService.getUserScoreStatistics(userId, competitionId);
            return ResponseEntity.ok(createSuccessResponse("获取成功", stats));
        } catch (SecurityException e) {
            return ResponseEntity.status(401).body(createErrorResponse("用户未登录"));
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(createErrorResponse("获取用户分数统计失败"));
        }
    }

    /**
     * 获取战队分数统计
     */
    @GetMapping("/teams/{teamId}/stats")
    public ResponseEntity<?> getTeamScoreStatistics(@PathVariable Integer teamId,
                                                    @RequestParam Integer competitionId) {
        try {
            Map<String, Object> stats = scoreService.getTeamScoreStatistics(teamId, competitionId);
            return ResponseEntity.ok(createSuccessResponse("获取成功", stats));
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(createErrorResponse("获取战队分数统计失败"));
        }
    }

    /**
     * 获取竞赛分数统计
     */
    @GetMapping("/competitions/{competitionId}/stats")
    public ResponseEntity<?> getCompetitionScoreStatistics(@PathVariable Integer competitionId,
                                                           HttpServletRequest request) {
        try {
            Map<String, Object> userInfo = getCurrentUserInfo(request);
            String userRole = (String) userInfo.get("userRole");

            // 检查权限：管理员或竞赛创建者
            if (!"ADMIN".equals(userRole)) {
                Integer userId = (Integer) userInfo.get("userId");
                boolean isCreator = scoreService.isUserCompetitionCreator(competitionId, userId);
                if (!isCreator) {
                    return ResponseEntity.status(403).body(createErrorResponse("无权查看统计信息"));
                }
            }

            Map<String, Object> stats = scoreService.getCompetitionScoreStatistics(competitionId);
            return ResponseEntity.ok(createSuccessResponse("获取成功", stats));
        } catch (SecurityException e) {
            return ResponseEntity.status(401).body(createErrorResponse("用户未登录"));
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(createErrorResponse("获取竞赛分数统计失败"));
        }
    }

    /**
     * 手动调整用户分数
     */
    @PostMapping("/users/{userId}/adjust")
    public ResponseEntity<?> adjustUserScore(@PathVariable Integer userId,
                                             @RequestBody Map<String, Object> adjustData,
                                             HttpServletRequest request) {
        try {
            // 检查权限
            checkAdminPermission(request);

            Integer competitionId = (Integer) adjustData.get("competitionId");
            Integer points = (Integer) adjustData.get("points");
            String description = (String) adjustData.get("description");

            if (competitionId == null || points == null) {
                return ResponseEntity.badRequest().body(createErrorResponse("参数不完整"));
            }

            Score score = scoreService.adjustUserScore(userId, competitionId, points, description);
            return ResponseEntity.ok(createSuccessResponse("调整用户分数成功", score));
        } catch (SecurityException e) {
            return ResponseEntity.status(403).body(createErrorResponse(e.getMessage()));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(createErrorResponse(e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(createErrorResponse("调整用户分数失败"));
        }
    }

    /**
     * 手动调整战队分数
     */
    @PostMapping("/teams/{teamId}/adjust")
    public ResponseEntity<?> adjustTeamScore(@PathVariable Integer teamId,
                                             @RequestBody Map<String, Object> adjustData,
                                             HttpServletRequest request) {
        try {
            // 检查权限
            checkAdminPermission(request);

            Integer competitionId = (Integer) adjustData.get("competitionId");
            Integer points = (Integer) adjustData.get("points");
            String description = (String) adjustData.get("description");

            if (competitionId == null || points == null) {
                return ResponseEntity.badRequest().body(createErrorResponse("参数不完整"));
            }

            Score score = scoreService.adjustTeamScore(teamId, competitionId, points, description);
            return ResponseEntity.ok(createSuccessResponse("调整战队分数成功", score));
        } catch (SecurityException e) {
            return ResponseEntity.status(403).body(createErrorResponse(e.getMessage()));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(createErrorResponse(e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(createErrorResponse("调整战队分数失败"));
        }
    }

    /**
     * 获取用户排名
     */
    @GetMapping("/users/{userId}/rank")
    public ResponseEntity<?> getUserRank(@PathVariable Integer userId,
                                         @RequestParam Integer competitionId,
                                         HttpServletRequest request) {
        try {
            // 检查权限
            checkUserPermission(userId, request);

            Integer rank = scoreService.getUserRank(userId, competitionId);

            Map<String, Object> response = new HashMap<>();
            response.put("userId", userId);
            response.put("competitionId", competitionId);
            response.put("rank", rank != null ? rank : 0); // 如果排名为null，返回0

            return ResponseEntity.ok(createSuccessResponse("获取成功", response));
        } catch (SecurityException e) {
            return ResponseEntity.status(403).body(createErrorResponse(e.getMessage()));
        } catch (Exception e) {
            e.printStackTrace(); // 打印异常堆栈以便调试
            return ResponseEntity.internalServerError().body(createErrorResponse("获取用户排名失败: " + e.getMessage()));
        }
    }

    /**
     * 获取当前用户排名
     */
    @GetMapping("/my-rank")
    public ResponseEntity<?> getMyRank(@RequestParam Integer competitionId,
                                       HttpServletRequest request) {
        try {
            Map<String, Object> userInfo = getCurrentUserInfo(request);
            Integer userId = (Integer) userInfo.get("userId");

            Integer rank = scoreService.getUserRank(userId, competitionId);

            Map<String, Object> response = new HashMap<>();
            response.put("userId", userId);
            response.put("competitionId", competitionId);
            response.put("rank", rank);

            return ResponseEntity.ok(createSuccessResponse("获取成功", response));
        } catch (SecurityException e) {
            return ResponseEntity.status(401).body(createErrorResponse("用户未登录"));
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(createErrorResponse("获取用户排名失败"));
        }
    }

    /**
     * 获取战队排名
     */
    @GetMapping("/teams/{teamId}/rank")
    public ResponseEntity<?> getTeamRank(@PathVariable Integer teamId,
                                         @RequestParam Integer competitionId) {
        try {
            Integer rank = scoreService.getTeamRank(teamId, competitionId);

            Map<String, Object> response = new HashMap<>();
            response.put("teamId", teamId);
            response.put("competitionId", competitionId);
            response.put("rank", rank);

            return ResponseEntity.ok(createSuccessResponse("获取成功", response));
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(createErrorResponse("获取战队排名失败"));
        }
    }

    /**
     * 导出竞赛分数记录
     */
    @GetMapping("/competitions/{competitionId}/export")
    public ResponseEntity<?> exportCompetitionScores(@PathVariable Integer competitionId,
                                                     HttpServletRequest request) {
        try {
            Map<String, Object> userInfo = getCurrentUserInfo(request);
            String userRole = (String) userInfo.get("userRole");

            // 检查权限：管理员或竞赛创建者
            if (!"ADMIN".equals(userRole)) {
                Integer userId = (Integer) userInfo.get("userId");
                boolean isCreator = scoreService.isUserCompetitionCreator(competitionId, userId);
                if (!isCreator) {
                    return ResponseEntity.status(403).body(createErrorResponse("无权导出分数记录"));
                }
            }

            List<Score> scores = scoreService.exportCompetitionScores(competitionId);
            return ResponseEntity.ok(createSuccessResponse("导出成功", scores));
        } catch (SecurityException e) {
            return ResponseEntity.status(401).body(createErrorResponse("用户未登录"));
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(createErrorResponse("导出分数记录失败"));
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