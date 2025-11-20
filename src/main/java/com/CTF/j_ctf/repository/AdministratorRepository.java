package com.CTF.j_ctf.repository;

import com.CTF.j_ctf.entity.Administrator;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface AdministratorRepository extends JpaRepository<Administrator, Integer> {
    // 可以添加管理员特有的查询方法
}