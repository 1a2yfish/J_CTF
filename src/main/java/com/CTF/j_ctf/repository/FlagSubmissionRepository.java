package com.CTF.j_ctf.repository;

import com.CTF.j_ctf.entity.FlagSubmission;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Repository
public interface FlagSubmissionRepository extends JpaRepository<FlagSubmission, Integer> {

    // 单维度查询
    Page<FlagSubmission> findByUser_UserID(Integer userID, Pageable pageable);
    
    // 使用 JOIN FETCH 预先加载关联，避免 LazyInitializationException
    @Query("SELECT DISTINCT fs FROM FlagSubmission fs " +
           "LEFT JOIN FETCH fs.user " +
           "LEFT JOIN FETCH fs.challenge " +
           "LEFT JOIN FETCH fs.competition " +
           "LEFT JOIN FETCH fs.team " +
           "WHERE fs.user.userID = :userID " +
           "ORDER BY fs.submitTime DESC")
    List<FlagSubmission> findByUser_UserIDWithAssociations(@Param("userID") Integer userID);
    
    // 使用 JOIN FETCH 预先加载关联，带竞赛筛选
    @Query("SELECT DISTINCT fs FROM FlagSubmission fs " +
           "LEFT JOIN FETCH fs.user " +
           "LEFT JOIN FETCH fs.challenge " +
           "LEFT JOIN FETCH fs.competition " +
           "LEFT JOIN FETCH fs.team " +
           "WHERE fs.user.userID = :userID AND fs.competition.competitionID = :competitionId " +
           "ORDER BY fs.submitTime DESC")
    List<FlagSubmission> findByUser_UserIDAndCompetition_CompetitionIDWithAssociations(
        @Param("userID") Integer userID,
        @Param("competitionId") Integer competitionId
    );
    
    // 获取所有提交记录，使用 JOIN FETCH 预先加载关联
    @Query("SELECT DISTINCT fs FROM FlagSubmission fs " +
           "LEFT JOIN FETCH fs.user " +
           "LEFT JOIN FETCH fs.challenge " +
           "LEFT JOIN FETCH fs.competition " +
           "LEFT JOIN FETCH fs.team " +
           "ORDER BY fs.submitTime DESC")
    List<FlagSubmission> findAllWithAssociations();
    
    // 按竞赛获取提交记录，使用 JOIN FETCH 预先加载关联
    @Query("SELECT DISTINCT fs FROM FlagSubmission fs " +
           "LEFT JOIN FETCH fs.user " +
           "LEFT JOIN FETCH fs.challenge " +
           "LEFT JOIN FETCH fs.competition " +
           "LEFT JOIN FETCH fs.team " +
           "WHERE fs.competition.competitionID = :competitionId " +
           "ORDER BY fs.submitTime DESC")
    List<FlagSubmission> findByCompetition_CompetitionIDWithAssociations(@Param("competitionId") Integer competitionId);
    
    // 按 Flag 获取提交记录，使用 JOIN FETCH 预先加载关联
    @Query("SELECT DISTINCT fs FROM FlagSubmission fs " +
           "LEFT JOIN FETCH fs.user " +
           "LEFT JOIN FETCH fs.challenge " +
           "LEFT JOIN FETCH fs.competition " +
           "LEFT JOIN FETCH fs.team " +
           "WHERE fs.flag.flagID = :flagId " +
           "ORDER BY fs.submitTime DESC")
    List<FlagSubmission> findByFlag_FlagIDWithAssociations(@Param("flagId") Integer flagId);
    
    Page<FlagSubmission> findByChallenge_ChallengeID(Integer challengeID, Pageable pageable);
    Page<FlagSubmission> findByFlag_FlagID(Integer flagID, Pageable pageable);
    Page<FlagSubmission> findByTeam_TeamID(Integer teamID, Pageable pageable);

    // 组合查询
    Page<FlagSubmission> findByChallenge_ChallengeIDAndUser_UserID(Integer challengeId, Integer userId, Pageable pageable);
    Optional<FlagSubmission> findFirstByUser_UserIDAndChallenge_ChallengeIDAndIsCorrectTrue(Integer userId, Integer challengeId);
    List<FlagSubmission> findByUser_UserIDAndIsCorrectTrue(Integer userId);

    // 竞赛维度查询（原有 JPQL 保留）
    @Query("SELECT fs FROM FlagSubmission fs WHERE fs.flag.competition.competitionID = :competitionID")
    List<FlagSubmission> findByCompetition(@Param("competitionID") Integer competitionID);

    @Query("SELECT fs FROM FlagSubmission fs WHERE fs.flag.competition.competitionID = :competitionID")
    Page<FlagSubmission> findByCompetition(@Param("competitionID") Integer competitionID, Pageable pageable);

    @Query("SELECT fs FROM FlagSubmission fs WHERE fs.user.userID = :userID AND fs.flag.competition.competitionID = :competitionID")
    List<FlagSubmission> findByUserAndCompetition(@Param("userID") Integer userID, @Param("competitionID") Integer competitionID);

    @Query("SELECT fs FROM FlagSubmission fs WHERE fs.team.teamID = :teamID AND fs.flag.competition.competitionID = :competitionID")
    List<FlagSubmission> findByTeamAndCompetition(@Param("teamID") Integer teamID, @Param("competitionID") Integer competitionID);

    @Query("SELECT fs FROM FlagSubmission fs WHERE fs.flag.competition.competitionID = :competitionID ORDER BY fs.submitTime DESC")
    List<FlagSubmission> findRecentSubmissionsByCompetition(@Param("competitionID") Integer competitionID, Pageable pageable);

    // 新增：与 Service 调用一致的分页方法名
    Page<FlagSubmission> findByUser_UserIDAndCompetition_CompetitionID(Integer userId, Integer competitionId, Pageable pageable);

    Page<FlagSubmission> findByCompetition_CompetitionID(Integer competitionID, Pageable pageable);

    // 存在性检查（是否已正确解答某 Flag）
    boolean existsByUser_UserIDAndFlag_FlagIDAndIsCorrectTrue(Integer userId, Integer flagId);
    boolean existsByTeam_TeamIDAndFlag_FlagIDAndIsCorrectTrue(Integer teamId, Integer flagId);

    // 统计：总提交数
    Long countByChallenge_ChallengeID(Integer challengeID);

    @Query("SELECT COUNT(fs) FROM FlagSubmission fs WHERE fs.flag.competition.competitionID = :competitionID")
    Long countTotalSubmissionsByCompetition(@Param("competitionID") Integer competitionID);

    @Query("SELECT COUNT(fs) FROM FlagSubmission fs WHERE fs.user.userID = :userID AND fs.flag.competition.competitionID = :competitionID")
    Long countTotalSubmissionsByUserAndCompetition(@Param("userID") Integer userID, @Param("competitionID") Integer competitionID);

    @Query("SELECT COUNT(fs) FROM FlagSubmission fs WHERE fs.team.teamID = :teamID AND fs.flag.competition.competitionID = :competitionID")
    Long countTotalSubmissionsByTeamAndCompetition(@Param("teamID") Integer teamID, @Param("competitionID") Integer competitionID);

    // 新增：与 Service 期望的命名一致的统计方法（总/正确/用户/战队/去重）
    Long countByCompetition_CompetitionID(Integer competitionID);

    Long countByChallenge_ChallengeIDAndIsCorrectTrue(Integer challengeID);

    Long countByCompetition_CompetitionIDAndIsCorrectTrue(Integer competitionID);

    Long countByUser_UserIDAndCompetition_CompetitionID(Integer userID, Integer competitionID);

    Long countByUser_UserIDAndCompetition_CompetitionIDAndIsCorrectTrue(Integer userID, Integer competitionID);

    @Query("SELECT COUNT(DISTINCT fs.flag.flagID) FROM FlagSubmission fs WHERE fs.user.userID = :userID AND fs.flag.competition.competitionID = :competitionID AND fs.isCorrect = true")
    Long countDistinctFlagByUserAndCompetitionAndCorrect(@Param("userID") Integer userID, @Param("competitionID") Integer competitionID);

    Long countByTeam_TeamIDAndCompetition_CompetitionID(Integer teamID, Integer competitionID);

    Long countByTeam_TeamIDAndCompetition_CompetitionIDAndIsCorrectTrue(Integer teamID, Integer competitionID);

    @Query("SELECT COUNT(DISTINCT fs.flag.flagID) FROM FlagSubmission fs WHERE fs.team.teamID = :teamID AND fs.flag.competition.competitionID = :competitionID AND fs.isCorrect = true")
    Long countDistinctFlagByTeamAndCompetitionAndCorrect(@Param("teamID") Integer teamID, @Param("competitionID") Integer competitionID);

    // 删除/批量操作
    @Transactional
    @Modifying
    @Query("DELETE FROM FlagSubmission fs WHERE fs.challenge.challengeID = :challengeID")
    long deleteByChallenge_ChallengeID(@Param("challengeID") Integer challengeID);

    @Transactional
    @Modifying
    @Query("DELETE FROM FlagSubmission fs WHERE fs.flag.competition.competitionID = :competitionID")
    long deleteByCompetition(@Param("competitionID") Integer competitionID);

    @Transactional
    @Modifying(clearAutomatically = true)
    int deleteByUser_UserID(Integer userId);
}
