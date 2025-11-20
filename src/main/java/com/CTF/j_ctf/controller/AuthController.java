package com.CTF.j_ctf.controller;

import com.CTF.j_ctf.common.Result;
import com.CTF.j_ctf.entity.OrdinaryUser;
import com.CTF.j_ctf.entity.User;
import com.CTF.j_ctf.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.Optional;

/**
 * 用户模块控制器
 * 路由前缀：/api/users
 */
@RestController
@RequestMapping("/api/users")
public class UserController {

    // 注入UserService实现类（需确保Service层已添加@Service注解）
    @Autowired
    private UserService userService;

    /**
     * 通用用户注册接口
     * 请求方式：POST
     * 路由：/api/users/register
     * 请求体：User对象的JSON数据
     */
    @PostMapping("/register")
    public Result<User> register(@RequestBody User user) {
        try {
            User registeredUser = userService.register(user);
            return Result.success(registeredUser);
        } catch (Exception e) {
            // 捕获业务异常，返回失败信息
            return Result.fail("通用注册失败：" + e.getMessage());
        }
    }

    /**
     * 普通用户注册接口
     * 请求方式：POST
     * 路由：/api/users/register/ordinary
     * 请求参数：通过URL参数或表单传递
     */
    @PostMapping("/register/ordinary")
    public Result<OrdinaryUser> registerOrdinaryUser(
            @RequestParam String userPassword,
            @RequestParam String phoneNumber,
            @RequestParam String userEmail,
            @RequestParam String userName,
            @RequestParam String gender,
            @RequestParam String schoolWorkunit) {
        try {
            OrdinaryUser ordinaryUser = userService.registerOrdinaryUser(
                    userPassword, phoneNumber, userEmail, userName, gender, schoolWorkunit);
            return Result.success(ordinaryUser);
        } catch (Exception e) {
            return Result.fail("普通用户注册失败：" + e.getMessage());
        }
    }

    /**
     * 用户登录接口
     * 请求方式：POST
     * 路由：/api/users/login
     * 请求参数：账号（account）、密码（password）
     */
    @PostMapping("/login")
    public Result<User> login(
            @RequestParam String account,
            @RequestParam String password) {
        Optional<User> userOptional = userService.login(account, password);
        // 判断Optional是否有值，有则返回用户信息，无则返回登录失败
        return userOptional.map(Result::success)
                .orElseGet(() -> Result.fail("账号或密码错误"));
    }

    /**
     * 根据ID查询用户
     * 请求方式：GET
     * 路由：/api/users/{id}
     * 路径参数：用户ID
     */
    @GetMapping("/{id}")
    public Result<User> findById(@PathVariable Integer id) {
        Optional<User> userOptional = userService.findById(id);
        return userOptional.map(Result::success)
                .orElseGet(() -> Result.fail("未找到ID为" + id + "的用户"));
    }

    /**
     * 根据账号查询用户
     * 请求方式：GET
     * 路由：/api/users/account/{account}
     * 路径参数：用户账号
     */
    @GetMapping("/account/{account}")
    public Result<User> findByAccount(@PathVariable String account) {
        Optional<User> userOptional = userService.findByAccount(account);
        return userOptional.map(Result::success)
                .orElseGet(() -> Result.fail("未找到账号为" + account + "的用户"));
    }
}