package com.CTF.j_ctf.service;

import com.CTF.j_ctf.entity.OrdinaryUser;
import com.CTF.j_ctf.entity.User;

import java.util.Optional;

public interface UserProfileService {

    // 查看个人信息
    Optional<OrdinaryUser> getUserProfile(Integer userId);

    // 修改个人信息
    OrdinaryUser updateUserProfile(Integer userId, OrdinaryUser updatedProfile);

    // 修改密码
    boolean changePassword(Integer userId, String oldPassword, String newPassword);

    // 注销账户
    boolean deactivateAccount(Integer userId, String password);

    // 密码找回 - 发送验证码
    boolean sendPasswordResetCode(String identifier); // identifier可以是邮箱或手机号

    // 密码找回 - 验证并重置密码
    boolean resetPassword(String identifier, String verificationCode, String newPassword);

    // 检查用户名是否可用
    boolean isUsernameAvailable(String username);

    // 检查邮箱是否可用
    boolean isEmailAvailable(String email);

    // 检查手机号是否可用
    boolean isPhoneAvailable(String phone);
}