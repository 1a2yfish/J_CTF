package com.CTF.j_ctf.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Setter
@Getter
@Entity
@Table(name = "writeup")
public class WriteUp {
    // Getter和Setter
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "WriteUpID")
    private Integer writeUpID;

    @ManyToOne
    @JoinColumn(name = "userID", nullable = false)
    private User user;

    @ManyToOne
    @JoinColumn(name = "CompetitionID", nullable = false)
    private Competition competition;

    @Column(name = "Title", length = 100)
    private String title;

    @Column(name = "Content", nullable = false, columnDefinition = "TEXT")
    private String content;

    @Column(name = "CreateTime")
    private LocalDateTime createTime;

    // 构造方法
    public WriteUp() {
        this.createTime = LocalDateTime.now();
    }

    public WriteUp(User user, Competition competition, String title, String content) {
        this();
        this.user = user;
        this.competition = competition;
        this.title = title;
        this.content = content;
    }

}