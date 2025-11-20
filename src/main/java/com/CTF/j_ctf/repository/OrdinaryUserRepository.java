package com.CTF.j_ctf.repository;

import com.CTF.j_ctf.entity.OrdinaryUser;
import org.springframework.data.domain.Page;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import org.springframework.data.domain.Pageable;
import java.util.List;
import java.util.Optional;

@Repository
public interface OrdinaryUserRepository extends JpaRepository<OrdinaryUser, Integer> {

    Optional<OrdinaryUser> findByUserName(String userName);

    Optional<OrdinaryUser> findByUserEmail(String userEmail);

    Optional<OrdinaryUser> findByPhoneNumber(String phoneNumber);

    List<OrdinaryUser> findByGender(String gender);

    List<OrdinaryUser> findBySchoolWorkunit(String schoolWorkunit);

    // 根据用户名模糊搜索
//    @Query("SELECT u FROM OrdinaryUser u WHERE u.userName LIKE %:userName%")
//    List<OrdinaryUser> findByUserNameContaining(@Param("userName") String userName);
    @Query("SELECT u FROM OrdinaryUser u WHERE u.userName LIKE %:userName%")
    Page<OrdinaryUser> findByUserNameContaining(@Param("userName") String userName, Pageable pageable);
}