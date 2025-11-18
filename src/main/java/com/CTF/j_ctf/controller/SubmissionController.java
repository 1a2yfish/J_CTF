package com.CTF.j_ctf.controller;

import com.CTF.j_ctf.entity.Submission;
import com.CTF.j_ctf.entity.Problem;
import com.CTF.j_ctf.repository.SubmissionRepository;
import com.CTF.j_ctf.repository.ProblemRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;

@RestController
@RequestMapping("/api/submissions")
public class SubmissionController {
    private final SubmissionRepository submissions;
    private final ProblemRepository problems;

    public SubmissionController(SubmissionRepository submissions, ProblemRepository problems) {
        this.submissions = submissions; this.problems = problems;
    }

    @PostMapping
    public ResponseEntity<Submission> submit(@RequestBody Submission s) {
        s.setSubmittedAt(LocalDateTime.now());
        // 简单判定：实际应当校验 flag 存储/哈希比对或比对题目答案
        Problem p = problems.findById(s.getProblemId()).orElse(null);
        boolean correct = false;
        if (p != null) {
            // TODO: replace with secure flag storage/check
            correct = "FLAG{demo}".equals(s.getFlag());
        }
        s.setCorrect(correct);
        Submission saved = submissions.save(s);
        return ResponseEntity.ok(saved);
    }
}
