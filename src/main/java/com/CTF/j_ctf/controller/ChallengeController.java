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
     * 创建题目
     */
    @PostMapping
    public ResponseEntity<?> createChallenge(@RequestBody Challenge challenge, HttpServletRequest request) {
        try {
            // 检查权限，只有管理员或竞赛创建者可以创建题目
            // 这里省略权限检查的具体实现

            Challenge createdChallenge = challengeService.createChallenge(challenge);
            return ResponseEntity.ok(createSuccessResponse("创建题目成功", createdChallenge));
        } catch (SecurityException e) {
            return ResponseEntity.status(401).body(createErrorResponse("用户未登录"));
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
            // 检查权限

            challenge.setChallengeID(challengeId);
            Challenge updatedChallenge = challengeService.updateChallenge(challenge);
            return ResponseEntity.ok(createSuccessResponse("更新题目成功", updatedChallenge));
        } catch (SecurityException e) {
            return ResponseEntity.status(401).body(createErrorResponse("用户未登录"));
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
            // 检查权限

            boolean success = challengeService.deleteChallenge(challengeId);
            if (success) {
                return ResponseEntity.ok(createSuccessResponse("删除题目成功"));
            } else {
                return ResponseEntity.badRequest().body(createErrorResponse("删除题目失败"));
            }
        } catch (SecurityException e) {
            return ResponseEntity.status(401).body(createErrorResponse("用户未登录"));
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
            Optional<Challenge> challengeOpt = challengeService.getChallengeById(challengeId);
            if (challengeOpt.isPresent()) {
                Challenge challenge = challengeOpt.get();

                // 对于非管理员，隐藏flag和其他敏感信息
                boolean isAdmin = false; // 这里需要实现权限检查
                if (!isAdmin) {
                    challenge.setFlag(null);
                    // 可以添加其他敏感信息的隐藏
                }

                return ResponseEntity.ok(createSuccessResponse("获取成功", challenge));
            } else {
                return ResponseEntity.notFound().build();
            }
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
            HttpServletRequest request) {
        try {
            Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, sort));
            Page<Challenge> challenges;

            if (keyword != null && !keyword.trim().isEmpty()) {
                if (competitionId != null) {
                    challenges = challengeService.searchChallengesByCompetition(keyword, competitionId, pageable);
                } else {
                    challenges = challengeService.searchChallenges(keyword, pageable);
                }
            } else if (competitionId != null) {
                challenges = challengeService.getChallengesByCompetition(competitionId, pageable);
            } else if (category != null) {
                challenges = challengeService.getChallengesByCategory(category, pageable);
            } else if (difficulty != null) {
                challenges = challengeService.getChallengesByDifficulty(difficulty, pageable);
            } else {
                challenges = challengeService.getAllChallenges(pageable);
            }

            Map<String, Object> response = new HashMap<>();
            response.put("challenges", challenges.getContent());
            response.put("totalPages", challenges.getTotalPages());
            response.put("totalElements", challenges.getTotalElements());
            response.put("currentPage", challenges.getNumber());

            return ResponseEntity.ok(createSuccessResponse("获取成功", response));
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(createErrorResponse("获取题目列表失败"));
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
            Integer userId = getCurrentUserId(request);
            String submittedFlag = submitData.get("flag");
            String ipAddress = getClientIpAddress(request);

            if (submittedFlag == null || submittedFlag.trim().isEmpty()) {
                return ResponseEntity.badRequest().body(createErrorResponse("Flag不能为空"));
            }

            FlagSubmission submission = challengeService.submitFlag(challengeId, userId, submittedFlag, ipAddress);

            Map<String, Object> response = new HashMap<>();
            response.put("isCorrect", submission.getIsCorrect());
            response.put("message", submission.getIsCorrect() ? "Flag正确！恭喜您解决了这道题目！" : "Flag错误，请重新尝试！");

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
     * 获取题目提示
     */
    @GetMapping("/{challengeId}/hints")
    public ResponseEntity<?> getHints(@PathVariable Integer challengeId, HttpServletRequest request) {
        try {
            List<ChallengeHint> hints = challengeService.getHintsByChallenge(challengeId);
            return ResponseEntity.ok(createSuccessResponse("获取成功", hints));
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
            // 检查权限

            ChallengeHint createdHint = challengeService.addHint(challengeId, hint);
            return ResponseEntity.ok(createSuccessResponse("添加提示成功", createdHint));
        } catch (SecurityException e) {
            return ResponseEntity.status(401).body(createErrorResponse("用户未登录"));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(createErrorResponse(e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(createErrorResponse("添加提示失败"));
        }
    }

    /**
     * 删除提示
     */
    @DeleteMapping("/hints/{hintId}")
    public ResponseEntity<?> removeHint(@PathVariable Integer hintId, HttpServletRequest request) {
        try {
            // 检查权限

            boolean success = challengeService.removeHint(hintId);
            if (success) {
                return ResponseEntity.ok(createSuccessResponse("删除提示成功"));
            } else {
                return ResponseEntity.badRequest().body(createErrorResponse("删除提示失败"));
            }
        } catch (SecurityException e) {
            return ResponseEntity.status(401).body(createErrorResponse("用户未登录"));
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
            // 检查权限，只有管理员或竞赛创建者可以查看提交记录

            Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "submitTime"));
            Page<FlagSubmission> submissions = challengeService.getSubmissionsByChallenge(challengeId, pageable);

            Map<String, Object> response = new HashMap<>();
            response.put("submissions", submissions.getContent());
            response.put("totalPages", submissions.getTotalPages());
            response.put("totalElements", submissions.getTotalElements());
            response.put("currentPage", submissions.getNumber());

            return ResponseEntity.ok(createSuccessResponse("获取成功", response));
        } catch (SecurityException e) {
            return ResponseEntity.status(401).body(createErrorResponse("用户未登录"));
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
            Map<String, Object> stats = challengeService.getChallengeStatistics(competitionId);
            return ResponseEntity.ok(createSuccessResponse("获取成功", stats));
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(createErrorResponse("获取统计信息失败"));
        }
    }

    /**
     * 获取竞赛题目类别
     */
    @GetMapping("/competitions/{competitionId}/categories")
    public ResponseEntity<?> getCategories(@PathVariable Integer competitionId) {
        try {
            List<String> categories = challengeService.getCategoriesByCompetition(competitionId);
            return ResponseEntity.ok(createSuccessResponse("获取成功", categories));
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
            Integer userId = getCurrentUserId(request);
            boolean solved = challengeService.hasUserSolvedChallenge(challengeId, userId);

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