package com.CTF.j_ctf.entity;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import java.io.Serializable;

@Setter
@Getter
@EqualsAndHashCode
public class TeamMemberId implements Serializable {

    private Integer team;
    private Integer user;

    public TeamMemberId() {}

    public TeamMemberId(Integer teamId, Integer userId) {
        this.team = teamId;
        this.user = userId;
    }
}