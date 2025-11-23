package com.CTF.j_ctf.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Setter
@Getter
@Entity
@Table(name = "Flagsubmission") // 修正表名
public class FlagSubmission {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "SubmissionID")
    private Integer submissionID;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "userID", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "CompetitionID", nullable = false)
    private Competition competition;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ChallengeID")
    private Challenge challenge;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "FlagID")
    private Flag flag;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "TeamID")
    private Team team;

    @Column(name = "SubmittedContent", nullable = false, length = 100)
    private String submittedContent;

    @Column(name = "IsCorrect")
    private Boolean isCorrect;

    @Column(name = "SubmitTime")
    private LocalDateTime submitTime;

    @Column(name = "IPAddress", length = 45)
    private String ipAddress;

    @Column(name = "UserAgent", length = 500)
    private String userAgent;

    @Column(name = "PointsAwarded")
    private Integer pointsAwarded;

    public FlagSubmission() {
        this.submitTime = LocalDateTime.now();
    }

    // 构造方法保持不变...

    // 业务方法
    public void markAsCorrect(Integer points) {
        this.isCorrect = true;
        this.pointsAwarded = points;
    }

    public void setSubmittedFlag(String flag) {
        this.submittedContent = flag;
    }

    public void markAsIncorrect() {
        this.isCorrect = false;
        this.pointsAwarded = 0;
    }

    public boolean isSuccessful() {
        return Boolean.TRUE.equals(isCorrect);
    }

    @Override
    public String toString() {
        return "FlagSubmission{" +
                "submissionID=" + submissionID +
                ", userID=" + (user != null ? user.getUserID() : null) +
                ", isCorrect=" + isCorrect +
                ", pointsAwarded=" + pointsAwarded +
                '}';
    }
}