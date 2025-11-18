package com.CTF.j_ctf.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "problems")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Problem {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String title;
    @Column(length = 4000)
    private String description;

    private Integer score;
    private boolean published;
    private Long competitionId;
    private Long creatorId;
}