package com.CTF.j_ctf.repository;

import com.CTF.j_ctf.entity.ChallengeHint;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Repository
public interface ChallengeHintRepository extends JpaRepository<ChallengeHint, Integer> {
    List<ChallengeHint> findByChallenge_ChallengeIDOrderByCostAsc(Integer challengeID);
    @Query("SELECT ch FROM ChallengeHint ch WHERE ch.challenge.challengeID = :challengeID AND ch.cost BETWEEN :minCost AND :maxCost")
    List<ChallengeHint> findByChallengeIDAndCostBetween(
            @Param("challengeID") Integer challengeID,
            @Param("minCost") Integer minCost,
            @Param("maxCost") Integer maxCost
    );
    @Transactional
    @Modifying
    long deleteByChallenge_ChallengeID(Integer challengeID);
}