package com.CTF.j_ctf.controller;

import com.CTF.j_ctf.entity.Competition;
import com.CTF.j_ctf.entity.User;
import com.CTF.j_ctf.service.CompetitionService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import java.time.LocalDateTime;
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
     * 获取当前用户ID和角色
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
     * 创建竞赛
     */
    @PostMapping
    public ResponseEntity<?> createCompetition(@RequestBody Competition competition,
                                               HttpServletRequest request) {
        try {
            Map<String, Object> userInfo = getCurrentUserInfo(request);
            Integer userId = (Integer) userInfo.get("userId");
            String userRole = (String) userInfo.get("userRole");

            // 检查权限：只有管理员可以创建竞赛
            if (!"ADMIN".equals(userRole)) {
                return ResponseEntity.status(403).body(createErrorResponse("权限不足，只有管理员可以创建竞赛"));
            }

            // 设置创建者和发布时间
            User creator = new User();
            creator.setUserID(userId);
            competition.setCreator(creator);
            competition.setPublishTime(LocalDateTime.now());

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
            Map<String, Object> userInfo = getCurrentUserInfo(request);
            Integer userId = (Integer) userInfo.get("userId");
            String userRole = (String) userInfo.get("userRole");

            // 检查权限：管理员或竞赛创建者
            if (!"ADMIN".equals(userRole) && !isCompetitionCreator(competitionId, userId)) {
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
            Map<String, Object> userInfo = getCurrentUserInfo(request);
            Integer userId = (Integer) userInfo.get("userId");
            String userRole = (String) userInfo.get("userRole");

            // 检查权限：管理员或竞赛创建者
            if (!"ADMIN".equals(userRole) && !isCompetitionCreator(competitionId, userId)) {
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
            Map<String, Object> userInfo = getCurrentUserInfo(request);
            Integer userId = (Integer) userInfo.get("userId");
            String userRole = (String) userInfo.get("userRole");

            // 检查权限：管理员或竞赛创建者
            if (!"ADMIN".equals(userRole) && !isCompetitionCreator(competitionId, userId)) {
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
                Competition competition = competitionOpt.get();
                // 检查竞赛是否已发布或用户有权限查看
                if (competition.isPublished() || isCompetitionAccessible(competitionId)) {
                    return ResponseEntity.ok(createSuccessResponse("获取成功", competition));
                } else {
                    return ResponseEntity.status(403).body(createErrorResponse("无权查看该竞赛"));
                }
            } else {
                return ResponseEntity.notFound().build();
            }
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(createErrorResponse("获取竞赛详情失败"));
        }
    }

    /**
     * 获取公开竞赛列表（分页）
     */
    @GetMapping
    public ResponseEntity<?> getCompetitions(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "publishTime") String sort,
            @RequestParam(required = false) String type, // ongoing, upcoming, finished, my, all
            @RequestParam(required = false) String keyword,
            HttpServletRequest request) {
        try {
            Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, sort));
            Page<Competition> competitions;

            if (keyword != null && !keyword.trim().isEmpty()) {
                competitions = competitionService.searchCompetitions(keyword, pageable);
            } else {
                switch (type != null ? type.toLowerCase() : "public") {
                    case "ongoing":
                        competitions = competitionService.getOngoingCompetitions(pageable);
                        break;
                    case "upcoming":
                        competitions = competitionService.getUpcomingCompetitions(pageable);
                        break;
                    case "finished":
                        competitions = competitionService.getFinishedCompetitions(pageable);
                        break;
                    case "my":
                        Map<String, Object> userInfo = getCurrentUserInfo(request);
                        Integer userId = (Integer) userInfo.get("userId");
                        competitions = competitionService.getCompetitionsByCreator(userId, pageable);
                        break;
                    case "all":
                        // 只有管理员可以查看所有竞赛
                        Map<String, Object> adminInfo = getCurrentUserInfo(request);
                        String userRole = (String) adminInfo.get("userRole");
                        if (!"ADMIN".equals(userRole)) {
                            return ResponseEntity.status(403).body(createErrorResponse("权限不足"));
                        }
                        competitions = competitionService.getAllCompetitions(pageable);
                        break;
                    default:
                        competitions = competitionService.getPublicCompetitions(pageable);
                        break;
                }
            }

            Map<String, Object> response = new HashMap<>();
            response.put("competitions", competitions.getContent());
            response.put("totalPages", competitions.getTotalPages());
            response.put("totalElements", competitions.getTotalElements());
            response.put("currentPage", competitions.getNumber());
            response.put("pageSize", competitions.getSize());

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
            Map<String, Object> userInfo = getCurrentUserInfo(request);
            Integer userId = (Integer) userInfo.get("userId");
            String userRole = (String) userInfo.get("userRole");

            // 检查权限：管理员或竞赛创建者
            if (!"ADMIN".equals(userRole) && !isCompetitionCreator(competitionId, userId)) {
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
            Map<String, Object> userInfo = getCurrentUserInfo(request);
            Integer userId = (Integer) userInfo.get("userId");

            boolean canJoin = competitionService.canUserJoinCompetition(competitionId, userId);

            Map<String, Object> response = new HashMap<>();
            response.put("canJoin", canJoin);

            if (!canJoin) {
                // 提供不能参加的原因
                String reason = competitionService.getJoinRestrictionReason(competitionId, userId);
                response.put("reason", reason);
            }

            return ResponseEntity.ok(createSuccessResponse("获取成功", response));
        } catch (SecurityException e) {
            return ResponseEntity.status(401).body(createErrorResponse("用户未登录"));
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(createErrorResponse("检查失败"));
        }
    }

    /**
     * 参加竞赛
     */
    @PostMapping("/{competitionId}/join")
    public ResponseEntity<?> joinCompetition(@PathVariable Integer competitionId,
                                             HttpServletRequest request) {
        try {
            Map<String, Object> userInfo = getCurrentUserInfo(request);
            Integer userId = (Integer) userInfo.get("userId");

            boolean success = competitionService.joinCompetition(competitionId, userId);
            if (success) {
                return ResponseEntity.ok(createSuccessResponse("参加竞赛成功"));
            } else {
                return ResponseEntity.badRequest().body(createErrorResponse("参加竞赛失败"));
            }
        } catch (SecurityException e) {
            return ResponseEntity.status(401).body(createErrorResponse("用户未登录"));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(createErrorResponse(e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(createErrorResponse("参加竞赛失败"));
        }
    }

    /**
     * 退出竞赛
     */
    @PostMapping("/{competitionId}/leave")
    public ResponseEntity<?> leaveCompetition(@PathVariable Integer competitionId,
                                              HttpServletRequest request) {
        try {
            Map<String, Object> userInfo = getCurrentUserInfo(request);
            Integer userId = (Integer) userInfo.get("userId");

            boolean success = competitionService.leaveCompetition(competitionId, userId);
            if (success) {
                return ResponseEntity.ok(createSuccessResponse("退出竞赛成功"));
            } else {
                return ResponseEntity.badRequest().body(createErrorResponse("退出竞赛失败"));
            }
        } catch (SecurityException e) {
            return ResponseEntity.status(401).body(createErrorResponse("用户未登录"));
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(createErrorResponse("退出竞赛失败"));
        }
    }

    /**
     * 获取竞赛统计信息
     */
    @GetMapping("/{competitionId}/statistics")
    public ResponseEntity<?> getCompetitionStatistics(@PathVariable Integer competitionId,
                                                      HttpServletRequest request) {
        try {
            Map<String, Object> userInfo = getCurrentUserInfo(request);
            Integer userId = (Integer) userInfo.get("userId");

            // 检查权限：管理员、创建者或参赛者
            if (!isCompetitionAccessible(competitionId, userId)) {
                return ResponseEntity.status(403).body(createErrorResponse("无权查看统计信息"));
            }

            Map<String, Object> stats = competitionService.getCompetitionStatistics(competitionId);
            return ResponseEntity.ok(createSuccessResponse("获取成功", stats));
        } catch (SecurityException e) {
            return ResponseEntity.status(401).body(createErrorResponse("用户未登录"));
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(createErrorResponse("获取统计信息失败"));
        }
    }

    /**
     * 检查用户是否是竞赛创建者
     */
    private boolean isCompetitionCreator(Integer competitionId, Integer userId) {
        try {
            Optional<Competition> competitionOpt = competitionService.getCompetitionById(competitionId);
            if (competitionOpt.isPresent()) {
                Competition competition = competitionOpt.get();
                return competition.getCreator() != null &&
                        competition.getCreator().getUserID().equals(userId);
            }
            return false;
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * 检查用户是否有权限访问竞赛
     */
    private boolean isCompetitionAccessible(Integer competitionId) {
        // 公开竞赛或用户已登录且有权限
        try {
            Optional<Competition> competitionOpt = competitionService.getCompetitionById(competitionId);
            return competitionOpt.map(Competition::isPublished).orElse(false);
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * 检查用户是否有权限访问竞赛（带用户ID）
     */
    private boolean isCompetitionAccessible(Integer competitionId, Integer userId) {
        try {
            Optional<Competition> competitionOpt = competitionService.getCompetitionById(competitionId);
            if (competitionOpt.isPresent()) {
                Competition competition = competitionOpt.get();
                return competition.isPublished() ||
                        isCompetitionCreator(competitionId, userId) ||
                        competitionService.isUserParticipant(competitionId, userId);
            }
            return false;
        } catch (Exception e) {
            return false;
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