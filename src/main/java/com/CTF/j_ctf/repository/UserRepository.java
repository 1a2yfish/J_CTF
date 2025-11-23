package com.CTF.j_ctf.repository;

import com.CTF.j_ctf.entity.User;
import com.CTF.j_ctf.entity.User.UserType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Integer> {

    // ===================== 原有 OrdinaryUserRepository 功能 =====================
    /**
     * 根据用户名模糊搜索（所有用户）
     * @param userName 用户名关键词
     * @return 匹配的用户列表
     */
    @Query("SELECT u FROM User u WHERE u.userName LIKE %:userName%")
    List<User> findByUserNameContaining(@Param("userName") String userName);

    /**
     * 根据用户名模糊搜索（所有用户）- 分页版
     * @param userName 用户名关键词
     * @param pageable 分页参数
     * @return 分页的用户列表
     */
    @Query("SELECT u FROM User u WHERE u.userName LIKE %:userName%")
    Page<User> findByUserNameContaining(@Param("userName") String userName, Pageable pageable);

    // ===================== 原有 UserRepository 功能（适配统一实体） =====================
    /**
     * 根据用户名查询用户（通用，因userName全局唯一）
     * 原findByAccount方法的逻辑：查询普通用户的userName，现改为全局唯一查询
     * @param userName 用户名
     * @return 匹配的用户
     */
    Optional<User> findByUserName(String userName);

    /**
     * 根据邮箱查询用户（仅普通用户）
     * @param userEmail 用户邮箱
     * @return 匹配的普通用户
     */
    @Query("SELECT u FROM User u WHERE u.userType = :type AND u.userEmail = :userEmail")
    Optional<User> findOrdinaryUserByEmail(@Param("type") UserType type, @Param("userEmail") String userEmail);

    /**
     * 根据手机号查询用户（仅普通用户）
     * @param phoneNumber 用户手机号
     * @return 匹配的普通用户
     */
    @Query("SELECT u FROM User u WHERE u.userType = :type AND u.phoneNumber = :phoneNumber")
    Optional<User> findOrdinaryUserByPhoneNumber(@Param("type") UserType type, @Param("phoneNumber") String phoneNumber);

    /**
     * 检查用户名是否存在（全局唯一）
     * @param userName 用户名
     * @return 是否存在
     */
    boolean existsByUserName(String userName);

    /**
     * 检查邮箱是否存在（仅普通用户）
     * @param userEmail 用户邮箱
     * @return 是否存在
     */
    @Query("SELECT COUNT(u) > 0 FROM User u WHERE u.userType = :type AND u.userEmail = :userEmail")
    boolean existsOrdinaryUserByEmail(@Param("type") UserType type, @Param("userEmail") String userEmail);

    /**
     * 检查手机号是否存在（仅普通用户）
     * @param phoneNumber 用户手机号
     * @return 是否存在
     */
    @Query("SELECT COUNT(u) > 0 FROM User u WHERE u.userType = :type AND u.phoneNumber = :phoneNumber")
    boolean existsOrdinaryUserByPhoneNumber(@Param("type") UserType type, @Param("phoneNumber") String phoneNumber);

    // ===================== 新增：按用户类型的精准查询（补充功能） =====================
    /**
     * 查询所有指定类型的用户
     * @param userType 用户类型（ADMIN/ORDINARY）
     * @return 该类型的用户列表
     */
    List<User> findByUserType(UserType userType);

    /**
     * 查询所有指定类型的用户-分页版
     * @param userType 用户类型（ADMIN/ORDINARY）
     * @param pageable 分页参数
     * @return 分页的用户列表
     */
    Page<User> findByUserType(UserType userType, Pageable pageable);

    /**
     * 根据管理员角色查询管理员
     * @param adminRole 管理员角色（SYSTEM/OPERATOR/AUDITOR）
     * @return 匹配的管理员列表
     */
    @Query("SELECT u FROM User u WHERE u.userType = :type AND u.adminRole = :adminRole")
    List<User> findAdminByRole(@Param("type") UserType type, @Param("adminRole") String adminRole);

    /**
     * 检查普通用户状态是否为激活
     * @param userId 用户ID
     * @return 是否激活
     */
    @Query("SELECT u.userStatus FROM User u WHERE u.userType = :type AND u.userID = :userId")
    Optional<Boolean> findOrdinaryUserStatusById(@Param("type") UserType type, @Param("userId") Integer userId);
    
    /**
     * 根据邮箱查询用户（全局，用于唯一性检查）
     * @param userEmail 用户邮箱
     * @return 匹配的用户
     */
    Optional<User> findByUserEmail(String userEmail);
    
    /**
     * 根据手机号查询用户（全局，用于唯一性检查）
     * @param phoneNumber 用户手机号
     * @return 匹配的用户
     */
    Optional<User> findByPhoneNumber(String phoneNumber);
}