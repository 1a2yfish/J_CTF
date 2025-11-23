package com.CTF.j_ctf.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Setter
@Getter
@Entity
@Table(name = "User") // 对应数据库的统一用户表
public class User {
    public enum UserType {
        ADMIN,    // 管理员
        ORDINARY  // 普通用户
    }

    // 主键ID，自增
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "userID")
    private Integer userID;

    // 用户密码（加密存储）
    @Column(name = "userPassword", nullable = false, length = 100)
    private String userPassword;

    // 用户创建时间，默认当前时间，不可更新
    @Column(name = "CreateTime", nullable = false, updatable = false)
    private LocalDateTime createTime = LocalDateTime.now();

    // 用户类型（ADMIN/ORDINARY），非空
    @Enumerated(EnumType.STRING) // 枚举值以字符串存储到数据库
    @Column(name = "UserType", nullable = false, length = 20)
    private UserType userType;

    // 统一用户名/管理员姓名（唯一，非空）
    @Column(name = "UserName", unique = true, nullable = false, length = 50)
    private String userName;

    // 管理员专属：角色（默认SYSTEM）
    @Column(name = "AdminRole", length = 20)
    private String adminRole = "SYSTEM";

    // 普通用户专属：手机号（唯一）
    @Column(name = "PhoneNumber", unique = true, length = 11)
    private String phoneNumber;

    // 普通用户专属：邮箱（唯一）
    @Column(name = "userEmail", unique = true, length = 100)
    private String userEmail;

    // 普通用户专属：性别（M/F/U）
    @Column(name = "Gender", length = 1)
    private String gender;

    // 普通用户专属：学校/工作单位
    @Column(name = "School_Workunit", length = 100)
    private String schoolWorkunit;

    // 普通用户专属：注册时间，默认当前时间，不可更新
    @Column(name = "RegisterTime", nullable = false, updatable = false)
    private LocalDateTime registerTime = LocalDateTime.now();

    // 普通用户专属：用户状态（1-正常/0-禁用，默认正常）
    @Column(name = "UserStatus")
    private Boolean userStatus = true;

    // ========== 构造方法 ==========
    // 空参构造（JPA必需）
    public User() {}

    // 管理员构造方法（核心字段）
    public User(String userPassword, UserType userType, String userName) {
        this.userPassword = userPassword;
        this.userType = userType;
        this.userName = userName;
    }

    // 管理员构造方法（含角色）
    public User(String userPassword, UserType userType, String userName, String adminRole) {
        this(userPassword, userType, userName);
        this.adminRole = adminRole;
    }

    // 普通用户构造方法（核心字段）
    public User(String userPassword, UserType userType, String userName, String phoneNumber, String userEmail) {
        this(userPassword, userType, userName);
        this.phoneNumber = phoneNumber;
        this.userEmail = userEmail;
    }

    // 普通用户构造方法（全量字段）
    public User(String userPassword, UserType userType, String userName, String phoneNumber,
                String userEmail, String gender, String schoolWorkunit) {
        this(userPassword, userType, userName, phoneNumber, userEmail);
        this.gender = gender;
        this.schoolWorkunit = schoolWorkunit;
    }

    // ========== 业务方法 ==========
    // 判断是否为管理员
    public boolean isAdministrator() {
        return UserType.ADMIN.equals(this.userType);
    }

    // 判断是否为普通用户
    public boolean isOrdinaryUser() {
        return UserType.ORDINARY.equals(this.userType);
    }

    // 普通用户：判断是否激活
    public boolean isActive() {
        return isOrdinaryUser() && Boolean.TRUE.equals(this.userStatus);
    }

    // 普通用户：激活账号
    public void activate() {
        if (isOrdinaryUser()) {
            this.userStatus = true;
        }
    }

    // 普通用户：禁用账号
    public void deactivate() {
        if (isOrdinaryUser()) {
            this.userStatus = false;
        }
    }

    // ========== 重写toString ==========
    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder("User{");
        sb.append("userID=").append(userID);
        sb.append(", userType=").append(userType);
        sb.append(", userName='").append(userName).append('\'');
        // 管理员专属字段
        if (isAdministrator()) {
            sb.append(", adminRole='").append(adminRole).append('\'');
        }
        // 普通用户专属字段
        if (isOrdinaryUser()) {
            sb.append(", phoneNumber='").append(phoneNumber).append('\'');
            sb.append(", userEmail='").append(userEmail).append('\'');
            sb.append(", userStatus=").append(userStatus);
        }
        sb.append(", createTime=").append(createTime);
        sb.append('}');
        return sb.toString();
    }
}