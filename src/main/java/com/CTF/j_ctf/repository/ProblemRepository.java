package com.CTF.j_ctf.repository;

import com.CTF.j_ctf.entity.Problem;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ProblemRepository extends JpaRepository<Problem, Long> {
    List<Problem> findByCompetitionIdAndPublishedTrue(Long competitionId);
}
