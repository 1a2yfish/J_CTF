package com.CTF.j_ctf.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Setter
@Getter
@Entity
@Table(name = "ChallengeHint")
public class ChallengeHint {
    // Getter和Setter
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "HintID")
    private Integer hintID;

    @ManyToOne
    @JoinColumn(name = "ChallengeID", nullable = false)
    private Challenge challenge;

    @Column(name = "Content", nullable = false, columnDefinition = "TEXT")
    private String content;

    @Column(name = "Cost")
    private Integer cost; // 提示消耗的分数

    @Column(name = "CreateTime")
    private LocalDateTime createTime;

    // 构造方法
    public ChallengeHint() {
        this.createTime = LocalDateTime.now();
    }

    public ChallengeHint(Challenge challenge, String content, Integer cost) {
        this();
        this.challenge = challenge;
        this.content = content;
        this.cost = cost;
    }

}