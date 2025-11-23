package com.CTF.j_ctf.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Setter
@Getter
@Entity
@Table(name = "Challengehint")
public class ChallengeHint {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "HintID")
    private Integer hintID;

    @ManyToOne
    @JoinColumn(name = "ChallengeID", nullable = false)
    private Challenge challenge;

    @Column(name = "Content", nullable = false, columnDefinition = "TEXT")
    private String content; // 修复：字段名应该是content而不是hintContent

    @Column(name = "Cost")
    private Integer cost; // 提示消耗的分数

    @Column(name = "CreateTime")
    private LocalDateTime createTime;

    @Column(name = "UpdateTime")
    private LocalDateTime updateTime;

    // 构造方法
    public ChallengeHint() {
        this.createTime = LocalDateTime.now();
        this.updateTime = LocalDateTime.now();
    }

    public String getHintContent() {
        return content;
    }

    public ChallengeHint(Challenge challenge, String content, Integer cost) {
        this();
        this.challenge = challenge;
        this.content = content;
        this.cost = cost;
    }
}