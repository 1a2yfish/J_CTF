package com.CTF.j_ctf.service;

import com.CTF.j_ctf.entity.Team;
import com.CTF.j_ctf.entity.TeamApplication;
import com.CTF.j_ctf.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.Map;
import java.util.Optional;

public interface TeamService {

    Team createTeam(Team team, User captain);
    Team updateTeam(Team team);
    boolean disbandTeam(Integer teamId);

    boolean deleteTeam(Integer teamId);

    Optional<Team> getTeamById(Integer teamId);
    Optional<Team> getTeamByIdWithMembers(Integer teamId);
    Page<Team> getAllTeams(Pageable pageable);
    Page<Team> getTeamsByCompetition(Integer competitionId, Pageable pageable);
    Page<Team> getTeamsByAuditState(String auditState, Pageable pageable);

    Page<Team> getTeamsByCaptain(Integer captainId, Pageable pageable);

    Page<Team> getTeamsByMember(Integer memberId, Pageable pageable);

    Page<Team> searchTeams(String keyword, Pageable pageable);
    Page<Team> searchTeamsByCompetition(String keyword, Integer competitionId, Pageable pageable);

    // 新增方法
    Optional<Team> getUserTeamInCompetition(Integer userId, Integer competitionId);
    Optional<Team> getUserCurrentTeam(Integer userId);
    boolean isUserCompetitionCreator(Integer competitionId, Integer userId);
    String getJoinRestrictionReason(Integer teamId, Integer userId);
    TeamApplication inviteUser(Integer teamId, Integer targetUserId, Integer inviterId);
    Page<TeamApplication> getTeamApplicationsByStatus(Integer teamId, String status, Pageable pageable);
    Optional<TeamApplication> getApplicationById(Integer applicationId);
    boolean isTeamMember(Integer teamId, Integer userId);
    Map<String, Object> getTeamStatistics(Integer teamId);

    // 已有方法保持不变
    TeamApplication applyToJoinTeam(Integer teamId, Integer applicantId, String remark);
    TeamApplication processApplication(Integer applicationId, boolean approved, String remark);
    Page<TeamApplication> getTeamApplications(Integer teamId, Pageable pageable);
    Page<TeamApplication> getUserApplications(Integer userId, Pageable pageable);
    boolean transferCaptain(Integer teamId, Integer newCaptainId, Integer currentCaptainId);
    boolean leaveTeam(Integer teamId, Integer userId);

    boolean addMember(Integer teamId, Integer userId);

    boolean removeMember(Integer teamId, Integer memberId);
    Team auditTeam(Integer teamId, String auditState, String auditRemark);
    boolean canUserJoinTeam(Integer teamId, Integer userId);
    boolean isTeamCaptain(Integer teamId, Integer userId);

    Page<TeamApplication> getPendingApplicationsByCaptain(Integer captainId, Pageable pageable);

    boolean isTeamFull(Integer teamId);

    boolean isUserInTeam(Integer teamId, Integer userId);

    Long getTeamCountByCompetition(Integer competitionId);

    Long getPendingApplicationCount(Integer teamId);
}