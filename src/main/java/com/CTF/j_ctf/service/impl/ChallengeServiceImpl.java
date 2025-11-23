package com.CTF.j_ctf.service.impl;

import com.CTF.j_ctf.entity.*;
import com.CTF.j_ctf.repository.*;
import com.CTF.j_ctf.service.ChallengeService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@Transactional
public class ChallengeServiceImpl implements ChallengeService {
    private final ChallengeRepository challengeRepository;
    private final FlagSubmissionRepository flagSubmissionRepository;
    private final ChallengeHintRepository challengeHintRepository;
    private final UserRepository userRepository;
    private final CompetitionRepository competitionRepository;
    private final ScoreRepository scoreRepository;
    private final TeamRepository teamRepository;

    public ChallengeServiceImpl(ChallengeRepository challengeRepository,
                                FlagSubmissionRepository flagSubmissionRepository,
                                ChallengeHintRepository challengeHintRepository,
                                UserRepository userRepository,
                                CompetitionRepository competitionRepository,
                                ScoreRepository scoreRepository,
                                TeamRepository teamRepository) {
        this.challengeRepository = challengeRepository;
        this.flagSubmissionRepository = flagSubmissionRepository;
        this.challengeHintRepository = challengeHintRepository;
        this.userRepository = userRepository;
        this.competitionRepository = competitionRepository;
        this.scoreRepository = scoreRepository;
        this.teamRepository = teamRepository;
    }

    @Override
    public Challenge createChallenge(Challenge challenge) {
        // 验证数据
        validateChallenge(challenge);

        // 检查题目标题在同一个竞赛中是否唯一
        if (challengeRepository.existsByTitleAndCompetition(challenge.getTitle(), challenge.getCompetition().getCompetitionID())) {
            throw new IllegalArgumentException("该竞赛中已存在同名题目");
        }

        // 设置默认值
        if (challenge.getIsActive() == null) {
            challenge.setIsActive(true);
        }
        if (challenge.getSolveCount() == null) {
            challenge.setSolveCount(0);
        }

        challenge.setCreateTime(LocalDateTime.now());
        challenge.setUpdateTime(LocalDateTime.now());

        return challengeRepository.save(challenge);
    }

    @Override
    public Challenge updateChallenge(Challenge challenge) {
        Optional<Challenge> existingOpt = challengeRepository.findById(challenge.getChallengeID());
        if (existingOpt.isEmpty()) {
            throw new IllegalArgumentException("题目不存在");
        }

        Challenge existing = existingOpt.get();

        // 更新允许修改的字段
        if (challenge.getTitle() != null && !challenge.getTitle().equals(existing.getTitle())) {
            // 检查新标题是否唯一
            if (challengeRepository.existsByTitleAndCompetition(challenge.getTitle(), existing.getCompetition().getCompetitionID())) {
                throw new IllegalArgumentException("该竞赛中已存在同名题目");
            }
            existing.setTitle(challenge.getTitle());
        }

        if (challenge.getDescription() != null) {
            existing.setDescription(challenge.getDescription());
        }

        if (challenge.getCategory() != null) {
            existing.setCategory(challenge.getCategory());
        }

        if (challenge.getDifficulty() != null) {
            existing.setDifficulty(challenge.getDifficulty());
        }

        if (challenge.getPoints() != null) {
            existing.setPoints(challenge.getPoints());
        }

        if (challenge.getFlag() != null) {
            existing.setFlag(challenge.getFlag());
        }

        if (challenge.getIsActive() != null) {
            existing.setIsActive(challenge.getIsActive());
        }

        if (challenge.getAttachmentUrl() != null) {
            existing.setAttachmentUrl(challenge.getAttachmentUrl());
        }

        if (challenge.getHint() != null) {
            existing.setHint(challenge.getHint());
        }

        if (challenge.getSolveCount() != null) {
            existing.setSolveCount(challenge.getSolveCount());
        }

        // 更新创建者ID（如果需要）
        if (challenge.getCreatorId() != null) {
            existing.setCreatorId(challenge.getCreatorId());
        }

        existing.setUpdateTime(LocalDateTime.now());

        return challengeRepository.save(existing);
    }

    @Override
    public boolean deleteChallenge(Integer challengeId) {
        if (!challengeRepository.existsById(challengeId)) {
            return false;
        }

        // 先删除相关的提交记录和提示
        flagSubmissionRepository.deleteByChallenge_ChallengeID(challengeId);
        challengeHintRepository.deleteByChallenge_ChallengeID(challengeId);

        challengeRepository.deleteById(challengeId);
        return true;
    }

    @Override
    public Optional<Challenge> getChallengeById(Integer challengeId) {
        return challengeRepository.findById(challengeId);
    }

    @Override
    public Page<Challenge> getAllChallenges(Pageable pageable) {
        return challengeRepository.findAll(pageable);
    }

    @Override
    public Page<Challenge> getChallengesByCompetition(Integer competitionId, Pageable pageable) {
        return challengeRepository.findByCompetition_CompetitionID(competitionId, pageable);
    }

    @Override
    public Page<Challenge> getChallengesByCategory(String category, Pageable pageable) {
        return challengeRepository.findByCategory(category, pageable);
    }

    @Override
    public Page<Challenge> getChallengesByDifficulty(String difficulty, Pageable pageable) {
        return challengeRepository.findByDifficulty(difficulty, pageable);
    }

    @Override
    public Page<Challenge> searchChallenges(String keyword, Pageable pageable) {
        return challengeRepository.findByTitleContainingOrDescriptionContaining(keyword, keyword, pageable);
    }

    @Override
    public Page<Challenge> searchChallengesByCompetition(String keyword, Integer competitionId, Pageable pageable) {
        return challengeRepository.findByTitleContainingOrDescriptionContainingAndCompetition_CompetitionID(keyword, keyword, competitionId, pageable);
    }

    @Override
    public Page<Challenge> getChallengesByMultipleConditions(Integer competitionId, String category, String difficulty, Pageable pageable) {
        // 使用Repository的多条件查询方法，支持同时应用多个筛选条件
        return challengeRepository.findByMultipleConditions(category, difficulty, competitionId, true, pageable);
    }

    @Override
    @Transactional
    public FlagSubmission submitFlag(Integer challengeId, Integer userId, String submittedFlag, String ipAddress) {
        Optional<Challenge> challengeOpt = challengeRepository.findById(challengeId);
        Optional<User> userOpt = userRepository.findById(userId);

        if (challengeOpt.isEmpty() || userOpt.isEmpty()) {
            throw new IllegalArgumentException("题目或用户不存在");
        }

        Challenge challenge = challengeOpt.get();
        User user = userOpt.get();

        // 检查题目是否激活
        if (!challenge.getIsActive()) {
            throw new IllegalArgumentException("题目未激活");
        }

        // 检查用户是否已经解答过该题目
        if (hasUserSolvedChallenge(challengeId, userId)) {
            throw new IllegalArgumentException("您已经解答过该题目");
        }

        // 检查竞赛是否在进行中
        Competition competition = challenge.getCompetition();
        if (competition == null || !isCompetitionOngoing(competition)) {
            throw new IllegalArgumentException("竞赛已结束或未开始");
        }

        // 检查用户是否加入了该竞赛的团队
        boolean isParticipant = teamRepository.existsByCompetition_CompetitionIDAndMembers_UserID(
                competition.getCompetitionID(), userId);
        if (!isParticipant) {
            throw new IllegalArgumentException("您尚未加入该竞赛的团队，无法提交Flag");
        }

        FlagSubmission submission = new FlagSubmission();
        submission.setUser(user);
        submission.setChallenge(challenge);
        submission.setCompetition(competition); // 设置竞赛，这是必需的字段
        submission.setSubmittedFlag(submittedFlag);
        submission.setIpAddress(ipAddress);
        submission.setSubmitTime(LocalDateTime.now());

        boolean isCorrect = challenge.getFlag().equals(submittedFlag);
        submission.setIsCorrect(isCorrect);

        FlagSubmission savedSubmission = flagSubmissionRepository.save(submission);

        if (isCorrect) {
            // 增加解题人数
            challenge.incrementSolveCount();
            challengeRepository.save(challenge);

            // 记录得分
            Score score = new Score();
            score.setUser(user);
            score.setCompetition(competition);
            score.setPoints(challenge.getPoints());
            score.setScoreTime(LocalDateTime.now());
            scoreRepository.save(score);
        }

        return savedSubmission;
    }

    @Override
    public Page<FlagSubmission> getSubmissionsByChallenge(Integer challengeId, Pageable pageable) {
        return flagSubmissionRepository.findByChallenge_ChallengeID(challengeId, pageable);
    }

    @Override
    public Page<FlagSubmission> getSubmissionsByUser(Integer userId, Pageable pageable) {
        return flagSubmissionRepository.findByUser_UserID(userId, pageable);
    }

    @Override
    public Page<FlagSubmission> getUserSubmissionsByChallenge(Integer challengeId, Integer userId, Pageable pageable) {
        return flagSubmissionRepository.findByChallenge_ChallengeIDAndUser_UserID(challengeId, userId, pageable);
    }

    @Override
    public boolean hasUserSolvedChallenge(Integer challengeId, Integer userId) {
        Optional<FlagSubmission> submission = flagSubmissionRepository
                .findFirstByUser_UserIDAndChallenge_ChallengeIDAndIsCorrectTrue(userId, challengeId);
        return submission.isPresent();
    }

    @Override
    public List<Integer> getSolvedChallengeIdsByUser(Integer userId) {
        List<FlagSubmission> submissions = flagSubmissionRepository
                .findByUser_UserIDAndIsCorrectTrue(userId);
        return submissions.stream()
                .map(submission -> submission.getChallenge().getChallengeID())
                .distinct()
                .collect(Collectors.toList());
    }

    @Override
    public ChallengeHint addHint(Integer challengeId, ChallengeHint hint) {
        Optional<Challenge> challengeOpt = challengeRepository.findById(challengeId);
        if (challengeOpt.isEmpty()) {
            throw new IllegalArgumentException("题目不存在");
        }

        hint.setChallenge(challengeOpt.get());
        hint.setCreateTime(LocalDateTime.now());
        return challengeHintRepository.save(hint);
    }

    @Override
    public ChallengeHint updateHint(ChallengeHint hint) {
        Optional<ChallengeHint> existingHintOpt = challengeHintRepository.findById(hint.getHintID());
        if (existingHintOpt.isEmpty()) {
            throw new IllegalArgumentException("提示不存在");
        }

        ChallengeHint existingHint = existingHintOpt.get();

        if (hint.getContent() != null) {
            existingHint.setContent(hint.getContent());
        }
        if (hint.getCost() != null) {
            existingHint.setCost(hint.getCost());
        }

        existingHint.setUpdateTime(LocalDateTime.now());

        return challengeHintRepository.save(existingHint);
    }

    @Override
    public boolean removeHint(Integer hintId) {
        if (!challengeHintRepository.existsById(hintId)) {
            return false;
        }

        challengeHintRepository.deleteById(hintId);
        return true;
    }

    @Override
    public List<ChallengeHint> getHintsByChallenge(Integer challengeId) {
        return challengeHintRepository.findByChallenge_ChallengeIDOrderByCostAsc(challengeId);
    }

    @Override
    public Integer getChallengeIdByHintId(Integer hintId) {
        Optional<ChallengeHint> hintOpt = challengeHintRepository.findById(hintId);
        if (hintOpt.isPresent()) {
            return hintOpt.get().getChallenge().getChallengeID();
        }
        throw new IllegalArgumentException("提示不存在");
    }

    @Override
    public boolean isChallengeCreator(Integer challengeId, Integer userId) {
        Optional<Challenge> challengeOpt = challengeRepository.findById(challengeId);
        return challengeOpt.isPresent() &&
                challengeOpt.get().getCreatorId() != null &&
                challengeOpt.get().getCreatorId().equals(userId);
    }

    @Override
    public boolean isChallengeActive(Integer challengeId) {
        Optional<Challenge> challengeOpt = challengeRepository.findById(challengeId);
        return challengeOpt.map(Challenge::getIsActive).orElse(false);
    }

    @Override
    public Map<String, Object> getChallengeStatistics(Integer competitionId) {
        Map<String, Object> stats = new HashMap<>();

        Long totalChallenges = challengeRepository.countByCompetition_CompetitionID(competitionId);
        Long webChallenges = challengeRepository.countByCompetition_CompetitionIDAndCategory(competitionId, "Web");
        Long pwnChallenges = challengeRepository.countByCompetition_CompetitionIDAndCategory(competitionId, "Pwn");
        Long cryptoChallenges = challengeRepository.countByCompetition_CompetitionIDAndCategory(competitionId, "Crypto");
        Long reverseChallenges = challengeRepository.countByCompetition_CompetitionIDAndCategory(competitionId, "Reverse");
        Long miscChallenges = challengeRepository.countByCompetition_CompetitionIDAndCategory(competitionId, "Misc");

        Long easyChallenges = challengeRepository.countByCompetition_CompetitionIDAndDifficulty(competitionId, "Easy");
        Long mediumChallenges = challengeRepository.countByCompetition_CompetitionIDAndDifficulty(competitionId, "Medium");
        Long hardChallenges = challengeRepository.countByCompetition_CompetitionIDAndDifficulty(competitionId, "Hard");

        // 计算解决率
        List<Challenge> challenges = challengeRepository.findByCompetition_CompetitionID(competitionId);
        long totalSolves = challenges.stream().mapToLong(Challenge::getSolveCount).sum();
        double averageSolveRate = totalChallenges > 0 ? (double) totalSolves / totalChallenges : 0;

        stats.put("totalChallenges", totalChallenges);
        stats.put("webChallenges", webChallenges);
        stats.put("pwnChallenges", pwnChallenges);
        stats.put("cryptoChallenges", cryptoChallenges);
        stats.put("reverseChallenges", reverseChallenges);
        stats.put("miscChallenges", miscChallenges);
        stats.put("easyChallenges", easyChallenges);
        stats.put("mediumChallenges", mediumChallenges);
        stats.put("hardChallenges", hardChallenges);
        stats.put("totalSolves", totalSolves);
        stats.put("averageSolveRate", Math.round(averageSolveRate * 100.0) / 100.0);

        return stats;
    }

    @Override
    public Long getChallengeCountByCompetition(Integer competitionId) {
        return challengeRepository.countByCompetition_CompetitionID(competitionId);
    }

    @Override
    public List<String> getCategoriesByCompetition(Integer competitionId) {
        return challengeRepository.findDistinctCategoriesByCompetition_CompetitionID(competitionId);
    }

    @Override
    public Long getTotalSubmissionsByChallenge(Integer challengeId) {
        return flagSubmissionRepository.countByChallenge_ChallengeID(challengeId);
    }

    @Override
    public Long getCorrectSubmissionsByChallenge(Integer challengeId) {
        return flagSubmissionRepository.countByChallenge_ChallengeIDAndIsCorrectTrue(challengeId);
    }

    @Override
    public Double getSolveRateByChallenge(Integer challengeId) {
        Long total = getTotalSubmissionsByChallenge(challengeId);
        Long correct = getCorrectSubmissionsByChallenge(challengeId);

        if (total == 0) {
            return 0.0;
        }

        return (double) correct / total * 100;
    }

    /**
     * 验证题目数据的有效性
     */
    private void validateChallenge(Challenge challenge) {
        if (challenge.getTitle() == null || challenge.getTitle().trim().isEmpty()) {
            throw new IllegalArgumentException("题目标题不能为空");
        }

        if (challenge.getDescription() == null || challenge.getDescription().trim().isEmpty()) {
            throw new IllegalArgumentException("题目描述不能为空");
        }

        if (challenge.getCategory() == null || challenge.getCategory().trim().isEmpty()) {
            throw new IllegalArgumentException("题目类别不能为空");
        }

        if (challenge.getDifficulty() == null || challenge.getDifficulty().trim().isEmpty()) {
            throw new IllegalArgumentException("题目难度不能为空");
        }

        if (challenge.getPoints() == null || challenge.getPoints() <= 0) {
            throw new IllegalArgumentException("题目分值必须大于0");
        }

        if (challenge.getFlag() == null || challenge.getFlag().trim().isEmpty()) {
            throw new IllegalArgumentException("题目Flag不能为空");
        }

        if (challenge.getCompetition() == null) {
            throw new IllegalArgumentException("必须指定竞赛");
        }
    }

    /**
     * 检查竞赛是否在进行中
     */
    private boolean isCompetitionOngoing(Competition competition) {
        LocalDateTime now = LocalDateTime.now();
        return now.isAfter(competition.getStartTime()) && now.isBefore(competition.getEndTime());
    }
}