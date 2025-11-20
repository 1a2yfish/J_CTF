package com.CTF.j_ctf.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Setter
@Getter
@Entity
@Table(name = "Teamapplication")
public class TeamApplication {
    // Getter和Setter
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "ApplicationID")
    private Integer applicationID;

    @ManyToOne
    @JoinColumn(name = "TeamID", nullable = false)
    private Team team;

    @ManyToOne
    @JoinColumn(name = "ApplicantID", nullable = false)
    private User applicant;

    @Column(name = "Status", length = 20)
    private String status; // PENDING, APPROVED, REJECTED

    @Column(name = "ApplyTime")
    private LocalDateTime applyTime;

    @Column(name = "ProcessTime")
    private LocalDateTime processTime;

    @Column(name = "Remark", length = 500)
    private String remark;

    // 构造方法
    public TeamApplication() {
        this.applyTime = LocalDateTime.now();
        this.status = "PENDING";
    }

    public TeamApplication(Team team, User applicant) {
        this();
        this.team = team;
        this.applicant = applicant;
    }

}