package com.CTF.j_ctf.service;

import com.CTF.j_ctf.entity.OrdinaryUser;
import com.CTF.j_ctf.entity.User;

import java.util.Optional;

public interface UserService {

    // 通用注册方法
    User register(User user);

    // 普通用户注册方法
    OrdinaryUser registerOrdinaryUser(String userPassword, String phoneNumber,
                                      String userEmail, String userName,
                                      String gender, String schoolWorkunit);

    // 登录方法
    Optional<User> login(String account, String password);

    // 查找方法
    Optional<User> findById(Integer id);
    Optional<User> findByAccount(String account);
}