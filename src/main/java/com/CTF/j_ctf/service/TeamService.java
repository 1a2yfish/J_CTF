package com.CTF.j_ctf.service;

import com.CTF.j_ctf.entity.Team;
import com.CTF.j_ctf.entity.TeamApplication;
import com.CTF.j_ctf.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Map;
import java.util.Optional;

public interface TeamService {

    // 战队管理
    Team createTeam(Team team, User captain);
    Team updateTeam(Team team);
    boolean deleteTeam(Integer teamId);
    Optional<Team> getTeamById(Integer teamId);

    // 查询
    Page<Team> getAllTeams(Pageable pageable);
    Page<Team> getTeamsByCompetition(Integer competitionId, Pageable pageable);
    Page<Team> getTeamsByAuditState(String auditState, Pageable pageable);
    Page<Team> getTeamsByCaptain(Integer captainId, Pageable pageable);
    Page<Team> getTeamsByMember(Integer memberId, Pageable pageable);
    Page<Team> searchTeams(String keyword, Pageable pageable);
    Page<Team> searchTeamsByCompetition(String keyword, Integer competitionId, Pageable pageable);

    // 成员管理
    boolean addMember(Integer teamId, Integer userId);
    boolean removeMember(Integer teamId, Integer userId);
    boolean transferCaptain(Integer teamId, Integer newCaptainId, Integer currentCaptainId);
    boolean leaveTeam(Integer teamId, Integer userId);

    // 申请管理
    TeamApplication applyToJoinTeam(Integer teamId, Integer applicantId, String remark);
    TeamApplication processApplication(Integer applicationId, boolean approved, String remark);
    Page<TeamApplication> getTeamApplications(Integer teamId, Pageable pageable);
    Page<TeamApplication> getUserApplications(Integer userId, Pageable pageable);
    Page<TeamApplication> getPendingApplicationsByCaptain(Integer captainId, Pageable pageable);

    // 审核管理
    Team auditTeam(Integer teamId, boolean approved, String auditRemark);

    // 验证
    boolean canUserJoinTeam(Integer teamId, Integer userId);
    boolean isTeamFull(Integer teamId);
    boolean isUserInTeam(Integer teamId, Integer userId);
    boolean isTeamCaptain(Integer teamId, Integer userId);

    // 统计
    Map<String, Object> getTeamStatistics(Integer competitionId);
    Long getTeamCountByCompetition(Integer competitionId);
    Long getPendingApplicationCount(Integer teamId);
}