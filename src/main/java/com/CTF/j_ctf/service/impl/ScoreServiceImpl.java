package com.CTF.j_ctf.service.impl;

import com.CTF.j_ctf.entity.*;
import com.CTF.j_ctf.repository.*;
import com.CTF.j_ctf.service.ScoreService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
@Transactional
public class ScoreServiceImpl implements ScoreService {
    private final ScoreRepository scoreRepository;
    private final UserRepository userRepository;
    private final CompetitionRepository competitionRepository;
    private final TeamRepository teamRepository;
    private final ChallengeRepository challengeRepository;
    private final FlagRepository flagRepository;

    public ScoreServiceImpl(ScoreRepository scoreRepository,
                            UserRepository userRepository,
                            CompetitionRepository competitionRepository,
                            TeamRepository teamRepository,
                            ChallengeRepository challengeRepository,
                            FlagRepository flagRepository) {
        this.scoreRepository = scoreRepository;
        this.userRepository = userRepository;
        this.competitionRepository = competitionRepository;
        this.teamRepository = teamRepository;
        this.challengeRepository = challengeRepository;
        this.flagRepository = flagRepository;
    }

    @Override
    public Score createScore(Score score) {
        // 验证数据
        validateScore(score);

        return scoreRepository.save(score);
    }

    @Override
    public Score updateScore(Score score) {
        Optional<Score> existingOpt = scoreRepository.findById(score.getChangeID());
        if (existingOpt.isEmpty()) {
            throw new IllegalArgumentException("分数记录不存在");
        }

        Score existing = existingOpt.get();

        // 更新允许修改的字段
        if (score.getScoreValue() != null) {
            existing.setScoreValue(score.getScoreValue());
        }

        if (score.getDescription() != null) {
            existing.setDescription(score.getDescription());
        }

        if (score.getType() != null) {
            existing.setType(score.getType());
        }

        if (score.getIsValid() != null) {
            existing.setIsValid(score.getIsValid());
        }

        if (score.getChallenge() != null) {
            existing.setChallenge(score.getChallenge());
        }

        if (score.getFlag() != null) {
            existing.setFlag(score.getFlag());
        }

        if (score.getTeam() != null) {
            existing.setTeam(score.getTeam());
        }

        return scoreRepository.save(existing);
    }

    @Override
    public boolean deleteScore(Integer scoreId) {
        if (!scoreRepository.existsById(scoreId)) {
            return false;
        }

        scoreRepository.deleteById(scoreId);
        return true;
    }

    @Override
    public Optional<Score> getScoreById(Integer scoreId) {
        return scoreRepository.findById(scoreId);
    }

    @Override
    public Page<Score> getAllScores(Pageable pageable) {
        return scoreRepository.findAll(pageable);
    }

    @Override
    public Page<Score> getScoresByUser(Integer userId, Pageable pageable) {
        return scoreRepository.findByUser_UserID(userId, pageable);
    }

    @Override
    public Page<Score> getUserScoresByCompetition(Integer userId, Integer competitionId, Pageable pageable) {
        return scoreRepository.findByUser_UserIDAndCompetition_CompetitionID(userId, competitionId, pageable);
    }

    @Override
    public Page<Score> getScoresByTeam(Integer teamId, Pageable pageable) {
        return scoreRepository.findByTeam_TeamID(teamId, pageable);
    }

    @Override
    public Page<Score> getScoresByCompetition(Integer competitionId, Pageable pageable) {
        return scoreRepository.findByCompetition_CompetitionID(competitionId, pageable);
    }

    @Override
    public Page<Score> getScoresByValidity(Boolean isValid, Pageable pageable) {
        return scoreRepository.findByIsValid(isValid, pageable);
    }

    @Override
    public boolean invalidateScore(Integer scoreId) {
        Optional<Score> scoreOpt = scoreRepository.findById(scoreId);
        if (scoreOpt.isEmpty()) {
            return false;
        }

        Score score = scoreOpt.get();
        score.invalidate();
        scoreRepository.save(score);
        return true;
    }

    @Override
    public boolean restoreScore(Integer scoreId) {
        Optional<Score> scoreOpt = scoreRepository.findById(scoreId);
        if (scoreOpt.isEmpty()) {
            return false;
        }

        Score score = scoreOpt.get();
        score.setIsValid(true);
        scoreRepository.save(score);
        return true;
    }

    @Override
    public Integer getTotalScoreByUserAndCompetition(Integer userId, Integer competitionId) {
        return scoreRepository.sumScoreValueByUserAndCompetitionAndIsValidTrue(userId, competitionId);
    }

    @Override
    public Integer getTotalScoreByTeamAndCompetition(Integer teamId, Integer competitionId) {
        return scoreRepository.sumScoreValueByTeamAndCompetitionAndIsValidTrue(teamId, competitionId);
    }

    @Override
    public Page<ScoreSummary> getUserRankingByCompetition(Integer competitionId, Pageable pageable) {
        return scoreRepository.findUserRankingByCompetition(competitionId, pageable);
    }

    @Override
    public Page<ScoreSummary> getTeamRankingByCompetition(Integer competitionId, Pageable pageable) {
        // 将接口投影转换为ScoreSummary实体
        Page<ScoreRepository.TeamRankingProjection> projectionPage = scoreRepository.findTeamRankingByCompetition(competitionId, pageable);
        
        // 转换为ScoreSummary列表
        List<ScoreSummary> scoreSummaries = projectionPage.getContent().stream()
                .map(proj -> {
                    ScoreSummary summary = new ScoreSummary();
                    summary.setEntityID(proj.getEntityID());
                    summary.setEntityName(proj.getName());
                    summary.setTotalScore(proj.getTotalScore());
                    summary.setLastSubmitTime(proj.getLastSubmitTime());
                    summary.setEntityType("TEAM");
                    summary.setCompetitionID(competitionId);
                    return summary;
                })
                .collect(java.util.stream.Collectors.toList());
        
        return new org.springframework.data.domain.PageImpl<>(scoreSummaries, pageable, projectionPage.getTotalElements());
    }

    @Override
    public Page<ScoreSummary> getOverallUserRanking(Pageable pageable) {
        return scoreRepository.findOverallUserRanking(pageable);
    }

    @Override
    public Page<ScoreSummary> getOverallTeamRanking(Pageable pageable) {
        return scoreRepository.findOverallTeamRanking(pageable);
    }

    @Override
    public Map<String, Object> getUserScoreStatistics(Integer userId, Integer competitionId) {
        Map<String, Object> stats = new HashMap<>();

        // 总分数
        Integer totalScore = getTotalScoreByUserAndCompetition(userId, competitionId);
        stats.put("totalScore", totalScore != null ? totalScore : 0);

        // 解题数量
        Long solveCount = scoreRepository.countByUser_UserIDAndCompetition_CompetitionIDAndTypeAndIsValidTrue(
                userId, competitionId, "FLAG_SUBMISSION");
        stats.put("solveCount", solveCount);

        // 挑战解决数量
        Long challengeSolveCount = scoreRepository.countByUser_UserIDAndCompetition_CompetitionIDAndTypeAndIsValidTrue(
                userId, competitionId, "CHALLENGE_SOLVE");
        stats.put("challengeSolveCount", challengeSolveCount);

        // 奖励分数
        Integer bonusScore = scoreRepository.sumScoreValueByUserAndCompetitionAndTypeAndIsValidTrue(
                userId, competitionId, "BONUS");
        stats.put("bonusScore", bonusScore != null ? bonusScore : 0);

        // 处罚分数
        Integer penaltyScore = scoreRepository.sumScoreValueByUserAndCompetitionAndTypeAndIsValidTrue(
                userId, competitionId, "PENALTY");
        stats.put("penaltyScore", penaltyScore != null ? penaltyScore : 0);

        // 调整分数
        Integer adjustmentScore = scoreRepository.sumScoreValueByUserAndCompetitionAndTypeAndIsValidTrue(
                userId, competitionId, "ADJUSTMENT");
        stats.put("adjustmentScore", adjustmentScore != null ? adjustmentScore : 0);

        // 最后提交时间
        LocalDateTime lastSubmitTime = scoreRepository.findLastSubmitTimeByUserAndCompetition(userId, competitionId);
        stats.put("lastSubmitTime", lastSubmitTime);

        // 用户排名
        Integer rank = getUserRank(userId, competitionId);
        stats.put("rank", rank);

        return stats;
    }

    @Override
    public Map<String, Object> getTeamScoreStatistics(Integer teamId, Integer competitionId) {
        Map<String, Object> stats = new HashMap<>();

        // 总分数
        Integer totalScore = getTotalScoreByTeamAndCompetition(teamId, competitionId);
        stats.put("totalScore", totalScore != null ? totalScore : 0);

        // 解题数量
        Long solveCount = scoreRepository.countByTeam_TeamIDAndCompetition_CompetitionIDAndTypeAndIsValidTrue(
                teamId, competitionId, "FLAG_SUBMISSION");
        stats.put("solveCount", solveCount);

        // 挑战解决数量
        Long challengeSolveCount = scoreRepository.countByTeam_TeamIDAndCompetition_CompetitionIDAndTypeAndIsValidTrue(
                teamId, competitionId, "CHALLENGE_SOLVE");
        stats.put("challengeSolveCount", challengeSolveCount);

        // 奖励分数
        Integer bonusScore = scoreRepository.sumScoreValueByTeamAndCompetitionAndTypeAndIsValidTrue(
                teamId, competitionId, "BONUS");
        stats.put("bonusScore", bonusScore != null ? bonusScore : 0);

        // 处罚分数
        Integer penaltyScore = scoreRepository.sumScoreValueByTeamAndCompetitionAndTypeAndIsValidTrue(
                teamId, competitionId, "PENALTY");
        stats.put("penaltyScore", penaltyScore != null ? penaltyScore : 0);

        // 调整分数
        Integer adjustmentScore = scoreRepository.sumScoreValueByTeamAndCompetitionAndTypeAndIsValidTrue(
                teamId, competitionId, "ADJUSTMENT");
        stats.put("adjustmentScore", adjustmentScore != null ? adjustmentScore : 0);

        // 最后提交时间
        LocalDateTime lastSubmitTime = scoreRepository.findLastSubmitTimeByTeamAndCompetition(teamId, competitionId);
        stats.put("lastSubmitTime", lastSubmitTime);

        // 战队排名
        Integer rank = getTeamRank(teamId, competitionId);
        stats.put("rank", rank);

        // 成员数量
        Optional<Team> teamOpt = teamRepository.findById(teamId);
        if (teamOpt.isPresent()) {
            stats.put("memberCount", teamOpt.get().getMembers().size());
        }

        return stats;
    }

    @Override
    public Map<String, Object> getCompetitionScoreStatistics(Integer competitionId) {
        Map<String, Object> stats = new HashMap<>();

        // 总分数记录数
        Long totalScoreRecords = scoreRepository.countByCompetition_CompetitionID(competitionId);
        stats.put("totalScoreRecords", totalScoreRecords);

        // 有效分数记录数
        Long validScoreRecords = scoreRepository.countByCompetition_CompetitionIDAndIsValidTrue(competitionId);
        stats.put("validScoreRecords", validScoreRecords);

        // 无效分数记录数
        Long invalidScoreRecords = scoreRepository.countByCompetition_CompetitionIDAndIsValidFalse(competitionId);
        stats.put("invalidScoreRecords", invalidScoreRecords);

        // 参赛用户数
        Long participantCount = scoreRepository.countDistinctUsersByCompetition(competitionId);
        stats.put("participantCount", participantCount);

        // 参赛战队数
        Long teamCount = scoreRepository.countDistinctTeamsByCompetition(competitionId);
        stats.put("teamCount", teamCount);

        // 平均分数
        Double averageScore = scoreRepository.averageScoreByCompetitionAndIsValidTrue(competitionId);
        stats.put("averageScore", averageScore != null ? averageScore : 0);

        // 最高分数
        Integer maxScore = scoreRepository.maxScoreByCompetitionAndIsValidTrue(competitionId);
        stats.put("maxScore", maxScore != null ? maxScore : 0);

        // 最低分数
        Integer minScore = scoreRepository.minScoreByCompetitionAndIsValidTrue(competitionId);
        stats.put("minScore", minScore != null ? minScore : 0);

        // 分数类型分布
        Map<String, Long> scoreTypeDistribution = new HashMap<>();
        scoreTypeDistribution.put("FLAG_SUBMISSION",
                scoreRepository.countByCompetition_CompetitionIDAndTypeAndIsValidTrue(competitionId, "FLAG_SUBMISSION"));
        scoreTypeDistribution.put("CHALLENGE_SOLVE",
                scoreRepository.countByCompetition_CompetitionIDAndTypeAndIsValidTrue(competitionId, "CHALLENGE_SOLVE"));
        scoreTypeDistribution.put("BONUS",
                scoreRepository.countByCompetition_CompetitionIDAndTypeAndIsValidTrue(competitionId, "BONUS"));
        scoreTypeDistribution.put("PENALTY",
                scoreRepository.countByCompetition_CompetitionIDAndTypeAndIsValidTrue(competitionId, "PENALTY"));
        scoreTypeDistribution.put("ADJUSTMENT",
                scoreRepository.countByCompetition_CompetitionIDAndTypeAndIsValidTrue(competitionId, "ADJUSTMENT"));
        stats.put("scoreTypeDistribution", scoreTypeDistribution);

        return stats;
    }

    @Override
    public Score adjustUserScore(Integer userId, Integer competitionId, Integer points, String description) {
        Optional<User> userOpt = userRepository.findById(userId);
        Optional<Competition> competitionOpt = competitionRepository.findById(competitionId);

        if (userOpt.isEmpty() || competitionOpt.isEmpty()) {
            throw new IllegalArgumentException("用户或竞赛不存在");
        }

        Score score = new Score(userOpt.get(), competitionOpt.get(), points);
        score.setType("ADJUSTMENT");
        score.setDescription(description != null ? description : "管理员手动调整分数");

        return scoreRepository.save(score);
    }

    @Override
    public Score adjustTeamScore(Integer teamId, Integer competitionId, Integer points, String description) {
        Optional<Team> teamOpt = teamRepository.findById(teamId);
        Optional<Competition> competitionOpt = competitionRepository.findById(competitionId);

        if (teamOpt.isEmpty() || competitionOpt.isEmpty()) {
            throw new IllegalArgumentException("战队或竞赛不存在");
        }

        Team team = teamOpt.get();
        Competition competition = competitionOpt.get();

        // 为战队所有成员创建分数记录
        Score firstScore = null;
        for (User member : team.getMembers()) {
            Score score = new Score(member, competition, points);
            score.setTeam(team);
            score.setType("ADJUSTMENT");
            score.setDescription(description != null ? description : "管理员手动调整战队分数");

            Score savedScore = scoreRepository.save(score);
            if (firstScore == null) {
                firstScore = savedScore;
            }
        }

        return firstScore;
    }

    @Override
    public Integer getUserRank(Integer userId, Integer competitionId) {
        try {
            List<ScoreSummary> rankings = scoreRepository.findUserRankingByCompetition(competitionId);
            
            if (rankings == null || rankings.isEmpty()) {
                return null; // 没有排行榜数据
            }

            for (int i = 0; i < rankings.size(); i++) {
                ScoreSummary summary = rankings.get(i);
                if (summary != null && summary.getEntityID() != null && summary.getEntityID().equals(userId)) {
                    return i + 1; // 排名从1开始
                }
            }

            return null; // 用户不在排行榜中
        } catch (Exception e) {
            // 记录错误但不抛出异常，返回null表示无法获取排名
            return null;
        }
    }

    @Override
    public Integer getTeamRank(Integer teamId, Integer competitionId) {
        // 获取所有团队数据
        List<ScoreRepository.TeamRankingProjection> projectionList = scoreRepository.findTeamRankingByCompetition(competitionId);
        
        if (projectionList == null || projectionList.isEmpty()) {
            return null; // 没有排行榜数据
        }
        
        // 按照和排行榜相同的逻辑排序：总分降序，相同分数按提交时间升序（早提交的排名靠前）
        List<ScoreRepository.TeamRankingProjection> sortedTeams = new ArrayList<>(projectionList);
        sortedTeams.sort((a, b) -> {
            // 先按总分降序排序
            int scoreCompare = Integer.compare(
                b.getTotalScore() != null ? b.getTotalScore() : 0,
                a.getTotalScore() != null ? a.getTotalScore() : 0
            );
            if (scoreCompare != 0) {
                return scoreCompare;
            }
            // 如果分数相同，按最后提交时间升序排序（早提交的排名靠前）
            if (a.getLastSubmitTime() != null && b.getLastSubmitTime() != null) {
                return a.getLastSubmitTime().compareTo(b.getLastSubmitTime());
            }
            // 如果提交时间为null，有提交时间的排在前面
            if (a.getLastSubmitTime() != null) {
                return -1;
            }
            if (b.getLastSubmitTime() != null) {
                return 1;
            }
            return 0;
        });
        
        // 计算排名（处理相同分数的情况）
        int currentRank = 1;
        Integer previousScore = null;
        Integer previousRank = null;
        
        for (int i = 0; i < sortedTeams.size(); i++) {
            ScoreRepository.TeamRankingProjection team = sortedTeams.get(i);
            Integer currentScore = team.getTotalScore() != null ? team.getTotalScore() : 0;
            
            // 如果当前分数和上一个分数相同，使用相同的排名
            if (previousScore != null && currentScore.equals(previousScore)) {
                // 使用上一个排名
                if (team.getEntityID() != null && team.getEntityID().equals(teamId)) {
                    return previousRank;
                }
            } else {
                // 分数不同，排名递增
                currentRank = i + 1;
                previousRank = currentRank;
                previousScore = currentScore;
                
                if (team.getEntityID() != null && team.getEntityID().equals(teamId)) {
                    return currentRank;
                }
            }
        }
        
        return null; // 战队不在排行榜中
    }

    @Override
    public List<Score> exportCompetitionScores(Integer competitionId) {
        return scoreRepository.findByCompetition_CompetitionID(competitionId);
    }

    @Override
    public boolean isUserCompetitionCreator(Integer competitionId, Integer userId) {
        Optional<Competition> competitionOpt = competitionRepository.findById(competitionId);
        return competitionOpt.map(competition ->
                competition.getCreator().getUserID().equals(userId)
        ).orElse(false);
    }

    /**
     * 验证分数数据的有效性
     */
    private void validateScore(Score score) {
        if (score.getScoreValue() == null) {
            throw new IllegalArgumentException("分数值不能为空");
        }

        if (score.getUser() == null) {
            throw new IllegalArgumentException("必须指定用户");
        }

        if (score.getCompetition() == null) {
            throw new IllegalArgumentException("必须指定竞赛");
        }

        if (score.getType() == null || score.getType().trim().isEmpty()) {
            throw new IllegalArgumentException("分数类型不能为空");
        }

        // 验证用户存在
        if (!userRepository.existsById(score.getUser().getUserID())) {
            throw new IllegalArgumentException("用户不存在");
        }

        // 验证竞赛存在
        if (!competitionRepository.existsById(score.getCompetition().getCompetitionID())) {
            throw new IllegalArgumentException("竞赛不存在");
        }

        // 验证战队存在（如果指定了战队）
        if (score.getTeam() != null && !teamRepository.existsById(score.getTeam().getTeamID())) {
            throw new IllegalArgumentException("战队不存在");
        }

        // 验证挑战存在（如果指定了挑战）
        if (score.getChallenge() != null && !challengeRepository.existsById(score.getChallenge().getChallengeID())) {
            throw new IllegalArgumentException("挑战不存在");
        }

        // 验证Flag存在（如果指定了Flag）
        if (score.getFlag() != null && !flagRepository.existsById(score.getFlag().getFlagID())) {
            throw new IllegalArgumentException("Flag不存在");
        }
    }
}