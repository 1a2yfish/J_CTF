package com.CTF.j_ctf.service;

import com.CTF.j_ctf.entity.Competition;
import com.CTF.j_ctf.entity.Team;
import com.CTF.j_ctf.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.Map;

public interface AdminService {

    // === 用户管理方法 ===
    Page<User> getAllUsers(Pageable pageable);
    Page<User> searchUsers(String keyword, Pageable pageable);
    User getUserDetails(Integer userId);
    User updateUser(User user);
    boolean disableUser(Integer userId);
    boolean enableUser(Integer userId);
    boolean deleteUser(Integer userId);

    // === 战队管理方法 ===
    Page<Team> getAllTeams(Pageable pageable);
    Page<Team> getTeamsByCompetition(Integer competitionId, Pageable pageable);
    Page<Team> getTeamsByAuditState(String auditState, Pageable pageable);
    Team getTeamDetails(Integer teamId);
    Team updateTeamAuditState(Integer teamId, String auditState, String auditRemark);
    boolean deleteTeam(Integer teamId);
    Map<String, Object> getTeamStatistics(Integer competitionId);

    // === 竞赛管理方法 ===
    Page<Competition> getAllCompetitions(Pageable pageable);
    Page<Competition> getCompetitionsByStatus(String status, Pageable pageable);
    Competition getCompetitionDetails(Integer competitionId);
    Competition createCompetition(Competition competition);
    Competition updateCompetition(Competition competition);
    Competition auditCompetition(Integer competitionId, boolean approved, String auditRemark);
    boolean deleteCompetition(Integer competitionId);
    Map<String, Object> getCompetitionStatistics(Integer competitionId);

    // === 系统统计方法 ===
    Map<String, Object> getSystemStatistics();
}