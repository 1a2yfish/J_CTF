package com.CTF.j_ctf.repository;

import com.CTF.j_ctf.entity.WriteUp;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface WriteUpRepository extends JpaRepository<WriteUp, Integer> {

    List<WriteUp> findByUser_UserID(Integer userID);

    List<WriteUp> findByCompetition_CompetitionID(Integer competitionID);

    List<WriteUp> findByTitleContaining(String title);

    @Query("SELECT w FROM WriteUp w WHERE w.user.userID = :userID AND w.competition.competitionID = :competitionID")
    List<WriteUp> findByUserAndCompetition(@Param("userID") Integer userID, @Param("competitionID") Integer competitionID);
    long countByCompetition_CompetitionID(Integer competitionId);
}