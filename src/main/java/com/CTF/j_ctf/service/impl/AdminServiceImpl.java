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
import java.util.Map;
import java.util.Optional;

@Service
@Transactional
public class AdminServiceImpl implements AdminService {
    private final OrdinaryUserRepository ordinaryUserRepository;
    private final TeamRepository teamRepository;
    private final CompetitionRepository competitionRepository;
    private final FlagRepository flagRepository;
    private final ScoreRepository scoreRepository;
    private final WriteUpRepository writeUpRepository;
    private TeamApplicationRepository teamApplicationRepository;
    private FlagSubmissionRepository flagSubmissionRepository;

    public AdminServiceImpl(OrdinaryUserRepository ordinaryUserRepository,
                            TeamRepository teamRepository,
                            CompetitionRepository competitionRepository,
                            FlagRepository flagRepository,
                            ScoreRepository scoreRepository,
                            WriteUpRepository writeUpRepository) {
        this.ordinaryUserRepository = ordinaryUserRepository;
        this.teamRepository = teamRepository;
        this.competitionRepository = competitionRepository;
        this.flagRepository = flagRepository;
        this.scoreRepository = scoreRepository;
        this.writeUpRepository = writeUpRepository;
    }

    // === 用户管理方法 ===

    @Override
    public Page<OrdinaryUser> getAllUsers(Pageable pageable) {
        return ordinaryUserRepository.findAll(pageable);
    }

    @Override
    public Page<OrdinaryUser> searchUsers(String keyword, Pageable pageable) {
        return ordinaryUserRepository.findByUserNameContaining(keyword, pageable);
    }

    @Override
    public OrdinaryUser getUserDetails(Integer userId) {
        Optional<OrdinaryUser> userOpt = ordinaryUserRepository.findById(userId);
        if (userOpt.isEmpty()) {
            throw new IllegalArgumentException("用户不存在");
        }
        return userOpt.get();
    }

    @Override
    public OrdinaryUser updateUser(OrdinaryUser user) {
        if (!ordinaryUserRepository.existsById(user.getUserID())) {
            throw new IllegalArgumentException("用户不存在");
        }
        return ordinaryUserRepository.save(user);
    }

    @Override
    public boolean disableUser(Integer userId) {
        Optional<OrdinaryUser> userOpt = ordinaryUserRepository.findById(userId);
        if (userOpt.isPresent()) {
            // 这里可以实现用户禁用逻辑，比如设置状态字段
            // 当前简单返回成功
            return true;
        }
        return false;
    }

    @Override
    public boolean enableUser(Integer userId) {
        Optional<OrdinaryUser> userOpt = ordinaryUserRepository.findById(userId);
        if (userOpt.isPresent()) {
            // 启用用户逻辑
            return true;
        }
        return false;
    }

    @Override
    public boolean deleteUser(Integer userId) {
        if (ordinaryUserRepository.existsById(userId)) {
            ordinaryUserRepository.deleteById(userId);
            return true;
        }
        return false;
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
        // 可以添加审核备注字段到Team实体中
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
        // 可以添加审核状态字段到Competition实体中

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
        // 获取已使用的Flag数量
        long usedFlagCount = flagSubmissionRepository.countCorrectSubmissionsByChallenge(competitionId);
        // 获取提交的WriteUp数量
        long writeUpCount = writeUpRepository.countByCompetition_CompetitionID(competitionId);

        stats.put("teamCount", teamCount);
        stats.put("flagCount", flagCount);
        stats.put("usedFlagCount", usedFlagCount);
        stats.put("writeUpCount", writeUpCount);

        return stats;
    }

    // === 系统统计方法 ===

    @Override
    public Map<String, Object> getSystemStatistics() {
        Map<String, Object> stats = new HashMap<>();

        // 总用户数
        long totalUsers = ordinaryUserRepository.count();
        // 总竞赛数
        long totalCompetitions = competitionRepository.count();
        // 总战队数
        long totalTeams = teamRepository.count();
        // 今日新增用户（需要扩展实体添加创建时间）

        stats.put("totalUsers", totalUsers);
        stats.put("totalCompetitions", totalCompetitions);
        stats.put("totalTeams", totalTeams);
        stats.put("activeCompetitions", competitionRepository.findOngoingCompetitions(LocalDateTime.now()).size());

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