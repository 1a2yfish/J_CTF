package com.CTF.j_ctf.entity;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
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
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler", "teams", "flags", "scores", "writeUps"})
public class Competition {
    // Getter和Setter
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "CompetitionID")
    private Integer competitionID;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "userID", nullable = false)
    @JsonIgnoreProperties({"hibernateLazyInitializer", "handler", "password", "flagSubmissions", "scores"})
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
        if (startTime == null || endTime == null) {
            return false;
        }
        LocalDateTime now = LocalDateTime.now();
        // 使用 isAfter 和 isBefore，但需要确保边界情况正确处理
        // 如果当前时间在开始时间和结束时间之间（包含边界），则认为是进行中
        return (now.isAfter(startTime) || now.isEqual(startTime)) && 
               (now.isBefore(endTime) || now.isEqual(endTime));
    }

    public boolean isUpcoming() {
        if (startTime == null) {
            return false;
        }
        LocalDateTime now = LocalDateTime.now();
        // 如果当前时间在开始时间之前，则是即将开始
        return now.isBefore(startTime);
    }

    public boolean isFinished() {
        if (endTime == null) {
            return false;
        }
        LocalDateTime now = LocalDateTime.now();
        // 如果当前时间在结束时间之后，则已结束
        return now.isAfter(endTime);
    }

    public boolean isPublished() {
        return "PUBLISHED".equals(status);
    }

    @com.fasterxml.jackson.annotation.JsonIgnore
    public boolean canJoin() {
        try {
            return ("PUBLISHED".equals(status) || "ONGOING".equals(status))
                    && isUpcoming() && isPublic;
        } catch (Exception e) {
            return false;
        }
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