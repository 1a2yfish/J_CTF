package com.CTF.j_ctf.common;

import lombok.Data;

/**
 * 全局统一响应结果
 */
@Data
public class Result<T> {
    // 响应码：200成功，500失败，400参数错误等
    private Integer code;
    // 响应信息
    private String msg;
    // 响应数据
    private T data;

    // 成功响应（无数据）
    public static <T> Result<T> success() {
        Result<T> result = new Result<>();
        result.setCode(200);
        result.setMsg("操作成功");
        return result;
    }

    // 成功响应（带数据）
    public static <T> Result<T> success(T data) {
        Result<T> result = new Result<>();
        result.setCode(200);
        result.setMsg("操作成功");
        result.setData(data);
        return result;
    }

    // 失败响应
    public static <T> Result<T> fail(String msg) {
        Result<T> result = new Result<>();
        result.setCode(500);
        result.setMsg(msg);
        return result;
    }
}