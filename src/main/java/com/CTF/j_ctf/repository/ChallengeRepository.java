package com.CTF.j_ctf.repository;

import com.CTF.j_ctf.entity.Challenge;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ChallengeRepository extends JpaRepository<Challenge, Integer> {

    // ===================== 核心基础查询 =====================
    // 按竞赛ID查询题目列表（分页/非分页）
    List<Challenge> findByCompetition_CompetitionID(Integer competitionID);
    Page<Challenge> findByCompetition_CompetitionID(Integer competitionID, Pageable pageable);

    // 按类别/难度分页查询
    Page<Challenge> findByCategory(String category, Pageable pageable);
    Page<Challenge> findByDifficulty(String difficulty, Pageable pageable);

    // ===================== 核心搜索查询 =====================
    // 按标题/描述模糊搜索（全局/指定竞赛）- 保留方法命名约定的简洁版本
    Page<Challenge> findByTitleContainingOrDescriptionContaining(String title, String description, Pageable pageable);
    Page<Challenge> findByTitleContainingOrDescriptionContainingAndCompetition_CompetitionID(String title, String description, Integer competitionID, Pageable pageable);

    // 多条件组合筛选（类别/难度/竞赛/状态，支持空值即不筛选）
    @Query("SELECT c FROM Challenge c WHERE " +
            "(:category IS NULL OR c.category = :category) AND " +
            "(:difficulty IS NULL OR c.difficulty = :difficulty) AND " +
            "(:competitionID IS NULL OR c.competition.competitionID = :competitionID) AND " +
            "(:isActive IS NULL OR c.isActive = :isActive)")
    Page<Challenge> findByMultipleConditions(@Param("category") String category,
                                             @Param("difficulty") String difficulty,
                                             @Param("competitionID") Integer competitionID,
                                             @Param("isActive") Boolean isActive,
                                             Pageable pageable);

    // ===================== 核心统计查询 =====================
    // 按竞赛统计题目总数/按类别/难度统计
    Long countByCompetition_CompetitionID(Integer competitionID);
    Long countByCompetition_CompetitionIDAndCategory(Integer competitionID, String category);
    Long countByCompetition_CompetitionIDAndDifficulty(Integer competitionID, String difficulty);

    // 竞赛内题目总解题数统计
    @Query("SELECT SUM(c.solveCount) FROM Challenge c WHERE c.competition.competitionID = :competitionID")
    Long sumSolveCountByCompetition(@Param("competitionID") Integer competitionID);

    // 按竞赛查询唯一的类别/难度列表
    List<String> findDistinctCategoriesByCompetition_CompetitionID(Integer competitionID);

    // 全局唯一类别列表
    @Query("SELECT DISTINCT c.category FROM Challenge c")
    List<String> findAllDistinctCategories();

    // ===================== 核心更新操作 =====================
    // 更新题目激活状态
    @Modifying
    @Query("UPDATE Challenge c SET c.isActive = :isActive WHERE c.challengeID = :challengeId")
    int updateChallengeStatus(@Param("challengeId") Integer challengeId, @Param("isActive") Boolean isActive);

    // 增加解题数（核心业务：用户解出题目后调用）
    @Modifying
    @Query("UPDATE Challenge c SET c.solveCount = c.solveCount + 1 WHERE c.challengeID = :challengeId")
    int incrementSolveCount(@Param("challengeId") Integer challengeId);

    // 更新题目分值
    @Modifying
    @Query("UPDATE Challenge c SET c.points = :points WHERE c.challengeID = :challengeId")
    int updateChallengePoints(@Param("challengeId") Integer challengeId, @Param("points") Integer points);

    // 检查竞赛内题目名称是否重复（唯一性验证）
    @Query("SELECT COUNT(c) > 0 FROM Challenge c WHERE c.title = :title AND c.competition.competitionID = :competitionID")
    boolean existsByTitleAndCompetition(@Param("title") String title, @Param("competitionID") Integer competitionID);

    // 题目名称唯一性验证（编辑时排除自身）
    @Query("SELECT COUNT(c) > 0 FROM Challenge c WHERE c.title = :title AND c.competition.competitionID = :competitionID AND c.challengeID != :excludeChallengeId")
    boolean existsByTitleAndCompetitionExcludingId(@Param("title") String title,
                                                   @Param("competitionID") Integer competitionID,
                                                   @Param("excludeChallengeId") Integer excludeChallengeId);
}