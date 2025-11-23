package com.CTF.j_ctf.service.impl;

import com.CTF.j_ctf.entity.*;
import com.CTF.j_ctf.repository.*;
import com.CTF.j_ctf.service.FlagService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.data.domain.PageImpl;

import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.ThreadLocalRandom;
import java.util.stream.Collectors;

@Service
@Transactional
public class FlagServiceImpl implements FlagService {
    private final FlagRepository flagRepository;
    private final FlagSubmissionRepository flagSubmissionRepository;
    private final UserRepository userRepository;
    private final CompetitionRepository competitionRepository;
    private final TeamRepository teamRepository;
    private final ScoreRepository scoreRepository;
    private final ChallengeRepository challengeRepository;

    public FlagServiceImpl(FlagRepository flagRepository,
                           FlagSubmissionRepository flagSubmissionRepository,
                           UserRepository userRepository,
                           CompetitionRepository competitionRepository,
                           TeamRepository teamRepository,
                           ScoreRepository scoreRepository,
                           ChallengeRepository challengeRepository) {
        this.flagRepository = flagRepository;
        this.flagSubmissionRepository = flagSubmissionRepository;
        this.userRepository = userRepository;
        this.competitionRepository = competitionRepository;
        this.teamRepository = teamRepository;
        this.scoreRepository = scoreRepository;
        this.challengeRepository = challengeRepository;
    }

    @Override
    public Flag createFlag(Flag flag) {
        // 验证数据
        validateFlag(flag);

        // 检查Flag值是否唯一
        if (flagRepository.existsByValue(flag.getValue())) {
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
            if (flagRepository.existsByValue(flag.getValue())) {
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

        if (flag.getTeam() != null) {
            existing.setTeam(flag.getTeam());
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

        // 创建提交记录
        FlagSubmission submission = new FlagSubmission();
        submission.setUser(user);
        submission.setCompetition(flag.getCompetition());
        submission.setChallenge(flag.getChallenge());
        submission.setFlag(flag);
        submission.setSubmittedContent(submittedValue);
        submission.setIpAddress(ipAddress);
        submission.setUserAgent(userAgent);

        boolean isCorrect = flag.getValue().equals(submittedValue);

        if (isCorrect) {
            submission.markAsCorrect(flag.getPoints());
            // 标记Flag为已使用
            flag.markAsUsed();
            flagRepository.save(flag);

            // 记录得分
            Score score = new Score(user, flag.getCompetition(), flag.getPoints());
            if (flag.getTeam() != null) {
                // 如果是战队Flag，为战队所有成员记录分数
                score.setTeam(flag.getTeam());
                for (User member : flag.getTeam().getMembers()) {
                    Score teamScore = new Score(member, flag.getCompetition(), flag.getPoints());
                    teamScore.setTeam(flag.getTeam());
                    scoreRepository.save(teamScore);
                }
            } else {
                scoreRepository.save(score);
            }
        } else {
            submission.markAsIncorrect();
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

        // 创建提交记录
        FlagSubmission submission = new FlagSubmission();
        submission.setUser(captain);
        submission.setCompetition(flag.getCompetition());
        submission.setChallenge(flag.getChallenge());
        submission.setFlag(flag);
        submission.setTeam(team);
        submission.setSubmittedContent(submittedValue);
        submission.setIpAddress(ipAddress);
        submission.setUserAgent(userAgent);

        boolean isCorrect = flag.getValue().equals(submittedValue);

        if (isCorrect) {
            submission.markAsCorrect(flag.getPoints());
            // 标记Flag为已使用
            flag.markAsUsed();
            flagRepository.save(flag);

            // 为战队所有成员记录分数
            for (User member : team.getMembers()) {
                Score score = new Score(member, flag.getCompetition(), flag.getPoints());
                score.setTeam(team);
                scoreRepository.save(score);
            }
        } else {
            submission.markAsIncorrect();
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
        return flagRepository.findByValueContainingOrDescriptionContaining(keyword, keyword, pageable);
    }

    @Override
    @org.springframework.transaction.annotation.Transactional(readOnly = true)
    public Page<FlagSubmission> getAllSubmissions(Pageable pageable) {
        try {
            // 使用 JOIN FETCH 预先加载所有关联
            List<FlagSubmission> allSubmissions = flagSubmissionRepository.findAllWithAssociations();
            
            // 手动实现分页
            int total = allSubmissions.size();
            int start = (int) pageable.getOffset();
            int end = Math.min(start + pageable.getPageSize(), total);
            
            List<FlagSubmission> pagedSubmissions = start < total 
                ? allSubmissions.subList(start, end) 
                : new ArrayList<>();
            
            return new PageImpl<>(pagedSubmissions, pageable, total);
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException("查询所有提交记录失败: " + e.getMessage(), e);
        }
    }

    @Override
    @org.springframework.transaction.annotation.Transactional(readOnly = true)
    public Page<FlagSubmission> getSubmissionsByUser(Integer userId, Pageable pageable) {
        if (userId == null) {
            throw new IllegalArgumentException("用户ID不能为空");
        }
        try {
            // 使用 JOIN FETCH 预先加载所有关联
            List<FlagSubmission> allSubmissions = flagSubmissionRepository.findByUser_UserIDWithAssociations(userId);
            
            // 手动实现分页
            int total = allSubmissions.size();
            int start = (int) pageable.getOffset();
            int end = Math.min(start + pageable.getPageSize(), total);
            
            List<FlagSubmission> pagedSubmissions = start < total 
                ? allSubmissions.subList(start, end) 
                : new ArrayList<>();
            
            return new PageImpl<>(pagedSubmissions, pageable, total);
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException("查询用户提交记录失败: " + e.getMessage(), e);
        }
    }

    @Override
    @org.springframework.transaction.annotation.Transactional(readOnly = true)
    public Page<FlagSubmission> getUserSubmissionsByCompetition(Integer userId, Integer competitionId, Pageable pageable) {
        if (userId == null || competitionId == null) {
            throw new IllegalArgumentException("用户ID和竞赛ID不能为空");
        }
        try {
            // 使用 JOIN FETCH 预先加载所有关联
            List<FlagSubmission> allSubmissions = flagSubmissionRepository
                .findByUser_UserIDAndCompetition_CompetitionIDWithAssociations(userId, competitionId);
            
            // 手动实现分页
            int total = allSubmissions.size();
            int start = (int) pageable.getOffset();
            int end = Math.min(start + pageable.getPageSize(), total);
            
            List<FlagSubmission> pagedSubmissions = start < total 
                ? allSubmissions.subList(start, end) 
                : new ArrayList<>();
            
            return new PageImpl<>(pagedSubmissions, pageable, total);
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException("查询用户竞赛提交记录失败: " + e.getMessage(), e);
        }
    }

    @Override
    public Page<FlagSubmission> getSubmissionsByTeam(Integer teamId, Pageable pageable) {
        return flagSubmissionRepository.findByTeam_TeamID(teamId, pageable);
    }

    @Override
    @org.springframework.transaction.annotation.Transactional(readOnly = true)
    public Page<FlagSubmission> getSubmissionsByCompetition(Integer competitionId, Pageable pageable) {
        if (competitionId == null) {
            throw new IllegalArgumentException("竞赛ID不能为空");
        }
        try {
            // 使用 JOIN FETCH 预先加载所有关联
            List<FlagSubmission> allSubmissions = flagSubmissionRepository
                .findByCompetition_CompetitionIDWithAssociations(competitionId);
            
            // 手动实现分页
            int total = allSubmissions.size();
            int start = (int) pageable.getOffset();
            int end = Math.min(start + pageable.getPageSize(), total);
            
            List<FlagSubmission> pagedSubmissions = start < total 
                ? allSubmissions.subList(start, end) 
                : new ArrayList<>();
            
            return new PageImpl<>(pagedSubmissions, pageable, total);
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException("查询竞赛提交记录失败: " + e.getMessage(), e);
        }
    }

    @Override
    @org.springframework.transaction.annotation.Transactional(readOnly = true)
    public Page<FlagSubmission> getSubmissionsByFlag(Integer flagId, Pageable pageable) {
        if (flagId == null) {
            throw new IllegalArgumentException("Flag ID不能为空");
        }
        try {
            // 使用 JOIN FETCH 预先加载所有关联
            List<FlagSubmission> allSubmissions = flagSubmissionRepository
                .findByFlag_FlagIDWithAssociations(flagId);
            
            // 手动实现分页
            int total = allSubmissions.size();
            int start = (int) pageable.getOffset();
            int end = Math.min(start + pageable.getPageSize(), total);
            
            List<FlagSubmission> pagedSubmissions = start < total 
                ? allSubmissions.subList(start, end) 
                : new ArrayList<>();
            
            return new PageImpl<>(pagedSubmissions, pageable, total);
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException("查询Flag提交记录失败: " + e.getMessage(), e);
        }
    }

    @Override
    public boolean canUserSubmitFlag(Integer flagId, Integer userId) {
        Optional<Flag> flagOpt = flagRepository.findById(flagId);
        Optional<User> userOpt = userRepository.findById(userId);

        if (flagOpt.isEmpty() || userOpt.isEmpty()) {
            return false;
        }

        Flag flag = flagOpt.get();
        User user = userOpt.get();

        // 检查Flag状态
        if (!flag.canBeUsed()) {
            return false;
        }

        // 检查用户是否已经解答过该Flag
        if (hasUserSolvedFlag(flagId, userId)) {
            return false;
        }

        // 检查竞赛状态
        Competition competition = flag.getCompetition();
        if (!competition.isOngoing() && !competition.isUpcoming()) {
            return false;
        }

        // 检查Flag是否过期
        if (flag.isExpired()) {
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
        Competition competition = flag.getCompetition();
        if (!competition.isOngoing() && !competition.isUpcoming()) {
            return false;
        }

        // 检查Flag是否过期
        if (flag.isExpired()) {
            return false;
        }

        // 检查战队审核状态
        if (!"APPROVED".equals(team.getAuditState())) {
            return false;
        }

        return true;
    }

    @Override
    public String getSubmitRestrictionReason(Integer flagId, Integer userId) {
        Optional<Flag> flagOpt = flagRepository.findById(flagId);
        Optional<User> userOpt = userRepository.findById(userId);

        if (flagOpt.isEmpty() || userOpt.isEmpty()) {
            return "Flag或用户不存在";
        }

        Flag flag = flagOpt.get();
        User user = userOpt.get();

        // 检查Flag状态
        if (!flag.canBeUsed()) {
            if (flag.getStatus() == 1) {
                return "Flag已被使用";
            } else if (flag.getStatus() == 2) {
                return "Flag已过期";
            }
        }

        // 检查用户是否已经解答过该Flag
        if (hasUserSolvedFlag(flagId, userId)) {
            return "您已经解答过该Flag";
        }

        // 检查竞赛状态
        Competition competition = flag.getCompetition();
        if (!competition.isOngoing() && !competition.isUpcoming()) {
            return "竞赛已结束或未开始";
        }

        // 检查Flag是否过期
        if (flag.isExpired()) {
            return "Flag已过期";
        }

        return "可以提交";
    }

    @Override
    public boolean hasUserSolvedFlag(Integer flagId, Integer userId) {
        return flagSubmissionRepository.existsByUser_UserIDAndFlag_FlagIDAndIsCorrectTrue(userId, flagId);
    }

    @Override
    public boolean hasTeamSolvedFlag(Integer flagId, Integer teamId) {
        return flagSubmissionRepository.existsByTeam_TeamIDAndFlag_FlagIDAndIsCorrectTrue(teamId, flagId);
    }

    @Override
    public boolean isUserCompetitionCreator(Integer competitionId, Integer userId) {
        Optional<Competition> competitionOpt = competitionRepository.findById(competitionId);
        return competitionOpt.map(competition ->
                competition.getCreator().getUserID().equals(userId)
        ).orElse(false);
    }

    @Override
    public void expireFlags() {
        List<Flag> expiredFlags = flagRepository.findByExpireTimeBeforeAndStatusNot(LocalDateTime.now(), 2);
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

        Long totalFlags = flagRepository.countByCompetition_CompetitionID(competitionId);
        Long usedFlags = flagRepository.countByCompetition_CompetitionIDAndStatus(competitionId, 1);
        Long expiredFlags = flagRepository.countByCompetition_CompetitionIDAndStatus(competitionId, 2);
        Long availableFlags = flagRepository.countByCompetition_CompetitionIDAndStatus(competitionId, 0);

        stats.put("totalFlags", totalFlags);
        stats.put("usedFlags", usedFlags);
        stats.put("expiredFlags", expiredFlags);
        stats.put("availableFlags", availableFlags);
        stats.put("usageRate", totalFlags > 0 ? (double) usedFlags / totalFlags * 100 : 0);

        // 获取提交统计
        Long totalSubmissions = flagSubmissionRepository.countByCompetition_CompetitionID(competitionId);
        Long correctSubmissions = flagSubmissionRepository.countByCompetition_CompetitionIDAndIsCorrectTrue(competitionId);

        stats.put("totalSubmissions", totalSubmissions);
        stats.put("correctSubmissions", correctSubmissions);
        stats.put("successRate", totalSubmissions > 0 ? (double) correctSubmissions / totalSubmissions * 100 : 0);

        return stats;
    }

    @Override
    public Map<String, Object> getUserFlagStatistics(Integer userId, Integer competitionId) {
        Map<String, Object> stats = new HashMap<>();

        Long totalSubmissions = flagSubmissionRepository.countByUser_UserIDAndCompetition_CompetitionID(userId, competitionId);
        Long correctSubmissions = flagSubmissionRepository.countByUser_UserIDAndCompetition_CompetitionIDAndIsCorrectTrue(userId, competitionId);
        Long flagsSolved = flagSubmissionRepository.countDistinctFlagByUserAndCompetitionAndCorrect(userId, competitionId);

        stats.put("totalSubmissions", totalSubmissions);
        stats.put("correctSubmissions", correctSubmissions);
        stats.put("flagsSolved", flagsSolved);
        stats.put("successRate", totalSubmissions > 0 ? (double) correctSubmissions / totalSubmissions * 100 : 0);

        // 获取用户总分
        Integer totalPoints = scoreRepository.sumPointsByUserAndCompetition(userId, competitionId);
        stats.put("totalPoints", totalPoints != null ? totalPoints : 0);

        return stats;
    }

    @Override
    public Map<String, Object> getTeamFlagStatistics(Integer teamId, Integer competitionId) {
        Map<String, Object> stats = new HashMap<>();

        Long totalSubmissions = flagSubmissionRepository.countByTeam_TeamIDAndCompetition_CompetitionID(teamId, competitionId);
        Long correctSubmissions = flagSubmissionRepository.countByTeam_TeamIDAndCompetition_CompetitionIDAndIsCorrectTrue(teamId, competitionId);
        Long flagsSolved = flagSubmissionRepository.countDistinctFlagByTeamAndCompetitionAndCorrect(teamId, competitionId);

        stats.put("totalSubmissions", totalSubmissions);
        stats.put("correctSubmissions", correctSubmissions);
        stats.put("flagsSolved", flagsSolved);
        stats.put("successRate", totalSubmissions > 0 ? (double) correctSubmissions / totalSubmissions * 100 : 0);

        // 获取战队总分
        Integer totalPoints = scoreRepository.sumPointsByTeamAndCompetition(teamId, competitionId);
        stats.put("totalPoints", totalPoints != null ? totalPoints : 0);

        return stats;
    }

    @Override
    public Page<Map<String, Object>> getCompetitionLeaderboard(Integer competitionId, Pageable pageable) {
        try {
            // 使用团队排行榜查询（接口投影）
            Page<ScoreRepository.TeamRankingProjection> summaryPage = scoreRepository.findTeamRankingByCompetition(competitionId, pageable);

            if (summaryPage == null || summaryPage.getContent() == null || summaryPage.getContent().isEmpty()) {
                // 如果没有数据，返回空的分页结果
                System.out.println("排行榜查询结果为空，competitionId: " + competitionId);
                return new PageImpl<>(new ArrayList<>(), pageable, 0);
            }

            // 获取所有团队数据以计算排名和解题数
            List<ScoreRepository.TeamRankingProjection> allTeams = scoreRepository.findTeamRankingByCompetition(competitionId);
            
            // 创建团队ID到解题数的映射（统计不同的ChallengeID）
            Map<Integer, Long> teamSolveCountMap = new HashMap<>();
            for (ScoreRepository.TeamRankingProjection team : allTeams) {
                if (team.getEntityID() != null) {
                    Long solveCount = scoreRepository.countDistinctChallengesByTeamAndCompetition(
                        team.getEntityID(), competitionId);
                    teamSolveCountMap.put(team.getEntityID(), solveCount != null ? solveCount : 0L);
                }
            }

            // 计算排名（基于总分，相同分数按提交时间排序）
            final List<ScoreRepository.TeamRankingProjection> sortedTeams = new ArrayList<>(allTeams);
            sortedTeams.sort((a, b) -> {
                int scoreCompare = Integer.compare(
                    b.getTotalScore() != null ? b.getTotalScore() : 0,
                    a.getTotalScore() != null ? a.getTotalScore() : 0
                );
                if (scoreCompare != 0) {
                    return scoreCompare;
                }
                // 如果分数相同，按最后提交时间排序（早提交的排名靠前）
                if (a.getLastSubmitTime() != null && b.getLastSubmitTime() != null) {
                    return a.getLastSubmitTime().compareTo(b.getLastSubmitTime());
                }
                return 0;
            });

            // 创建团队ID到排名的映射（处理相同分数的情况，相同分数使用相同排名）
            Map<Integer, Integer> teamRankMap = new HashMap<>();
            int currentRank = 1;
            Integer previousScore = null;
            Integer previousRank = null;
            
            for (int i = 0; i < sortedTeams.size(); i++) {
                ScoreRepository.TeamRankingProjection team = sortedTeams.get(i);
                if (team.getEntityID() == null) {
                    continue;
                }
                
                Integer currentScore = team.getTotalScore() != null ? team.getTotalScore() : 0;
                
                // 如果当前分数和上一个分数相同，使用相同的排名
                if (previousScore != null && currentScore.equals(previousScore)) {
                    teamRankMap.put(team.getEntityID(), previousRank);
                } else {
                    // 分数不同，排名递增
                    currentRank = i + 1;
                    previousRank = currentRank;
                    previousScore = currentScore;
                    teamRankMap.put(team.getEntityID(), currentRank);
                }
            }

            // 映射当前页的数据
            List<Map<String, Object>> mapped = summaryPage.getContent().stream()
                    .filter(ss -> ss != null && ss.getEntityID() != null) // 过滤null值
                    .map(ss -> {
                        Map<String, Object> m = new HashMap<>();
                        m.put("entityID", ss.getEntityID());
                        m.put("name", ss.getName() != null ? ss.getName() : "未知团队");
                        m.put("totalScore", ss.getTotalScore() != null ? ss.getTotalScore() : 0);
                        m.put("entityType", "TEAM");
                        m.put("competitionID", competitionId);
                        // 获取解题数
                        Long solveCount = teamSolveCountMap.get(ss.getEntityID());
                        m.put("solveCount", solveCount != null ? solveCount.intValue() : 0);
                        m.put("lastSubmitTime", ss.getLastSubmitTime());
                        // 获取排名
                        Integer rank = teamRankMap.get(ss.getEntityID());
                        m.put("rank", rank != null ? rank : 0);
                        return m;
                    })
                    .collect(Collectors.toList());

            System.out.println("排行榜查询成功，返回 " + mapped.size() + " 条记录，competitionId: " + competitionId);
            return new PageImpl<>(mapped, pageable, summaryPage.getTotalElements());
        } catch (Exception e) {
            e.printStackTrace();
            System.err.println("获取排行榜失败: " + e.getMessage());
            // 记录错误并返回空的分页结果
            return new PageImpl<>(new ArrayList<>(), pageable, 0);
        }
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

        if (flag.getUser() == null) {
            throw new IllegalArgumentException("必须指定创建者");
        }

        if (flag.getExpireTime() != null && flag.getExpireTime().isBefore(LocalDateTime.now())) {
            throw new IllegalArgumentException("过期时间不能早于当前时间");
        }
    }
}

