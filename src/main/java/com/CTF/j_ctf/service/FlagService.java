package com.CTF.j_ctf.service;

import com.CTF.j_ctf.entity.Flag;
import com.CTF.j_ctf.entity.FlagSubmission;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public interface FlagService {

    // Flag管理
    Flag createFlag(Flag flag);
    Flag updateFlag(Flag flag);
    boolean deleteFlag(Integer flagId);
    Optional<Flag> getFlagById(Integer flagId);

    // Flag提交
    FlagSubmission submitFlag(Integer flagId, Integer userId, String submittedValue, String ipAddress, String userAgent);
    FlagSubmission submitFlagForTeam(Integer flagId, Integer teamId, String submittedValue, String ipAddress, String userAgent);

    // 查询
    Page<Flag> getAllFlags(Pageable pageable);
    Page<Flag> getFlagsByCompetition(Integer competitionId, Pageable pageable);
    Page<Flag> getFlagsByUser(Integer userId, Pageable pageable);
    Page<Flag> getFlagsByTeam(Integer teamId, Pageable pageable);
    Page<Flag> getFlagsByStatus(Integer status, Pageable pageable);
    Page<Flag> searchFlags(String keyword, Pageable pageable);

    // 提交记录查询
    Page<FlagSubmission> getAllSubmissions(Pageable pageable);
    Page<FlagSubmission> getSubmissionsByUser(Integer userId, Pageable pageable);
    Page<FlagSubmission> getSubmissionsByTeam(Integer teamId, Pageable pageable);
    Page<FlagSubmission> getSubmissionsByCompetition(Integer competitionId, Pageable pageable);
    Page<FlagSubmission> getSubmissionsByFlag(Integer flagId, Pageable pageable);

    // 验证
    boolean canUserSubmitFlag(Integer flagId, Integer userId);
    boolean canTeamSubmitFlag(Integer flagId, Integer teamId);
    boolean hasUserSolvedFlag(Integer flagId, Integer userId);
    boolean hasTeamSolvedFlag(Integer flagId, Integer teamId);

    // 管理操作
    void expireFlags();
    Flag regenerateFlag(Integer flagId);

    // 统计
    Map<String, Object> getFlagStatistics(Integer competitionId);
    Map<String, Object> getUserFlagStatistics(Integer userId, Integer competitionId);
    Map<String, Object> getTeamFlagStatistics(Integer teamId, Integer competitionId);

    // 批量操作
    List<Flag> generateFlagsForCompetition(Integer competitionId, Integer count, Integer points, LocalDateTime expireTime);
}