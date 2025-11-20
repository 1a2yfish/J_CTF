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

@Service
@Transactional
public class ChallengeServiceImpl implements ChallengeService {
    private final ChallengeRepository challengeRepository;
    private final FlagSubmissionRepository flagSubmissionRepository;
    private final ChallengeHintRepository challengeHintRepository;
    private final UserRepository userRepository;
    private final CompetitionRepository competitionRepository;
    private final ScoreRepository scoreRepository;

    public ChallengeServiceImpl(ChallengeRepository challengeRepository,
                                FlagSubmissionRepository flagSubmissionRepository,
                                ChallengeHintRepository challengeHintRepository,
                                UserRepository userRepository,
                                CompetitionRepository competitionRepository,
                                ScoreRepository scoreRepository) {
        this.challengeRepository = challengeRepository;
        this.flagSubmissionRepository = flagSubmissionRepository;
        this.challengeHintRepository = challengeHintRepository;
        this.userRepository = userRepository;
        this.competitionRepository = competitionRepository;
        this.scoreRepository = scoreRepository;
    }

    @Override
    public Challenge createChallenge(Challenge challenge) {
        // 验证数据
        validateChallenge(challenge);

        // 检查题目标题在同一个竞赛中是否唯一
        if (challengeRepository.existsByTitleAndCompetition(challenge.getTitle(), challenge.getCompetition().getCompetitionID())) {
            throw new IllegalArgumentException("该竞赛中已存在同名题目");
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

        existing.setUpdateTime(LocalDateTime.now());

        return challengeRepository.save(existing);
    }

    @Override
    public boolean deleteChallenge(Integer challengeId) {
        if (!challengeRepository.existsById(challengeId)) {
            return false;
        }

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
        return challengeRepository.findByTitleOrDescriptionContaining(keyword, pageable);
    }

    @Override
    public Page<Challenge> searchChallengesByCompetition(String keyword, Integer competitionId, Pageable pageable) {
        return challengeRepository.findByTitleOrDescriptionContainingAndCompetition(keyword, competitionId, pageable);
    }

    @Override
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
        if (!challenge.getCompetition().isOngoing()) {
            throw new IllegalArgumentException("竞赛已结束或未开始");
        }

        FlagSubmission submission = new FlagSubmission(user, challenge, submittedFlag);
        submission.setIpAddress(ipAddress);

        boolean isCorrect = challenge.getFlag().equals(submittedFlag);
        submission.setIsCorrect(isCorrect);

        if (isCorrect) {
            // 增加解题人数
            challenge.incrementSolveCount();
            challengeRepository.save(challenge);

            // 记录得分
            Score score = new Score(user, challenge.getCompetition(), challenge.getPoints());
            scoreRepository.save(score);
        }

        return flagSubmissionRepository.save(submission);
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
    public boolean hasUserSolvedChallenge(Integer challengeId, Integer userId) {
        Optional<FlagSubmission> submission = flagSubmissionRepository
                .findCorrectSubmissionByUserAndChallenge(userId, challengeId);
        return submission.isPresent();
    }

    @Override
    public ChallengeHint addHint(Integer challengeId, ChallengeHint hint) {
        Optional<Challenge> challengeOpt = challengeRepository.findById(challengeId);
        if (challengeOpt.isEmpty()) {
            throw new IllegalArgumentException("题目不存在");
        }

        hint.setChallenge(challengeOpt.get());
        return challengeHintRepository.save(hint);
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
    public Map<String, Object> getChallengeStatistics(Integer competitionId) {
        Map<String, Object> stats = new HashMap<>();

        Long totalChallenges = challengeRepository.countByCompetition(competitionId);
        Long webChallenges = challengeRepository.countByCompetitionAndCategory(competitionId, "Web");
        Long pwnChallenges = challengeRepository.countByCompetitionAndCategory(competitionId, "Pwn");
        Long cryptoChallenges = challengeRepository.countByCompetitionAndCategory(competitionId, "Crypto");
        Long reverseChallenges = challengeRepository.countByCompetitionAndCategory(competitionId, "Reverse");
        Long miscChallenges = challengeRepository.countByCompetitionAndCategory(competitionId, "Misc");

        Long easyChallenges = challengeRepository.countByCompetitionAndDifficulty(competitionId, "Easy");
        Long mediumChallenges = challengeRepository.countByCompetitionAndDifficulty(competitionId, "Medium");
        Long hardChallenges = challengeRepository.countByCompetitionAndDifficulty(competitionId, "Hard");

        stats.put("totalChallenges", totalChallenges);
        stats.put("webChallenges", webChallenges);
        stats.put("pwnChallenges", pwnChallenges);
        stats.put("cryptoChallenges", cryptoChallenges);
        stats.put("reverseChallenges", reverseChallenges);
        stats.put("miscChallenges", miscChallenges);
        stats.put("easyChallenges", easyChallenges);
        stats.put("mediumChallenges", mediumChallenges);
        stats.put("hardChallenges", hardChallenges);

        return stats;
    }

    @Override
    public Long getChallengeCountByCompetition(Integer competitionId) {
        return challengeRepository.countByCompetition(competitionId);
    }

    @Override
    public List<String> getCategoriesByCompetition(Integer competitionId) {
        return challengeRepository.findDistinctCategoriesByCompetition(competitionId);
    }

    @Override
    public boolean isChallengeActive(Integer challengeId) {
        Optional<Challenge> challengeOpt = challengeRepository.findById(challengeId);
        return challengeOpt.map(Challenge::getIsActive).orElse(false);
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
}