package com.CTF.j_ctf.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
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
@Table(name = "Team", uniqueConstraints = {
        @UniqueConstraint(columnNames = {"CompetitionID", "TeamName"})
})
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler", "teamMembers", "applications", "flagSubmissions"})
public class Team {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "TeamID")
    private Integer teamID;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "CompetitionID", nullable = false)
    @JsonIgnoreProperties({"hibernateLazyInitializer", "handler", "challenges", "teams", "scores"})
    private Competition competition;

    @Column(name = "TeamName", nullable = false, length = 100)
    private String teamName;

    @Column(name = "CreationTime")
    private LocalDateTime creationTime;

    @Column(name = "AuditState", nullable = false, length = 1)
    private String auditState = "0"; // 0-待审核 1-通过 2-拒绝

    @Column(name = "Description", length = 500)
    private String description;

    @Column(name = "AuditRemark", length = 500)
    private String auditRemark;

    @Column(name = "AuditTime")
    private LocalDateTime auditTime;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "CaptainID")
    @JsonIgnoreProperties({"hibernateLazyInitializer", "handler", "password", "flagSubmissions", "scores"})
    private User captain;

    // 使用明确的关联实体
    @OneToMany(mappedBy = "team", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<TeamMember> teamMembers = new ArrayList<>();

    @OneToMany(mappedBy = "team", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<TeamApplication> applications = new ArrayList<>();

    @OneToMany(mappedBy = "team", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<FlagSubmission> flagSubmissions = new ArrayList<>();

    public Team() {
        this.creationTime = LocalDateTime.now();
        this.auditState = "0";
    }

    public Team(Competition competition, String teamName, User captain) {
        this();
        this.competition = competition;
        this.teamName = teamName;
        this.captain = captain;
        // 创建时队长自动成为成员
        addTeamMember(captain);
    }

    // 业务方法
    public boolean isAuditPending() {
        return "0".equals(auditState);
    }

    public boolean isAuditApproved() {
        return "1".equals(auditState);
    }

    public boolean isAuditRejected() {
        return "2".equals(auditState);
    }

    public void approve(String remark) {
        this.auditState = "1";
        this.auditRemark = remark;
        this.auditTime = LocalDateTime.now();
    }

    public void reject(String remark) {
        this.auditState = "2";
        this.auditRemark = remark;
        this.auditTime = LocalDateTime.now();
    }

    @JsonIgnore
    public boolean canJoin() {
        try {
            return isAuditApproved() &&
                    getCurrentSize() < competition.getTeamSizeLimit() &&
                    competition.isOngoing();
        } catch (Exception e) {
            // 避免序列化时触发 LAZY 加载异常
            return false;
        }
    }

    @JsonIgnore
    public int getCurrentSize() {
        try {
            // 如果 teamMembers 是 LAZY 且未初始化，返回 0
            if (teamMembers == null) {
                return 0;
            }
            return teamMembers.size();
        } catch (org.hibernate.LazyInitializationException e) {
            // 如果触发 LAZY 加载异常，返回 0
            return 0;
        }
    }

    @JsonIgnore
    public boolean isMember(User user) {
        try {
            if (teamMembers == null || user == null) {
                return false;
            }
            return teamMembers.stream()
                    .anyMatch(member -> member.getUser().getUserID().equals(user.getUserID()));
        } catch (org.hibernate.LazyInitializationException e) {
            return false;
        }
    }

    public boolean isCaptain(User user) {
        return captain != null && captain.getUserID().equals(user.getUserID());
    }

    public void addTeamMember(User user) {
        if (!isMember(user) && canJoin()) {
            TeamMember member = new TeamMember(this, user);
            teamMembers.add(member);
        }
    }

    public void removeTeamMember(User user) {
        if (!isCaptain(user)) {
            teamMembers.removeIf(member ->
                    member.getUser().getUserID().equals(user.getUserID()));
        }
    }

    @JsonIgnore
    public List<User> getMembers() {
        try {
            if (teamMembers == null) {
                return new ArrayList<>();
            }
            return teamMembers.stream()
                    .map(TeamMember::getUser)
                    .toList();
        } catch (org.hibernate.LazyInitializationException e) {
            // 如果触发 LAZY 加载异常，返回空列表
            return new ArrayList<>();
        }
    }

    @Override
    public String toString() {
        try {
            return "Team{" +
                    "teamID=" + teamID +
                    ", teamName='" + teamName + '\'' +
                    ", auditState='" + auditState + '\'' +
                    ", currentSize=" + (teamMembers != null ? teamMembers.size() : 0) +
                    '}';
        } catch (Exception e) {
            return "Team{" +
                    "teamID=" + teamID +
                    ", teamName='" + teamName + '\'' +
                    ", auditState='" + auditState + '\'' +
                    '}';
        }
    }
}