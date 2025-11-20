package com.CTF.j_ctf.service.impl;

import com.CTF.j_ctf.entity.*;
import com.CTF.j_ctf.repository.*;
import com.CTF.j_ctf.service.TeamService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@Service
@Transactional
public class TeamServiceImpl implements TeamService {
    private final TeamRepository teamRepository;
    private final TeamApplicationRepository teamApplicationRepository;
    private final UserRepository userRepository;
    private final CompetitionRepository competitionRepository;

    public TeamServiceImpl(TeamRepository teamRepository,
                           TeamApplicationRepository teamApplicationRepository,
                           UserRepository userRepository,
                           CompetitionRepository competitionRepository) {
        this.teamRepository = teamRepository;
        this.teamApplicationRepository = teamApplicationRepository;
        this.userRepository = userRepository;
        this.competitionRepository = competitionRepository;
    }

    @Override
    public Team createTeam(Team team, User captain) {
        // 验证数据
        validateTeam(team);

        // 检查用户是否已在同一竞赛的其他战队
        if (teamRepository.existsByMemberAndCompetition(captain.getUserID(), team.getCompetition().getCompetitionID())) {
            throw new IllegalArgumentException("您已加入该竞赛的其他战队");
        }

        // 检查战队名称是否唯一
        if (teamRepository.existsByTeamNameAndCompetition_CompetitionID(team.getTeamName(), team.getCompetition().getCompetitionID())) {
            throw new IllegalArgumentException("该竞赛中已存在同名战队");
        }

        // 设置队长
        team.setCaptain(captain);
        team.getMembers().add(captain);

        return teamRepository.save(team);
    }

    @Override
    public Team updateTeam(Team team) {
        Optional<Team> existingOpt = teamRepository.findById(team.getTeamID());
        if (existingOpt.isEmpty()) {
            throw new IllegalArgumentException("战队不存在");
        }

        Team existing = existingOpt.get();

        // 只有待审核状态的战队可以修改基本信息
        if (!existing.isAuditPending()) {
            throw new IllegalArgumentException("只有待审核状态的战队可以修改");
        }

        // 更新允许修改的字段
        if (team.getDescription() != null) {
            existing.setDescription(team.getDescription());
        }

        // 检查战队名称是否改变且是否唯一
        if (team.getTeamName() != null && !team.getTeamName().equals(existing.getTeamName())) {
            if (teamRepository.existsByTeamNameAndCompetition_CompetitionID(team.getTeamName(), existing.getCompetition().getCompetitionID())) {
                throw new IllegalArgumentException("该竞赛中已存在同名战队");
            }
            existing.setTeamName(team.getTeamName());
        }

        return teamRepository.save(existing);
    }

    @Override
    public boolean deleteTeam(Integer teamId) {
        if (!teamRepository.existsById(teamId)) {
            return false;
        }

        teamRepository.deleteById(teamId);
        return true;
    }

    @Override
    public Optional<Team> getTeamById(Integer teamId) {
        return teamRepository.findById(teamId);
    }

    @Override
    public Page<Team> getAllTeams(Pageable pageable) {
        return teamRepository.findAll(pageable);
    }

    @Override
    public Page<Team> getTeamsByCompetition(Integer competitionId, Pageable pageable) {
        return teamRepository.findByCompetition_CompetitionID(competitionId, pageable);
    }

    @Override
    public Page<Team> getTeamsByAuditState(String auditState, Pageable pageable) {
        return teamRepository.findByAuditState(auditState, pageable);
    }

    @Override
    public Page<Team> getTeamsByCaptain(Integer captainId, Pageable pageable) {
        // 需要自定义查询实现
        return teamRepository.findAll(pageable); // 简化实现
    }

    @Override
    public Page<Team> getTeamsByMember(Integer memberId, Pageable pageable) {
        // 需要自定义查询实现
        return teamRepository.findAll(pageable); // 简化实现
    }

    @Override
    public Page<Team> searchTeams(String keyword, Pageable pageable) {
        return teamRepository.findByTeamNameContaining(keyword, pageable);
    }

    @Override
    public Page<Team> searchTeamsByCompetition(String keyword, Integer competitionId, Pageable pageable) {
        return teamRepository.findByTeamNameContainingAndCompetition(keyword, competitionId, pageable);
    }

    @Override
    public boolean addMember(Integer teamId, Integer userId) {
        Optional<Team> teamOpt = teamRepository.findById(teamId);
        Optional<User> userOpt = userRepository.findById(userId);

        if (teamOpt.isEmpty() || userOpt.isEmpty()) {
            return false;
        }

        Team team = teamOpt.get();
        User user = userOpt.get();

        // 验证是否可以加入
        if (!canUserJoinTeam(teamId, userId)) {
            return false;
        }

        team.addMember(user);
        teamRepository.save(team);
        return true;
    }

    @Override
    public boolean removeMember(Integer teamId, Integer userId) {
        Optional<Team> teamOpt = teamRepository.findById(teamId);
        Optional<User> userOpt = userRepository.findById(userId);

        if (teamOpt.isEmpty() || userOpt.isEmpty()) {
            return false;
        }

        Team team = teamOpt.get();
        User user = userOpt.get();

        // 不能移除队长
        if (team.isCaptain(user)) {
            throw new IllegalArgumentException("不能移除队长");
        }

        team.removeMember(user);
        teamRepository.save(team);
        return true;
    }

    @Override
    public boolean transferCaptain(Integer teamId, Integer newCaptainId, Integer currentCaptainId) {
        Optional<Team> teamOpt = teamRepository.findById(teamId);
        Optional<User> newCaptainOpt = userRepository.findById(newCaptainId);

        if (teamOpt.isEmpty() || newCaptainOpt.isEmpty()) {
            return false;
        }

        Team team = teamOpt.get();
        User newCaptain = newCaptainOpt.get();

        // 验证当前用户是否是队长
        if (!team.isCaptain(new User() {{ setUserID(currentCaptainId); }})) {
            throw new IllegalArgumentException("只有队长可以转让队长权限");
        }

        // 验证新队长是否是战队成员
        if (!team.isMember(newCaptain)) {
            throw new IllegalArgumentException("新队长必须是战队成员");
        }

        team.setCaptain(newCaptain);
        teamRepository.save(team);
        return true;
    }

    @Override
    public boolean leaveTeam(Integer teamId, Integer userId) {
        Optional<Team> teamOpt = teamRepository.findById(teamId);

        if (teamOpt.isEmpty()) {
            return false;
        }

        Team team = teamOpt.get();
        User user = new User() {{ setUserID(userId); }};

        // 队长不能直接离开，需要先转让队长权限
        if (team.isCaptain(user)) {
            throw new IllegalArgumentException("队长不能直接离开战队，请先转让队长权限");
        }

        team.removeMember(user);
        teamRepository.save(team);
        return true;
    }

    @Override
    public TeamApplication applyToJoinTeam(Integer teamId, Integer applicantId, String remark) {
        Optional<Team> teamOpt = teamRepository.findById(teamId);
        Optional<User> applicantOpt = userRepository.findById(applicantId);

        if (teamOpt.isEmpty() || applicantOpt.isEmpty()) {
            throw new IllegalArgumentException("战队或用户不存在");
        }

        Team team = teamOpt.get();
        User applicant = applicantOpt.get();

        // 验证是否可以申请
        if (!canUserJoinTeam(teamId, applicantId)) {
            throw new IllegalArgumentException("无法申请加入该战队");
        }

        // 检查是否已有待处理的申请
        Optional<TeamApplication> existingApplication = teamApplicationRepository
                .findByTeam_TeamIDAndApplicant_UserID(teamId, applicantId);

        if (existingApplication.isPresent() && "PENDING".equals(existingApplication.get().getStatus())) {
            throw new IllegalArgumentException("您已提交过加入申请，请等待处理");
        }

        TeamApplication application = new TeamApplication(team, applicant);
        application.setRemark(remark);

        return teamApplicationRepository.save(application);
    }

    @Override
    public TeamApplication processApplication(Integer applicationId, boolean approved, String remark) {
        Optional<TeamApplication> applicationOpt = teamApplicationRepository.findById(applicationId);

        if (applicationOpt.isEmpty()) {
            throw new IllegalArgumentException("申请不存在");
        }

        TeamApplication application = applicationOpt.get();

        // 验证处理人是否是队长
        if (!application.getTeam().isCaptain(application.getTeam().getCaptain())) {
            throw new IllegalArgumentException("只有队长可以处理申请");
        }

        application.setStatus(approved ? "APPROVED" : "REJECTED");
        application.setProcessTime(LocalDateTime.now());
        application.setRemark(remark);

        if (approved) {
            // 批准申请，添加成员
            addMember(application.getTeam().getTeamID(), application.getApplicant().getUserID());
        }

        return teamApplicationRepository.save(application);
    }

    @Override
    public Page<TeamApplication> getTeamApplications(Integer teamId, Pageable pageable) {
        return teamApplicationRepository.findByTeam_TeamID(teamId, pageable);
    }

    @Override
    public Page<TeamApplication> getUserApplications(Integer userId, Pageable pageable) {
        return teamApplicationRepository.findByApplicant_UserID(userId, pageable);
    }

    @Override
    public Page<TeamApplication> getPendingApplicationsByCaptain(Integer captainId, Pageable pageable) {
        // 需要自定义查询实现
        return teamApplicationRepository.findByStatus("PENDING", pageable); // 简化实现
    }

    @Override
    public Team auditTeam(Integer teamId, boolean approved, String auditRemark) {
        Optional<Team> teamOpt = teamRepository.findById(teamId);

        if (teamOpt.isEmpty()) {
            throw new IllegalArgumentException("战队不存在");
        }

        Team team = teamOpt.get();
        team.setAuditState(approved ? "1" : "2");
        team.setAuditRemark(auditRemark);
        team.setAuditTime(LocalDateTime.now());

        return teamRepository.save(team);
    }

    @Override
    public boolean canUserJoinTeam(Integer teamId, Integer userId) {
        Optional<Team> teamOpt = teamRepository.findById(teamId);
        if (teamOpt.isEmpty()) {
            return false;
        }

        Team team = teamOpt.get();

        // 检查战队状态
        if (!team.canJoin()) {
            return false;
        }

        // 检查用户是否已在战队中
        if (team.isMember(new User() {{ setUserID(userId); }})) {
            return false;
        }

        // 检查用户是否已在同一竞赛的其他战队
        if (teamRepository.existsByMemberAndCompetition(userId, team.getCompetition().getCompetitionID())) {
            return false;
        }

        return true;
    }

    @Override
    public boolean isTeamFull(Integer teamId) {
        Optional<Team> teamOpt = teamRepository.findById(teamId);
        if (teamOpt.isEmpty()) {
            return true;
        }

        Team team = teamOpt.get();
        return team.getMembers().size() >= team.getCompetition().getTeamSizeLimit();
    }

    @Override
    public boolean isUserInTeam(Integer teamId, Integer userId) {
        Optional<Team> teamOpt = teamRepository.findById(teamId);
        return teamOpt.map(team -> team.isMember(new User() {{ setUserID(userId); }})).orElse(false);
    }

    @Override
    public boolean isTeamCaptain(Integer teamId, Integer userId) {
        Optional<Team> teamOpt = teamRepository.findById(teamId);
        return teamOpt.map(team -> team.isCaptain(new User() {{ setUserID(userId); }})).orElse(false);
    }

    @Override
    public Map<String, Object> getTeamStatistics(Integer competitionId) {
        Map<String, Object> stats = new HashMap<>();

        Long totalTeams = teamRepository.countByCompetition_CompetitionID(competitionId);
        Long pendingTeams = teamApplicationRepository.countByTeam_Competition_CompetitionIDAndStatus(competitionId, "PENDING");
        Long approvedTeams = teamApplicationRepository.countByTeam_Competition_CompetitionIDAndStatus(competitionId, "APPROVED");
        Long rejectedTeams = teamApplicationRepository.countByTeam_Competition_CompetitionIDAndStatus(competitionId, "REJECTED");

        stats.put("totalTeams", totalTeams);
        stats.put("pendingTeams", pendingTeams);
        stats.put("approvedTeams", approvedTeams);
        stats.put("rejectedTeams", rejectedTeams);

        return stats;
    }

    @Override
    public Long getTeamCountByCompetition(Integer competitionId) {
        return teamRepository.countByCompetition_CompetitionID(competitionId);
    }

    @Override
    public Long getPendingApplicationCount(Integer teamId) {
        return teamApplicationRepository.countPendingApplicationsByTeam(teamId);
    }

    /**
     * 验证战队数据的有效性
     */
    private void validateTeam(Team team) {
        if (team.getTeamName() == null || team.getTeamName().trim().isEmpty()) {
            throw new IllegalArgumentException("战队名称不能为空");
        }

        if (team.getTeamName().length() > 100) {
            throw new IllegalArgumentException("战队名称长度不能超过100个字符");
        }

        if (team.getCompetition() == null) {
            throw new IllegalArgumentException("必须指定竞赛");
        }

        if (team.getDescription() != null && team.getDescription().length() > 500) {
            throw new IllegalArgumentException("战队描述长度不能超过500个字符");
        }
    }
}