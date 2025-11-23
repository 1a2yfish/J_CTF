package com.CTF.j_ctf.service;

import com.CTF.j_ctf.entity.Competition;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.Map;
import java.util.Optional;

public interface CompetitionService {

    Competition createCompetition(Competition competition);
    Competition updateCompetition(Competition competition);
    Competition publishCompetition(Integer competitionId);
    Competition cancelCompetition(Integer competitionId);
    Competition startCompetition(Integer competitionId);
    Competition finishCompetition(Integer competitionId);
    Optional<Competition> getCompetitionById(Integer competitionId);

    Page<Competition> getAllCompetitions(Pageable pageable);
    Page<Competition> getCompetitionsByCreator(Integer userId, Pageable pageable);
    Page<Competition> getPublicCompetitions(Pageable pageable);
    Page<Competition> getOngoingCompetitions(Pageable pageable);
    Page<Competition> getUpcomingCompetitions(Pageable pageable);
    Page<Competition> getFinishedCompetitions(Pageable pageable);

    Page<Competition> searchCompetitions(String keyword, Pageable pageable);
    Page<Competition> searchPublicCompetitions(String keyword, Pageable pageable);

    boolean isCompetitionOngoing(Integer competitionId);
    boolean isCompetitionFinished(Integer competitionId);
    boolean isCompetitionPublished(Integer competitionId);

    boolean canUserJoinCompetition(Integer competitionId, Integer userId);
    String getJoinRestrictionReason(Integer competitionId, Integer userId);
    boolean joinCompetition(Integer competitionId, Integer userId);
    boolean leaveCompetition(Integer competitionId, Integer userId);

    boolean isUserParticipant(Integer competitionId, Integer userId);
    boolean isUserCreator(Integer competitionId, Integer userId);

    Map<String, Object> getCompetitionStatistics(Integer competitionId);

    boolean deleteCompetition(Integer competitionId);
    Competition auditCompetition(Integer competitionId, boolean approved, String remark);

    Long getCompetitionCountByUser(Integer userId);
    Long getCompetitionCountByStatus(String status);
}