package com.CTF.j_ctf.service;

import com.CTF.j_ctf.entity.Challenge;
import com.CTF.j_ctf.entity.ChallengeHint;
import com.CTF.j_ctf.entity.FlagSubmission;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Map;
import java.util.Optional;

public interface ChallengeService {

    // 题目管理
    Challenge createChallenge(Challenge challenge);
    Challenge updateChallenge(Challenge challenge);
    boolean deleteChallenge(Integer challengeId);
    Optional<Challenge> getChallengeById(Integer challengeId);
    Page<Challenge> getAllChallenges(Pageable pageable);
    Page<Challenge> getChallengesByCompetition(Integer competitionId, Pageable pageable);
    Page<Challenge> getChallengesByCategory(String category, Pageable pageable);
    Page<Challenge> getChallengesByDifficulty(String difficulty, Pageable pageable);
    Page<Challenge> searchChallenges(String keyword, Pageable pageable);
    Page<Challenge> searchChallengesByCompetition(String keyword, Integer competitionId, Pageable pageable);
    // 多条件筛选：支持同时应用competitionId、category、difficulty
    Page<Challenge> getChallengesByMultipleConditions(Integer competitionId, String category, String difficulty, Pageable pageable);

    // Flag提交相关
    FlagSubmission submitFlag(Integer challengeId, Integer userId, String submittedFlag, String ipAddress);
    Page<FlagSubmission> getSubmissionsByChallenge(Integer challengeId, Pageable pageable);
    Page<FlagSubmission> getSubmissionsByUser(Integer userId, Pageable pageable);
    Page<FlagSubmission> getUserSubmissionsByChallenge(Integer challengeId, Integer userId, Pageable pageable);

    // 解决状态检查
    boolean hasUserSolvedChallenge(Integer challengeId, Integer userId);
    List<Integer> getSolvedChallengeIdsByUser(Integer userId);

    // 提示管理
    ChallengeHint addHint(Integer challengeId, ChallengeHint hint);
    ChallengeHint updateHint(ChallengeHint hint);
    boolean removeHint(Integer hintId);
    List<ChallengeHint> getHintsByChallenge(Integer challengeId);
    Integer getChallengeIdByHintId(Integer hintId);

    // 权限检查
    boolean isChallengeCreator(Integer challengeId, Integer userId);
    boolean isChallengeActive(Integer challengeId);

    // 统计信息
    Map<String, Object> getChallengeStatistics(Integer competitionId);
    Long getChallengeCountByCompetition(Integer competitionId);
    List<String> getCategoriesByCompetition(Integer competitionId);
    Long getTotalSubmissionsByChallenge(Integer challengeId);
    Long getCorrectSubmissionsByChallenge(Integer challengeId);
    Double getSolveRateByChallenge(Integer challengeId);
}