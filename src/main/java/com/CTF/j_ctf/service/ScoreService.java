package com.CTF.j_ctf.service;

import com.CTF.j_ctf.entity.Score;
import com.CTF.j_ctf.entity.ScoreSummary;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public interface ScoreService {

    // 分数管理
    Score createScore(Score score);
    Score updateScore(Score score);
    boolean deleteScore(Integer scoreId);
    boolean invalidateScore(Integer scoreId); // 标记分数无效
    boolean restoreScore(Integer scoreId); // 恢复分数
    Optional<Score> getScoreById(Integer scoreId);

    // 查询
    Page<Score> getAllScores(Pageable pageable);
    Page<Score> getScoresByUser(Integer userId, Pageable pageable);
    Page<Score> getScoresByTeam(Integer teamId, Pageable pageable);
    Page<Score> getScoresByCompetition(Integer competitionId, Pageable pageable);
    Page<Score> getScoresByChallenge(Integer challengeId, Pageable pageable);
    Page<Score> searchScores(String keyword, Pageable pageable);

    // 排行榜
    List<ScoreSummary> getUserRankingByCompetition(Integer competitionId);
    List<ScoreSummary> getTeamRankingByCompetition(Integer competitionId);
    List<ScoreSummary> getOverallUserRanking();
    List<ScoreSummary> getOverallTeamRanking();

    // 统计
    Integer getTotalScoreByUserAndCompetition(Integer userId, Integer competitionId);
    Integer getTotalScoreByTeamAndCompetition(Integer teamId, Integer competitionId);
    Map<String, Object> getUserScoreStatistics(Integer userId, Integer competitionId);
    Map<String, Object> getTeamScoreStatistics(Integer teamId, Integer competitionId);
    Map<String, Object> getCompetitionScoreStatistics(Integer competitionId);

    // 管理操作
    Score adjustScore(Integer userId, Integer competitionId, Integer points, String description);
    Score adjustTeamScore(Integer teamId, Integer competitionId, Integer points, String description);

    // 验证
    boolean canUserEarnScore(Integer userId, Integer competitionId);
    boolean canTeamEarnScore(Integer teamId, Integer competitionId);
}