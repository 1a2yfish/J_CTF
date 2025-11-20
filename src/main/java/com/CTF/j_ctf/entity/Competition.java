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
@Table(name = "Competition")
public class Competition {
    // Getter和Setter
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "CompetitionID")
    private Integer competitionID;

    @ManyToOne
    @JoinColumn(name = "userID", nullable = false)
    private User creator;

    @Column(name = "Title", nullable = false, length = 100)
    private String title;

    @Column(name = "Introduction", nullable = false, columnDefinition = "TEXT")
    private String introduction;

    @Column(name = "TeamSizeLimit", nullable = false)
    private Integer teamSizeLimit;

    @Column(name = "StartTime", nullable = false)
    private LocalDateTime startTime;

    @Column(name = "EndTime", nullable = false)
    private LocalDateTime endTime;

    @Column(name = "PublishTime")
    private LocalDateTime publishTime;

    @Column(name = "AuditTime")
    private LocalDateTime auditTime;

    @Column(name = "Status", length = 20)
    private String status; // DRAFT, PUBLISHED, ONGOING, FINISHED, CANCELLED

    @Column(name = "AuditStatus", length = 20)
    private String auditStatus; // PENDING, APPROVED, REJECTED

    @Column(name = "AuditRemark", length = 500)
    private String auditRemark;

    @Column(name = "MaxTeams")
    private Integer maxTeams; // 最大参赛队伍数

    @Column(name = "IsPublic")
    private Boolean isPublic = true; // 是否公开竞赛

    @OneToMany(mappedBy = "competition", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<Team> teams = new ArrayList<>();

    @OneToMany(mappedBy = "competition", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<Flag> flags = new ArrayList<>();

    @OneToMany(mappedBy = "competition", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<Score> scores = new ArrayList<>();

    @OneToMany(mappedBy = "competition", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<WriteUp> writeUps = new ArrayList<>();

    // 构造方法
    public Competition() {
        this.publishTime = LocalDateTime.now();
        this.status = "DRAFT";
        this.auditStatus = "PENDING";
        this.isPublic = true;
    }

    public Competition(User creator, String title, String introduction,
                       Integer teamSizeLimit, LocalDateTime startTime,
                       LocalDateTime endTime) {
        this();
        this.creator = creator;
        this.title = title;
        this.introduction = introduction;
        this.teamSizeLimit = teamSizeLimit;
        this.startTime = startTime;
        this.endTime = endTime;
    }

    // 业务方法
    public boolean isOngoing() {
        LocalDateTime now = LocalDateTime.now();
        return now.isAfter(startTime) && now.isBefore(endTime);
    }

    public boolean isUpcoming() {
        return LocalDateTime.now().isBefore(startTime);
    }

    public boolean isFinished() {
        return LocalDateTime.now().isAfter(endTime);
    }

    public boolean canJoin() {
        return ("PUBLISHED".equals(status) || "ONGOING".equals(status))
                && isUpcoming() && isPublic;
    }

    @Override
    public String toString() {
        return "Competition{" +
                "competitionID=" + competitionID +
                ", title='" + title + '\'' +
                ", status='" + status + '\'' +
                ", startTime=" + startTime +
                ", endTime=" + endTime +
                '}';
    }
}