package com.CTF.j_ctf.controller;

import com.CTF.j_ctf.entity.Competition;
import com.CTF.j_ctf.repository.CompetitionRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/competitions")
public class CompetitionController {
    private final CompetitionRepository repo;
    public CompetitionController(CompetitionRepository repo) { this.repo = repo; }

    @PostMapping
    public ResponseEntity<Competition> create(@RequestBody Competition c) {
        return ResponseEntity.ok(repo.save(c));
    }

    @GetMapping
    public ResponseEntity<List<Competition>> listPublished() {
        return ResponseEntity.ok(repo.findByPublishedTrue());
    }

    @GetMapping("/{id}")
    public ResponseEntity<Competition> get(@PathVariable Long id) {
        return repo.findById(id).map(ResponseEntity::ok).orElse(ResponseEntity.notFound().build());
    }

    @PutMapping("/{id}")
    public ResponseEntity<Competition> update(@PathVariable Long id, @RequestBody Competition c) {
        return repo.findById(id).map(existing -> {
            c.setId(existing.getId());
            return ResponseEntity.ok(repo.save(c));
        }).orElse(ResponseEntity.notFound().build());
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        repo.deleteById(id);
        return ResponseEntity.noContent().build();
    }
}
