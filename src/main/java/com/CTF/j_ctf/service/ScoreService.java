package com.CTF.j_ctf.service;

import com.CTF.j_ctf.entity.Score;
import com.CTF.j_ctf.entity.ScoreSummary;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Map;
import java.util.Optional;

public interface ScoreService {

    // 分数记录管理
    Score createScore(Score score);
    Score updateScore(Score score);
    boolean deleteScore(Integer scoreId);
    Optional<Score> getScoreById(Integer scoreId);

    // 分数记录查询
    Page<Score> getAllScores(Pageable pageable);
    Page<Score> getScoresByUser(Integer userId, Pageable pageable);
    Page<Score> getUserScoresByCompetition(Integer userId, Integer competitionId, Pageable pageable);
    Page<Score> getScoresByTeam(Integer teamId, Pageable pageable);
    Page<Score> getScoresByCompetition(Integer competitionId, Pageable pageable);
    Page<Score> getScoresByValidity(Boolean isValid, Pageable pageable);

    // 分数记录操作
    boolean invalidateScore(Integer scoreId);
    boolean restoreScore(Integer scoreId);

    // 分数计算
    Integer getTotalScoreByUserAndCompetition(Integer userId, Integer competitionId);
    Integer getTotalScoreByTeamAndCompetition(Integer teamId, Integer competitionId);

    // 排行榜
    Page<ScoreSummary> getUserRankingByCompetition(Integer competitionId, Pageable pageable);
    Page<ScoreSummary> getTeamRankingByCompetition(Integer competitionId, Pageable pageable);
    Page<ScoreSummary> getOverallUserRanking(Pageable pageable);
    Page<ScoreSummary> getOverallTeamRanking(Pageable pageable);

    // 统计信息
    Map<String, Object> getUserScoreStatistics(Integer userId, Integer competitionId);
    Map<String, Object> getTeamScoreStatistics(Integer teamId, Integer competitionId);
    Map<String, Object> getCompetitionScoreStatistics(Integer competitionId);

    // 分数调整
    Score adjustUserScore(Integer userId, Integer competitionId, Integer points, String description);
    Score adjustTeamScore(Integer teamId, Integer competitionId, Integer points, String description);

    // 排名查询
    Integer getUserRank(Integer userId, Integer competitionId);
    Integer getTeamRank(Integer teamId, Integer competitionId);

    // 导出功能
    List<Score> exportCompetitionScores(Integer competitionId);

    // 权限检查
    boolean isUserCompetitionCreator(Integer competitionId, Integer userId);
}