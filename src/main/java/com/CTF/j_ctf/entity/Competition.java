package com.CTF.j_ctf.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "competitions")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Competition {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String title;
    @Column(length = 4000)
    private String description;

    private Long creatorId; // 发布者 userId

    private boolean published;
    private boolean approved;

    private LocalDateTime startAt;
    private LocalDateTime endAt;
}