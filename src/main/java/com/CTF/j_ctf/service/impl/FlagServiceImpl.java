package com.CTF.j_ctf.service.impl;

import com.CTF.j_ctf.entity.*;
import com.CTF.j_ctf.repository.*;
import com.CTF.j_ctf.service.FlagService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.ThreadLocalRandom;

@Service
@Transactional
public class FlagServiceImpl implements FlagService {
    private final FlagRepository flagRepository;
    private final FlagSubmissionRepository flagSubmissionRepository;
    private final UserRepository userRepository;
    private final CompetitionRepository competitionRepository;
    private final TeamRepository teamRepository;
    private final ScoreRepository scoreRepository;

    public FlagServiceImpl(FlagRepository flagRepository,
                           FlagSubmissionRepository flagSubmissionRepository,
                           UserRepository userRepository,
                           CompetitionRepository competitionRepository,
                           TeamRepository teamRepository,
                           ScoreRepository scoreRepository) {
        this.flagRepository = flagRepository;
        this.flagSubmissionRepository = flagSubmissionRepository;
        this.userRepository = userRepository;
        this.competitionRepository = competitionRepository;
        this.teamRepository = teamRepository;
        this.scoreRepository = scoreRepository;
    }

    @Override
    public Flag createFlag(Flag flag) {
        // 验证数据
        validateFlag(flag);

        // 检查Flag值是否唯一
        if (flagRepository.findByValue(flag.getValue()).isPresent()) {
            throw new IllegalArgumentException("Flag值已存在");
        }

        return flagRepository.save(flag);
    }

    @Override
    public Flag updateFlag(Flag flag) {
        Optional<Flag> existingOpt = flagRepository.findById(flag.getFlagID());
        if (existingOpt.isEmpty()) {
            throw new IllegalArgumentException("Flag不存在");
        }

        Flag existing = existingOpt.get();

        // 只有未使用的Flag可以修改
        if (!existing.canBeUsed()) {
            throw new IllegalArgumentException("只能修改未使用的Flag");
        }

        // 更新允许修改的字段
        if (flag.getValue() != null && !flag.getValue().equals(existing.getValue())) {
            // 检查新Flag值是否唯一
            if (flagRepository.findByValue(flag.getValue()).isPresent()) {
                throw new IllegalArgumentException("Flag值已存在");
            }
            existing.setValue(flag.getValue());
        }

        if (flag.getPoints() != null) {
            existing.setPoints(flag.getPoints());
        }

        if (flag.getDescription() != null) {
            existing.setDescription(flag.getDescription());
        }

        if (flag.getType() != null) {
            existing.setType(flag.getType());
        }

        if (flag.getExpireTime() != null) {
            existing.setExpireTime(flag.getExpireTime());
        }

        if (flag.getChallenge() != null) {
            existing.setChallenge(flag.getChallenge());
        }

        return flagRepository.save(existing);
    }

    @Override
    public boolean deleteFlag(Integer flagId) {
        if (!flagRepository.existsById(flagId)) {
            return false;
        }

        flagRepository.deleteById(flagId);
        return true;
    }

    @Override
    public Optional<Flag> getFlagById(Integer flagId) {
        return flagRepository.findById(flagId);
    }

    @Override
    public FlagSubmission submitFlag(Integer flagId, Integer userId, String submittedValue, String ipAddress, String userAgent) {
        Optional<Flag> flagOpt = flagRepository.findById(flagId);
        Optional<User> userOpt = userRepository.findById(userId);

        if (flagOpt.isEmpty() || userOpt.isEmpty()) {
            throw new IllegalArgumentException("Flag或用户不存在");
        }

        Flag flag = flagOpt.get();
        User user = userOpt.get();

        // 验证提交条件
        if (!canUserSubmitFlag(flagId, userId)) {
            throw new IllegalArgumentException("无法提交该Flag");
        }

        FlagSubmission submission = new FlagSubmission(user, flag, submittedValue);
        submission.setIpAddress(ipAddress);
        submission.setUserAgent(userAgent);

        boolean isCorrect = flag.getValue().equals(submittedValue);
        submission.setIsCorrect(isCorrect);

        if (isCorrect) {
            // 标记Flag为已使用
            flag.markAsUsed();
            flagRepository.save(flag);

            // 记录得分
            submission.setPointsAwarded(flag.getPoints());

            Score score = new Score(user, flag.getCompetition(), flag.getPoints());
            if (flag.getTeam() != null) {
                // 如果是战队Flag，为战队所有成员记录分数
                for (User member : flag.getTeam().getMembers()) {
                    Score teamScore = new Score(member, flag.getCompetition(), flag.getPoints());
                    scoreRepository.save(teamScore);
                }
            } else {
                scoreRepository.save(score);
            }
        }

        return flagSubmissionRepository.save(submission);
    }

    @Override
    public FlagSubmission submitFlagForTeam(Integer flagId, Integer teamId, String submittedValue, String ipAddress, String userAgent) {
        Optional<Flag> flagOpt = flagRepository.findById(flagId);
        Optional<Team> teamOpt = teamRepository.findById(teamId);

        if (flagOpt.isEmpty() || teamOpt.isEmpty()) {
            throw new IllegalArgumentException("Flag或战队不存在");
        }

        Flag flag = flagOpt.get();
        Team team = teamOpt.get();

        // 验证提交条件
        if (!canTeamSubmitFlag(flagId, teamId)) {
            throw new IllegalArgumentException("战队无法提交该Flag");
        }

        // 使用队长身份提交
        User captain = team.getCaptain();

        FlagSubmission submission = new FlagSubmission(captain, flag, submittedValue);
        submission.setTeam(team);
        submission.setIpAddress(ipAddress);
        submission.setUserAgent(userAgent);

        boolean isCorrect = flag.getValue().equals(submittedValue);
        submission.setIsCorrect(isCorrect);

        if (isCorrect) {
            // 标记Flag为已使用
            flag.markAsUsed();
            flagRepository.save(flag);

            // 记录得分
            submission.setPointsAwarded(flag.getPoints());

            // 为战队所有成员记录分数
            for (User member : team.getMembers()) {
                Score score = new Score(member, flag.getCompetition(), flag.getPoints());
                scoreRepository.save(score);
            }
        }

        return flagSubmissionRepository.save(submission);
    }

    @Override
    public Page<Flag> getAllFlags(Pageable pageable) {
        return flagRepository.findAll(pageable);
    }

    @Override
    public Page<Flag> getFlagsByCompetition(Integer competitionId, Pageable pageable) {
        return flagRepository.findByCompetition_CompetitionID(competitionId, pageable);
    }

    @Override
    public Page<Flag> getFlagsByUser(Integer userId, Pageable pageable) {
        return flagRepository.findByUser_UserID(userId, pageable);
    }

    @Override
    public Page<Flag> getFlagsByTeam(Integer teamId, Pageable pageable) {
        return flagRepository.findByTeam_TeamID(teamId, pageable);
    }

    @Override
    public Page<Flag> getFlagsByStatus(Integer status, Pageable pageable) {
        return flagRepository.findByStatus(status, pageable);
    }

    @Override
    public Page<Flag> searchFlags(String keyword, Pageable pageable) {
        return flagRepository.findByValueOrDescriptionContaining(keyword, pageable);
    }

    @Override
    public Page<FlagSubmission> getAllSubmissions(Pageable pageable) {
        return flagSubmissionRepository.findAll(pageable);
    }

    @Override
    public Page<FlagSubmission> getSubmissionsByUser(Integer userId, Pageable pageable) {
        return flagSubmissionRepository.findByUser_UserID(userId, pageable);
    }

    @Override
    public Page<FlagSubmission> getSubmissionsByTeam(Integer teamId, Pageable pageable) {
        return flagSubmissionRepository.findByTeam_TeamID(teamId, pageable);
    }

    @Override
    public Page<FlagSubmission> getSubmissionsByCompetition(Integer competitionId, Pageable pageable) {
        return flagSubmissionRepository.findByCompetition(competitionId, pageable);
    }

    @Override
    public Page<FlagSubmission> getSubmissionsByFlag(Integer flagId, Pageable pageable) {
        return flagSubmissionRepository.findByFlag_FlagID(flagId, pageable);
    }

    @Override
    public boolean canUserSubmitFlag(Integer flagId, Integer userId) {
        Optional<Flag> flagOpt = flagRepository.findById(flagId);
        if (flagOpt.isEmpty()) {
            return false;
        }

        Flag flag = flagOpt.get();

        // 检查Flag状态
        if (!flag.canBeUsed()) {
            return false;
        }

        // 检查用户是否已经解答过该Flag
        if (hasUserSolvedFlag(flagId, userId)) {
            return false;
        }

        // 检查竞赛状态
        if (!flag.getCompetition().isOngoing()) {
            return false;
        }

        return true;
    }

    @Override
    public boolean canTeamSubmitFlag(Integer flagId, Integer teamId) {
        Optional<Flag> flagOpt = flagRepository.findById(flagId);
        Optional<Team> teamOpt = teamRepository.findById(teamId);

        if (flagOpt.isEmpty() || teamOpt.isEmpty()) {
            return false;
        }

        Flag flag = flagOpt.get();
        Team team = teamOpt.get();

        // 检查Flag状态
        if (!flag.canBeUsed()) {
            return false;
        }

        // 检查战队是否已经解答过该Flag
        if (hasTeamSolvedFlag(flagId, teamId)) {
            return false;
        }

        // 检查竞赛状态
        if (!flag.getCompetition().isOngoing()) {
            return false;
        }

        // 检查战队审核状态
        if (!team.isAuditApproved()) {
            return false;
        }

        return true;
    }

    @Override
    public boolean hasUserSolvedFlag(Integer flagId, Integer userId) {
        Optional<FlagSubmission> submission = flagSubmissionRepository
                .findCorrectSubmissionByUserAndFlag(userId, flagId);
        return submission.isPresent();
    }

    @Override
    public boolean hasTeamSolvedFlag(Integer flagId, Integer teamId) {
        List<FlagSubmission> submissions = flagSubmissionRepository.findByTeamAndCompetition(teamId,
                flagRepository.findById(flagId).get().getCompetition().getCompetitionID());

        return submissions.stream().anyMatch(FlagSubmission::getIsCorrect);
    }

    @Override
    public void expireFlags() {
        List<Flag> expiredFlags = flagRepository.findExpiredFlags(LocalDateTime.now());
        for (Flag flag : expiredFlags) {
            flag.markAsExpired();
            flagRepository.save(flag);
        }
    }

    @Override
    public Flag regenerateFlag(Integer flagId) {
        Optional<Flag> flagOpt = flagRepository.findById(flagId);
        if (flagOpt.isEmpty()) {
            throw new IllegalArgumentException("Flag不存在");
        }

        Flag flag = flagOpt.get();

        // 生成新的Flag值
        String newFlagValue = generateFlagValue();
        flag.setValue(newFlagValue);
        flag.setStatus(0); // 重置为未使用
        flag.setUseTime(null);

        return flagRepository.save(flag);
    }

    @Override
    public Map<String, Object> getFlagStatistics(Integer competitionId) {
        Map<String, Object> stats = new HashMap<>();

        Long totalFlags = flagRepository.countFlagsByCompetition(competitionId);
        Long usedFlags = flagRepository.countUsedFlagsByCompetition(competitionId);
        Long availableFlags = totalFlags - usedFlags;

        stats.put("totalFlags", totalFlags);
        stats.put("usedFlags", usedFlags);
        stats.put("availableFlags", availableFlags);
        stats.put("usageRate", totalFlags > 0 ? (double) usedFlags / totalFlags * 100 : 0);

        return stats;
    }

    @Override
    public Map<String, Object> getUserFlagStatistics(Integer userId, Integer competitionId) {
        Map<String, Object> stats = new HashMap<>();

        Integer usedFlags = flagRepository.countUsedFlagsByUserAndCompetition(userId, competitionId);
        Long correctSubmissions = flagSubmissionRepository.countCorrectSubmissionsByUserAndCompetition(userId, competitionId);

        stats.put("usedFlags", usedFlags);
        stats.put("correctSubmissions", correctSubmissions);
        stats.put("successRate", usedFlags > 0 ? (double) correctSubmissions / usedFlags * 100 : 0);

        return stats;
    }

    @Override
    public Map<String, Object> getTeamFlagStatistics(Integer teamId, Integer competitionId) {
        Map<String, Object> stats = new HashMap<>();

        Integer usedFlags = flagRepository.countUsedFlagsByTeamAndCompetition(teamId, competitionId);
        Long correctSubmissions = flagSubmissionRepository.countCorrectSubmissionsByTeamAndCompetition(teamId, competitionId);

        stats.put("usedFlags", usedFlags);
        stats.put("correctSubmissions", correctSubmissions);
        stats.put("successRate", usedFlags > 0 ? (double) correctSubmissions / usedFlags * 100 : 0);

        return stats;
    }

    @Override
    public List<Flag> generateFlagsForCompetition(Integer competitionId, Integer count, Integer points, LocalDateTime expireTime) {
        Optional<Competition> competitionOpt = competitionRepository.findById(competitionId);
        if (competitionOpt.isEmpty()) {
            throw new IllegalArgumentException("竞赛不存在");
        }

        Competition competition = competitionOpt.get();
        List<Flag> flags = new ArrayList<>();

        for (int i = 0; i < count; i++) {
            Flag flag = new Flag();
            flag.setCompetition(competition);
            flag.setValue(generateFlagValue());
            flag.setPoints(points);
            flag.setExpireTime(expireTime);
            flag.setType("DYNAMIC");
            flag.setDescription("自动生成的Flag");

            flags.add(flagRepository.save(flag));
        }

        return flags;
    }

    /**
     * 生成随机的Flag值
     */
    private String generateFlagValue() {
        String characters = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789";
        StringBuilder flag = new StringBuilder("CTF{");
        ThreadLocalRandom random = ThreadLocalRandom.current();

        for (int i = 0; i < 16; i++) {
            flag.append(characters.charAt(random.nextInt(characters.length())));
        }

        flag.append("}");
        return flag.toString();
    }

    /**
     * 验证Flag数据的有效性
     */
    private void validateFlag(Flag flag) {
        if (flag.getValue() == null || flag.getValue().trim().isEmpty()) {
            throw new IllegalArgumentException("Flag值不能为空");
        }

        if (flag.getPoints() == null || flag.getPoints() <= 0) {
            throw new IllegalArgumentException("Flag分值必须大于0");
        }

        if (flag.getCompetition() == null) {
            throw new IllegalArgumentException("必须指定竞赛");
        }

        if (flag.getExpireTime() != null && flag.getExpireTime().isBefore(LocalDateTime.now())) {
            throw new IllegalArgumentException("过期时间不能早于当前时间");
        }
    }
}