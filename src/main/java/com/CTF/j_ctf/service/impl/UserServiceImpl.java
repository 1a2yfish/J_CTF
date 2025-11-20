package com.CTF.j_ctf.service.impl;

import com.CTF.j_ctf.entity.OrdinaryUser;
import com.CTF.j_ctf.entity.User;
import com.CTF.j_ctf.repository.UserRepository;
import com.CTF.j_ctf.service.UserService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.regex.Pattern;

@Service
public class UserServiceImpl implements UserService {
    private final UserRepository repo;
    private final BCryptPasswordEncoder passwordEncoder;

    // 正则表达式用于验证
    private static final Pattern EMAIL_PATTERN = Pattern.compile("^[A-Za-z0-9+_.-]+@(.+)$");
    private static final Pattern PHONE_PATTERN = Pattern.compile("^1[3-9]\\d{9}$");

    public UserServiceImpl(UserRepository repo) {
        this.repo = repo;
        this.passwordEncoder = new BCryptPasswordEncoder();
    }

    @Override
    public User register(User user) {
        // 验证用户数据
        validateUserData(user);

        // 加密密码
        String encodedPassword = passwordEncoder.encode(user.getUserPassword());
        user.setUserPassword(encodedPassword);

        return repo.save(user);
    }

    @Override
    public OrdinaryUser registerOrdinaryUser(String userPassword, String phoneNumber,
                                             String userEmail, String userName,
                                             String gender, String schoolWorkunit) {
        // 检查唯一性约束
        if (repo.existsByUserName(userName)) {
            throw new IllegalArgumentException("用户名已存在");
        }
        if (repo.existsByEmail(userEmail)) {
            throw new IllegalArgumentException("邮箱已被注册");
        }
        if (repo.existsByPhoneNumber(phoneNumber)) {
            throw new IllegalArgumentException("手机号已被注册");
        }

        // 创建普通用户
        OrdinaryUser user = new OrdinaryUser(userPassword, phoneNumber, userEmail,
                userName, gender, schoolWorkunit);
        user.setRegisterTime(LocalDateTime.now());

        // 加密密码并保存
        String encodedPassword = passwordEncoder.encode(user.getUserPassword());
        user.setUserPassword(encodedPassword);

        return repo.save(user);
    }

    @Override
    public Optional<User> login(String account, String password) {
        // 尝试通过不同方式查找用户
        Optional<User> user = findUserByIdentifier(account);

        if (user.isPresent()) {
            User foundUser = user.get();
            // 验证密码
            if (passwordEncoder.matches(password, foundUser.getUserPassword())) {
                return user;
            }
        }

        return Optional.empty();
    }

    @Override
    public Optional<User> findById(Integer id) {
        return repo.findById(id);
    }

    @Override
    public Optional<User> findByAccount(String account) {
        return findUserByIdentifier(account);
    }

    /**
     * 根据标识符查找用户（支持用户名、邮箱、手机号）
     */
    private Optional<User> findUserByIdentifier(String identifier) {
        // 尝试按用户名查找
        Optional<User> user = repo.findByAccount(identifier);
        if (user.isPresent()) {
            return user;
        }

        // 尝试按邮箱查找
        user = repo.findByEmail(identifier);
        if (user.isPresent()) {
            return user;
        }

        // 尝试按手机号查找
        return repo.findByPhoneNumber(identifier);
    }

    /**
     * 验证用户数据
     */
    private void validateUserData(User user) {
        if (user.getUserPassword() == null || user.getUserPassword().length() < 6) {
            throw new IllegalArgumentException("密码长度至少6位");
        }

        if (user instanceof OrdinaryUser ordinaryUser) {

            // 验证邮箱格式
            if (!EMAIL_PATTERN.matcher(ordinaryUser.getUserEmail()).matches()) {
                throw new IllegalArgumentException("邮箱格式不正确");
            }

            // 验证手机号格式
            if (!PHONE_PATTERN.matcher(ordinaryUser.getPhoneNumber()).matches()) {
                throw new IllegalArgumentException("手机号格式不正确");
            }

            // 验证用户名
            if (ordinaryUser.getUserName() == null || ordinaryUser.getUserName().trim().isEmpty()) {
                throw new IllegalArgumentException("用户名不能为空");
            }
        }
    }
}