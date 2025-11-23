package com.CTF.j_ctf.service.impl;

import com.CTF.j_ctf.entity.User;
import com.CTF.j_ctf.entity.User.UserType;
import com.CTF.j_ctf.repository.UserRepository;
import com.CTF.j_ctf.service.AuthService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.regex.Pattern;

@Service
public class AuthServiceImpl implements AuthService {
    private final UserRepository userRepo;
    private final BCryptPasswordEncoder passwordEncoder;

    // 正则表达式用于验证
    private static final Pattern EMAIL_PATTERN = Pattern.compile("^[A-Za-z0-9+_.-]+@(.+)$");
    private static final Pattern PHONE_PATTERN = Pattern.compile("^1[3-9]\\d{9}$");

    // 构造方法：移除AdministratorRepository，仅保留UserRepository
    public AuthServiceImpl(UserRepository userRepo) {
        this.userRepo = userRepo;
        this.passwordEncoder = new BCryptPasswordEncoder();
    }

    /**
     * 管理员登录：支持按ID/用户名查询管理员（userType=ADMIN）
     */
    @Override
    public Optional<User> adminLogin(String account, String password) {
        // 查找管理员账户（ID或用户名）
        Optional<User> admin = findAdminByIdentifier(account);

        if (admin.isPresent()) {
            User foundAdmin = admin.get();
            // 验证密码
            if (passwordEncoder.matches(password, foundAdmin.getUserPassword())) {
                return admin;
            }
        }
        return Optional.empty();
    }

    /**
     * 普通用户注册：创建userType=ORDINARY的User对象
     */
    @Override
    public User registerOrdinaryUser(String userPassword, String phoneNumber,
                                     String userEmail, String userName,
                                     String gender, String schoolWorkunit) {
        // 检查唯一性约束（用户名全局唯一，手机号/邮箱仅普通用户唯一）
        if (userRepo.existsByUserName(userName)) {
            throw new IllegalArgumentException("用户名已存在");
        }
        if (userRepo.existsOrdinaryUserByEmail(UserType.ORDINARY, userEmail)) {
            throw new IllegalArgumentException("邮箱已被注册");
        }
        if (userRepo.existsOrdinaryUserByPhoneNumber(UserType.ORDINARY, phoneNumber)) {
            throw new IllegalArgumentException("手机号已被注册");
        }

        // 创建普通用户对象（设置userType=ORDINARY）
        User user = new User(userPassword, UserType.ORDINARY, userName, phoneNumber, userEmail, gender, schoolWorkunit);
        user.setRegisterTime(LocalDateTime.now()); // 显式设置注册时间（也可依赖实体默认值）

        // 加密密码并保存
        String encodedPassword = passwordEncoder.encode(user.getUserPassword());
        user.setUserPassword(encodedPassword);

        return userRepo.save(user);
    }

    /**
     * 通用用户注册：支持管理员/普通用户（需提前设置userType）
     */
    @Override
    public User register(User user) {
        // 验证用户数据
        validateUserData(user);

        // 加密密码
        String encodedPassword = passwordEncoder.encode(user.getUserPassword());
        user.setUserPassword(encodedPassword);

        // 补充默认时间（若实体未设置）
        if (user.getCreateTime() == null) {
            user.setCreateTime(LocalDateTime.now());
        }
        if (user.isOrdinaryUser() && user.getRegisterTime() == null) {
            user.setRegisterTime(LocalDateTime.now());
        }

        return userRepo.save(user);
    }

    /**
     * 通用登录：支持管理员和普通用户，支持用户名/邮箱/手机号/ID
     */
    @Override
    public Optional<User> login(String account, String password) {
        // 先尝试查找管理员（支持ID/用户名）
        Optional<User> admin = findAdminByIdentifier(account);
        if (admin.isPresent()) {
            User foundAdmin = admin.get();
            // 验证密码
            if (passwordEncoder.matches(password, foundAdmin.getUserPassword())) {
                return admin;
            }
        }
        
        // 如果管理员登录失败，尝试查找普通用户（支持用户名/邮箱/手机号）
        Optional<User> user = findOrdinaryUserByIdentifier(account);
        if (user.isPresent()) {
            User foundUser = user.get();
            // 验证密码
            if (passwordEncoder.matches(password, foundUser.getUserPassword())) {
                return user;
            }
        }
        return Optional.empty();
    }

    /**
     * 按ID查询通用用户
     */
    @Override
    public Optional<User> findById(Integer id) {
        return userRepo.findById(id);
    }

    /**
     * 按账号（用户名/邮箱/手机号）查询普通用户
     */
    @Override
    public Optional<User> findByAccount(String account) {
        return findOrdinaryUserByIdentifier(account);
    }

    // ===================== 私有工具方法 =====================

    /**
     * 根据标识符查找管理员（支持ID/用户名）
     */
    private Optional<User> findAdminByIdentifier(String identifier) {
        // 尝试按ID查找（数字标识符）
        try {
            Integer adminId = Integer.parseInt(identifier);
            Optional<User> admin = userRepo.findById(adminId);
            if (admin.isPresent() && admin.get().isAdministrator()) {
                return admin;
            }
        } catch (NumberFormatException e) {
            // 非数字则按用户名查找
            Optional<User> admin = userRepo.findByUserName(identifier);
            if (admin.isPresent() && admin.get().isAdministrator()) {
                return admin;
            }
        }
        return Optional.empty();
    }

    /**
     * 根据标识符查找普通用户（支持用户名/邮箱/手机号）
     */
    private Optional<User> findOrdinaryUserByIdentifier(String identifier) {
        // 1. 尝试按用户名查找（全局唯一）
        Optional<User> user = userRepo.findByUserName(identifier);
        if (user.isPresent() && user.get().isOrdinaryUser()) {
            return user;
        }

        // 2. 尝试按邮箱查找（仅普通用户）
        user = userRepo.findOrdinaryUserByEmail(UserType.ORDINARY, identifier);
        if (user.isPresent()) {
            return user;
        }

        // 3. 尝试按手机号查找（仅普通用户）
        return userRepo.findOrdinaryUserByPhoneNumber(UserType.ORDINARY, identifier);
    }

    /**
     * 验证用户数据合法性：按用户类型区分验证规则
     */
    private void validateUserData(User user) {
        // 通用验证：密码长度
        if (user.getUserPassword() == null || user.getUserPassword().length() < 6) {
            throw new IllegalArgumentException("密码长度至少6位");
        }

        // 通用验证：用户类型不能为空
        if (user.getUserType() == null) {
            throw new IllegalArgumentException("用户类型不能为空");
        }

        // 通用验证：用户名不能为空（全局唯一）
        if (user.getUserName() == null || user.getUserName().trim().isEmpty()) {
            throw new IllegalArgumentException("用户名不能为空");
        }

        // 管理员专属验证：仅需验证角色（默认SYSTEM，非必需）
        if (user.isAdministrator()) {
            return; // 管理员无需验证手机号/邮箱，直接返回
        }

        // 普通用户专属验证
        if (user.isOrdinaryUser()) {
            // 验证邮箱格式
            if (user.getUserEmail() == null || !EMAIL_PATTERN.matcher(user.getUserEmail()).matches()) {
                throw new IllegalArgumentException("邮箱格式不正确");
            }

            // 验证手机号格式
            if (user.getPhoneNumber() == null || !PHONE_PATTERN.matcher(user.getPhoneNumber()).matches()) {
                throw new IllegalArgumentException("手机号格式不正确");
            }
        }
    }
}