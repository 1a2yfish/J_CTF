package com.CTF.j_ctf.repository;

import com.CTF.j_ctf.entity.Score;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
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

    // 统计查询
    @Query("SELECT SUM(s.score) FROM Score s WHERE s.user.userID = :userID AND s.competition.competitionID = :competitionID AND s.isValid = true")
    Integer getTotalScoreByUserAndCompetition(@Param("userID") Integer userID, @Param("competitionID") Integer competitionID);

    @Query("SELECT SUM(s.score) FROM Score s WHERE s.team.teamID = :teamID AND s.competition.competitionID = :competitionID AND s.isValid = true")
    Integer getTotalScoreByTeamAndCompetition(@Param("teamID") Integer teamID, @Param("competitionID") Integer competitionID);

    @Query("SELECT COUNT(s) FROM Score s WHERE s.user.userID = :userID AND s.competition.competitionID = :competitionID AND s.isValid = true AND s.score > 0")
    Long countPositiveScoresByUserAndCompetition(@Param("userID") Integer userID, @Param("competitionID") Integer competitionID);

    @Query("SELECT COUNT(s) FROM Score s WHERE s.team.teamID = :teamID AND s.competition.competitionID = :competitionID AND s.isValid = true AND s.score > 0")
    Long countPositiveScoresByTeamAndCompetition(@Param("teamID") Integer teamID, @Param("competitionID") Integer competitionID);

    // 排行榜查询
    @Query("SELECT s.user.userID as userID, u.userName as userName, SUM(s.score) as totalScore " +
            "FROM Score s JOIN s.user u " +
            "WHERE s.competition.competitionID = :competitionID AND s.isValid = true " +
            "GROUP BY s.user.userID, u.userName " +
            "ORDER BY totalScore DESC")
    List<Object[]> getUserRankingByCompetition(@Param("competitionID") Integer competitionID);

    @Query("SELECT s.team.teamID as teamID, t.teamName as teamName, SUM(s.score) as totalScore " +
            "FROM Score s JOIN s.team t " +
            "WHERE s.competition.competitionID = :competitionID AND s.isValid = true " +
            "GROUP BY s.team.teamID, t.teamName " +
            "ORDER BY totalScore DESC")
    List<Object[]> getTeamRankingByCompetition(@Param("competitionID") Integer competitionID);

    @Query("SELECT s.user.userID as userID, u.userName as userName, SUM(s.score) as totalScore " +
            "FROM Score s JOIN s.user u " +
            "WHERE s.isValid = true " +
            "GROUP BY s.user.userID, u.userName " +
            "ORDER BY totalScore DESC")
    List<Object[]> getOverallUserRanking();

    @Query("SELECT s.team.teamID as teamID, t.teamName as teamName, SUM(s.score) as totalScore " +
            "FROM Score s JOIN s.team t " +
            "WHERE s.isValid = true " +
            "GROUP BY s.team.teamID, t.teamName " +
            "ORDER BY totalScore DESC")
    List<Object[]> getOverallTeamRanking();

    // 最近得分查询
    @Query("SELECT s FROM Score s WHERE s.competition.competitionID = :competitionID ORDER BY s.createTime DESC")
    List<Score> findRecentScoresByCompetition(@Param("competitionID") Integer competitionID, Pageable pageable);

    // 检查用户是否已经获得某个题目的分数
    @Query("SELECT COUNT(s) > 0 FROM Score s WHERE s.user.userID = :userID AND s.challenge.challengeID = :challengeID AND s.isValid = true")
    boolean existsByUserAndChallenge(@Param("userID") Integer userID, @Param("challengeID") Integer challengeID);

    // 检查战队是否已经获得某个题目的分数
    @Query("SELECT COUNT(s) > 0 FROM Score s WHERE s.team.teamID = :teamID AND s.challenge.challengeID = :challengeID AND s.isValid = true")
    boolean existsByTeamAndChallenge(@Param("teamID") Integer teamID, @Param("challengeID") Integer challengeID);
}