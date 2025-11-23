// src/main/java/com/CTF/j_ctf/repository/ScoreRepository.java
package com.CTF.j_ctf.repository;

import com.CTF.j_ctf.entity.Score;
import com.CTF.j_ctf.entity.ScoreSummary;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Repository
public interface ScoreRepository extends JpaRepository<Score, Integer> {

    // 基础查询
    List<Score> findByUser_UserID(Integer userID);
    Page<Score> findByUser_UserID(Integer userID, Pageable pageable);
    List<Score> findByCompetition_CompetitionID(Integer competitionID);
    Page<Score> findByCompetition_CompetitionID(Integer competitionID, Pageable pageable);
    List<Score> findByTeam_TeamID(Integer teamID);
    Page<Score> findByTeam_TeamID(Integer teamID, Pageable pageable);
    List<Score> findByChallenge_ChallengeID(Integer challengeID);
    List<Score> findByFlag_FlagID(Integer flagID);

    // 新增：Service 所需的组合/分页方法
    Page<Score> findByUser_UserIDAndCompetition_CompetitionID(Integer userID, Integer competitionID, Pageable pageable);

    // 按有效性分页查询（Service 调用）
    Page<Score> findByIsValid(Boolean isValid, Pageable pageable);

    @Query("SELECT s FROM Score s WHERE s.isValid = :isValid AND s.competition.competitionID = :competitionID")
    Page<Score> findByIsValidAndCompetition(@Param("isValid") Boolean isValid, @Param("competitionID") Integer competitionID, Pageable pageable);

    // 用户和竞赛组合查询
    @Query("SELECT s FROM Score s WHERE s.user.userID = :userID AND s.competition.competitionID = :competitionID")
    List<Score> findByUserAndCompetition(@Param("userID") Integer userID, @Param("competitionID") Integer competitionID);

    @Query("SELECT s FROM Score s WHERE s.team.teamID = :teamID AND s.competition.competitionID = :competitionID")
    List<Score> findByTeamAndCompetition(@Param("teamID") Integer teamID, @Param("competitionID") Integer competitionID);

    // 类型查询
    List<Score> findByType(String type);
    Page<Score> findByType(String type, Pageable pageable);

    // 时间范围查询
    @Query("SELECT s FROM Score s WHERE s.createTime BETWEEN :startTime AND :endTime")
    List<Score> findByCreateTimeBetween(@Param("startTime") LocalDateTime startTime, @Param("endTime") LocalDateTime endTime);

    @Query("SELECT s FROM Score s WHERE s.user.userID = :userID AND s.createTime BETWEEN :startTime AND :endTime")
    List<Score> findByUserAndCreateTimeBetween(@Param("userID") Integer userID, @Param("startTime") LocalDateTime startTime, @Param("endTime") LocalDateTime endTime);

    // 总分/聚合（增加 Service 期望的方法名）
    @Query("SELECT SUM(s.scoreValue) FROM Score s WHERE s.user.userID = :userID AND s.competition.competitionID = :competitionID AND s.isValid = true")
    Integer getTotalScoreByUserAndCompetition(@Param("userID") Integer userID, @Param("competitionID") Integer competitionID);

    @Query("SELECT SUM(s.scoreValue) FROM Score s WHERE s.team.teamID = :teamID AND s.competition.competitionID = :competitionID AND s.isValid = true")
    Integer getTotalScoreByTeamAndCompetition(@Param("teamID") Integer teamID, @Param("competitionID") Integer competitionID);

    @Query("SELECT SUM(s.scoreValue) FROM Score s WHERE s.user.userID = :userID AND s.competition.competitionID = :competitionID AND s.isValid = true")
    Integer sumPointsByUserAndCompetition(@Param("userID") Integer userID, @Param("competitionID") Integer competitionID);

    @Query("SELECT SUM(s.scoreValue) FROM Score s WHERE s.team.teamID = :teamID AND s.competition.competitionID = :competitionID AND s.isValid = true")
    Integer sumPointsByTeamAndCompetition(@Param("teamID") Integer teamID, @Param("competitionID") Integer competitionID);

    // 兼容 Service 使用的命名（直接添加）
    @Query("SELECT SUM(s.scoreValue) FROM Score s WHERE s.user.userID = :userID AND s.competition.competitionID = :competitionID AND s.isValid = true")
    Integer sumScoreValueByUserAndCompetitionAndIsValidTrue(@Param("userID") Integer userID, @Param("competitionID") Integer competitionID);

    @Query("SELECT SUM(s.scoreValue) FROM Score s WHERE s.team.teamID = :teamID AND s.competition.competitionID = :competitionID AND s.isValid = true")
    Integer sumScoreValueByTeamAndCompetitionAndIsValidTrue(@Param("teamID") Integer teamID, @Param("competitionID") Integer competitionID);

    // 按类型的聚合与计数（Service 期望的方法名）
    @Query("SELECT SUM(s.scoreValue) FROM Score s WHERE s.user.userID = :userID AND s.competition.competitionID = :competitionID AND s.type = :type AND s.isValid = true")
    Integer sumScoreValueByUserAndCompetitionAndTypeAndIsValidTrue(@Param("userID") Integer userID, @Param("competitionID") Integer competitionID, @Param("type") String type);

    @Query("SELECT SUM(s.scoreValue) FROM Score s WHERE s.team.teamID = :teamID AND s.competition.competitionID = :competitionID AND s.type = :type AND s.isValid = true")
    Integer sumScoreValueByTeamAndCompetitionAndTypeAndIsValidTrue(@Param("teamID") Integer teamID, @Param("competitionID") Integer competitionID, @Param("type") String type);

    Long countByUser_UserIDAndCompetition_CompetitionIDAndTypeAndIsValidTrue(Integer userID, Integer competitionID, String type);
    Long countByTeam_TeamIDAndCompetition_CompetitionIDAndTypeAndIsValidTrue(Integer teamID, Integer competitionID, String type);

    // 最近提交时间（取最大 createTime）
    @Query("SELECT MAX(s.createTime) FROM Score s WHERE s.user.userID = :userID AND s.competition.competitionID = :competitionID")
    LocalDateTime findLastSubmitTimeByUserAndCompetition(@Param("userID") Integer userID, @Param("competitionID") Integer competitionID);

    @Query("SELECT MAX(s.createTime) FROM Score s WHERE s.team.teamID = :teamID AND s.competition.competitionID = :competitionID")
    LocalDateTime findLastSubmitTimeByTeamAndCompetition(@Param("teamID") Integer teamID, @Param("competitionID") Integer competitionID);

    // 排行榜相关：使用接口投影 ScoreSummary（确保接口属性名与别名匹配）
    @Query("SELECT s.user.userID as entityID, u.userName as name, SUM(s.scoreValue) as totalScore " +
            "FROM Score s JOIN s.user u " +
            "WHERE s.competition.competitionID = :competitionID AND s.isValid = true " +
            "GROUP BY s.user.userID, u.userName " +
            "ORDER BY SUM(s.scoreValue) DESC")
    Page<ScoreSummary> findUserRankingByCompetition(@Param("competitionID") Integer competitionID, Pageable pageable);

    @Query("SELECT s.user.userID as entityID, u.userName as name, SUM(s.scoreValue) as totalScore " +
            "FROM Score s JOIN s.user u " +
            "WHERE s.competition.competitionID = :competitionID AND s.isValid = true " +
            "GROUP BY s.user.userID, u.userName " +
            "ORDER BY SUM(s.scoreValue) DESC")
    List<ScoreSummary> findUserRankingByCompetition(@Param("competitionID") Integer competitionID);

    // 使用接口投影返回团队排行榜
    interface TeamRankingProjection {
        Integer getEntityID();
        String getName();
        Integer getTotalScore();
        java.time.LocalDateTime getLastSubmitTime();
    }
    
    @Query("SELECT s.team.teamID as entityID, t.teamName as name, SUM(s.scoreValue) as totalScore, " +
            "MAX(s.createTime) as lastSubmitTime " +
            "FROM Score s JOIN s.team t " +
            "WHERE s.competition.competitionID = :competitionID AND s.isValid = true AND s.team IS NOT NULL " +
            "GROUP BY s.team.teamID, t.teamName " +
            "ORDER BY SUM(s.scoreValue) DESC, MAX(s.createTime) ASC")
    Page<TeamRankingProjection> findTeamRankingByCompetition(@Param("competitionID") Integer competitionID, Pageable pageable);

    @Query("SELECT s.team.teamID as entityID, t.teamName as name, SUM(s.scoreValue) as totalScore, " +
            "MAX(s.createTime) as lastSubmitTime " +
            "FROM Score s JOIN s.team t " +
            "WHERE s.competition.competitionID = :competitionID AND s.isValid = true AND s.team IS NOT NULL " +
            "GROUP BY s.team.teamID, t.teamName " +
            "ORDER BY SUM(s.scoreValue) DESC, MAX(s.createTime) ASC")
    List<TeamRankingProjection> findTeamRankingByCompetition(@Param("competitionID") Integer competitionID);
    
    // 统计团队在竞赛中解决的题目数量（不同的ChallengeID）
    @Query("SELECT COUNT(DISTINCT s.challenge.challengeID) FROM Score s " +
            "WHERE s.team.teamID = :teamID AND s.competition.competitionID = :competitionID AND s.isValid = true")
    Long countDistinctChallengesByTeamAndCompetition(@Param("teamID") Integer teamID, @Param("competitionID") Integer competitionID);

    @Query("SELECT s.user.userID as entityID, u.userName as name, SUM(s.scoreValue) as totalScore " +
            "FROM Score s JOIN s.user u " +
            "WHERE s.isValid = true " +
            "GROUP BY s.user.userID, u.userName " +
            "ORDER BY SUM(s.scoreValue) DESC")
    Page<ScoreSummary> findOverallUserRanking(Pageable pageable);

    @Query("SELECT s.team.teamID as entityID, t.teamName as name, SUM(s.scoreValue) as totalScore " +
            "FROM Score s JOIN s.team t " +
            "WHERE s.isValid = true " +
            "GROUP BY s.team.teamID, t.teamName " +
            "ORDER BY SUM(s.scoreValue) DESC")
    Page<ScoreSummary> findOverallTeamRanking(Pageable pageable);

    // 统计/去重/聚合：比赛维度
    Long countByCompetition_CompetitionID(Integer competitionID);

    Long countByCompetition_CompetitionIDAndIsValidTrue(Integer competitionID);

    Long countByCompetition_CompetitionIDAndIsValidFalse(Integer competitionID);

    @Query("SELECT COUNT(DISTINCT s.user.userID) FROM Score s WHERE s.competition.competitionID = :competitionID")
    Long countDistinctUsersByCompetition(@Param("competitionID") Integer competitionID);

    @Query("SELECT COUNT(DISTINCT s.team.teamID) FROM Score s WHERE s.competition.competitionID = :competitionID")
    Long countDistinctTeamsByCompetition(@Param("competitionID") Integer competitionID);

    @Query("SELECT AVG(s.scoreValue) FROM Score s WHERE s.competition.competitionID = :competitionID AND s.isValid = true")
    Double averageScoreByCompetitionAndIsValidTrue(@Param("competitionID") Integer competitionID);

    @Query("SELECT MAX(s.scoreValue) FROM Score s WHERE s.competition.competitionID = :competitionID AND s.isValid = true")
    Integer maxScoreByCompetitionAndIsValidTrue(@Param("competitionID") Integer competitionID);

    @Query("SELECT MIN(s.scoreValue) FROM Score s WHERE s.competition.competitionID = :competitionID AND s.isValid = true")
    Integer minScoreByCompetitionAndIsValidTrue(@Param("competitionID") Integer competitionID);

    // 按类型计数（比赛维度）
    Long countByCompetition_CompetitionIDAndTypeAndIsValidTrue(Integer competitionID, String type);
}
