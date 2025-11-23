package com.CTF.j_ctf.service.impl;

import com.CTF.j_ctf.entity.User;
import com.CTF.j_ctf.entity.User.UserType;
import com.CTF.j_ctf.repository.UserRepository;
import com.CTF.j_ctf.service.UserProfileService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.regex.Pattern;

@Service
@Transactional
public class UserProfileServiceImpl implements UserProfileService {
    private final UserRepository userRepository;
    private final BCryptPasswordEncoder passwordEncoder;

    // 用于存储验证码（生产环境应该使用Redis等）
    private final ConcurrentHashMap<String, String> verificationCodes = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<String, Long> codeExpiry = new ConcurrentHashMap<>();

    // 验证码过期时间（5分钟）
    private static final long CODE_EXPIRY_TIME = 5 * 60 * 1000;

    // 正则表达式
    private static final Pattern EMAIL_PATTERN = Pattern.compile("^[A-Za-z0-9+_.-]+@(.+)$");
    private static final Pattern PHONE_PATTERN = Pattern.compile("^1[3-9]\\d{9}$");
    private static final Pattern PASSWORD_PATTERN = Pattern.compile("^(?=.*[A-Za-z])(?=.*\\d)[A-Za-z\\d@$!%*#?&]{6,}$");

    public UserProfileServiceImpl(UserRepository userRepository, BCryptPasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public Optional<User> getUserProfile(Integer userId) {
        Optional<User> user = userRepository.findById(userId);
        // 只返回普通用户的完整信息
        if (user.isPresent() && user.get().isOrdinaryUser()) {
            return user;
        }
        return Optional.empty();
    }

    @Override
    public User updateUserProfile(Integer userId, User updatedProfile) {
        Optional<User> existingUserOpt = userRepository.findById(userId);

        if (existingUserOpt.isEmpty()) {
            throw new IllegalArgumentException("用户不存在");
        }

        User existingUser = existingUserOpt.get();

        // 确保是普通用户
        if (!existingUser.isOrdinaryUser()) {
            throw new IllegalArgumentException("只有普通用户可以更新个人信息");
        }

        // 验证并更新可修改的字段
        if (updatedProfile.getPhoneNumber() != null &&
                !updatedProfile.getPhoneNumber().equals(existingUser.getPhoneNumber())) {

            if (!PHONE_PATTERN.matcher(updatedProfile.getPhoneNumber()).matches()) {
                throw new IllegalArgumentException("手机号格式不正确");
            }

            if (isPhoneAvailable(updatedProfile.getPhoneNumber())) {
                existingUser.setPhoneNumber(updatedProfile.getPhoneNumber());
            } else {
                throw new IllegalArgumentException("手机号已被使用");
            }
        }

        if (updatedProfile.getUserEmail() != null &&
                !updatedProfile.getUserEmail().equals(existingUser.getUserEmail())) {

            if (!EMAIL_PATTERN.matcher(updatedProfile.getUserEmail()).matches()) {
                throw new IllegalArgumentException("邮箱格式不正确");
            }

            if (isEmailAvailable(updatedProfile.getUserEmail())) {
                existingUser.setUserEmail(updatedProfile.getUserEmail());
            } else {
                throw new IllegalArgumentException("邮箱已被使用");
            }
        }

        if (updatedProfile.getUserName() != null &&
                !updatedProfile.getUserName().equals(existingUser.getUserName())) {

            if (updatedProfile.getUserName().trim().isEmpty()) {
                throw new IllegalArgumentException("用户名不能为空");
            }

            if (isUsernameAvailable(updatedProfile.getUserName())) {
                existingUser.setUserName(updatedProfile.getUserName());
            } else {
                throw new IllegalArgumentException("用户名已被使用");
            }
        }

        // 更新其他允许修改的字段
        if (updatedProfile.getGender() != null) {
            existingUser.setGender(updatedProfile.getGender());
        }

        if (updatedProfile.getSchoolWorkunit() != null) {
            existingUser.setSchoolWorkunit(updatedProfile.getSchoolWorkunit());
        }

        return userRepository.save(existingUser);
    }

    @Override
    public boolean changePassword(Integer userId, String oldPassword, String newPassword) {
        Optional<User> userOpt = userRepository.findById(userId);

        if (userOpt.isEmpty()) {
            throw new IllegalArgumentException("用户不存在");
        }

        User user = userOpt.get();

        // 验证旧密码
        if (!passwordEncoder.matches(oldPassword, user.getUserPassword())) {
            throw new IllegalArgumentException("原密码不正确");
        }

        // 验证新密码格式
        if (!PASSWORD_PATTERN.matcher(newPassword).matches()) {
            throw new IllegalArgumentException("新密码必须包含字母和数字，且长度至少6位");
        }

        // 更新密码
        String hashedNewPassword = passwordEncoder.encode(newPassword);
        user.setUserPassword(hashedNewPassword);
        userRepository.save(user);

        return true;
    }

    @Override
    public boolean deactivateAccount(Integer userId, String password) {
        Optional<User> userOpt = userRepository.findById(userId);

        if (userOpt.isEmpty()) {
            throw new IllegalArgumentException("用户不存在");
        }

        User user = userOpt.get();

        // 确保是普通用户
        if (!user.isOrdinaryUser()) {
            throw new IllegalArgumentException("只有普通用户可以注销账户");
        }

        // 验证密码
        if (!passwordEncoder.matches(password, user.getUserPassword())) {
            throw new IllegalArgumentException("密码不正确");
        }

        // 软删除：将用户状态设置为禁用
        user.setUserStatus(false);
        userRepository.save(user);

        return true;
    }

    @Override
    public boolean sendPasswordResetCode(String identifier) {
        // 查找用户
        Optional<User> userOpt = findOrdinaryUserByIdentifier(identifier);

        if (userOpt.isEmpty()) {
            // 出于安全考虑，不透露用户是否存在
            return true;
        }

        // 生成6位验证码
        String code = generateVerificationCode();

        // 存储验证码和过期时间
        verificationCodes.put(identifier, code);
        codeExpiry.put(identifier, System.currentTimeMillis() + CODE_EXPIRY_TIME);

        // 这里应该调用邮件服务或短信服务发送验证码
        // 模拟发送验证码
        System.out.println("向 " + identifier + " 发送验证码: " + code);

        return true;
    }

    @Override
    public boolean resetPassword(String identifier, String verificationCode, String newPassword) {
        // 验证验证码
        String storedCode = verificationCodes.get(identifier);
        Long expiryTime = codeExpiry.get(identifier);

        if (storedCode == null || expiryTime == null) {
            throw new IllegalArgumentException("验证码无效或已过期");
        }

        if (System.currentTimeMillis() > expiryTime) {
            // 清理过期验证码
            verificationCodes.remove(identifier);
            codeExpiry.remove(identifier);
            throw new IllegalArgumentException("验证码已过期");
        }

        if (!storedCode.equals(verificationCode)) {
            throw new IllegalArgumentException("验证码不正确");
        }

        // 验证新密码格式
        if (!PASSWORD_PATTERN.matcher(newPassword).matches()) {
            throw new IllegalArgumentException("新密码必须包含字母和数字，且长度至少6位");
        }

        // 查找用户
        Optional<User> userOpt = findOrdinaryUserByIdentifier(identifier);
        if (userOpt.isEmpty()) {
            throw new IllegalArgumentException("用户不存在");
        }

        // 更新密码
        User user = userOpt.get();
        String hashedPassword = passwordEncoder.encode(newPassword);
        user.setUserPassword(hashedPassword);
        userRepository.save(user);

        // 清理验证码
        verificationCodes.remove(identifier);
        codeExpiry.remove(identifier);

        return true;
    }

    @Override
    public boolean isUsernameAvailable(String username) {
        return !userRepository.existsByUserName(username);
    }

    @Override
    public boolean isEmailAvailable(String email) {
        return !userRepository.existsOrdinaryUserByEmail(UserType.ORDINARY, email);
    }

    @Override
    public boolean isPhoneAvailable(String phone) {
        return !userRepository.existsOrdinaryUserByPhoneNumber(UserType.ORDINARY, phone);
    }

    /**
     * 根据标识符查找普通用户（支持用户名、邮箱、手机号）
     */
    private Optional<User> findOrdinaryUserByIdentifier(String identifier) {
        // 1. 尝试按用户名查找
        Optional<User> user = userRepository.findByUserName(identifier);
        if (user.isPresent() && user.get().isOrdinaryUser()) {
            return user;
        }

        // 2. 尝试按邮箱查找
        user = userRepository.findOrdinaryUserByEmail(UserType.ORDINARY, identifier);
        if (user.isPresent()) {
            return user;
        }

        // 3. 尝试按手机号查找
        return userRepository.findOrdinaryUserByPhoneNumber(UserType.ORDINARY, identifier);
    }

    /**
     * 生成6位数字验证码
     */
    private String generateVerificationCode() {
        java.util.Random random = new java.util.Random();
        int code = 100000 + random.nextInt(900000);
        return String.valueOf(code);
    }
}