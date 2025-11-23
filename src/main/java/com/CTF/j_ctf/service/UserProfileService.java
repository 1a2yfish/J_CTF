package com.CTF.j_ctf.service;

import com.CTF.j_ctf.entity.User;

import java.util.Optional;

public interface UserProfileService {

    // 获取用户个人信息
    Optional<User> getUserProfile(Integer userId);

    // 更新用户个人信息
    User updateUserProfile(Integer userId, User updatedProfile);

    // 修改密码
    boolean changePassword(Integer userId, String oldPassword, String newPassword);

    // 注销账户
    boolean deactivateAccount(Integer userId, String password);

    // 发送密码重置验证码
    boolean sendPasswordResetCode(String identifier);

    // 重置密码
    boolean resetPassword(String identifier, String verificationCode, String newPassword);

    // 检查用户名是否可用
    boolean isUsernameAvailable(String username);

    // 检查邮箱是否可用
    boolean isEmailAvailable(String email);

    // 检查手机号是否可用
    boolean isPhoneAvailable(String phone);
}