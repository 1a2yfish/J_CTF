package com.CTF.j_ctf.repository;

import com.CTF.j_ctf.entity.Competition;
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
public interface CompetitionRepository extends JpaRepository<Competition, Integer> {

    // 基础查询
    List<Competition> findByCreator_UserID(Integer userID);
    Page<Competition> findByCreator_UserID(Integer userID, Pageable pageable);
    Page<Competition> findByTitleContaining(String title, Pageable pageable);
    Page<Competition> findByStatus(String status, Pageable pageable);
    Page<Competition> findByAuditStatus(String auditStatus, Pageable pageable);

    // 状态查询
    @Query("SELECT c FROM Competition c WHERE c.startTime <= :now AND c.endTime >= :now AND c.status = 'PUBLISHED'")
    List<Competition> findOngoingCompetitions(@Param("now") LocalDateTime now);

    @Query("SELECT c FROM Competition c WHERE c.startTime <= :now AND c.endTime >= :now AND c.status = 'PUBLISHED'")
    Page<Competition> findOngoingCompetitions(@Param("now") LocalDateTime now, Pageable pageable);

    @Query("SELECT c FROM Competition c WHERE c.endTime < :now AND c.status = 'PUBLISHED'")
    List<Competition> findFinishedCompetitions(@Param("now") LocalDateTime now);

    @Query("SELECT c FROM Competition c WHERE c.endTime < :now AND c.status = 'PUBLISHED'")
    Page<Competition> findFinishedCompetitions(@Param("now") LocalDateTime now, Pageable pageable);

    @Query("SELECT c FROM Competition c WHERE c.startTime > :now AND c.status = 'PUBLISHED'")
    List<Competition> findUpcomingCompetitions(@Param("now") LocalDateTime now);

    @Query("SELECT c FROM Competition c WHERE c.startTime > :now AND c.status = 'PUBLISHED'")
    Page<Competition> findUpcomingCompetitions(@Param("now") LocalDateTime now, Pageable pageable);

    // 公开竞赛查询
    @Query("SELECT c FROM Competition c WHERE c.isPublic = true AND c.status = 'PUBLISHED'")
    Page<Competition> findPublicCompetitions(Pageable pageable);

    @Query("SELECT c FROM Competition c WHERE c.isPublic = true AND c.status = 'PUBLISHED' AND c.startTime <= :now AND c.endTime >= :now")
    Page<Competition> findPublicOngoingCompetitions(@Param("now") LocalDateTime now, Pageable pageable);

    // 统计查询
    boolean existsByTitle(String title);

    @Query("SELECT COUNT(c) FROM Competition c WHERE c.creator.userID = :userId")
    Long countByCreator(@Param("userId") Integer userId);

    @Query("SELECT COUNT(c) FROM Competition c WHERE c.status = :status")
    Long countByStatus(@Param("status") String status);

    // 搜索查询
    @Query("SELECT c FROM Competition c WHERE " +
            "(c.title LIKE %:keyword% OR c.introduction LIKE %:keyword%) AND " +
            "c.status = 'PUBLISHED' AND c.isPublic = true")
    Page<Competition> searchPublicCompetitions(@Param("keyword") String keyword, Pageable pageable);

    // 管理员搜索
    @Query("SELECT c FROM Competition c WHERE " +
            "c.title LIKE %:keyword% OR c.introduction LIKE %:keyword%")
    Page<Competition> searchAllCompetitions(@Param("keyword") String keyword, Pageable pageable);

    // 检查用户是否创建了指定竞赛
    @Query("SELECT COUNT(c) > 0 FROM Competition c WHERE c.competitionID = :competitionId AND c.creator.userID = :userId")
    boolean isCreator(@Param("competitionId") Integer competitionId, @Param("userId") Integer userId);
}