package com.CTF.j_ctf.controller;

import com.CTF.j_ctf.entity.Competition;
import com.CTF.j_ctf.service.CompetitionService;
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
@RequestMapping("/api/competitions")
public class CompetitionController {
    private final CompetitionService competitionService;

    public CompetitionController(CompetitionService competitionService) {
        this.competitionService = competitionService;
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
     * 创建竞赛
     */
    @PostMapping
    public ResponseEntity<?> createCompetition(@RequestBody Competition competition,
                                               HttpServletRequest request) {
        try {
            Integer userId = getCurrentUserId(request);
            // 设置创建者（这里需要根据实际情况设置User对象）
            // competition.setCreator(...);

            Competition createdCompetition = competitionService.createCompetition(competition);
            return ResponseEntity.ok(createSuccessResponse("创建竞赛成功", createdCompetition));
        } catch (SecurityException e) {
            return ResponseEntity.status(401).body(createErrorResponse("用户未登录"));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(createErrorResponse(e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(createErrorResponse("创建竞赛失败"));
        }
    }

    /**
     * 更新竞赛
     */
    @PutMapping("/{competitionId}")
    public ResponseEntity<?> updateCompetition(@PathVariable Integer competitionId,
                                               @RequestBody Competition competition,
                                               HttpServletRequest request) {
        try {
            Integer userId = getCurrentUserId(request);

            // 检查用户是否有权限修改该竞赛
            if (!isCompetitionCreator(competitionId, userId)) {
                return ResponseEntity.status(403).body(createErrorResponse("无权修改该竞赛"));
            }

            competition.setCompetitionID(competitionId);
            Competition updatedCompetition = competitionService.updateCompetition(competition);
            return ResponseEntity.ok(createSuccessResponse("更新竞赛成功", updatedCompetition));
        } catch (SecurityException e) {
            return ResponseEntity.status(401).body(createErrorResponse("用户未登录"));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(createErrorResponse(e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(createErrorResponse("更新竞赛失败"));
        }
    }

    /**
     * 发布竞赛
     */
    @PostMapping("/{competitionId}/publish")
    public ResponseEntity<?> publishCompetition(@PathVariable Integer competitionId,
                                                HttpServletRequest request) {
        try {
            Integer userId = getCurrentUserId(request);

            // 检查用户是否有权限发布该竞赛
            if (!isCompetitionCreator(competitionId, userId)) {
                return ResponseEntity.status(403).body(createErrorResponse("无权发布该竞赛"));
            }

            Competition publishedCompetition = competitionService.publishCompetition(competitionId);
            return ResponseEntity.ok(createSuccessResponse("发布竞赛成功", publishedCompetition));
        } catch (SecurityException e) {
            return ResponseEntity.status(401).body(createErrorResponse("用户未登录"));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(createErrorResponse(e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(createErrorResponse("发布竞赛失败"));
        }
    }

    /**
     * 取消竞赛
     */
    @PostMapping("/{competitionId}/cancel")
    public ResponseEntity<?> cancelCompetition(@PathVariable Integer competitionId,
                                               HttpServletRequest request) {
        try {
            Integer userId = getCurrentUserId(request);

            // 检查用户是否有权限取消该竞赛
            if (!isCompetitionCreator(competitionId, userId)) {
                return ResponseEntity.status(403).body(createErrorResponse("无权取消该竞赛"));
            }

            Competition cancelledCompetition = competitionService.cancelCompetition(competitionId);
            return ResponseEntity.ok(createSuccessResponse("取消竞赛成功", cancelledCompetition));
        } catch (SecurityException e) {
            return ResponseEntity.status(401).body(createErrorResponse("用户未登录"));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(createErrorResponse(e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(createErrorResponse("取消竞赛失败"));
        }
    }

    /**
     * 获取竞赛详情
     */
    @GetMapping("/{competitionId}")
    public ResponseEntity<?> getCompetition(@PathVariable Integer competitionId) {
        try {
            Optional<Competition> competitionOpt = competitionService.getCompetitionById(competitionId);
            if (competitionOpt.isPresent()) {
                return ResponseEntity.ok(createSuccessResponse("获取成功", competitionOpt.get()));
            } else {
                return ResponseEntity.notFound().build();
            }
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(createErrorResponse("获取竞赛详情失败"));
        }
    }

    /**
     * 获取竞赛列表（分页）
     */
    @GetMapping
    public ResponseEntity<?> getCompetitions(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "publishTime") String sort,
            @RequestParam(required = false) String type, // ongoing, upcoming, finished, my
            @RequestParam(required = false) String keyword,
            HttpServletRequest request) {
        try {
            Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, sort));
            Page<Competition> competitions;

            if (keyword != null && !keyword.trim().isEmpty()) {
                competitions = competitionService.searchPublicCompetitions(keyword, pageable);
            } else if ("ongoing".equals(type)) {
                competitions = competitionService.getOngoingCompetitions(pageable);
            } else if ("upcoming".equals(type)) {
                competitions = competitionService.getUpcomingCompetitions(pageable);
            } else if ("finished".equals(type)) {
                competitions = competitionService.getFinishedCompetitions(pageable);
            } else if ("my".equals(type)) {
                Integer userId = getCurrentUserId(request);
                competitions = competitionService.getCompetitionsByCreator(userId, pageable);
            } else {
                competitions = competitionService.getPublicCompetitions(pageable);
            }

            Map<String, Object> response = new HashMap<>();
            response.put("competitions", competitions.getContent());
            response.put("totalPages", competitions.getTotalPages());
            response.put("totalElements", competitions.getTotalElements());
            response.put("currentPage", competitions.getNumber());

            return ResponseEntity.ok(createSuccessResponse("获取成功", response));
        } catch (SecurityException e) {
            return ResponseEntity.status(401).body(createErrorResponse("用户未登录"));
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(createErrorResponse("获取竞赛列表失败"));
        }
    }

    /**
     * 删除竞赛
     */
    @DeleteMapping("/{competitionId}")
    public ResponseEntity<?> deleteCompetition(@PathVariable Integer competitionId,
                                               HttpServletRequest request) {
        try {
            Integer userId = getCurrentUserId(request);

            // 检查用户是否有权限删除该竞赛
            if (!isCompetitionCreator(competitionId, userId)) {
                return ResponseEntity.status(403).body(createErrorResponse("无权删除该竞赛"));
            }

            boolean success = competitionService.deleteCompetition(competitionId);
            if (success) {
                return ResponseEntity.ok(createSuccessResponse("删除竞赛成功"));
            } else {
                return ResponseEntity.badRequest().body(createErrorResponse("删除竞赛失败"));
            }
        } catch (SecurityException e) {
            return ResponseEntity.status(401).body(createErrorResponse("用户未登录"));
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(createErrorResponse("删除竞赛失败"));
        }
    }

    /**
     * 检查用户是否可以参加竞赛
     */
    @GetMapping("/{competitionId}/can-join")
    public ResponseEntity<?> canJoinCompetition(@PathVariable Integer competitionId,
                                                HttpServletRequest request) {
        try {
            Integer userId = getCurrentUserId(request);
            boolean canJoin = competitionService.canUserJoinCompetition(competitionId, userId);

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
     * 检查用户是否是竞赛创建者
     */
    private boolean isCompetitionCreator(Integer competitionId, Integer userId) {
        // 这里需要实现检查逻辑
        // 可以使用 competitionRepository.isCreator(competitionId, userId)
        return true; // 简化实现
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