package com.CTF.j_ctf.entity;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
@Entity
@Table(name = "user")
@Inheritance(strategy = InheritanceType.JOINED)
public class User {
    // Getter和Setter
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "userID")
    private Integer userID;

    @Column(name = "userPassword", nullable = false, length = 100)
    private String userPassword;

    // 用于登录的账号字段（可以是用户名、邮箱、手机号）
    @Transient
    private String account;

    // 构造方法
    public User() {}

    public User(String userPassword) {
        this.userPassword = userPassword;
    }

}