package com.CTF.j_ctf.controller;

import com.CTF.j_ctf.entity.Flag;
import com.CTF.j_ctf.entity.FlagSubmission;
import com.CTF.j_ctf.service.FlagService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import jakarta.servlet.http.HttpServletRequest;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/flags")
public class FlagController {
    private final FlagService flagService;

    public FlagController(FlagService flagService) {
        this.flagService = flagService;
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

            Flag createdFlag = flagService.createFlag(flag);
            return ResponseEntity.ok(createSuccessResponse("创建Flag成功", createdFlag));
        } catch (SecurityException e) {
            return ResponseEntity.status(401).body(createErrorResponse("用户未登录"));
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
            // 检查权限

            flag.setFlagID(flagId);
            Flag updatedFlag = flagService.updateFlag(flag);
            return ResponseEntity.ok(createSuccessResponse("更新Flag成功", updatedFlag));
        } catch (SecurityException e) {
            return ResponseEntity.status(401).body(createErrorResponse("用户未登录"));
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
            // 检查权限

            boolean success = flagService.deleteFlag(flagId);
            if (success) {
                return ResponseEntity.ok(createSuccessResponse("删除Flag成功"));
            } else {
                return ResponseEntity.badRequest().body(createErrorResponse("删除Flag失败"));
            }
        } catch (SecurityException e) {
            return ResponseEntity.status(401).body(createErrorResponse("用户未登录"));
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
            Optional<Flag> flagOpt = flagService.getFlagById(flagId);
            if (flagOpt.isPresent()) {
                // 对于非管理员，隐藏Flag值
                boolean isAdmin = false; // 这里需要实现权限检查
                Flag flag = flagOpt.get();
                if (!isAdmin) {
                    flag.setValue("***"); // 隐藏真实Flag值
                }

                return ResponseEntity.ok(createSuccessResponse("获取成功", flag));
            } else {
                return ResponseEntity.notFound().build();
            }
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
            @RequestParam(required = false) Integer status,
            @RequestParam(required = false) String keyword,
            HttpServletRequest request) {
        try {
            Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, sort));
            Page<Flag> flags;

            if (keyword != null && !keyword.trim().isEmpty()) {
                flags = flagService.searchFlags(keyword, pageable);
            } else if (competitionId != null) {
                flags = flagService.getFlagsByCompetition(competitionId, pageable);
            } else if (status != null) {
                flags = flagService.getFlagsByStatus(status, pageable);
            } else {
                flags = flagService.getAllFlags(pageable);
            }

            // 对于非管理员，隐藏Flag值
            boolean isAdmin = false; // 这里需要实现权限检查
            if (!isAdmin) {
                for (Flag flag : flags.getContent()) {
                    flag.setValue("***"); // 隐藏真实Flag值
                }
            }

            Map<String, Object> response = new HashMap<>();
            response.put("flags", flags.getContent());
            response.put("totalPages", flags.getTotalPages());
            response.put("totalElements", flags.getTotalElements());
            response.put("currentPage", flags.getNumber());

            return ResponseEntity.ok(createSuccessResponse("获取成功", response));
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
            Integer userId = getCurrentUserId(request);
            String submittedValue = submitData.get("value");
            String ipAddress = getClientIpAddress(request);
            String userAgent = getClientUserAgent(request);

            if (submittedValue == null || submittedValue.trim().isEmpty()) {
                return ResponseEntity.badRequest().body(createErrorResponse("Flag值不能为空"));
            }

            FlagSubmission submission = flagService.submitFlag(flagId, userId, submittedValue, ipAddress, userAgent);

            Map<String, Object> response = new HashMap<>();
            response.put("isCorrect", submission.getIsCorrect());
            response.put("pointsAwarded", submission.getPointsAwarded());
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
            Integer userId = getCurrentUserId(request);
            Integer teamId = Integer.parseInt(submitData.get("teamId"));
            String submittedValue = submitData.get("value");
            String ipAddress = getClientIpAddress(request);
            String userAgent = getClientUserAgent(request);

            if (submittedValue == null || submittedValue.trim().isEmpty()) {
                return ResponseEntity.badRequest().body(createErrorResponse("Flag值不能为空"));
            }

            if (teamId == null) {
                return ResponseEntity.badRequest().body(createErrorResponse("战队ID不能为空"));
            }

            FlagSubmission submission = flagService.submitFlagForTeam(flagId, teamId, submittedValue, ipAddress, userAgent);

            Map<String, Object> response = new HashMap<>();
            response.put("isCorrect", submission.getIsCorrect());
            response.put("pointsAwarded", submission.getPointsAwarded());
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
    public ResponseEntity<?> getSubmissions(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "submitTime") String sort,
            @RequestParam(required = false) Integer competitionId,
            @RequestParam(required = false) Integer flagId,
            HttpServletRequest request) {
        try {
            Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, sort));
            Page<FlagSubmission> submissions;

            if (competitionId != null) {
                submissions = flagService.getSubmissionsByCompetition(competitionId, pageable);
            } else if (flagId != null) {
                submissions = flagService.getSubmissionsByFlag(flagId, pageable);
            } else {
                submissions = flagService.getAllSubmissions(pageable);
            }

            Map<String, Object> response = new HashMap<>();
            response.put("submissions", submissions.getContent());
            response.put("totalPages", submissions.getTotalPages());
            response.put("totalElements", submissions.getTotalElements());
            response.put("currentPage", submissions.getNumber());

            return ResponseEntity.ok(createSuccessResponse("获取成功", response));
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(createErrorResponse("获取提交记录失败"));
        }
    }

    /**
     * 获取用户提交记录
     */
    @GetMapping("/my-submissions")
    public ResponseEntity<?> getMySubmissions(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            HttpServletRequest request) {
        try {
            Integer userId = getCurrentUserId(request);
            Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "submitTime"));
            Page<FlagSubmission> submissions = flagService.getSubmissionsByUser(userId, pageable);

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
     * 检查用户是否可以提交Flag
     */
    @GetMapping("/{flagId}/can-submit")
    public ResponseEntity<?> canSubmitFlag(@PathVariable Integer flagId, HttpServletRequest request) {
        try {
            Integer userId = getCurrentUserId(request);
            boolean canSubmit = flagService.canUserSubmitFlag(flagId, userId);

            Map<String, Object> response = new HashMap<>();
            response.put("canSubmit", canSubmit);

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
            Integer userId = getCurrentUserId(request);
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
            // 检查权限

            Flag regeneratedFlag = flagService.regenerateFlag(flagId);
            return ResponseEntity.ok(createSuccessResponse("重新生成Flag成功", regeneratedFlag));
        } catch (SecurityException e) {
            return ResponseEntity.status(401).body(createErrorResponse("用户未登录"));
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
            // 检查权限

            Integer competitionId = (Integer) generateData.get("competitionId");
            Integer count = (Integer) generateData.get("count");
            Integer points = (Integer) generateData.get("points");
            String expireTimeStr = (String) generateData.get("expireTime");

            if (competitionId == null || count == null || points == null) {
                return ResponseEntity.badRequest().body(createErrorResponse("参数不完整"));
            }

            LocalDateTime expireTime = expireTimeStr != null ? LocalDateTime.parse(expireTimeStr) : null;

            List<Flag> flags = flagService.generateFlagsForCompetition(competitionId, count, points, expireTime);

            return ResponseEntity.ok(createSuccessResponse("生成Flag成功", flags));
        } catch (SecurityException e) {
            return ResponseEntity.status(401).body(createErrorResponse("用户未登录"));
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
            Map<String, Object> stats = flagService.getFlagStatistics(competitionId);
            return ResponseEntity.ok(createSuccessResponse("获取成功", stats));
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
            Integer userId = getCurrentUserId(request);
            Map<String, Object> stats = flagService.getUserFlagStatistics(userId, competitionId);
            return ResponseEntity.ok(createSuccessResponse("获取成功", stats));
        } catch (SecurityException e) {
            return ResponseEntity.status(401).body(createErrorResponse("用户未登录"));
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(createErrorResponse("获取统计信息失败"));
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