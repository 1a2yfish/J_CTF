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
@Table(name = "Team", uniqueConstraints = {
        @UniqueConstraint(columnNames = {"CompetitionID", "TeamName"})
})
public class Team {
    // Getter和Setter
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "TeamID")
    private Integer teamID;

    @ManyToOne
    @JoinColumn(name = "CompetitionID", nullable = false)
    private Competition competition;

    @Column(name = "TeamName", nullable = false, length = 100)
    private String teamName;

    @Column(name = "CreationTime")
    private LocalDateTime creationTime;

    @Column(name = "AuditState", nullable = false, length = 1)
    private String auditState; // '0' 待审核 / '1' 通过 / '2' 拒绝

    @Column(name = "Description", length = 500)
    private String description;

    @Column(name = "AuditRemark", length = 500)
    private String auditRemark;

    @Column(name = "AuditTime")
    private LocalDateTime auditTime;

    @ManyToOne
    @JoinColumn(name = "CaptainID")
    private User captain; // 队长

    @ManyToMany
    @JoinTable(
            name = "Teammember",
            joinColumns = @JoinColumn(name = "TeamID"),
            inverseJoinColumns = @JoinColumn(name = "userID")
    )
    private List<User> members = new ArrayList<>();

    @OneToMany(mappedBy = "team", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<TeamApplication> applications = new ArrayList<>();

    // 构造方法
    public Team() {
        this.creationTime = LocalDateTime.now();
        this.auditState = "0"; // 默认待审核
    }

    public Team(Competition competition, String teamName, User captain) {
        this();
        this.competition = competition;
        this.teamName = teamName;
        this.captain = captain;
        this.members.add(captain); // 队长自动成为成员
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

    public boolean canJoin() {
        return isAuditApproved() &&
                members.size() < competition.getTeamSizeLimit() &&
                competition.isOngoing();
    }

    public boolean isMember(User user) {
        return members.stream().anyMatch(m -> m.getUserID().equals(user.getUserID()));
    }

    public boolean isCaptain(User user) {
        return captain != null && captain.getUserID().equals(user.getUserID());
    }

    public void addMember(User user) {
        if (!isMember(user) && canJoin()) {
            members.add(user);
        }
    }

    public void removeMember(User user) {
        if (!isCaptain(user)) { // 不能移除队长
            members.removeIf(m -> m.getUserID().equals(user.getUserID()));
        }
    }

    @Override
    public String toString() {
        return "Team{" +
                "teamID=" + teamID +
                ", teamName='" + teamName + '\'' +
                ", auditState='" + auditState + '\'' +
                '}';
    }
}