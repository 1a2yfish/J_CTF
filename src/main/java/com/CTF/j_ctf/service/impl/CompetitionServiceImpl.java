package com.CTF.j_ctf.service.impl;

import com.CTF.j_ctf.entity.Competition;
import com.CTF.j_ctf.repository.CompetitionRepository;
import com.CTF.j_ctf.repository.UserRepository;
import com.CTF.j_ctf.service.CompetitionService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Optional;

@Service
@Transactional
public class CompetitionServiceImpl implements CompetitionService {
    private final CompetitionRepository competitionRepository;
    private final UserRepository userRepository;

    public CompetitionServiceImpl(CompetitionRepository competitionRepository,
                                  UserRepository userRepository) {
        this.competitionRepository = competitionRepository;
        this.userRepository = userRepository;
    }

    @Override
    public Competition createCompetition(Competition competition) {
        // 验证数据
        validateCompetition(competition);

        // 设置默认值
        competition.setStatus("DRAFT");
        competition.setAuditStatus("PENDING");
        competition.setPublishTime(LocalDateTime.now());

        return competitionRepository.save(competition);
    }

    @Override
    public Competition updateCompetition(Competition competition) {
        Optional<Competition> existingOpt = competitionRepository.findById(competition.getCompetitionID());
        if (existingOpt.isEmpty()) {
            throw new IllegalArgumentException("竞赛不存在");
        }

        Competition existing = existingOpt.get();

        // 只有草稿状态的竞赛可以修改基本信息
        if (!"DRAFT".equals(existing.getStatus())) {
            throw new IllegalArgumentException("只有草稿状态的竞赛可以修改");
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
        return competitionRepository.findPublicCompetitions(pageable);
    }

    @Override
    public Page<Competition> getOngoingCompetitions(Pageable pageable) {
        return competitionRepository.findOngoingCompetitions(LocalDateTime.now(), pageable);
    }

    @Override
    public Page<Competition> getUpcomingCompetitions(Pageable pageable) {
        return competitionRepository.findUpcomingCompetitions(LocalDateTime.now(), pageable);
    }

    @Override
    public Page<Competition> getFinishedCompetitions(Pageable pageable) {
        return competitionRepository.findFinishedCompetitions(LocalDateTime.now(), pageable);
    }

    @Override
    public Page<Competition> searchCompetitions(String keyword, Pageable pageable) {
        return competitionRepository.searchAllCompetitions(keyword, pageable);
    }

    @Override
    public Page<Competition> searchPublicCompetitions(String keyword, Pageable pageable) {
        return competitionRepository.searchPublicCompetitions(keyword, pageable);
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
    public boolean canUserJoinCompetition(Integer competitionId, Integer userId) {
        Optional<Competition> competitionOpt = competitionRepository.findById(competitionId);
        if (competitionOpt.isEmpty()) {
            return false;
        }

        Competition competition = competitionOpt.get();

        // 检查竞赛状态
        if (!competition.canJoin()) {
            return false;
        }

        // 检查用户是否是创建者
        if (competition.getCreator().getUserID().equals(userId)) {
            return false;
        }

        // 检查队伍数量限制
        if (competition.getMaxTeams() != null &&
                competition.getTeams().size() >= competition.getMaxTeams()) {
            return false;
        }

        return true;
    }

    @Override
    public boolean deleteCompetition(Integer competitionId) {
        if (!competitionRepository.existsById(competitionId)) {
            return false;
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
        return competitionRepository.countByCreator(userId);
    }

    @Override
    public Long getCompetitionCountByStatus(String status) {
        return competitionRepository.countByStatus(status);
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

        if (competition.getMaxTeams() != null && competition.getMaxTeams() <= 0) {
            throw new IllegalArgumentException("最大队伍数必须大于0");
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

    public UserRepository getUserRepository() {
        return userRepository;
    }
}