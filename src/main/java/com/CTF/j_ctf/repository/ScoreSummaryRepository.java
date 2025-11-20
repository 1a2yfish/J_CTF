package com.CTF.j_ctf.repository;

import com.CTF.j_ctf.entity.ScoreSummary;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ScoreSummaryRepository extends JpaRepository<ScoreSummary, String> {

    // 竞赛用户排行榜
    @Query("SELECT ss FROM ScoreSummary ss WHERE ss.competitionID = :competitionID AND ss.entityType = 'USER' ORDER BY ss.totalScore DESC, ss.lastSubmitTime ASC")
    List<ScoreSummary> findUserRankingByCompetition(@Param("competitionID") Integer competitionID);

    // 竞赛战队排行榜
    @Query("SELECT ss FROM ScoreSummary ss WHERE ss.competitionID = :competitionID AND ss.entityType = 'TEAM' ORDER BY ss.totalScore DESC, ss.lastSubmitTime ASC")
    List<ScoreSummary> findTeamRankingByCompetition(@Param("competitionID") Integer competitionID);

    // 总体用户排行榜
    @Query("SELECT ss FROM ScoreSummary ss WHERE ss.entityType = 'USER' ORDER BY ss.totalScore DESC, ss.lastSubmitTime ASC")
    List<ScoreSummary> findOverallUserRanking();

    // 总体战队排行榜
    @Query("SELECT ss FROM ScoreSummary ss WHERE ss.entityType = 'TEAM' ORDER BY ss.totalScore DESC, ss.lastSubmitTime ASC")
    List<ScoreSummary> findOverallTeamRanking();

    // 获取用户排名
    @Query("SELECT ss.rank FROM ScoreSummary ss WHERE ss.entityID = :userID AND ss.entityType = 'USER' AND ss.competitionID = :competitionID")
    Integer findUserRank(@Param("userID") Integer userID, @Param("competitionID") Integer competitionID);

    // 获取战队排名
    @Query("SELECT ss.rank FROM ScoreSummary ss WHERE ss.entityID = :teamID AND ss.entityType = 'TEAM' AND ss.competitionID = :competitionID")
    Integer findTeamRank(@Param("teamID") Integer teamID, @Param("competitionID") Integer competitionID);
}