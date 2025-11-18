package com.CTF.j_ctf.controller;

import com.CTF.j_ctf.entity.Team;
import com.CTF.j_ctf.repository.TeamRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/teams")
public class TeamController {
    private final TeamRepository repo;
    public TeamController(TeamRepository repo) { this.repo = repo; }

    @PostMapping
    public ResponseEntity<Team> create(@RequestBody Team t) {
        t.setApproved(false); // 需管理员审核
        return ResponseEntity.ok(repo.save(t));
    }

    @GetMapping("/competition/{competitionId}")
    public ResponseEntity<List<Team>> byCompetition(@PathVariable Long competitionId) {
        return ResponseEntity.ok(repo.findByCompetitionId(competitionId));
    }

    @PutMapping("/{id}")
    public ResponseEntity<Team> update(@PathVariable Long id, @RequestBody Team t) {
        return repo.findById(id).map(existing -> {
            t.setId(existing.getId());
            return ResponseEntity.ok(repo.save(t));
        }).orElse(ResponseEntity.notFound().build());
    }
}
