package com.CTF.j_ctf.service;

import com.CTF.j_ctf.entity.User;

import java.util.Optional;

public interface UserService {
    User register(User user);
    Optional<User> login(String account, String password);
    Optional<User> findById(Long id);
}
