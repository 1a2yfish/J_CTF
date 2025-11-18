package com.CTF.j_ctf.controller;

import com.CTF.j_ctf.entity.Problem;
import com.CTF.j_ctf.repository.ProblemRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/problems")
public class ProblemController {
    private final ProblemRepository repo;
    public ProblemController(ProblemRepository repo) { this.repo = repo; }

    @PostMapping
    public ResponseEntity<Problem> create(@RequestBody Problem p) {
        p.setPublished(false);
        return ResponseEntity.ok(repo.save(p));
    }

    @GetMapping("/competition/{competitionId}")
    public ResponseEntity<List<Problem>> list(@PathVariable Long competitionId) {
        return ResponseEntity.ok(repo.findByCompetitionIdAndPublishedTrue(competitionId));
    }

    @PutMapping("/{id}")
    public ResponseEntity<Problem> update(@PathVariable Long id, @RequestBody Problem p) {
        return repo.findById(id).map(existing -> {
            p.setId(existing.getId());
            return ResponseEntity.ok(repo.save(p));
        }).orElse(ResponseEntity.notFound().build());
    }
}
