package com.CTF.j_ctf.entity;

import jakarta.persistence.*;
import org.hibernate.annotations.Immutable;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Setter
@Getter
@Entity
@Immutable  // 标记为不可变，因为这是视图
@Table(name = "Scoresummary")
public class ScoreSummary {
    // Getter和Setter
    @Id
    @Column(name = "ID")
    private String id; // 组合ID: user_{userId} 或 team_{teamId}

    @Column(name = "EntityID")
    private Integer entityID; // 用户ID或战队ID

    @Column(name = "EntityType")
    private String entityType; // USER 或 TEAM

    @Column(name = "EntityName")
    private String entityName; // 用户名或战队名

    @Column(name = "CompetitionID")
    private Integer competitionID;

    @Column(name = "TotalScore")
    private Integer totalScore;

    @Column(name = "SolveCount")
    private Integer solveCount; // 解题数量

    @Column(name = "LastSubmitTime")
    private LocalDateTime lastSubmitTime;

    @Column(name = "Rank")
    private Integer rank;

}