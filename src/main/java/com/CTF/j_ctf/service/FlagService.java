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

    // Flag管理方法
    Flag createFlag(Flag flag);
    Flag updateFlag(Flag flag);
    boolean deleteFlag(Integer flagId);
    Optional<Flag> getFlagById(Integer flagId);

    Page<Flag> getAllFlags(Pageable pageable);
    Page<Flag> getFlagsByCompetition(Integer competitionId, Pageable pageable);
    Page<Flag> getFlagsByUser(Integer userId, Pageable pageable);
    Page<Flag> getFlagsByTeam(Integer teamId, Pageable pageable);
    Page<Flag> getFlagsByStatus(Integer status, Pageable pageable);
    Page<Flag> searchFlags(String keyword, Pageable pageable);

    // Flag提交方法
    FlagSubmission submitFlag(Integer flagId, Integer userId, String submittedValue, String ipAddress, String userAgent);
    FlagSubmission submitFlagForTeam(Integer flagId, Integer teamId, String submittedValue, String ipAddress, String userAgent);

    // 提交记录查询
    Page<FlagSubmission> getAllSubmissions(Pageable pageable);
    Page<FlagSubmission> getSubmissionsByUser(Integer userId, Pageable pageable);
    Page<FlagSubmission> getUserSubmissionsByCompetition(Integer userId, Integer competitionId, Pageable pageable);
    Page<FlagSubmission> getSubmissionsByTeam(Integer teamId, Pageable pageable);
    Page<FlagSubmission> getSubmissionsByCompetition(Integer competitionId, Pageable pageable);
    Page<FlagSubmission> getSubmissionsByFlag(Integer flagId, Pageable pageable);

    // 验证方法
    boolean canUserSubmitFlag(Integer flagId, Integer userId);
    boolean canTeamSubmitFlag(Integer flagId, Integer teamId);
    String getSubmitRestrictionReason(Integer flagId, Integer userId);
    boolean hasUserSolvedFlag(Integer flagId, Integer userId);
    boolean hasTeamSolvedFlag(Integer flagId, Integer teamId);
    boolean isUserCompetitionCreator(Integer competitionId, Integer userId);

    // 管理方法
    void expireFlags();
    Flag regenerateFlag(Integer flagId);
    List<Flag> generateFlagsForCompetition(Integer competitionId, Integer count, Integer points, LocalDateTime expireTime);

    // 统计方法
    Map<String, Object> getFlagStatistics(Integer competitionId);
    Map<String, Object> getUserFlagStatistics(Integer userId, Integer competitionId);
    Map<String, Object> getTeamFlagStatistics(Integer teamId, Integer competitionId);
    Page<Map<String, Object>> getCompetitionLeaderboard(Integer competitionId, Pageable pageable);
}