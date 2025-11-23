package com.CTF.j_ctf.repository;

import com.CTF.j_ctf.entity.Flag;
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
public interface FlagRepository extends JpaRepository<Flag, Integer> {

    Page<Flag> findByUser_UserID(Integer userID, Pageable pageable);
    List<Flag> findByCompetition_CompetitionID(Integer competitionID);
    Page<Flag> findByCompetition_CompetitionID(Integer competitionID, Pageable pageable);

    Page<Flag> findByTeam_TeamID(Integer teamID, Pageable pageable);

    Page<Flag> findByStatus(Integer status, Pageable pageable);

    // 值查询
    Optional<Flag> findByValue(String value);

    // 新增：检查是否存在指定值的 Flag（Service 使用）
    boolean existsByValue(String value);

    @Query("SELECT f FROM Flag f WHERE f.value = :value AND f.competition.competitionID = :competitionID")
    Optional<Flag> findByValueAndCompetition(@Param("value") String value, @Param("competitionID") Integer competitionID);

    // 用户和竞赛组合查询
    @Query("SELECT f FROM Flag f WHERE f.user.userID = :userID AND f.competition.competitionID = :competitionID")
    List<Flag> findByUserAndCompetition(@Param("userID") Integer userID, @Param("competitionID") Integer competitionID);

    @Query("SELECT f FROM Flag f WHERE f.team.teamID = :teamID AND f.competition.competitionID = :competitionID")
    List<Flag> findByTeamAndCompetition(@Param("teamID") Integer teamID, @Param("competitionID") Integer competitionID);

    // 统计查询（保留原有的同时补充 Service 需要的命名）
    @Query("SELECT COUNT(f) FROM Flag f WHERE f.user.userID = :userID AND f.competition.competitionID = :competitionID AND f.status = 1")
    Integer countUsedFlagsByUserAndCompetition(@Param("userID") Integer userID, @Param("competitionID") Integer competitionID);

    @Query("SELECT COUNT(f) FROM Flag f WHERE f.team.teamID = :teamID AND f.competition.competitionID = :competitionID AND f.status = 1")
    Integer countUsedFlagsByTeamAndCompetition(@Param("teamID") Integer teamID, @Param("competitionID") Integer competitionID);

    @Query("SELECT COUNT(f) FROM Flag f WHERE f.competition.competitionID = :competitionID AND f.status = 1")
    Long countUsedFlagsByCompetition(@Param("competitionID") Integer competitionID);

    @Query("SELECT COUNT(f) FROM Flag f WHERE f.competition.competitionID = :competitionID")
    Long countFlagsByCompetition(@Param("competitionID") Integer competitionID);

    // 补充：与 Service 调用一致的命名
    Long countByCompetition_CompetitionID(Integer competitionID);

    Long countByCompetition_CompetitionIDAndStatus(Integer competitionID, Integer status);

    // 过期Flag查询
    @Query("SELECT f FROM Flag f WHERE f.expireTime < :now AND f.status = 0")
    List<Flag> findExpiredFlags(@Param("now") LocalDateTime now);

    // 新增：按时间过期且排除指定状态（Service 使用）
    @Query("SELECT f FROM Flag f WHERE f.expireTime < :now AND f.status <> :status")
    List<Flag> findByExpireTimeBeforeAndStatusNot(@Param("now") LocalDateTime now, @Param("status") Integer status);

    // 可用的Flag查询
    @Query("SELECT f FROM Flag f WHERE f.competition.competitionID = :competitionID AND f.status = 0 AND (f.expireTime IS NULL OR f.expireTime > :now)")
    List<Flag> findAvailableFlagsByCompetition(@Param("competitionID") Integer competitionID, @Param("now") LocalDateTime now);

    // 搜索查询
    @Query("SELECT f FROM Flag f WHERE f.value LIKE %:keyword% OR f.description LIKE %:keyword%")
    Page<Flag> findByValueOrDescriptionContaining(@Param("keyword") String keyword, Pageable pageable);

    // 新增：与 Service 中调用的命名保持一致（按值或描述模糊搜索）
    Page<Flag> findByValueContainingOrDescriptionContaining(String valueKeyword, String descriptionKeyword, Pageable pageable);
}
