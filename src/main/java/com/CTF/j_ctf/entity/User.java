package com.CTF.j_ctf.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "users")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class User {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String account; // 手机或邮箱

    @Column(nullable = false)
    private String password;

    @Column(nullable = false)
    private String role; // "USER" or "ADMIN"
}