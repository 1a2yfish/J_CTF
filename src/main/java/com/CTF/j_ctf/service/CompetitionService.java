package com.CTF.j_ctf.service;

import com.CTF.j_ctf.entity.Competition;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.Optional;

public interface CompetitionService {

    // 创建和更新
    Competition createCompetition(Competition competition);
    Competition updateCompetition(Competition competition);
    Competition publishCompetition(Integer competitionId);
    Competition cancelCompetition(Integer competitionId);

    // 查询
    Optional<Competition> getCompetitionById(Integer competitionId);
    Page<Competition> getAllCompetitions(Pageable pageable);
    Page<Competition> getCompetitionsByCreator(Integer userId, Pageable pageable);
    Page<Competition> getPublicCompetitions(Pageable pageable);
    Page<Competition> getOngoingCompetitions(Pageable pageable);
    Page<Competition> getUpcomingCompetitions(Pageable pageable);
    Page<Competition> getFinishedCompetitions(Pageable pageable);

    // 搜索
    Page<Competition> searchCompetitions(String keyword, Pageable pageable);
    Page<Competition> searchPublicCompetitions(String keyword, Pageable pageable);

    // 状态检查
    boolean isCompetitionOngoing(Integer competitionId);
    boolean isCompetitionFinished(Integer competitionId);
    boolean canUserJoinCompetition(Integer competitionId, Integer userId);

    // 管理操作
    boolean deleteCompetition(Integer competitionId);
    Competition auditCompetition(Integer competitionId, boolean approved, String remark);

    // 统计
    Long getCompetitionCountByUser(Integer userId);
    Long getCompetitionCountByStatus(String status);
}