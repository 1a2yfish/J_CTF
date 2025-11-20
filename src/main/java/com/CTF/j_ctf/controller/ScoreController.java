package com.CTF.j_ctf.controller;

import com.CTF.j_ctf.entity.Score;
import com.CTF.j_ctf.entity.ScoreSummary;
import com.CTF.j_ctf.service.ScoreService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import jakarta.servlet.http.HttpServletRequest;
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
     * 创建分数记录
     */
    @PostMapping
    public ResponseEntity<?> createScore(@RequestBody Score score, HttpServletRequest request) {
        try {
            // 检查权限，只有管理员可以手动创建分数记录

            Score createdScore = scoreService.createScore(score);
            return ResponseEntity.ok(createSuccessResponse("创建分数记录成功", createdScore));
        } catch (SecurityException e) {
            return ResponseEntity.status(401).body(createErrorResponse("用户未登录"));
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

            score.setChangeID(scoreId);
            Score updatedScore = scoreService.updateScore(score);
            return ResponseEntity.ok(createSuccessResponse("更新分数记录成功", updatedScore));
        } catch (SecurityException e) {
            return ResponseEntity.status(401).body(createErrorResponse("用户未登录"));
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

            boolean success = scoreService.deleteScore(scoreId);
            if (success) {
                return ResponseEntity.ok(createSuccessResponse("删除分数记录成功"));
            } else {
                return ResponseEntity.badRequest().body(createErrorResponse("删除分数记录失败"));
            }
        } catch (SecurityException e) {
            return ResponseEntity.status(401).body(createErrorResponse("用户未登录"));
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

            boolean success = scoreService.invalidateScore(scoreId);
            if (success) {
                return ResponseEntity.ok(createSuccessResponse("标记分数无效成功"));
            } else {
                return ResponseEntity.badRequest().body(createErrorResponse("标记分数无效失败"));
            }
        } catch (SecurityException e) {
            return ResponseEntity.status(401).body(createErrorResponse("用户未登录"));
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

            boolean success = scoreService.restoreScore(scoreId);
            if (success) {
                return ResponseEntity.ok(createSuccessResponse("恢复分数成功"));
            } else {
                return ResponseEntity.badRequest().body(createErrorResponse("恢复分数失败"));
            }
        } catch (SecurityException e) {
            return ResponseEntity.status(401).body(createErrorResponse("用户未登录"));
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
            HttpServletRequest request) {
        try {
            Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, sort));
            Page<Score> scores;

            if (userId != null) {
                scores = scoreService.getScoresByUser(userId, pageable);
            } else if (teamId != null) {
                scores = scoreService.getScoresByTeam(teamId, pageable);
            } else if (competitionId != null) {
                scores = scoreService.getScoresByCompetition(competitionId, pageable);
            } else {
                scores = scoreService.getAllScores(pageable);
            }

            Map<String, Object> response = new HashMap<>();
            response.put("scores", scores.getContent());
            response.put("totalPages", scores.getTotalPages());
            response.put("totalElements", scores.getTotalElements());
            response.put("currentPage", scores.getNumber());

            return ResponseEntity.ok(createSuccessResponse("获取成功", response));
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
            HttpServletRequest request) {
        try {
            Integer userId = getCurrentUserId(request);
            Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createTime"));
            Page<Score> scores = scoreService.getScoresByUser(userId, pageable);

            Map<String, Object> response = new HashMap<>();
            response.put("scores", scores.getContent());
            response.put("totalPages", scores.getTotalPages());
            response.put("totalElements", scores.getTotalElements());
            response.put("currentPage", scores.getNumber());

            return ResponseEntity.ok(createSuccessResponse("获取成功", response));
        } catch (SecurityException e) {
            return ResponseEntity.status(401).body(createErrorResponse("用户未登录"));
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(createErrorResponse("获取个人分数记录失败"));
        }
    }

    /**
     * 获取用户排行榜
     */
    @GetMapping("/rankings/users")
    public ResponseEntity<?> getUserRankings(
            @RequestParam(required = false) Integer competitionId) {
        try {
            List<ScoreSummary> rankings;

            if (competitionId != null) {
                rankings = scoreService.getUserRankingByCompetition(competitionId);
            } else {
                rankings = scoreService.getOverallUserRanking();
            }

            return ResponseEntity.ok(createSuccessResponse("获取成功", rankings));
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(createErrorResponse("获取用户排行榜失败"));
        }
    }

    /**
     * 获取战队排行榜
     */
    @GetMapping("/rankings/teams")
    public ResponseEntity<?> getTeamRankings(
            @RequestParam(required = false) Integer competitionId) {
        try {
            List<ScoreSummary> rankings;

            if (competitionId != null) {
                rankings = scoreService.getTeamRankingByCompetition(competitionId);
            } else {
                rankings = scoreService.getOverallTeamRanking();
            }

            return ResponseEntity.ok(createSuccessResponse("获取成功", rankings));
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(createErrorResponse("获取战队排行榜失败"));
        }
    }

    /**
     * 获取用户总分数
     */
    @GetMapping("/users/{userId}/total")
    public ResponseEntity<?> getUserTotalScore(@PathVariable Integer userId,
                                               @RequestParam Integer competitionId) {
        try {
            Integer totalScore = scoreService.getTotalScoreByUserAndCompetition(userId, competitionId);

            Map<String, Object> response = new HashMap<>();
            response.put("userId", userId);
            response.put("competitionId", competitionId);
            response.put("totalScore", totalScore);

            return ResponseEntity.ok(createSuccessResponse("获取成功", response));
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
                                                    @RequestParam Integer competitionId) {
        try {
            Map<String, Object> stats = scoreService.getUserScoreStatistics(userId, competitionId);
            return ResponseEntity.ok(createSuccessResponse("获取成功", stats));
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
    public ResponseEntity<?> getCompetitionScoreStatistics(@PathVariable Integer competitionId) {
        try {
            Map<String, Object> stats = scoreService.getCompetitionScoreStatistics(competitionId);
            return ResponseEntity.ok(createSuccessResponse("获取成功", stats));
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

            Integer competitionId = (Integer) adjustData.get("competitionId");
            Integer points = (Integer) adjustData.get("points");
            String description = (String) adjustData.get("description");

            if (competitionId == null || points == null) {
                return ResponseEntity.badRequest().body(createErrorResponse("参数不完整"));
            }

            Score score = scoreService.adjustScore(userId, competitionId, points, description);
            return ResponseEntity.ok(createSuccessResponse("调整用户分数成功", score));
        } catch (SecurityException e) {
            return ResponseEntity.status(401).body(createErrorResponse("用户未登录"));
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

            Integer competitionId = (Integer) adjustData.get("competitionId");
            Integer points = (Integer) adjustData.get("points");
            String description = (String) adjustData.get("description");

            if (competitionId == null || points == null) {
                return ResponseEntity.badRequest().body(createErrorResponse("参数不完整"));
            }

            Score score = scoreService.adjustTeamScore(teamId, competitionId, points, description);
            return ResponseEntity.ok(createSuccessResponse("调整战队分数成功", score));
        } catch (SecurityException e) {
            return ResponseEntity.status(401).body(createErrorResponse("用户未登录"));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(createErrorResponse(e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(createErrorResponse("调整战队分数失败"));
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