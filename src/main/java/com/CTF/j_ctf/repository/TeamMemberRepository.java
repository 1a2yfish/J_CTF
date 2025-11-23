package com.CTF.j_ctf.repository;

import com.CTF.j_ctf.entity.TeamMember;
import com.CTF.j_ctf.entity.TeamMemberId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface TeamMemberRepository extends JpaRepository<TeamMember, TeamMemberId> {

    // 检查用户是否在某个战队中
    boolean existsByTeamTeamIDAndUserUserID(Integer teamId, Integer userId);

    // 根据战队ID和用户ID删除成员
    @Modifying
    @Query("DELETE FROM TeamMember tm WHERE tm.team.teamID = :teamId AND tm.user.userID = :userId")
    void deleteByTeamAndUser(@Param("teamId") Integer teamId, @Param("userId") Integer userId);

    // 根据战队ID查找所有成员
    List<TeamMember> findByTeamTeamID(Integer teamId);

    // 根据用户ID查找所有加入的战队
    List<TeamMember> findByUserUserID(Integer userId);

    // 根据用户ID和竞赛ID查找战队成员关系
    @Query("SELECT tm FROM TeamMember tm WHERE tm.user.userID = :userId AND tm.team.competition.competitionID = :competitionId")
    Optional<TeamMember> findByUserAndCompetition(@Param("userId") Integer userId, @Param("competitionId") Integer competitionId);

    // 统计战队成员数量
    Long countByTeamTeamID(Integer teamId);

    // 删除战队的所有成员
    @Modifying(clearAutomatically = true)
    @Query("DELETE FROM TeamMember tm WHERE tm.team.teamID = :teamId")
    @org.springframework.transaction.annotation.Transactional
    void deleteAllByTeamId(@Param("teamId") Integer teamId);
}