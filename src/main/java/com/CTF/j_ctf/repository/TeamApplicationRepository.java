// src/main/java/com/CTF/j_ctf/repository/TeamApplicationRepository.java
package com.CTF.j_ctf.repository;

import com.CTF.j_ctf.entity.Team;
import com.CTF.j_ctf.entity.TeamApplication;
import com.CTF.j_ctf.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Repository
public interface TeamApplicationRepository extends JpaRepository<TeamApplication, Integer> {

    List<TeamApplication> findByTeam_TeamID(Integer teamID);
    Page<TeamApplication> findByTeam_TeamID(Integer teamID, Pageable pageable);

    List<TeamApplication> findByApplicant_UserID(Integer applicantID);
    Page<TeamApplication> findByApplicant_UserID(Integer applicantID, Pageable pageable);

    List<TeamApplication> findByStatus(String status);
    Page<TeamApplication> findByStatus(String status, Pageable pageable);

    @Query("SELECT ta FROM TeamApplication ta WHERE ta.team.captain.userID = :captainID")
    List<TeamApplication> findByTeamCaptain(@Param("captainID") Integer captainID);

    @Query("SELECT ta FROM TeamApplication ta WHERE ta.team.captain.userID = :captainID AND ta.status = :status")
    List<TeamApplication> findByTeamCaptainAndStatus(@Param("captainID") Integer captainID, @Param("status") String status);

    Optional<TeamApplication> findByTeam_TeamIDAndApplicant_UserID(Integer teamID, Integer applicantID);

    @Query("SELECT COUNT(ta) FROM TeamApplication ta WHERE ta.team.teamID = :teamID AND ta.status = 'PENDING'")
    Long countPendingApplicationsByTeam(@Param("teamID") Integer teamID);

    long countByTeam_Competition_CompetitionIDAndStatus(Integer competitionID, String status);

    long countByTeam_Competition_CompetitionID(Integer competitionID);

    Page<TeamApplication> findByTeam_Competition_CompetitionIDAndStatus(Integer competitionID, String status, Pageable pageable);

    Optional<TeamApplication> findByTeamAndApplicantAndStatus(Team team, User applicant, String status);

    // 原有：按 teamId 和 status 分页
    @Query("SELECT ta FROM TeamApplication ta WHERE ta.team.teamID = :teamId AND ta.status = :status")
    Page<TeamApplication> findByTeamAndStatus(@Param("teamId") Integer teamId, @Param("status") String status, Pageable pageable);

    Long countByTeam_TeamIDAndStatus(Integer teamId, String status);

    Long countByTeam_TeamID(Integer teamId);

    /* 新增：补齐 Service 中使用的常见签名（按实体参数的计数/删除别名） */

    // 按实体计数（countByTeam 和 countByTeamAndStatus 在 Service 中可能使用实体参数）
    long countByTeam(Team team);
    long countByTeamAndStatus(Team team, String status);

    // 按 teamId 删除申请（返回删除数量）
    @Transactional
    @Modifying(clearAutomatically = true)
    int deleteByTeam_TeamID(Integer teamID);
    
    // 按用户ID删除申请（返回删除数量）
    @Transactional
    @Modifying(clearAutomatically = true)
    @Query("DELETE FROM TeamApplication ta WHERE ta.applicant.userID = :userId")
    int deleteByUser_UserID(@Param("userId") Integer userId);
}
