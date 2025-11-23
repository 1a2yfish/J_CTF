package com.CTF.j_ctf.service.impl;

import com.CTF.j_ctf.entity.*;
import com.CTF.j_ctf.repository.*;
import com.CTF.j_ctf.service.TeamService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import org.springframework.data.domain.PageImpl;

@Service
@Transactional
public class TeamServiceImpl implements TeamService {
    private final TeamRepository teamRepository;
    private final TeamApplicationRepository teamApplicationRepository;
    private final UserRepository userRepository;
    private final CompetitionRepository competitionRepository;
    private final TeamMemberRepository teamMemberRepository;

    public TeamServiceImpl(TeamRepository teamRepository,
                           TeamApplicationRepository teamApplicationRepository,
                           UserRepository userRepository,
                           CompetitionRepository competitionRepository,
                           TeamMemberRepository teamMemberRepository) {
        this.teamRepository = teamRepository;
        this.teamApplicationRepository = teamApplicationRepository;
        this.userRepository = userRepository;
        this.competitionRepository = competitionRepository;
        this.teamMemberRepository = teamMemberRepository;
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

        // 保存战队
        Team savedTeam = teamRepository.save(team);

        // 添加队长为成员
        TeamMember captainMember = new TeamMember(savedTeam, captain);
        teamMemberRepository.save(captainMember);

        return savedTeam;
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
    public boolean disbandTeam(Integer teamId) {
        return deleteTeam(teamId);
    }

    @Override
    public boolean deleteTeam(Integer teamId) {
        if (!teamRepository.existsById(teamId)) {
            return false;
        }

        // 先删除相关数据
        teamMemberRepository.deleteAllByTeamId(teamId);
        teamApplicationRepository.deleteByTeam_TeamID(teamId);

        teamRepository.deleteById(teamId);
        return true;
    }

    @Override
    public Optional<Team> getTeamById(Integer teamId) {
        return teamRepository.findById(teamId);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<Team> getTeamByIdWithMembers(Integer teamId) {
        return teamRepository.findByIdWithMembers(teamId);
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
        return teamRepository.findByCaptain_UserID(captainId, pageable);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<Team> getTeamsByMember(Integer memberId, Pageable pageable) {
        // 获取用户加入的所有团队（已使用JOIN FETCH预加载关联数据）
        List<Team> teams = teamRepository.findByMemberId(memberId);
        System.out.println("用户 " + memberId + " 加入的团队总数: " + teams.size());
        
        // 在事务内初始化LAZY关联，避免后续序列化问题
        for (Team team : teams) {
            // 触发teamMembers的加载（如果还未加载）
            if (team.getTeamMembers() != null) {
                team.getTeamMembers().size(); // 触发LAZY加载
                // 初始化每个成员的user关联
                for (com.CTF.j_ctf.entity.TeamMember member : team.getTeamMembers()) {
                    if (member.getUser() != null) {
                        member.getUser().getUserName(); // 触发LAZY加载
                    }
                }
            }
        }
        
        // 手动实现分页
        int start = (int) pageable.getOffset();
        int end = Math.min((start + pageable.getPageSize()), teams.size());
        List<Team> pagedTeams = start < teams.size() ? teams.subList(start, end) : new ArrayList<>();
        System.out.println("分页结果: start=" + start + ", end=" + end + ", 返回数量=" + pagedTeams.size());
        return new PageImpl<>(pagedTeams, pageable, teams.size());
    }

    @Override
    public Page<Team> searchTeams(String keyword, Pageable pageable) {
        return teamRepository.findByTeamNameContaining(keyword, pageable);
    }

    @Override
    public Page<Team> searchTeamsByCompetition(String keyword, Integer competitionId, Pageable pageable) {
        return teamRepository.findByTeamNameContainingAndCompetition_CompetitionID(keyword, competitionId, pageable);
    }

    @Override
    public Optional<Team> getUserTeamInCompetition(Integer userId, Integer competitionId) {
        return teamRepository.findByMemberAndCompetition(userId, competitionId);
    }

    @Override
    public Optional<Team> getUserCurrentTeam(Integer userId) {
        return teamRepository.findLatestByMember(userId);
    }

    @Override
    public boolean isUserCompetitionCreator(Integer competitionId, Integer userId) {
        Optional<Competition> competitionOpt = competitionRepository.findById(competitionId);
        return competitionOpt.map(competition ->
                competition.getCreator() != null &&
                        competition.getCreator().getUserID().equals(userId)
        ).orElse(false);
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

        // 检查是否已经是成员
        if (teamMemberRepository.existsByTeamTeamIDAndUserUserID(teamId, userId)) {
            return false;
        }

        TeamMember member = new TeamMember(team, user);
        teamMemberRepository.save(member);
        return true;
    }

    @Override
    public boolean removeMember(Integer teamId, Integer memberId) {
        Optional<Team> teamOpt = teamRepository.findById(teamId);
        Optional<User> userOpt = userRepository.findById(memberId);

        if (teamOpt.isEmpty() || userOpt.isEmpty()) {
            return false;
        }

        Team team = teamOpt.get();
        User user = userOpt.get();

        // 不能移除队长
        if (team.isCaptain(user)) {
            throw new IllegalArgumentException("不能移除队长");
        }

        // 检查是否是成员
        if (!teamMemberRepository.existsByTeamTeamIDAndUserUserID(teamId, memberId)) {
            return false;
        }

        teamMemberRepository.deleteByTeamAndUser(teamId, memberId);
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
        if (!team.isCaptain(createUserWithId(currentCaptainId))) {
            throw new IllegalArgumentException("只有队长可以转让队长权限");
        }

        // 验证新队长是否是战队成员
        if (!teamMemberRepository.existsByTeamTeamIDAndUserUserID(teamId, newCaptainId)) {
            throw new IllegalArgumentException("新队长必须是战队成员");
        }

        team.setCaptain(newCaptain);
        teamRepository.save(team);
        return true;
    }

    @Override
    public boolean leaveTeam(Integer teamId, Integer userId) {
        Optional<Team> teamOpt = teamRepository.findById(teamId);
        Optional<User> userOpt = userRepository.findById(userId);

        if (teamOpt.isEmpty() || userOpt.isEmpty()) {
            return false;
        }

        Team team = teamOpt.get();
        User user = userOpt.get();

        // 队长不能直接离开，需要先转让队长权限
        if (team.isCaptain(user)) {
            throw new IllegalArgumentException("队长不能直接离开战队，请先转让队长权限");
        }

        // 检查是否是成员
        if (!teamMemberRepository.existsByTeamTeamIDAndUserUserID(teamId, userId)) {
            return false;
        }

        teamMemberRepository.deleteByTeamAndUser(teamId, userId);
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
    public Page<TeamApplication> getTeamApplicationsByStatus(Integer teamId, String status, Pageable pageable) {
        return teamApplicationRepository.findByTeamAndStatus(teamId, status, pageable);
    }

    @Override
    public Optional<TeamApplication> getApplicationById(Integer applicationId) {
        return teamApplicationRepository.findById(applicationId);
    }

    @Override
    public TeamApplication inviteUser(Integer teamId, Integer targetUserId, Integer inviterId) {
        Optional<Team> teamOpt = teamRepository.findById(teamId);
        Optional<User> targetUserOpt = userRepository.findById(targetUserId);
        Optional<User> inviterOpt = userRepository.findById(inviterId);

        if (teamOpt.isEmpty() || targetUserOpt.isEmpty() || inviterOpt.isEmpty()) {
            throw new IllegalArgumentException("战队或用户不存在");
        }

        Team team = teamOpt.get();
        User targetUser = targetUserOpt.get();
        User inviter = inviterOpt.get();

        // 验证邀请权限
        if (!team.isCaptain(inviter)) {
            throw new IllegalArgumentException("只有队长可以邀请用户");
        }

        // 验证目标用户是否可以加入
        if (!canUserJoinTeam(teamId, targetUserId)) {
            throw new IllegalArgumentException("目标用户无法加入该战队");
        }

        // 检查是否已有待处理的邀请
        Optional<TeamApplication> existingInvitation = teamApplicationRepository
                .findByTeamAndApplicantAndStatus(team, targetUser, "PENDING");

        if (existingInvitation.isPresent()) {
            throw new IllegalArgumentException("已向该用户发送过邀请，请等待处理");
        }

        TeamApplication invitation = new TeamApplication(team, targetUser);
        invitation.setRemark("队长 " + inviter.getUserName() + " 邀请您加入战队");

        return teamApplicationRepository.save(invitation);
    }

    @Override
    public boolean canUserJoinTeam(Integer teamId, Integer userId) {
        Optional<Team> teamOpt = teamRepository.findById(teamId);
        if (teamOpt.isEmpty()) {
            return false;
        }

        Team team = teamOpt.get();

        // 检查战队状态
        if (!team.isAuditApproved()) {
            return false;
        }

        // 检查竞赛状态
        if (!team.getCompetition().isOngoing()) {
            return false;
        }

        // 检查战队人数
        Long memberCount = teamMemberRepository.countByTeamTeamID(teamId);
        if (memberCount >= team.getCompetition().getTeamSizeLimit()) {
            return false;
        }

        // 检查用户是否已在战队中
        if (teamMemberRepository.existsByTeamTeamIDAndUserUserID(teamId, userId)) {
            return false;
        }

        // 检查用户是否已在同一竞赛的其他战队
        if (teamRepository.existsByMemberAndCompetition(userId, team.getCompetition().getCompetitionID())) {
            return false;
        }

        return true;
    }

    @Override
    public String getJoinRestrictionReason(Integer teamId, Integer userId) {
        Optional<Team> teamOpt = teamRepository.findById(teamId);
        if (teamOpt.isEmpty()) {
            return "战队不存在";
        }

        Team team = teamOpt.get();

        // 检查战队状态
        if (!team.isAuditApproved()) {
            return "战队尚未通过审核";
        }

        // 检查竞赛状态
        if (!team.getCompetition().isOngoing()) {
            return "竞赛已结束或未开始";
        }

        // 检查战队人数
        Long memberCount = teamMemberRepository.countByTeamTeamID(teamId);
        if (memberCount >= team.getCompetition().getTeamSizeLimit()) {
            return "战队人数已满";
        }

        // 检查用户是否已在战队中
        if (teamMemberRepository.existsByTeamTeamIDAndUserUserID(teamId, userId)) {
            return "您已是该战队成员";
        }

        // 检查用户是否已在同一竞赛的其他战队
        if (teamRepository.existsByMemberAndCompetition(userId, team.getCompetition().getCompetitionID())) {
            return "您已加入该竞赛的其他战队";
        }

        return "可以加入";
    }

    @Override
    public boolean isTeamMember(Integer teamId, Integer userId) {
        return teamMemberRepository.existsByTeamTeamIDAndUserUserID(teamId, userId);
    }

    @Override
    public boolean isTeamCaptain(Integer teamId, Integer userId) {
        Optional<Team> teamOpt = teamRepository.findById(teamId);
        return teamOpt.map(team -> team.isCaptain(createUserWithId(userId))).orElse(false);
    }

    @Override
    public Team auditTeam(Integer teamId, String auditState, String auditRemark) {
        Optional<Team> teamOpt = teamRepository.findById(teamId);
        if (teamOpt.isEmpty()) {
            throw new IllegalArgumentException("战队不存在");
        }

        Team team = teamOpt.get();

        switch (auditState) {
            case "1":
                team.approve(auditRemark);
                break;
            case "2":
                team.reject(auditRemark);
                break;
            default:
                throw new IllegalArgumentException("无效的审核状态");
        }

        return teamRepository.save(team);
    }

    @Override
    public Map<String, Object> getTeamStatistics(Integer teamId) {
        Optional<Team> teamOpt = teamRepository.findById(teamId);
        if (teamOpt.isEmpty()) {
            throw new IllegalArgumentException("战队不存在");
        }

        Team team = teamOpt.get();
        Map<String, Object> stats = new HashMap<>();

        // 基础信息
        stats.put("teamName", team.getTeamName());
        stats.put("captainName", team.getCaptain().getUserName());
        stats.put("currentSize", teamMemberRepository.countByTeamTeamID(teamId));
        stats.put("maxSize", team.getCompetition().getTeamSizeLimit());
        stats.put("auditState", team.getAuditState());
        stats.put("creationTime", team.getCreationTime());

        // 申请统计
        Long pendingApplications = teamApplicationRepository.countByTeamAndStatus(team, "PENDING");
        Long totalApplications = teamApplicationRepository.countByTeam(team);
        stats.put("pendingApplications", pendingApplications);
        stats.put("totalApplications", totalApplications);

        return stats;
    }

    // 以下是不在Controller中使用的方法，提供基本实现

    @Override
    public Page<TeamApplication> getPendingApplicationsByCaptain(Integer captainId, Pageable pageable) {
        return teamApplicationRepository.findByStatus("PENDING", pageable);
    }

    @Override
    public boolean isTeamFull(Integer teamId) {
        Optional<Team> teamOpt = teamRepository.findById(teamId);
        if (teamOpt.isEmpty()) {
            return true;
        }

        Team team = teamOpt.get();
        Long memberCount = teamMemberRepository.countByTeamTeamID(teamId);
        return memberCount >= team.getCompetition().getTeamSizeLimit();
    }

    @Override
    public boolean isUserInTeam(Integer teamId, Integer userId) {
        return isTeamMember(teamId, userId);
    }

    @Override
    public Long getTeamCountByCompetition(Integer competitionId) {
        return teamRepository.countByCompetition_CompetitionID(competitionId);
    }

    @Override
    public Long getPendingApplicationCount(Integer teamId) {
        return teamApplicationRepository.countPendingApplicationsByTeam(teamId);
    }

    // 辅助方法
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

    private User createUserWithId(Integer userId) {
        User user = new User();
        user.setUserID(userId);
        return user;
    }
}