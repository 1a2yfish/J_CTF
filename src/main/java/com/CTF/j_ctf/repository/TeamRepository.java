package com.CTF.j_ctf.repository;

import com.CTF.j_ctf.entity.Team;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface TeamRepository extends JpaRepository<Team, Long> {
    List<Team> findByCompetitionId(Long competitionId);
}