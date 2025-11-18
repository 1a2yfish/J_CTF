package com.CTF.j_ctf.service.impl;

import com.CTF.j_ctf.entity.User;
import com.CTF.j_ctf.repository.UserRepository;
import com.CTF.j_ctf.service.UserService;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class UserServiceImpl implements UserService {
    private final UserRepository repo;

    public UserServiceImpl(UserRepository repo) { this.repo = repo; }

    @Override
    public User register(User user) {
        // TODO: add password hashing & validation
        return repo.save(user);
    }

    @Override
    public Optional<User> login(String account, String password) {
        return repo.findByAccount(account)
                .filter(u -> u.getPassword().equals(password));
    }

    @Override
    public Optional<User> findById(Long id) { return repo.findById(id); }
}
