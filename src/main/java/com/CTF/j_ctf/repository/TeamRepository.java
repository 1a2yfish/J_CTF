package com.CTF.j_ctf.repository;

import com.CTF.j_ctf.entity.Team;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * 战队仓库接口
 * 整合原两个TeamRepository，支持基础查询、用户关联、审核统计、模糊搜索等全量业务场景
 * 继承JpaRepository，获得基础的CRUD、分页、排序等功能
 */
@Repository
public interface TeamRepository extends JpaRepository<Team, Integer> {

    // ===================== 基础查询：比赛/审核状态的列表与分页查询 =====================
    /**
     * 按比赛ID查询战队列表（非分页，适配小数据量场景）
     * @param competitionID 比赛ID
     * @return 战队列表
     */
    List<Team> findByCompetition_CompetitionID(Integer competitionID);

    /**
     * 按比赛ID分页查询战队（适配大数据量场景）
     * @param competitionID 比赛ID
     * @param pageable 分页参数（页码、页大小、排序规则）
     * @return 分页的战队列表
     */
    Page<Team> findByCompetition_CompetitionID(Integer competitionID, Pageable pageable);

    /**
     * 按审核状态分页查询所有比赛的战队
     * @param auditState 审核状态（0：待审核，1：已通过，2：已拒绝）
     * @param pageable 分页参数
     * @return 分页的战队列表
     */
    Page<Team> findByAuditState(String auditState, Pageable pageable);

    /**
     * 按比赛ID和审核状态分页查询战队（高频业务：展示某比赛下指定审核状态的战队）
     * @param competitionID 比赛ID
     * @param auditState 审核状态
     * @param pageable 分页参数
     * @return 分页的战队列表
     */
    Page<Team> findByCompetition_CompetitionIDAndAuditState(Integer competitionID, String auditState, Pageable pageable);

    // ===================== 战队名称与唯一性校验 =====================
    /**
     * 按战队名称和比赛ID查询战队（用于校验战队名在指定比赛中是否唯一）
     * @param teamName 战队名称
     * @param competitionID 比赛ID
     * @return 封装的战队对象（Optional避免空指针）
     */
    Optional<Team> findByTeamNameAndCompetition_CompetitionID(String teamName, Integer competitionID);

    /**
     * 校验战队名称在指定比赛中是否已存在（创建战队时的唯一性校验）
     * @param teamName 战队名称
     * @param competitionID 比赛ID
     * @return 存在返回true，不存在返回false
     */
    boolean existsByTeamNameAndCompetition_CompetitionID(String teamName, Integer competitionID);

    // ===================== 用户关联查询：成员/队长与战队的关联 =====================
    /**
     * 按成员ID查询所属的所有战队
     * @param userID 成员用户ID
     * @return 战队列表
     */
    @Query("SELECT t FROM Team t JOIN t.members m WHERE m.userID = :userID")
    List<Team> findByMemberId(@Param("userID") Integer userID);

    /**
     * 按队长ID查询其创建的所有战队
     * @param captainId 队长用户ID
     * @return 战队列表
     */
    @Query("SELECT t FROM Team t WHERE t.captain.userID = :captainId")
    List<Team> findByCaptainId(@Param("captainId") Integer captainId);

    /**
     * 按成员ID和比赛ID查询所属战队（校验用户在指定比赛中是否已有战队）
     * @param userID 成员用户ID
     * @param competitionID 比赛ID
     * @return 封装的战队对象（Optional避免空指针）
     */
    @Query("SELECT t FROM Team t JOIN t.members m WHERE m.userID = :userID AND t.competition.competitionID = :competitionID")
    Optional<Team> findByMemberAndCompetition(@Param("userID") Integer userID, @Param("competitionID") Integer competitionID);

    /**
     * 检查用户是否在指定比赛中已有所属战队（批量/快速校验场景）
     * @param userID 成员用户ID
     * @param competitionID 比赛ID
     * @return 存在返回true，不存在返回false
     */
    @Query("SELECT COUNT(t) > 0 FROM Team t JOIN t.members m WHERE m.userID = :userID AND t.competition.competitionID = :competitionID")
    boolean existsByMemberAndCompetition(@Param("userID") Integer userID, @Param("competitionID") Integer competitionID);

    // ===================== 审核统计：比赛维度的战队数量统计 =====================
    /**
     * 按比赛ID统计战队总数（业务中用于获取比赛的总战队数量）
     * @param competitionID 比赛ID
     * @return 战队总数（long类型避免整数溢出）
     */
    long countByCompetition_CompetitionID(Integer competitionID);

    /**
     * 按比赛ID和审核状态统计战队数（业务中统计待审核/已通过/已拒绝的战队数）
     * @param competitionID 比赛ID
     * @param auditState 审核状态
     * @return 对应状态的战队数
     */
    long countByCompetition_CompetitionIDAndAuditState(Integer competitionID, String auditState);

    // ===================== 搜索查询：战队名模糊搜索 =====================
    /**
     * 按战队名称模糊查询所有比赛的战队（分页）
     * @param keyword 搜索关键字
     * @param pageable 分页参数
     * @return 分页的战队列表
     */
    @Query("SELECT t FROM Team t WHERE t.teamName LIKE %:keyword%")
    Page<Team> findByTeamNameContaining(@Param("keyword") String keyword, Pageable pageable);

    /**
     * 按战队名称模糊查询指定比赛的战队（分页，高频业务：比赛内的战队搜索）
     * @param keyword 搜索关键字
     * @param competitionID 比赛ID
     * @param pageable 分页参数
     * @return 分页的战队列表
     */
    @Query("SELECT t FROM Team t WHERE t.teamName LIKE %:keyword% AND t.competition.competitionID = :competitionID")
    Page<Team> findByTeamNameContainingAndCompetition(@Param("keyword") String keyword, @Param("competitionID") Integer competitionID, Pageable pageable);
}