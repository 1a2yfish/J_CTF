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

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "ChangeID")
    private Integer changeID;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "userID", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "CompetitionID", nullable = false)
    private Competition competition;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "TeamID")
    private Team team;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ChallengeID")
    private Challenge challenge;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "FlagID")
    private Flag flag;

    @Column(name = "Score", nullable = false)
    private Integer scoreValue; // 重命名避免冲突

    @Column(name = "CreateTime", nullable = false)
    private LocalDateTime createTime;

    @Column(name = "Type", length = 20)
    private String type; // FLAG_SUBMISSION/BONUS/PENALTY/ADJUSTMENT

    @Column(name = "Description", length = 500)
    private String description;

    @Column(name = "IsValid")
    private Boolean isValid = true;

    public Score() {
        this.createTime = LocalDateTime.now();
        this.isValid = true;
    }

    // 构造方法更新
    public Score(User user, Competition competition, Integer scoreValue) {
        this();
        this.user = user;
        this.competition = competition;
        this.scoreValue = scoreValue;
        this.type = "ADJUSTMENT";
    }

    public Score(User user, Competition competition, Team team, Integer scoreValue) {
        this(user, competition, scoreValue);
        this.team = team;
    }

    public Score(User user, Competition competition, Challenge challenge, Integer scoreValue) {
        this(user, competition, scoreValue);
        this.challenge = challenge;
        this.type = "CHALLENGE_SOLVE";
    }

    public Score(User user, Competition competition, Flag flag, Integer scoreValue) {
        this(user, competition, scoreValue);
        this.flag = flag;
        this.type = "FLAG_SUBMISSION";
    }

    // 业务方法
    public boolean isPositive() {
        return scoreValue > 0;
    }

    public boolean isNegative() {
        return scoreValue < 0;
    }

    public boolean isTeamScore() {
        return team != null;
    }

    public void invalidate() {
        this.isValid = false;
    }

    // 兼容方法
    public void setPoints(Integer points) {
        this.scoreValue = points;
    }

    public void setScoreTime(LocalDateTime time) {
        this.createTime = time;
    }

    @Override
    public String toString() {
        return "Score{" +
                "changeID=" + changeID +
                ", userID=" + (user != null ? user.getUserID() : null) +
                ", scoreValue=" + scoreValue +
                ", type='" + type + '\'' +
                ", isValid=" + isValid +
                '}';
    }
}