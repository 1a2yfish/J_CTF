package com.CTF.j_ctf.service.impl;

import com.CTF.j_ctf.entity.*;
import com.CTF.j_ctf.repository.*;
import com.CTF.j_ctf.service.AdminService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Service
@Transactional
public class AdminServiceImpl implements AdminService {
    private final UserRepository userRepository;
    private final TeamRepository teamRepository;
    private final CompetitionRepository competitionRepository;
    private final FlagRepository flagRepository;
    private final ScoreRepository scoreRepository;
    private final WriteUpRepository writeUpRepository;
    private final TeamApplicationRepository teamApplicationRepository;
    private final FlagSubmissionRepository flagSubmissionRepository;
    private final ChallengeRepository challengeRepository;
    private final TeamMemberRepository teamMemberRepository;

    public AdminServiceImpl(UserRepository userRepository,
                            TeamRepository teamRepository,
                            CompetitionRepository competitionRepository,
                            FlagRepository flagRepository,
                            ScoreRepository scoreRepository,
                            WriteUpRepository writeUpRepository,
                            TeamApplicationRepository teamApplicationRepository,
                            FlagSubmissionRepository flagSubmissionRepository,
                            ChallengeRepository challengeRepository,
                            TeamMemberRepository teamMemberRepository) {
        this.userRepository = userRepository;
        this.teamRepository = teamRepository;
        this.competitionRepository = competitionRepository;
        this.flagRepository = flagRepository;
        this.scoreRepository = scoreRepository;
        this.writeUpRepository = writeUpRepository;
        this.teamApplicationRepository = teamApplicationRepository;
        this.flagSubmissionRepository = flagSubmissionRepository;
        this.challengeRepository = challengeRepository;
        this.teamMemberRepository = teamMemberRepository;
    }

    // === 用户管理方法 ===

    @Override
    public Page<User> getAllUsers(Pageable pageable) {
        return userRepository.findAll(pageable);
    }

    @Override
    public Page<User> searchUsers(String keyword, Pageable pageable) {
        return userRepository.findByUserNameContaining(keyword, pageable);
    }

    @Override
    public User getUserDetails(Integer userId) {
        Optional<User> userOpt = userRepository.findById(userId);
        if (userOpt.isEmpty()) {
            throw new IllegalArgumentException("用户不存在");
        }
        return userOpt.get();
    }

    @Override
    @Transactional
    public User updateUser(User user) {
        Optional<User> userOpt = userRepository.findById(user.getUserID());
        if (userOpt.isEmpty()) {
            throw new IllegalArgumentException("用户不存在");
        }
        
        User existingUser = userOpt.get();
        
        // 更新用户名（如果提供且不同）
        if (user.getUserName() != null && !user.getUserName().equals(existingUser.getUserName())) {
            // 检查用户名是否已被其他用户使用
            Optional<User> userWithSameName = userRepository.findByUserName(user.getUserName());
            if (userWithSameName.isPresent() && !userWithSameName.get().getUserID().equals(existingUser.getUserID())) {
                throw new IllegalArgumentException("用户名已被使用");
            }
            existingUser.setUserName(user.getUserName());
        }
        
        // 更新邮箱（如果提供且不同，仅普通用户）
        if (user.getUserEmail() != null && !user.getUserEmail().equals(existingUser.getUserEmail())) {
            // 检查邮箱是否已被其他用户使用
            Optional<User> userWithSameEmail = userRepository.findByUserEmail(user.getUserEmail());
            if (userWithSameEmail.isPresent() && !userWithSameEmail.get().getUserID().equals(existingUser.getUserID())) {
                throw new IllegalArgumentException("邮箱已被使用");
            }
            existingUser.setUserEmail(user.getUserEmail());
        }
        
        // 更新手机号（如果提供且不同，仅普通用户）
        if (user.getPhoneNumber() != null && !user.getPhoneNumber().equals(existingUser.getPhoneNumber())) {
            // 检查手机号是否已被其他用户使用
            Optional<User> userWithSamePhone = userRepository.findByPhoneNumber(user.getPhoneNumber());
            if (userWithSamePhone.isPresent() && !userWithSamePhone.get().getUserID().equals(existingUser.getUserID())) {
                throw new IllegalArgumentException("手机号已被使用");
            }
            existingUser.setPhoneNumber(user.getPhoneNumber());
        }
        
        // 更新用户类型（如果提供）
        if (user.getUserType() != null) {
            existingUser.setUserType(user.getUserType());
        }
        
        // 更新管理员角色（如果提供且是管理员）
        if (user.getAdminRole() != null && existingUser.isAdministrator()) {
            existingUser.setAdminRole(user.getAdminRole());
        }
        
        // 更新用户状态（如果提供且是普通用户）
        if (user.getUserStatus() != null && existingUser.isOrdinaryUser()) {
            existingUser.setUserStatus(user.getUserStatus());
        }
        
        // 保存更新后的用户
        return userRepository.save(existingUser);
    }

    @Override
    public boolean disableUser(Integer userId) {
        Optional<User> userOpt = userRepository.findById(userId);
        if (userOpt.isPresent() && userOpt.get().isOrdinaryUser()) {
            User user = userOpt.get();
            user.setUserStatus(false);
            userRepository.save(user);
            return true;
        }
        return false;
    }

    @Override
    public boolean enableUser(Integer userId) {
        Optional<User> userOpt = userRepository.findById(userId);
        if (userOpt.isPresent() && userOpt.get().isOrdinaryUser()) {
            User user = userOpt.get();
            user.setUserStatus(true);
            userRepository.save(user);
            return true;
        }
        return false;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean deleteUser(Integer userId) {
        Optional<User> userOpt = userRepository.findById(userId);
        if (userOpt.isEmpty()) {
            return false;
        }
        
        // 1. 删除该用户的所有Flag提交记录
        flagSubmissionRepository.deleteByUser_UserID(userId);
        
        // 2. 删除该用户的所有得分记录
        List<Score> userScores = scoreRepository.findByUser_UserID(userId);
        if (userScores != null && !userScores.isEmpty()) {
            scoreRepository.deleteAll(userScores);
        }
        
        // 3. 处理该用户创建的团队（队长）
        List<Team> captainTeams = teamRepository.findByCaptainId(userId);
        if (captainTeams != null && !captainTeams.isEmpty()) {
            for (Team team : captainTeams) {
                // 先删除团队成员关系
                teamMemberRepository.deleteAllByTeamId(team.getTeamID());
                // 删除团队申请
                teamApplicationRepository.deleteByTeam_TeamID(team.getTeamID());
                // 删除团队
                teamRepository.delete(team);
            }
        }
        
        // 4. 删除该用户的所有团队成员关系
        List<TeamMember> userTeamMembers = teamMemberRepository.findByUserUserID(userId);
        if (userTeamMembers != null && !userTeamMembers.isEmpty()) {
            teamMemberRepository.deleteAll(userTeamMembers);
        }
        
        // 5. 删除该用户的团队申请记录
        teamApplicationRepository.deleteByUser_UserID(userId);
        
        // 6. 最后删除用户本身
        userRepository.deleteById(userId);
        
        return true;
    }

    // === 战队管理方法 ===

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
    public Team getTeamDetails(Integer teamId) {
        Optional<Team> teamOpt = teamRepository.findById(teamId);
        if (teamOpt.isEmpty()) {
            throw new IllegalArgumentException("战队不存在");
        }
        return teamOpt.get();
    }

    @Override
    public Team updateTeamAuditState(Integer teamId, String auditState, String auditRemark) {
        Optional<Team> teamOpt = teamRepository.findById(teamId);
        if (teamOpt.isEmpty()) {
            throw new IllegalArgumentException("战队不存在");
        }

        Team team = teamOpt.get();
        team.setAuditState(auditState);
        team.setAuditRemark(auditRemark);
        team.setAuditTime(LocalDateTime.now());
        return teamRepository.save(team);
    }

    @Override
    public boolean deleteTeam(Integer teamId) {
        if (teamRepository.existsById(teamId)) {
            teamRepository.deleteById(teamId);
            return true;
        }
        return false;
    }

    @Override
    public Map<String, Object> getTeamStatistics(Integer competitionId) {
        Map<String, Object> stats = new HashMap<>();

        // 获取待审核战队申请数量
        long pendingCount = teamApplicationRepository.countByTeam_Competition_CompetitionIDAndStatus(competitionId, "PENDING");
        // 获取已通过战队申请数量
        long approvedCount = teamApplicationRepository.countByTeam_Competition_CompetitionIDAndStatus(competitionId, "APPROVED");
        // 获取已拒绝战队申请数量
        long rejectedCount = teamApplicationRepository.countByTeam_Competition_CompetitionIDAndStatus(competitionId, "REJECTED");
        // 获取该竞赛下的战队申请总数
        long totalCount = teamApplicationRepository.countByTeam_Competition_CompetitionID(competitionId);

        stats.put("pendingCount", pendingCount);
        stats.put("approvedCount", approvedCount);
        stats.put("rejectedCount", rejectedCount);
        stats.put("totalCount", totalCount);

        return stats;
    }

    // === 竞赛管理方法 ===

    @Override
    public Page<Competition> getAllCompetitions(Pageable pageable) {
        return competitionRepository.findAll(pageable);
    }

    @Override
    public Page<Competition> getCompetitionsByStatus(String status, Pageable pageable) {
        LocalDateTime now = LocalDateTime.now();

        return switch (status.toLowerCase()) {
            case "ongoing" -> competitionRepository.findOngoingCompetitions(now, pageable);
            case "upcoming" -> competitionRepository.findUpcomingCompetitions(now, pageable);
            case "finished" -> competitionRepository.findFinishedCompetitions(now, pageable);
            default -> competitionRepository.findAll(pageable);
        };
    }

    @Override
    public Competition getCompetitionDetails(Integer competitionId) {
        Optional<Competition> competitionOpt = competitionRepository.findById(competitionId);
        if (competitionOpt.isEmpty()) {
            throw new IllegalArgumentException("竞赛不存在");
        }
        return competitionOpt.get();
    }

    @Override
    public Competition createCompetition(Competition competition) {
        // 验证竞赛数据
        validateCompetition(competition);

        competition.setPublishTime(LocalDateTime.now());
        return competitionRepository.save(competition);
    }

    @Override
    public Competition updateCompetition(Competition competition) {
        if (!competitionRepository.existsById(competition.getCompetitionID())) {
            throw new IllegalArgumentException("竞赛不存在");
        }

        validateCompetition(competition);
        return competitionRepository.save(competition);
    }

    @Override
    public Competition auditCompetition(Integer competitionId, boolean approved, String auditRemark) {
        Optional<Competition> competitionOpt = competitionRepository.findById(competitionId);
        if (competitionOpt.isEmpty()) {
            throw new IllegalArgumentException("竞赛不存在");
        }

        Competition competition = competitionOpt.get();
        competition.setAuditTime(LocalDateTime.now());
        competition.setAuditStatus(approved ? "APPROVED" : "REJECTED");
        competition.setAuditRemark(auditRemark);

        return competitionRepository.save(competition);
    }

    @Override
    public boolean deleteCompetition(Integer competitionId) {
        if (competitionRepository.existsById(competitionId)) {
            competitionRepository.deleteById(competitionId);
            return true;
        }
        return false;
    }

    @Override
    public Map<String, Object> getCompetitionStatistics(Integer competitionId) {
        Map<String, Object> stats = new HashMap<>();

        // 获取参赛队伍数量
        long teamCount = teamRepository.countByCompetition_CompetitionID(competitionId);
        // 获取提交的Flag数量
        long flagCount = flagSubmissionRepository.countTotalSubmissionsByCompetition(competitionId);
        // 获取提交的WriteUp数量
        long writeUpCount = writeUpRepository.countByCompetition_CompetitionID(competitionId);

        stats.put("teamCount", teamCount);
        stats.put("flagCount", flagCount);
        stats.put("writeUpCount", writeUpCount);

        return stats;
    }

    // === 系统统计方法 ===

    @Override
    public Map<String, Object> getSystemStatistics() {
        Map<String, Object> stats = new HashMap<>();

        // 总用户数
        long totalUsers = userRepository.count();
        // 总普通用户数
        long totalOrdinaryUsers = userRepository.findByUserType(User.UserType.ORDINARY).size();
        // 总管理员数
        long totalAdmins = userRepository.findByUserType(User.UserType.ADMIN).size();
        // 总竞赛数
        long totalCompetitions = competitionRepository.count();
        // 总战队数
        long totalTeams = teamRepository.count();
        // 总题目数
        long totalChallenges = challengeRepository.count();
        // 活跃竞赛数
        long activeCompetitions = competitionRepository.findOngoingCompetitions(LocalDateTime.now()).size();

        stats.put("totalUsers", totalUsers);
        stats.put("totalOrdinaryUsers", totalOrdinaryUsers);
        stats.put("totalAdmins", totalAdmins);
        stats.put("totalCompetitions", totalCompetitions);
        stats.put("totalTeams", totalTeams);
        stats.put("totalChallenges", totalChallenges);
        stats.put("activeCompetitions", activeCompetitions);

        return stats;
    }

    /**
     * 验证竞赛数据的有效性
     */
    private void validateCompetition(Competition competition) {
        if (competition.getEndTime().isBefore(competition.getStartTime())) {
            throw new IllegalArgumentException("结束时间不能早于开始时间");
        }

        if (competition.getTeamSizeLimit() <= 0) {
            throw new IllegalArgumentException("团队规模限制必须大于0");
        }

        if (competition.getTitle() == null || competition.getTitle().trim().isEmpty()) {
            throw new IllegalArgumentException("竞赛标题不能为空");
        }

        if (competition.getIntroduction() == null || competition.getIntroduction().trim().isEmpty()) {
            throw new IllegalArgumentException("竞赛介绍不能为空");
        }
    }
}