package com.CTF.j_ctf.repository;

import com.CTF.j_ctf.entity.FlagSubmission;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Flag提交记录仓库
 * 整合原两个FlagSubmissionRepository，支持Challenge/Flag/Team/竞赛多维度的查询与统计
 * 适配整合后的FlagSubmission实体（关联User/Challenge/Flag/Team）
 */
@Repository
public interface FlagSubmissionRepository extends JpaRepository<FlagSubmission, Integer> {

    // ===================== 基础查询：单维度关联查询 =====================
    /**
     * 按用户ID查询Flag提交记录（列表）
     * @param userID 用户ID
     * @return 提交记录列表
     */
    List<FlagSubmission> findByUser_UserID(Integer userID);

    /**
     * 按用户ID分页查询Flag提交记录
     * @param userID 用户ID
     * @param pageable 分页参数
     * @return 分页的提交记录
     */
    Page<FlagSubmission> findByUser_UserID(Integer userID, Pageable pageable);

    /**
     * 按挑战ID查询Flag提交记录（列表）
     * @param challengeID 挑战ID
     * @return 提交记录列表
     */
    List<FlagSubmission> findByChallenge_ChallengeID(Integer challengeID);

    /**
     * 按挑战ID分页查询Flag提交记录
     * @param challengeID 挑战ID
     * @param pageable 分页参数
     * @return 分页的提交记录
     */
    Page<FlagSubmission> findByChallenge_ChallengeID(Integer challengeID, Pageable pageable);

    /**
     * 按FlagID查询Flag提交记录（列表）
     * @param flagID FlagID
     * @return 提交记录列表
     */
    List<FlagSubmission> findByFlag_FlagID(Integer flagID);

    /**
     * 按FlagID分页查询Flag提交记录
     * @param flagID FlagID
     * @param pageable 分页参数
     * @return 分页的提交记录
     */
    Page<FlagSubmission> findByFlag_FlagID(Integer flagID, Pageable pageable);

    /**
     * 按战队ID查询Flag提交记录（列表）
     * @param teamID 战队ID
     * @return 提交记录列表
     */
    List<FlagSubmission> findByTeam_TeamID(Integer teamID);

    /**
     * 按战队ID分页查询Flag提交记录
     * @param teamID 战队ID
     * @param pageable 分页参数
     * @return 分页的提交记录
     */
    Page<FlagSubmission> findByTeam_TeamID(Integer teamID, Pageable pageable);

    /**
     * 按提交是否正确查询Flag提交记录（列表）
     * @param isCorrect 是否正确
     * @return 提交记录列表
     */
    List<FlagSubmission> findByIsCorrect(Boolean isCorrect);

    /**
     * 按提交是否正确分页查询Flag提交记录
     * @param isCorrect 是否正确
     * @param pageable 分页参数
     * @return 分页的提交记录
     */
    Page<FlagSubmission> findByIsCorrect(Boolean isCorrect, Pageable pageable);

    // ===================== 组合查询：多维度关联查询 =====================
    /**
     * 按用户ID+挑战ID查询Flag提交记录
     * @param userID 用户ID
     * @param challengeID 挑战ID
     * @return 提交记录列表
     */
    @Query("SELECT fs FROM FlagSubmission fs WHERE fs.user.userID = :userID AND fs.challenge.challengeID = :challengeID")
    List<FlagSubmission> findByUserAndChallenge(@Param("userID") Integer userID, @Param("challengeID") Integer challengeID);

    /**
     * 按用户ID+挑战ID查询**正确的**Flag提交记录
     * @param userID 用户ID
     * @param challengeID 挑战ID
     * @return 封装的正确提交记录（Optional避免空指针）
     */
    @Query("SELECT fs FROM FlagSubmission fs WHERE fs.user.userID = :userID AND fs.challenge.challengeID = :challengeID AND fs.isCorrect = true")
    Optional<FlagSubmission> findCorrectSubmissionByUserAndChallenge(@Param("userID") Integer userID, @Param("challengeID") Integer challengeID);

    /**
     * 按用户ID+FlagID查询Flag提交记录
     * @param userID 用户ID
     * @param flagID FlagID
     * @return 提交记录列表
     */
    @Query("SELECT fs FROM FlagSubmission fs WHERE fs.user.userID = :userID AND fs.flag.flagID = :flagID")
    List<FlagSubmission> findByUserAndFlag(@Param("userID") Integer userID, @Param("flagID") Integer flagID);

    /**
     * 按用户ID+FlagID查询**正确的**Flag提交记录
     * @param userID 用户ID
     * @param flagID FlagID
     * @return 封装的正确提交记录（Optional避免空指针）
     */
    @Query("SELECT fs FROM FlagSubmission fs WHERE fs.user.userID = :userID AND fs.flag.flagID = :flagID AND fs.isCorrect = true")
    Optional<FlagSubmission> findCorrectSubmissionByUserAndFlag(@Param("userID") Integer userID, @Param("flagID") Integer flagID);

    // ===================== 竞赛维度查询：关联竞赛的多维度查询 =====================
    /**
     * 按竞赛ID查询Flag提交记录（列表）
     * 关联路径：FlagSubmission -> Flag -> Competition -> competitionID
     * @param competitionID 竞赛ID
     * @return 提交记录列表
     */
    @Query("SELECT fs FROM FlagSubmission fs WHERE fs.flag.competition.competitionID = :competitionID")
    List<FlagSubmission> findByCompetition(@Param("competitionID") Integer competitionID);

    /**
     * 按竞赛ID分页查询Flag提交记录
     * @param competitionID 竞赛ID
     * @param pageable 分页参数
     * @return 分页的提交记录
     */
    @Query("SELECT fs FROM FlagSubmission fs WHERE fs.flag.competition.competitionID = :competitionID")
    Page<FlagSubmission> findByCompetition(@Param("competitionID") Integer competitionID, Pageable pageable);

    /**
     * 按用户ID+竞赛ID查询Flag提交记录
     * @param userID 用户ID
     * @param competitionID 竞赛ID
     * @return 提交记录列表
     */
    @Query("SELECT fs FROM FlagSubmission fs WHERE fs.user.userID = :userID AND fs.flag.competition.competitionID = :competitionID")
    List<FlagSubmission> findByUserAndCompetition(@Param("userID") Integer userID, @Param("competitionID") Integer competitionID);

    /**
     * 按战队ID+竞赛ID查询Flag提交记录
     * @param teamID 战队ID
     * @param competitionID 竞赛ID
     * @return 提交记录列表
     */
    @Query("SELECT fs FROM FlagSubmission fs WHERE fs.team.teamID = :teamID AND fs.flag.competition.competitionID = :competitionID")
    List<FlagSubmission> findByTeamAndCompetition(@Param("teamID") Integer teamID, @Param("competitionID") Integer competitionID);

    /**
     * 按竞赛ID查询最近的Flag提交记录（按提交时间倒序）
     * @param competitionID 竞赛ID
     * @param pageable 分页参数（控制返回条数和页码）
     * @return 按时间倒序的提交记录列表
     */
    @Query("SELECT fs FROM FlagSubmission fs WHERE fs.flag.competition.competitionID = :competitionID ORDER BY fs.submitTime DESC")
    List<FlagSubmission> findRecentSubmissionsByCompetition(@Param("competitionID") Integer competitionID, Pageable pageable);

    // ===================== 统计查询：总提交数统计（不区分是否正确） =====================
    /**
     * 按挑战ID统计**总**Flag提交数（不区分是否正确）
     * @param challengeID 挑战ID
     * @return 总提交数
     */
    @Query("SELECT COUNT(fs) FROM FlagSubmission fs WHERE fs.challenge.challengeID = :challengeID")
    Long countTotalSubmissionsByChallenge(@Param("challengeID") Integer challengeID);

    /**
     * 按FlagID统计**总**Flag提交数（不区分是否正确）
     * @param flagID FlagID
     * @return 总提交数
     */
    @Query("SELECT COUNT(fs) FROM FlagSubmission fs WHERE fs.flag.flagID = :flagID")
    Long countTotalSubmissionsByFlag(@Param("flagID") Integer flagID);

    /**
     * 按用户ID统计**总**Flag提交数（不区分是否正确）
     * @param userID 用户ID
     * @return 总提交数
     */
    @Query("SELECT COUNT(fs) FROM FlagSubmission fs WHERE fs.user.userID = :userID")
    Long countTotalSubmissionsByUser(@Param("userID") Integer userID);

    /**
     * 按战队ID统计**总**Flag提交数（不区分是否正确）
     * @param teamID 战队ID
     * @return 总提交数
     */
    @Query("SELECT COUNT(fs) FROM FlagSubmission fs WHERE fs.team.teamID = :teamID")
    Long countTotalSubmissionsByTeam(@Param("teamID") Integer teamID);

    /**
     * 按竞赛ID统计**总**Flag提交数（不区分是否正确）
     * 关联路径：FlagSubmission -> Flag -> Competition -> competitionID
     * @param competitionID 竞赛ID
     * @return 总提交数
     */
    @Query("SELECT COUNT(fs) FROM FlagSubmission fs WHERE fs.flag.competition.competitionID = :competitionID")
    Long countTotalSubmissionsByCompetition(@Param("competitionID") Integer competitionID);

    /**
     * 按用户ID+竞赛ID统计**总**Flag提交数（不区分是否正确）
     * @param userID 用户ID
     * @param competitionID 竞赛ID
     * @return 总提交数
     */
    @Query("SELECT COUNT(fs) FROM FlagSubmission fs WHERE fs.user.userID = :userID AND fs.flag.competition.competitionID = :competitionID")
    Long countTotalSubmissionsByUserAndCompetition(@Param("userID") Integer userID, @Param("competitionID") Integer competitionID);

    /**
     * 按战队ID+竞赛ID统计**总**Flag提交数（不区分是否正确）
     * @param teamID 战队ID
     * @param competitionID 竞赛ID
     * @return 总提交数
     */
    @Query("SELECT COUNT(fs) FROM FlagSubmission fs WHERE fs.team.teamID = :teamID AND fs.flag.competition.competitionID = :competitionID")
    Long countTotalSubmissionsByTeamAndCompetition(@Param("teamID") Integer teamID, @Param("competitionID") Integer competitionID);

    // ===================== 统计查询：各类正确提交数的统计 =====================
    /**
     * 按挑战ID统计**正确的**Flag提交数
     * @param challengeID 挑战ID
     * @return 正确提交数
     */
    @Query("SELECT COUNT(fs) FROM FlagSubmission fs WHERE fs.challenge.challengeID = :challengeID AND fs.isCorrect = true")
    Long countCorrectSubmissionsByChallenge(@Param("challengeID") Integer challengeID);

    /**
     * 按FlagID统计**正确的**Flag提交数
     * @param flagID FlagID
     * @return 正确提交数
     */
    @Query("SELECT COUNT(fs) FROM FlagSubmission fs WHERE fs.flag.flagID = :flagID AND fs.isCorrect = true")
    Long countCorrectSubmissionsByFlag(@Param("flagID") Integer flagID);

    /**
     * 按用户ID统计**正确的**Flag提交数
     * @param userID 用户ID
     * @return 正确提交数
     */
    @Query("SELECT COUNT(fs) FROM FlagSubmission fs WHERE fs.user.userID = :userID AND fs.isCorrect = true")
    Long countCorrectSubmissionsByUser(@Param("userID") Integer userID);

    /**
     * 按战队ID统计**正确的**Flag提交数
     * @param teamID 战队ID
     * @return 正确提交数
     */
    @Query("SELECT COUNT(fs) FROM FlagSubmission fs WHERE fs.team.teamID = :teamID AND fs.isCorrect = true")
    Long countCorrectSubmissionsByTeam(@Param("teamID") Integer teamID);

    /**
     * 按用户ID+竞赛ID统计**正确的**Flag提交数
     * @param userID 用户ID
     * @param competitionID 竞赛ID
     * @return 正确提交数
     */
    @Query("SELECT COUNT(fs) FROM FlagSubmission fs WHERE fs.user.userID = :userID AND fs.flag.competition.competitionID = :competitionID AND fs.isCorrect = true")
    Long countCorrectSubmissionsByUserAndCompetition(@Param("userID") Integer userID, @Param("competitionID") Integer competitionID);

    /**
     * 按战队ID+竞赛ID统计**正确的**Flag提交数
     * @param teamID 战队ID
     * @param competitionID 竞赛ID
     * @return 正确提交数
     */
    @Query("SELECT COUNT(fs) FROM FlagSubmission fs WHERE fs.team.teamID = :teamID AND fs.flag.competition.competitionID = :competitionID AND fs.isCorrect = true")
    Long countCorrectSubmissionsByTeamAndCompetition(@Param("teamID") Integer teamID, @Param("competitionID") Integer competitionID);
}