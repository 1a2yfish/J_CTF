package com.CTF.j_ctf.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
@Entity
@Table(name = "Teammember")
@IdClass(TeamMemberId.class)
public class TeamMember {

    @Id
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "TeamID", nullable = false)
    private Team team;

    @Id
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "userID", nullable = false)
    private User user;

    @Column(name = "JoinTime")
    private java.time.LocalDateTime joinTime;

    public TeamMember() {
        this.joinTime = java.time.LocalDateTime.now();
    }

    public TeamMember(Team team, User user) {
        this();
        this.team = team;
        this.user = user;
    }

    @Override
    public String toString() {
        return "TeamMember{" +
                "teamID=" + (team != null ? team.getTeamID() : null) +
                ", userID=" + (user != null ? user.getUserID() : null) +
                ", joinTime=" + joinTime +
                '}';
    }
}