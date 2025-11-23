package com.CTF.j_ctf.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Setter
@Getter
@Entity
@Table(name = "Flag")
public class Flag {
    // Getter和Setter
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "flagID")
    private Integer flagID;

    @ManyToOne
    @JoinColumn(name = "userID", nullable = false)
    private User user;

    @ManyToOne
    @JoinColumn(name = "CompetitionID", nullable = false)
    private Competition competition;

    @ManyToOne
    @JoinColumn(name = "ChallengeID")
    private Challenge challenge; // 关联到具体的题目

    @ManyToOne
    @JoinColumn(name = "TeamID")
    private Team team; // 关联到战队

    @Column(name = "Value", nullable = false, length = 100)
    private String value;

    @Column(name = "Status", nullable = false)
    private Integer status; // 0 未使用 / 1 已使用 / 2 已过期

    @Column(name = "CreateTime")
    private LocalDateTime createTime;

    @Column(name = "UseTime")
    private LocalDateTime useTime; // 使用时间

    @Column(name = "ExpireTime")
    private LocalDateTime expireTime; // 过期时间

    @Column(name = "Points")
    private Integer points; // 该Flag对应的分值

    @Column(name = "Type", length = 20)
    private String type; // DYNAMIC, STATIC, BONUS 等类型

    @Column(name = "Description", length = 500)
    private String description; // Flag描述

    // 构造方法
    public Flag() {
        this.createTime = LocalDateTime.now();
        this.status = 0; // 默认未使用
    }

    public Flag(User user, Competition competition, String value, Integer points) {
        this();
        this.user = user;
        this.competition = competition;
        this.value = value;
        this.points = points;
    }

    // 业务方法
    public boolean isExpired() {
        return expireTime != null && LocalDateTime.now().isAfter(expireTime);
    }

    public boolean canBeUsed() {
        return status == 0 && !isExpired();
    }

    public void markAsUsed() {
        this.status = 1;
        this.useTime = LocalDateTime.now();
    }

    public void markAsExpired() {
        this.status = 2;
    }

    public void setFlagValue(String value) {
        this.value = value;
    }

    @Override
    public String toString() {
        return "Flag{" +
                "flagID=" + flagID +
                ", value='" + value + '\'' +
                ", status=" + status +
                ", points=" + points +
                '}';
    }
}