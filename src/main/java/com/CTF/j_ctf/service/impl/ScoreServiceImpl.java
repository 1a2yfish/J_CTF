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
    private final ScoreSummaryRepository scoreSummaryRepository;
    private final UserRepository userRepository;
    private final CompetitionRepository competitionRepository;
    private final TeamRepository teamRepository;
    private final ChallengeRepository challengeRepository;

    public ScoreServiceImpl(ScoreRepository scoreRepository,
                            ScoreSummaryRepository scoreSummaryRepository,
                            UserRepository userRepository,
                            CompetitionRepository competitionRepository,
                            TeamRepository teamRepository,
                            ChallengeRepository challengeRepository) {
        this.scoreRepository = scoreRepository;
        this.scoreSummaryRepository = scoreSummaryRepository;
        this.userRepository = userRepository;
        this.competitionRepository = competitionRepository;
        this.teamRepository = teamRepository;
        this.challengeRepository = challengeRepository;
    }

    @Override
    public Score createScore(Score score) {
        // 验证数据
        validateScore(score);

        // 设置默认值
        if (score.getType() == null) {
            score.setType("ADJUSTMENT");
        }

        if (score.getDescription() == null) {
            score.setDescription("分数调整");
        }

        return scoreRepository.save(score);
    }

    @Override
    public Score updateScore(Score score) {
        Optional<Score> existingOpt = scoreRepository.findById(score.getChangeID());
        if (existingOpt.isEmpty()) {
            throw new IllegalArgumentException("分数记录不存在");
        }

        Score existing = existingOpt.get();

        // 只能更新描述和类型
        if (score.getDescription() != null) {
            existing.setDescription(score.getDescription());
        }

        if (score.getType() != null) {
            existing.setType(score.getType());
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
    public boolean invalidateScore(Integer scoreId) {
        Optional<Score> scoreOpt = scoreRepository.findById(scoreId);
        if (scoreOpt.isEmpty()) {
            return false;
        }

        Score score = scoreOpt.get();
        score.setIsValid(false);
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
    public Page<Score> getScoresByTeam(Integer teamId, Pageable pageable) {
        return scoreRepository.findByTeam_TeamID(teamId, pageable);
    }

    @Override
    public Page<Score> getScoresByCompetition(Integer competitionId, Pageable pageable) {
        return scoreRepository.findByCompetition_CompetitionID(competitionId, pageable);
    }

    @Override
    public Page<Score> getScoresByChallenge(Integer challengeId, Pageable pageable) {
        // 需要自定义实现
        return scoreRepository.findAll(pageable); // 简化实现
    }

    @Override
    public Page<Score> searchScores(String keyword, Pageable pageable) {
        // 需要自定义实现
        return scoreRepository.findAll(pageable); // 简化实现
    }

    @Override
    public List<ScoreSummary> getUserRankingByCompetition(Integer competitionId) {
        return scoreSummaryRepository.findUserRankingByCompetition(competitionId);
    }

    @Override
    public List<ScoreSummary> getTeamRankingByCompetition(Integer competitionId) {
        return scoreSummaryRepository.findTeamRankingByCompetition(competitionId);
    }

    @Override
    public List<ScoreSummary> getOverallUserRanking() {
        return scoreSummaryRepository.findOverallUserRanking();
    }

    @Override
    public List<ScoreSummary> getOverallTeamRanking() {
        return scoreSummaryRepository.findOverallTeamRanking();
    }

    @Override
    public Integer getTotalScoreByUserAndCompetition(Integer userId, Integer competitionId) {
        Integer totalScore = scoreRepository.getTotalScoreByUserAndCompetition(userId, competitionId);
        return totalScore != null ? totalScore : 0;
    }

    @Override
    public Integer getTotalScoreByTeamAndCompetition(Integer teamId, Integer competitionId) {
        Integer totalScore = scoreRepository.getTotalScoreByTeamAndCompetition(teamId, competitionId);
        return totalScore != null ? totalScore : 0;
    }

    @Override
    public Map<String, Object> getUserScoreStatistics(Integer userId, Integer competitionId) {
        Map<String, Object> stats = new HashMap<>();

        Integer totalScore = getTotalScoreByUserAndCompetition(userId, competitionId);
        Long positiveScoreCount = scoreRepository.countPositiveScoresByUserAndCompetition(userId, competitionId);
        List<Score> recentScores = scoreRepository.findByUserAndCompetition(userId, competitionId);

        // 计算解题数量
        long challengeSolveCount = recentScores.stream()
                .filter(score -> score.getChallenge() != null && score.getScore() > 0)
                .map(score -> score.getChallenge().getChallengeID())
                .distinct()
                .count();

        // 计算最近得分时间
        Optional<LocalDateTime> lastScoreTime = recentScores.stream()
                .filter(score -> score.getScore() > 0)
                .map(Score::getCreateTime)
                .max(LocalDateTime::compareTo);

        stats.put("totalScore", totalScore);
        stats.put("positiveScoreCount", positiveScoreCount);
        stats.put("challengeSolveCount", challengeSolveCount);
        stats.put("lastScoreTime", lastScoreTime.orElse(null));
        stats.put("totalTransactions", recentScores.size());

        return stats;
    }

    @Override
    public Map<String, Object> getTeamScoreStatistics(Integer teamId, Integer competitionId) {
        Map<String, Object> stats = new HashMap<>();

        Integer totalScore = getTotalScoreByTeamAndCompetition(teamId, competitionId);
        Long positiveScoreCount = scoreRepository.countPositiveScoresByTeamAndCompetition(teamId, competitionId);
        List<Score> recentScores = scoreRepository.findByTeamAndCompetition(teamId, competitionId);

        // 计算解题数量
        long challengeSolveCount = recentScores.stream()
                .filter(score -> score.getChallenge() != null && score.getScore() > 0)
                .map(score -> score.getChallenge().getChallengeID())
                .distinct()
                .count();

        // 计算最近得分时间
        Optional<LocalDateTime> lastScoreTime = recentScores.stream()
                .filter(score -> score.getScore() > 0)
                .map(Score::getCreateTime)
                .max(LocalDateTime::compareTo);

        stats.put("totalScore", totalScore);
        stats.put("positiveScoreCount", positiveScoreCount);
        stats.put("challengeSolveCount", challengeSolveCount);
        stats.put("lastScoreTime", lastScoreTime.orElse(null));
        stats.put("totalTransactions", recentScores.size());

        return stats;
    }

    @Override
    public Map<String, Object> getCompetitionScoreStatistics(Integer competitionId) {
        Map<String, Object> stats = new HashMap<>();

        List<Score> competitionScores = scoreRepository.findByCompetition_CompetitionID(competitionId);

        // 计算总分数
        int totalScore = competitionScores.stream()
                .filter(Score::getIsValid)
                .mapToInt(Score::getScore)
                .sum();

        // 计算正分数
        int positiveScore = competitionScores.stream()
                .filter(Score::getIsValid)
                .filter(score -> score.getScore() > 0)
                .mapToInt(Score::getScore)
                .sum();

        // 计算负分数
        int negativeScore = competitionScores.stream()
                .filter(Score::getIsValid)
                .filter(score -> score.getScore() < 0)
                .mapToInt(Score::getScore)
                .sum();

        // 计算参与用户数
        long userCount = competitionScores.stream()
                .map(score -> score.getUser().getUserID())
                .distinct()
                .count();

        // 计算参与战队数
        long teamCount = competitionScores.stream()
                .filter(score -> score.getTeam() != null)
                .map(score -> score.getTeam().getTeamID())
                .distinct()
                .count();

        stats.put("totalScore", totalScore);
        stats.put("positiveScore", positiveScore);
        stats.put("negativeScore", negativeScore);
        stats.put("userCount", userCount);
        stats.put("teamCount", teamCount);
        stats.put("transactionCount", competitionScores.size());

        return stats;
    }

    @Override
    public Score adjustScore(Integer userId, Integer competitionId, Integer points, String description) {
        Optional<User> userOpt = userRepository.findById(userId);
        Optional<Competition> competitionOpt = competitionRepository.findById(competitionId);

        if (userOpt.isEmpty() || competitionOpt.isEmpty()) {
            throw new IllegalArgumentException("用户或竞赛不存在");
        }

        Score score = new Score(userOpt.get(), competitionOpt.get(), points);
        score.setDescription(description);
        score.setType("MANUAL_ADJUSTMENT");

        return scoreRepository.save(score);
    }

    @Override
    public Score adjustTeamScore(Integer teamId, Integer competitionId, Integer points, String description) {
        Optional<Team> teamOpt = teamRepository.findById(teamId);
        Optional<Competition> competitionOpt = competitionRepository.findById(competitionId);

        if (teamOpt.isEmpty() || competitionOpt.isEmpty()) {
            throw new IllegalArgumentException("战队或竞赛不存在");
        }

        // 为战队所有成员添加分数
        Team team = teamOpt.get();
        Score score = null;

        for (User member : team.getMembers()) {
            Score memberScore = new Score(member, competitionOpt.get(), points);
            memberScore.setTeam(team);
            memberScore.setDescription(description + " (战队调整)");
            memberScore.setType("TEAM_ADJUSTMENT");
            score = scoreRepository.save(memberScore);
        }

        return score; // 返回最后一个成员的分数记录
    }

    @Override
    public boolean canUserEarnScore(Integer userId, Integer competitionId) {
        Optional<Competition> competitionOpt = competitionRepository.findById(competitionId);
        if (competitionOpt.isEmpty()) {
            return false;
        }

        Competition competition = competitionOpt.get();

        // 检查竞赛状态
        if (!competition.isOngoing()) {
            return false;
        }

        // 检查用户是否被禁用（这里需要扩展用户实体）

        return true;
    }

    @Override
    public boolean canTeamEarnScore(Integer teamId, Integer competitionId) {
        Optional<Team> teamOpt = teamRepository.findById(teamId);
        Optional<Competition> competitionOpt = competitionRepository.findById(competitionId);

        if (teamOpt.isEmpty() || competitionOpt.isEmpty()) {
            return false;
        }

        Team team = teamOpt.get();
        Competition competition = competitionOpt.get();

        // 检查竞赛状态
        if (!competition.isOngoing()) {
            return false;
        }

        // 检查战队审核状态
        if (!team.isAuditApproved()) {
            return false;
        }

        return true;
    }

    /**
     * 验证分数数据的有效性
     */
    private void validateScore(Score score) {
        if (score.getUser() == null) {
            throw new IllegalArgumentException("必须指定用户");
        }

        if (score.getCompetition() == null) {
            throw new IllegalArgumentException("必须指定竞赛");
        }

        if (score.getScore() == null) {
            throw new IllegalArgumentException("分数值不能为空");
        }

        if (score.getScore() == 0) {
            throw new IllegalArgumentException("分数值不能为0");
        }
    }
}