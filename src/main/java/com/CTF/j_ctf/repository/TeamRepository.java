package com.CTF.j_ctf.repository;

import com.CTF.j_ctf.entity.Team;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface TeamRepository extends JpaRepository<Team, Integer> {
    List<Team> findByCompetition_CompetitionID(Integer competitionID);
    Page<Team> findByCompetition_CompetitionID(Integer competitionID, Pageable pageable);
    Page<Team> findByAuditState(String auditState, Pageable pageable);
    boolean existsByTeamNameAndCompetition_CompetitionID(String teamName, Integer competitionID);

    @Query("SELECT DISTINCT t FROM Team t " +
           "LEFT JOIN FETCH t.captain " +
           "LEFT JOIN FETCH t.competition " +
           "LEFT JOIN FETCH t.teamMembers tm " +
           "LEFT JOIN FETCH tm.user " +
           "JOIN t.teamMembers m WHERE m.user.userID = :userID")
    List<Team> findByMemberId(@Param("userID") Integer userID);

    @Query("SELECT t FROM Team t WHERE t.captain.userID = :captainId")
    List<Team> findByCaptainId(@Param("captainId") Integer captainId);

    // 新增：分页版本（别名，匹配 Service 的 findByCaptain_UserID 调用）
    Page<Team> findByCaptain_UserID(Integer captainId, Pageable pageable);

    @Query("SELECT t FROM Team t JOIN t.teamMembers m WHERE m.user.userID = :userID AND t.competition.competitionID = :competitionID")
    Optional<Team> findByMemberAndCompetition(@Param("userID") Integer userID, @Param("competitionID") Integer competitionID);

    @Query("SELECT COUNT(t) > 0 FROM Team t JOIN t.teamMembers m WHERE m.user.userID = :userID AND t.competition.competitionID = :competitionID")
    boolean existsByMemberAndCompetition(@Param("userID") Integer userID, @Param("competitionID") Integer competitionID);

    long countByCompetition_CompetitionID(Integer competitionID);

    @Query("SELECT t FROM Team t WHERE t.teamName LIKE %:keyword%")
    Page<Team> findByTeamNameContaining(@Param("keyword") String keyword, Pageable pageable);

    @Query("SELECT t FROM Team t WHERE t.teamName LIKE %:keyword% AND t.competition.competitionID = :competitionID")
    Page<Team> findByTeamNameContainingAndCompetition(@Param("keyword") String keyword, @Param("competitionID") Integer competitionID, Pageable pageable);

    // 新增：别名匹配 Service 中的调用 findByTeamNameContainingAndCompetition_CompetitionID
    Page<Team> findByTeamNameContainingAndCompetition_CompetitionID(String keyword, Integer competitionID, Pageable pageable);

    /* 下面是补充的方法，供 Service 使用 */

    // 检查用户是否通过任意队伍（teamMembers）已经参加了指定竞赛
    @Query("SELECT COUNT(t) > 0 FROM Team t JOIN t.teamMembers m WHERE t.competition.competitionID = :competitionID AND m.user.userID = :userID")
    boolean existsByCompetition_CompetitionIDAndMembers_UserID(@Param("competitionID") Integer competitionID, @Param("userID") Integer userID);

    // 统计指定竞赛的参赛用户数量（去重）
    @Query("SELECT COUNT(DISTINCT m.user) FROM Team t JOIN t.teamMembers m WHERE t.competition.competitionID = :competitionID")
    long countParticipantsByCompetition(@Param("competitionID") Integer competitionID);

    // 按竞赛ID和审核状态计数（Service 所需）
    long countByCompetition_CompetitionIDAndAuditState(Integer competitionID, String auditState);

    // 新增：根据成员获取最近加入/最近存在的战队（用于获取用户当前战队）
    @Query("SELECT t FROM Team t JOIN t.teamMembers m WHERE m.user.userID = :userID ORDER BY t.creationTime DESC")
    Optional<Team> findLatestByMember(@Param("userID") Integer userID);

    // 获取团队详情（包含成员列表）- 使用 JOIN FETCH 立即加载成员
    @Query("SELECT DISTINCT t FROM Team t LEFT JOIN FETCH t.teamMembers tm LEFT JOIN FETCH tm.user LEFT JOIN FETCH t.captain LEFT JOIN FETCH t.competition WHERE t.teamID = :teamId")
    Optional<Team> findByIdWithMembers(@Param("teamId") Integer teamId);
}
