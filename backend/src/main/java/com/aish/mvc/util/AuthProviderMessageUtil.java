package com.aish.mvc.util;


import com.aish.mvc.entity.enums.AuthProviders;

/**
 * Sinh thông báo lỗi khi email đã đăng ký nhưng bằng PROVIDER KHÁC (vd: đăng ký bằng Google
 * rồi lại thử đăng nhập bằng mật khẩu) — giúp người dùng biết cần đăng nhập bằng cách nào.
 */
public final class AuthProviderMessageUtil {

    private AuthProviderMessageUtil() {
    }

    // Đầu vào: provider đã đăng ký trước đó. Trả về: câu thông báo tiếng Việt tương ứng.
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