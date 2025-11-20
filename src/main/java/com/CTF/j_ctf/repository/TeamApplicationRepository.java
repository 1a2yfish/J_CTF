package com.CTF.j_ctf.repository;

import com.CTF.j_ctf.entity.TeamApplication;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

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
    /**
     * 统计指定竞赛下、指定审核状态的战队申请数量
     * 关联路径：TeamApplication -> Team -> Competition -> competitionID
     * @param competitionID 竞赛ID
     * @param status 审核状态（PENDING/APPROVED/REJECTED）
     * @return 对应状态的申请数量
     */
    long countByTeam_Competition_CompetitionIDAndStatus(Integer competitionID, String status);

    /**
     * 统计指定竞赛下的战队申请总数
     * @param competitionID 竞赛ID
     * @return 该竞赛下的所有战队申请数量
     */
    long countByTeam_Competition_CompetitionID(Integer competitionID);

    // 可选：补充分页查询方法，满足后台列表展示需求
    Page<TeamApplication> findByTeam_Competition_CompetitionIDAndStatus(Integer competitionID, String status, Pageable pageable);
}