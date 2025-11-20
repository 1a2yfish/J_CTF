package com.CTF.j_ctf.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Setter
@Getter
@Entity
@Table(name = "Challenge")
public class Challenge {
    // Getter和Setter
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "ChallengeID")
    private Integer challengeID;

    @ManyToOne
    @JoinColumn(name = "CompetitionID", nullable = false)
    private Competition competition;

    @Column(name = "Title", nullable = false, length = 100)
    private String title;

    @Column(name = "Description", nullable = false, columnDefinition = "TEXT")
    private String description;

    @Column(name = "Category", nullable = false, length = 50)
    private String category; // Web, Pwn, Crypto, Reverse, Misc等

    @Column(name = "Difficulty", nullable = false, length = 20)
    private String difficulty; // Easy, Medium, Hard

    @Column(name = "Points", nullable = false)
    private Integer points;

    @Column(name = "Flag", nullable = false, length = 100)
    private String flag;

    @Column(name = "IsActive")
    private Boolean isActive = true;

    @Column(name = "CreateTime")
    private LocalDateTime createTime;

    @Column(name = "UpdateTime")
    private LocalDateTime updateTime;

    @Column(name = "AttachmentUrl", length = 500)
    private String attachmentUrl; // 附件下载链接

    @Column(name = "Hint", length = 500)
    private String hint; // 题目提示

    @Column(name = "SolveCount")
    private Integer solveCount = 0; // 解题人数

    @OneToMany(mappedBy = "challenge", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<FlagSubmission> flagSubmissions = new ArrayList<>();

    @OneToMany(mappedBy = "challenge", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<ChallengeHint> hints = new ArrayList<>();

    // 构造方法
    public Challenge() {
        this.createTime = LocalDateTime.now();
        this.updateTime = LocalDateTime.now();
    }

    public Challenge(Competition competition, String title, String description,
                     String category, String difficulty, Integer points, String flag) {
        this();
        this.competition = competition;
        this.title = title;
        this.description = description;
        this.category = category;
        this.difficulty = difficulty;
        this.points = points;
        this.flag = flag;
    }

    // 业务方法
    public void incrementSolveCount() {
        this.solveCount++;
        this.updateTime = LocalDateTime.now();
    }

    public boolean isSolvedByUser(User user) {
        return flagSubmissions.stream()
                .anyMatch(submission -> submission.getUser().getUserID().equals(user.getUserID())
                        && submission.getIsCorrect());
    }

    @Override
    public String toString() {
        return "Challenge{" +
                "challengeID=" + challengeID +
                ", title='" + title + '\'' +
                ", category='" + category + '\'' +
                ", difficulty='" + difficulty + '\'' +
                ", points=" + points +
                '}';
    }
}