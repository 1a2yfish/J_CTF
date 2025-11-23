package com.CTF.j_ctf.service.impl;

import com.CTF.j_ctf.entity.Competition;
import com.CTF.j_ctf.entity.User;
import com.CTF.j_ctf.repository.CompetitionRepository;
import com.CTF.j_ctf.repository.TeamRepository;
import com.CTF.j_ctf.repository.UserRepository;
import com.CTF.j_ctf.service.CompetitionService;
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
public class CompetitionServiceImpl implements CompetitionService {
    private final CompetitionRepository competitionRepository;
    private final UserRepository userRepository;
    private final TeamRepository teamRepository;

    public CompetitionServiceImpl(CompetitionRepository competitionRepository,
                                  UserRepository userRepository,
                                  TeamRepository teamRepository) {
        this.competitionRepository = competitionRepository;
        this.userRepository = userRepository;
        this.teamRepository = teamRepository;
    }

    @Override
    public Competition createCompetition(Competition competition) {
        // 验证数据
        validateCompetition(competition);

        // 使用实体的默认值，不需要重复设置
        return competitionRepository.save(competition);
    }

    @Override
    public Competition updateCompetition(Competition competition) {
        Optional<Competition> existingOpt = competitionRepository.findById(competition.getCompetitionID());
        if (existingOpt.isEmpty()) {
            throw new IllegalArgumentException("竞赛不存在");
        }

        Competition existing = existingOpt.get();

        // 只有草稿或待审核状态的竞赛可以修改基本信息
        if (!"DRAFT".equals(existing.getStatus()) && !"PENDING".equals(existing.getAuditStatus())) {
            throw new IllegalArgumentException("只有草稿或待审核状态的竞赛可以修改");
        }

        // 更新允许修改的字段
        existing.setTitle(competition.getTitle());
        existing.setIntroduction(competition.getIntroduction());
        existing.setTeamSizeLimit(competition.getTeamSizeLimit());
        existing.setStartTime(competition.getStartTime());
        existing.setEndTime(competition.getEndTime());
        existing.setMaxTeams(competition.getMaxTeams());
        existing.setIsPublic(competition.getIsPublic());

        validateCompetition(existing);

        return competitionRepository.save(existing);
    }

    @Override
    public Competition publishCompetition(Integer competitionId) {
        Optional<Competition> competitionOpt = competitionRepository.findById(competitionId);
        if (competitionOpt.isEmpty()) {
            throw new IllegalArgumentException("竞赛不存在");
        }

        Competition competition = competitionOpt.get();

        // 检查是否可以发布
        if (!"DRAFT".equals(competition.getStatus())) {
            throw new IllegalArgumentException("只有草稿状态的竞赛可以发布");
        }

        if (competition.getStartTime().isBefore(LocalDateTime.now())) {
            throw new IllegalArgumentException("开始时间不能早于当前时间");
        }

        // 检查审核状态
        if (!"APPROVED".equals(competition.getAuditStatus())) {
            throw new IllegalArgumentException("竞赛需要先通过审核才能发布");
        }

        competition.setStatus("PUBLISHED");
        competition.setPublishTime(LocalDateTime.now());

        return competitionRepository.save(competition);
    }

    @Override
    public Competition cancelCompetition(Integer competitionId) {
        Optional<Competition> competitionOpt = competitionRepository.findById(competitionId);
        if (competitionOpt.isEmpty()) {
            throw new IllegalArgumentException("竞赛不存在");
        }

        Competition competition = competitionOpt.get();

        // 检查是否可以取消
        if ("FINISHED".equals(competition.getStatus()) || "CANCELLED".equals(competition.getStatus())) {
            throw new IllegalArgumentException("竞赛已结束或已取消");
        }

        competition.setStatus("CANCELLED");

        return competitionRepository.save(competition);
    }

    @Override
    public Optional<Competition> getCompetitionById(Integer competitionId) {
        return competitionRepository.findById(competitionId);
    }

    @Override
    public Page<Competition> getAllCompetitions(Pageable pageable) {
        return competitionRepository.findAll(pageable);
    }

    @Override
    public Page<Competition> getCompetitionsByCreator(Integer userId, Pageable pageable) {
        return competitionRepository.findByCreator_UserID(userId, pageable);
    }

    @Override
    public Page<Competition> getPublicCompetitions(Pageable pageable) {
        return competitionRepository.findByIsPublicTrueAndStatus("PUBLISHED", pageable);
    }

    @Override
    public Page<Competition> getOngoingCompetitions(Pageable pageable) {
        // 使用实体的 isOngoing() 方法逻辑
        LocalDateTime now = LocalDateTime.now();
        return competitionRepository.findByStatusAndStartTimeBeforeAndEndTimeAfter("PUBLISHED", now, now, pageable);
    }

    @Override
    public Page<Competition> getUpcomingCompetitions(Pageable pageable) {
        // 使用实体的 isUpcoming() 方法逻辑
        LocalDateTime now = LocalDateTime.now();
        return competitionRepository.findByStatusAndStartTimeAfter("PUBLISHED", now, pageable);
    }

    @Override
    public Page<Competition> getFinishedCompetitions(Pageable pageable) {
        // 使用实体的 isFinished() 方法逻辑
        LocalDateTime now = LocalDateTime.now();
        return competitionRepository.findByStatusAndEndTimeBefore("PUBLISHED", now, pageable);
    }

    @Override
    public Page<Competition> searchCompetitions(String keyword, Pageable pageable) {
        return competitionRepository.findByTitleContainingOrIntroductionContaining(keyword, keyword, pageable);
    }

    @Override
    public Page<Competition> searchPublicCompetitions(String keyword, Pageable pageable) {
        return competitionRepository.findPublicCompetitionsByKeyword(keyword, pageable);
    }

    @Override
    public boolean isCompetitionOngoing(Integer competitionId) {
        Optional<Competition> competitionOpt = competitionRepository.findById(competitionId);
        return competitionOpt.map(Competition::isOngoing).orElse(false);
    }

    @Override
    public boolean isCompetitionFinished(Integer competitionId) {
        Optional<Competition> competitionOpt = competitionRepository.findById(competitionId);
        return competitionOpt.map(Competition::isFinished).orElse(false);
    }

    @Override
    public boolean isCompetitionPublished(Integer competitionId) {
        Optional<Competition> competitionOpt = competitionRepository.findById(competitionId);
        return competitionOpt.map(Competition::isPublished).orElse(false);
    }

    @Override
    public boolean canUserJoinCompetition(Integer competitionId, Integer userId) {
        Optional<Competition> competitionOpt = competitionRepository.findById(competitionId);
        if (competitionOpt.isEmpty()) {
            return false;
        }

        Competition competition = competitionOpt.get();

        // 使用实体的 canJoin() 方法
        if (!competition.canJoin()) {
            return false;
        }

        // 检查用户是否是创建者
        if (competition.getCreator().getUserID().equals(userId)) {
            return false;
        }

        // 检查用户是否已经参加了该竞赛（通过队伍）
        boolean isAlreadyParticipant = teamRepository.existsByCompetition_CompetitionIDAndMembers_UserID(
                competitionId, userId);
        if (isAlreadyParticipant) {
            return false;
        }

        // 检查队伍数量限制
        if (competition.getMaxTeams() != null) {
            long currentTeamCount = teamRepository.countByCompetition_CompetitionID(competitionId);
            if (currentTeamCount >= competition.getMaxTeams()) {
                return false;
            }
        }

        return true;
    }

    @Override
    public String getJoinRestrictionReason(Integer competitionId, Integer userId) {
        Optional<Competition> competitionOpt = competitionRepository.findById(competitionId);
        if (competitionOpt.isEmpty()) {
            return "竞赛不存在";
        }

        Competition competition = competitionOpt.get();

        // 使用实体的 canJoin() 方法检查基本条件
        if (!competition.canJoin()) {
            if (!competition.isPublished()) {
                return "竞赛未发布";
            }
            if (!competition.isUpcoming()) {
                return "竞赛已开始或已结束";
            }
            if (!competition.getIsPublic()) {
                return "竞赛不公开";
            }
        }

        // 检查用户是否是创建者
        if (competition.getCreator().getUserID().equals(userId)) {
            return "创建者不能参加自己的竞赛";
        }

        // 检查用户是否已经参加了该竞赛
        boolean isAlreadyParticipant = teamRepository.existsByCompetition_CompetitionIDAndMembers_UserID(
                competitionId, userId);
        if (isAlreadyParticipant) {
            return "您已经参加了该竞赛";
        }

        // 检查队伍数量限制
        if (competition.getMaxTeams() != null) {
            long currentTeamCount = teamRepository.countByCompetition_CompetitionID(competitionId);
            if (currentTeamCount >= competition.getMaxTeams()) {
                return "竞赛队伍数量已达上限";
            }
        }

        return "可以参加";
    }

    @Override
    public boolean joinCompetition(Integer competitionId, Integer userId) {
        // 检查是否可以参加
        if (!canUserJoinCompetition(competitionId, userId)) {
            return false;
        }

        // 这里需要实现具体的参加逻辑
        // 通常用户需要创建或加入一个队伍来参加竞赛
        // 这里简化处理，返回true表示可以参加
        return true;
    }

    @Override
    public boolean leaveCompetition(Integer competitionId, Integer userId) {
        // 检查用户是否参加了该竞赛
        boolean isParticipant = teamRepository.existsByCompetition_CompetitionIDAndMembers_UserID(
                competitionId, userId);

        if (!isParticipant) {
            return false;
        }

        // 这里需要实现具体的退出逻辑
        // 通常是从队伍中移除用户
        // 这里简化处理，返回true表示可以退出
        return true;
    }

    @Override
    public boolean isUserParticipant(Integer competitionId, Integer userId) {
        return teamRepository.existsByCompetition_CompetitionIDAndMembers_UserID(competitionId, userId);
    }

    @Override
    public boolean isUserCreator(Integer competitionId, Integer userId) {
        Optional<Competition> competitionOpt = competitionRepository.findById(competitionId);
        return competitionOpt.map(competition ->
                competition.getCreator().getUserID().equals(userId)
        ).orElse(false);
    }

    @Override
    public Map<String, Object> getCompetitionStatistics(Integer competitionId) {
        Map<String, Object> stats = new HashMap<>();

        Optional<Competition> competitionOpt = competitionRepository.findById(competitionId);
        if (competitionOpt.isEmpty()) {
            return stats;
        }

        Competition competition = competitionOpt.get();

        // 获取参赛队伍数量
        long teamCount = teamRepository.countByCompetition_CompetitionID(competitionId);
        stats.put("teamCount", teamCount);

        // 获取参赛用户数量
        long participantCount = teamRepository.countParticipantsByCompetition(competitionId);
        stats.put("participantCount", participantCount);

        // 获取竞赛状态信息
        stats.put("status", competition.getStatus());
        stats.put("auditStatus", competition.getAuditStatus());
        stats.put("startTime", competition.getStartTime());
        stats.put("endTime", competition.getEndTime());
        stats.put("publishTime", competition.getPublishTime());
        stats.put("isPublic", competition.getIsPublic());
        stats.put("teamSizeLimit", competition.getTeamSizeLimit());
        stats.put("maxTeams", competition.getMaxTeams());

        // 使用实体的业务方法
        stats.put("isOngoing", competition.isOngoing());
        stats.put("isUpcoming", competition.isUpcoming());
        stats.put("isFinished", competition.isFinished());
        stats.put("isPublished", competition.isPublished());
        stats.put("canJoin", competition.canJoin());

        // 获取队伍审核状态统计
        Map<String, Long> teamAuditStats = new HashMap<>();
        teamAuditStats.put("PENDING", teamRepository.countByCompetition_CompetitionIDAndAuditState(competitionId, "PENDING"));
        teamAuditStats.put("APPROVED", teamRepository.countByCompetition_CompetitionIDAndAuditState(competitionId, "APPROVED"));
        teamAuditStats.put("REJECTED", teamRepository.countByCompetition_CompetitionIDAndAuditState(competitionId, "REJECTED"));
        stats.put("teamAuditStats", teamAuditStats);

        return stats;
    }

    @Override
    public boolean deleteCompetition(Integer competitionId) {
        if (!competitionRepository.existsById(competitionId)) {
            return false;
        }

        // 检查竞赛状态，只有草稿或已取消的竞赛可以删除
        Optional<Competition> competitionOpt = competitionRepository.findById(competitionId);
        if (competitionOpt.isPresent()) {
            Competition competition = competitionOpt.get();
            if ("PUBLISHED".equals(competition.getStatus()) ||
                    "ONGOING".equals(competition.getStatus())) {
                throw new IllegalArgumentException("已发布或进行中的竞赛不能删除");
            }
        }

        competitionRepository.deleteById(competitionId);
        return true;
    }

    @Override
    public Competition auditCompetition(Integer competitionId, boolean approved, String remark) {
        Optional<Competition> competitionOpt = competitionRepository.findById(competitionId);
        if (competitionOpt.isEmpty()) {
            throw new IllegalArgumentException("竞赛不存在");
        }

        Competition competition = competitionOpt.get();
        competition.setAuditStatus(approved ? "APPROVED" : "REJECTED");
        competition.setAuditRemark(remark);
        competition.setAuditTime(LocalDateTime.now());

        return competitionRepository.save(competition);
    }

    @Override
    public Long getCompetitionCountByUser(Integer userId) {
        return competitionRepository.countByCreator_UserID(userId);
    }

    @Override
    public Long getCompetitionCountByStatus(String status) {
        return competitionRepository.countByStatus(status);
    }

    @Override
    public Competition startCompetition(Integer competitionId) {
        Optional<Competition> competitionOpt = competitionRepository.findById(competitionId);
        if (competitionOpt.isEmpty()) {
            throw new IllegalArgumentException("竞赛不存在");
        }

        Competition competition = competitionOpt.get();

        // 检查是否可以开始（必须是已发布状态且开始时间已到）
        if (!"PUBLISHED".equals(competition.getStatus())) {
            throw new IllegalArgumentException("只有已发布的竞赛可以开始");
        }

        if (competition.getStartTime().isAfter(LocalDateTime.now())) {
            throw new IllegalArgumentException("竞赛开始时间未到");
        }

        competition.setStatus("ONGOING");
        return competitionRepository.save(competition);
    }

    @Override
    public Competition finishCompetition(Integer competitionId) {
        Optional<Competition> competitionOpt = competitionRepository.findById(competitionId);
        if (competitionOpt.isEmpty()) {
            throw new IllegalArgumentException("竞赛不存在");
        }

        Competition competition = competitionOpt.get();

        // 检查是否可以结束（必须是进行中状态且结束时间已到）
        if (!"ONGOING".equals(competition.getStatus())) {
            throw new IllegalArgumentException("只有进行中的竞赛可以结束");
        }

        if (competition.getEndTime().isAfter(LocalDateTime.now())) {
            throw new IllegalArgumentException("竞赛结束时间未到");
        }

        competition.setStatus("FINISHED");
        return competitionRepository.save(competition);
    }

    /**
     * 验证竞赛数据的有效性
     */
    private void validateCompetition(Competition competition) {
        if (competition.getEndTime().isBefore(competition.getStartTime())) {
            throw new IllegalArgumentException("结束时间不能早于开始时间");
        }

        if (competition.getTeamSizeLimit() == null || competition.getTeamSizeLimit() <= 0) {
            throw new IllegalArgumentException("团队规模限制必须大于0");
        }

        if (competition.getTitle() == null || competition.getTitle().trim().isEmpty()) {
            throw new IllegalArgumentException("竞赛标题不能为空");
        }

        if (competition.getIntroduction() == null || competition.getIntroduction().trim().isEmpty()) {
            throw new IllegalArgumentException("竞赛介绍不能为空");
        }

        if (competition.getMaxTeams() != null && competition.getMaxTeams() <= 0) {
            throw new IllegalArgumentException("最大队伍数必须大于0");
        }

        // 检查创建者是否存在
        if (competition.getCreator() == null || competition.getCreator().getUserID() == null) {
            throw new IllegalArgumentException("竞赛创建者不能为空");
        }

        Optional<User> creatorOpt = userRepository.findById(competition.getCreator().getUserID());
        if (creatorOpt.isEmpty()) {
            throw new IllegalArgumentException("创建者用户不存在");
        }

        // 检查标题是否重复（排除自身）
        if (competition.getCompetitionID() == null) {
            // 新增
            if (competitionRepository.existsByTitle(competition.getTitle())) {
                throw new IllegalArgumentException("竞赛标题已存在");
            }
        } else {
            // 更新 - 需要检查其他竞赛是否有相同标题
            Optional<Competition> existing = competitionRepository.findById(competition.getCompetitionID());
            if (existing.isPresent() && !existing.get().getTitle().equals(competition.getTitle())) {
                if (competitionRepository.existsByTitle(competition.getTitle())) {
                    throw new IllegalArgumentException("竞赛标题已存在");
                }
            }
        }
    }
}