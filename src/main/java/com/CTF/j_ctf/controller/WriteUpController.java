package com.CTF.j_ctf.controller;

import com.CTF.j_ctf.entity.WriteUp;
import com.CTF.j_ctf.service.WriteUpService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/writeups")
public class WriteUpController {
    
    @Autowired
    private WriteUpService writeUpService;
    
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
     * 检查管理员权限
     */
    private void checkAdminPermission(HttpServletRequest request) {
        Map<String, Object> userInfo = getCurrentUserInfo(request);
        String userRole = (String) userInfo.get("userRole");
        if (!"ADMIN".equals(userRole)) {
            throw new SecurityException("权限不足");
        }
    }
    
    /**
     * 上传或更新WriteUp
     */
    @PostMapping
    public ResponseEntity<?> uploadWriteUp(@RequestBody Map<String, Object> writeUpData,
                                           HttpServletRequest request) {
        try {
            Map<String, Object> userInfo = getCurrentUserInfo(request);
            Integer userId = (Integer) userInfo.get("userId");
            
            Integer competitionId = (Integer) writeUpData.get("competitionId");
            String title = (String) writeUpData.get("title");
            String content = (String) writeUpData.get("content");
            
            if (competitionId == null) {
                return ResponseEntity.badRequest().body(createErrorResponse("竞赛ID不能为空"));
            }
            if (title == null || title.trim().isEmpty()) {
                return ResponseEntity.badRequest().body(createErrorResponse("报告标题不能为空"));
            }
            if (content == null || content.trim().isEmpty()) {
                return ResponseEntity.badRequest().body(createErrorResponse("报告内容不能为空"));
            }
            
            WriteUp writeUp = writeUpService.createWriteUp(userId, competitionId, title, content);
            return ResponseEntity.ok(createSuccessResponse("WriteUp提交成功", writeUp));
        } catch (SecurityException e) {
            return ResponseEntity.status(401).body(createErrorResponse("用户未登录"));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(createErrorResponse(e.getMessage()));
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.internalServerError().body(createErrorResponse("提交WriteUp失败: " + e.getMessage()));
        }
    }
    
    /**
     * 获取当前用户的WriteUp列表
     */
    @GetMapping("/my")
    public ResponseEntity<?> getMyWriteUps(HttpServletRequest request) {
        try {
            Map<String, Object> userInfo = getCurrentUserInfo(request);
            Integer userId = (Integer) userInfo.get("userId");
            
            List<WriteUp> writeUps = writeUpService.getWriteUpsByUser(userId);
            return ResponseEntity.ok(createSuccessResponse("获取成功", writeUps));
        } catch (SecurityException e) {
            return ResponseEntity.status(401).body(createErrorResponse("用户未登录"));
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.internalServerError().body(createErrorResponse("获取WriteUp列表失败"));
        }
    }
    
    /**
     * 获取指定竞赛的WriteUp列表（管理员或战队成员）
     */
    @GetMapping("/competitions/{competitionId}")
    public ResponseEntity<?> getWriteUpsByCompetition(@PathVariable Integer competitionId,
                                                       HttpServletRequest request) {
        try {
            Map<String, Object> userInfo = getCurrentUserInfo(request);
            String userRole = (String) userInfo.get("userRole");
            
            List<WriteUp> writeUps;
            
            // 管理员可以查看所有WriteUp，普通用户只能查看自己的
            if ("ADMIN".equals(userRole)) {
                writeUps = writeUpService.getWriteUpsByCompetition(competitionId);
            } else {
                Integer userId = (Integer) userInfo.get("userId");
                writeUps = writeUpService.getWriteUpsByUserAndCompetition(userId, competitionId);
            }
            
            return ResponseEntity.ok(createSuccessResponse("获取成功", writeUps));
        } catch (SecurityException e) {
            return ResponseEntity.status(401).body(createErrorResponse("用户未登录"));
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.internalServerError().body(createErrorResponse("获取WriteUp列表失败"));
        }
    }
    
    /**
     * 获取指定用户的WriteUp列表（管理员）
     */
    @GetMapping("/users/{userId}")
    public ResponseEntity<?> getWriteUpsByUser(@PathVariable Integer userId,
                                                HttpServletRequest request) {
        try {
            checkAdminPermission(request);
            
            List<WriteUp> writeUps = writeUpService.getWriteUpsByUser(userId);
            return ResponseEntity.ok(createSuccessResponse("获取成功", writeUps));
        } catch (SecurityException e) {
            return ResponseEntity.status(403).body(createErrorResponse("权限不足"));
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.internalServerError().body(createErrorResponse("获取WriteUp列表失败"));
        }
    }
    
    /**
     * 获取WriteUp详情
     */
    @GetMapping("/{writeUpId}")
    public ResponseEntity<?> getWriteUpById(@PathVariable Integer writeUpId,
                                             HttpServletRequest request) {
        try {
            Map<String, Object> userInfo = getCurrentUserInfo(request);
            Integer userId = (Integer) userInfo.get("userId");
            String userRole = (String) userInfo.get("userRole");
            
            Optional<WriteUp> writeUpOpt = writeUpService.getWriteUpById(writeUpId);
            if (writeUpOpt.isEmpty()) {
                return ResponseEntity.badRequest().body(createErrorResponse("WriteUp不存在"));
            }
            
            WriteUp writeUp = writeUpOpt.get();
            
            // 检查权限：管理员或WriteUp所有者可以查看
            if (!"ADMIN".equals(userRole) && !writeUp.getUser().getUserID().equals(userId)) {
                return ResponseEntity.status(403).body(createErrorResponse("无权查看该WriteUp"));
            }
            
            return ResponseEntity.ok(createSuccessResponse("获取成功", writeUp));
        } catch (SecurityException e) {
            return ResponseEntity.status(401).body(createErrorResponse("用户未登录"));
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.internalServerError().body(createErrorResponse("获取WriteUp失败"));
        }
    }
    
    /**
     * 下载WriteUp（管理员或WriteUp所有者）
     */
    @GetMapping("/{writeUpId}/download")
    public ResponseEntity<?> downloadWriteUp(@PathVariable Integer writeUpId,
                                              HttpServletRequest request) {
        try {
            Map<String, Object> userInfo = getCurrentUserInfo(request);
            Integer userId = (Integer) userInfo.get("userId");
            String userRole = (String) userInfo.get("userRole");
            
            Optional<WriteUp> writeUpOpt = writeUpService.getWriteUpById(writeUpId);
            if (writeUpOpt.isEmpty()) {
                return ResponseEntity.badRequest().body(createErrorResponse("WriteUp不存在"));
            }
            
            WriteUp writeUp = writeUpOpt.get();
            
            // 检查权限：管理员或WriteUp所有者可以下载
            if (!"ADMIN".equals(userRole) && !writeUp.getUser().getUserID().equals(userId)) {
                return ResponseEntity.status(403).body(createErrorResponse("无权下载该WriteUp"));
            }
            
            // 构建文件内容
            StringBuilder content = new StringBuilder();
            content.append("WriteUp报告\n");
            content.append("==========\n\n");
            content.append("标题: ").append(writeUp.getTitle()).append("\n");
            content.append("用户: ").append(writeUp.getUser().getUserName()).append("\n");
            content.append("竞赛: ").append(writeUp.getCompetition().getTitle()).append("\n");
            content.append("创建时间: ").append(writeUp.getCreateTime()).append("\n\n");
            content.append("内容:\n");
            content.append("--------\n");
            content.append(writeUp.getContent()).append("\n");
            
            // 转换为字节数组
            byte[] contentBytes = content.toString().getBytes(StandardCharsets.UTF_8);
            ByteArrayResource resource = new ByteArrayResource(contentBytes);
            
            // 设置响应头
            HttpHeaders headers = new HttpHeaders();
            String filename = "WriteUp_" + writeUp.getWriteUpID() + "_" + 
                            writeUp.getUser().getUserName() + "_" +
                            writeUp.getCompetition().getTitle().replaceAll("[^a-zA-Z0-9]", "_") + ".txt";
            headers.add(HttpHeaders.CONTENT_DISPOSITION, 
                       "attachment; filename=\"" + filename + "\"");
            headers.add(HttpHeaders.CONTENT_TYPE, MediaType.TEXT_PLAIN_VALUE + "; charset=UTF-8");
            
            return ResponseEntity.ok()
                    .headers(headers)
                    .contentLength(contentBytes.length)
                    .body(resource);
        } catch (SecurityException e) {
            return ResponseEntity.status(401).body(createErrorResponse("用户未登录"));
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.internalServerError().body(createErrorResponse("下载WriteUp失败: " + e.getMessage()));
        }
    }
    
    /**
     * 搜索WriteUp（管理员）
     */
    @GetMapping("/search")
    public ResponseEntity<?> searchWriteUps(@RequestParam String keyword,
                                             HttpServletRequest request) {
        try {
            checkAdminPermission(request);
            
            List<WriteUp> writeUps = writeUpService.searchWriteUpsByTitle(keyword);
            return ResponseEntity.ok(createSuccessResponse("搜索成功", writeUps));
        } catch (SecurityException e) {
            return ResponseEntity.status(403).body(createErrorResponse("权限不足"));
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.internalServerError().body(createErrorResponse("搜索WriteUp失败"));
        }
    }
    
    /**
     * 删除WriteUp
     */
    @DeleteMapping("/{writeUpId}")
    public ResponseEntity<?> deleteWriteUp(@PathVariable Integer writeUpId,
                                           HttpServletRequest request) {
        try {
            Map<String, Object> userInfo = getCurrentUserInfo(request);
            Integer userId = (Integer) userInfo.get("userId");
            String userRole = (String) userInfo.get("userRole");
            
            Optional<WriteUp> writeUpOpt = writeUpService.getWriteUpById(writeUpId);
            if (writeUpOpt.isEmpty()) {
                return ResponseEntity.badRequest().body(createErrorResponse("WriteUp不存在"));
            }
            
            WriteUp writeUp = writeUpOpt.get();
            
            // 检查权限：管理员或WriteUp所有者可以删除
            if (!"ADMIN".equals(userRole) && !writeUp.getUser().getUserID().equals(userId)) {
                return ResponseEntity.status(403).body(createErrorResponse("无权删除该WriteUp"));
            }
            
            writeUpService.deleteWriteUp(writeUpId);
            return ResponseEntity.ok(createSuccessResponse("删除成功"));
        } catch (SecurityException e) {
            return ResponseEntity.status(401).body(createErrorResponse("用户未登录"));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(createErrorResponse(e.getMessage()));
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.internalServerError().body(createErrorResponse("删除WriteUp失败"));
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
        Map<String, Object> response = createSuccessResponse(message);
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
        response.put("timestamp", System.currentTimeMillis());
        return response;
    }
}

