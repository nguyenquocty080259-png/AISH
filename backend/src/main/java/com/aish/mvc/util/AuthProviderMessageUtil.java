package com.aish.mvc.util;


import com.aish.mvc.entity.enums.AuthProviders;

public final class AuthProviderMessageUtil {

    private AuthProviderMessageUtil() {
    }

    public static String getProviderMessage(AuthProviders provider) {
        return switch (provider) {

            case LOCAL ->
                    "Địa chỉ email này được đăng ký bằng Email & Mật khẩu.";

            case GOOGLE ->
                    "Tài khoản này đã tồn tại. Vui lòng đăng nhập bằng tài khoản Google.";

            case GITHUB ->
                    "Tài khoản này được đăng ký bằng GitHub.";

        };
    }
}