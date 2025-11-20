package com.CTF.j_ctf.repository;

import com.CTF.j_ctf.entity.Challenge;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ChallengeRepository extends JpaRepository<Challenge, Integer> {

    // 基础查询
    List<Challenge> findByCompetition_CompetitionID(Integer competitionID);
    Page<Challenge> findByCompetition_CompetitionID(Integer competitionID, Pageable pageable);
    Page<Challenge> findByCategory(String category, Pageable pageable);
    Page<Challenge> findByDifficulty(String difficulty, Pageable pageable);
    Page<Challenge> findByIsActive(Boolean isActive, Pageable pageable);

    // 竞赛和类别组合查询
    Page<Challenge> findByCompetition_CompetitionIDAndCategory(Integer competitionID, String category, Pageable pageable);
    Page<Challenge> findByCompetition_CompetitionIDAndDifficulty(Integer competitionID, String difficulty, Pageable pageable);

    // 搜索查询
    @Query("SELECT c FROM Challenge c WHERE c.title LIKE %:keyword% OR c.description LIKE %:keyword%")
    Page<Challenge> findByTitleOrDescriptionContaining(@Param("keyword") String keyword, Pageable pageable);

    @Query("SELECT c FROM Challenge c WHERE (c.title LIKE %:keyword% OR c.description LIKE %:keyword%) AND c.competition.competitionID = :competitionID")
    Page<Challenge> findByTitleOrDescriptionContainingAndCompetition(@Param("keyword") String keyword, @Param("competitionID") Integer competitionID, Pageable pageable);

    // 统计查询
    @Query("SELECT COUNT(c) FROM Challenge c WHERE c.competition.competitionID = :competitionID")
    Long countByCompetition(@Param("competitionID") Integer competitionID);

    @Query("SELECT COUNT(c) FROM Challenge c WHERE c.competition.competitionID = :competitionID AND c.category = :category")
    Long countByCompetitionAndCategory(@Param("competitionID") Integer competitionID, @Param("category") String category);

    @Query("SELECT COUNT(c) FROM Challenge c WHERE c.competition.competitionID = :competitionID AND c.difficulty = :difficulty")
    Long countByCompetitionAndDifficulty(@Param("competitionID") Integer competitionID, @Param("difficulty") String difficulty);

    // 检查唯一性
    @Query("SELECT COUNT(c) > 0 FROM Challenge c WHERE c.title = :title AND c.competition.competitionID = :competitionID")
    boolean existsByTitleAndCompetition(@Param("title") String title, @Param("competitionID") Integer competitionID);

    // 获取竞赛中所有类别
    @Query("SELECT DISTINCT c.category FROM Challenge c WHERE c.competition.competitionID = :competitionID")
    List<String> findDistinctCategoriesByCompetition(@Param("competitionID") Integer competitionID);
}