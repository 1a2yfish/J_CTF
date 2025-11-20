package com.CTF.j_ctf.repository;

import com.CTF.j_ctf.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Integer> {

    // 根据用户名查找用户
    @Query("SELECT u FROM OrdinaryUser u WHERE u.userName = :account")
    Optional<User> findByAccount(@Param("account") String account);

    // 根据邮箱查找用户
    @Query("SELECT u FROM OrdinaryUser u WHERE u.userEmail = :account")
    Optional<User> findByEmail(@Param("account") String account);

    // 根据手机号查找用户
    @Query("SELECT u FROM OrdinaryUser u WHERE u.phoneNumber = :account")
    Optional<User> findByPhoneNumber(@Param("account") String account);

    // 检查用户名是否存在
    @Query("SELECT COUNT(u) > 0 FROM OrdinaryUser u WHERE u.userName = :userName")
    boolean existsByUserName(@Param("userName") String userName);

    // 检查邮箱是否存在
    @Query("SELECT COUNT(u) > 0 FROM OrdinaryUser u WHERE u.userEmail = :email")
    boolean existsByEmail(@Param("email") String email);

    // 检查手机号是否存在
    @Query("SELECT COUNT(u) > 0 FROM OrdinaryUser u WHERE u.phoneNumber = :phoneNumber")
    boolean existsByPhoneNumber(@Param("phoneNumber") String phoneNumber);
}