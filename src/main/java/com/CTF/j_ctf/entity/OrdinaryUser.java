package com.CTF.j_ctf.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Setter
@Getter
@Entity
@Table(name = "Ordinaryuser")
@PrimaryKeyJoinColumn(name = "userID")
public class OrdinaryUser extends User {

    // Getter和Setter
    @Column(name = "PhoneNumber", unique = true, length = 11)
    private String phoneNumber;

    @Column(name = "userEmail", unique = true, length = 100)
    private String userEmail;

    @Column(name = "userName", unique = true, length = 100)
    private String userName;

    @Column(name = "Gender", length = 1)
    private String gender;

    @Column(name = "School_Workunit", length = 100)
    private String schoolWorkunit;

    @Column(name = "RegisterTime")
    private LocalDateTime registerTime;

    // 构造方法
    public OrdinaryUser() {}

    public OrdinaryUser(String userPassword, String phoneNumber, String userEmail,
                        String userName, String gender, String schoolWorkunit) {
        super(userPassword);
        this.phoneNumber = phoneNumber;
        this.userEmail = userEmail;
        this.userName = userName;
        this.gender = gender;
        this.schoolWorkunit = schoolWorkunit;
        this.registerTime = LocalDateTime.now();
    }

}