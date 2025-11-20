package com.CTF.j_ctf.entity;

import jakarta.persistence.*;

@Entity
@Table(name = "Administrator")
@PrimaryKeyJoinColumn(name = "userID")
@DiscriminatorValue("ADMIN")
public class Administrator extends User {

    public Administrator() {}

    public Administrator(String userPassword) {
        super(userPassword);
    }

    @Override
    public String toString() {
        return "Administrator{" +
                "userID=" + getUserID() +
                '}';
    }
}