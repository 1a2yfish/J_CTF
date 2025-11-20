package com.CTF.j_ctf.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Setter
@Getter
@Entity
@Table(name = "Score")
public class Score {
    // Getter和Setter
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "ChangeID")
    private Integer changeID;

    @ManyToOne
    @JoinColumn(name = "userID", nullable = false)
    private User user;

    @ManyToOne
    @JoinColumn(name = "CompetitionID", nullable = false)
    private Competition competition;

    @ManyToOne
    @JoinColumn(name = "TeamID")
    private Team team; // 关联战队，如果是个人得分则为null

    @ManyToOne
    @JoinColumn(name = "ChallengeID")
    private Challenge challenge; // 关联题目

    @ManyToOne
    @JoinColumn(name = "FlagID")
    private Flag flag; // 关联Flag

    @Column(name = "Score", nullable = false)
    private Integer score; // 分数值，可正可负

    @Column(name = "CreateTime", nullable = false)
    private LocalDateTime createTime;

    @Column(name = "Type", length = 20)
    private String type; // FLAG_SUBMISSION, BONUS, PENALTY, ADJUSTMENT

    @Column(name = "Description", length = 500)
    private String description; // 分数变动描述

    @Column(name = "IsValid")
    private Boolean isValid = true; // 是否有效（用于撤销分数）

    // 构造方法
    public Score() {
        this.createTime = LocalDateTime.now();
        this.isValid = true;
    }

    public Score(User user, Competition competition, Integer score) {
        this();
        this.user = user;
        this.competition = competition;
        this.score = score;
        this.type = "ADJUSTMENT";
    }

    public Score(User user, Competition competition, Team team, Integer score) {
        this(user, competition, score);
        this.team = team;
    }

    public Score(User user, Competition competition, Challenge challenge, Integer score) {
        this(user, competition, score);
        this.challenge = challenge;
        this.type = "CHALLENGE_SOLVE";
    }

    public Score(User user, Competition competition, Flag flag, Integer score) {
        this(user, competition, score);
        this.flag = flag;
        this.type = "FLAG_SUBMISSION";
    }

    // 业务方法
    public boolean isPositive() {
        return score > 0;
    }

    public boolean isNegative() {
        return score < 0;
    }

    public boolean isTeamScore() {
        return team != null;
    }

    @Override
    public String toString() {
        return "Score{" +
                "changeID=" + changeID +
                ", user=" + (user != null ? user.getUserID() : null) +
                ", score=" + score +
                ", type='" + type + '\'' +
                '}';
    }
}