package com.CTF.j_ctf.repository;

import com.CTF.j_ctf.entity.ChallengeHint;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ChallengeHintRepository extends JpaRepository<ChallengeHint, Integer> {

    List<ChallengeHint> findByChallenge_ChallengeID(Integer challengeID);

    List<ChallengeHint> findByChallenge_ChallengeIDOrderByCostAsc(Integer challengeID);
}