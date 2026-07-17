package com.aish.mvc.service.auth;

import com.aish.mvc.entity.auth.AuthUser;
import com.aish.mvc.entity.auth.AuthUserProfile;
import com.aish.mvc.repository.auth.AuthUserProfileRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Component;

import java.text.Normalizer;
import java.util.regex.Pattern;

// Single authority cho việc sinh username từ fullName (slug, dùng làm URL trong tương lai).
// username không còn do người dùng nhập/sửa; mọi nơi tạo AuthUserProfile phải đi qua đây
// để đảm bảo cùng một thuật toán và cùng một cơ chế chống trùng (unique constraint).
@Component
@RequiredArgsConstructor
public class UsernameGenerator {

    private static final Pattern COMBINING_MARKS = Pattern.compile("\\p{Mn}+");
    private static final Pattern NON_ALNUM = Pattern.compile("[^a-z0-9]");
    private static final String FALLBACK_BASE = "user";

    private final AuthUserProfileRepository authUserProfileRepository;

    public String slugify(String fullName) {
        if (fullName == null) {
            return FALLBACK_BASE;
        }

        String normalized = fullName.replace('đ', 'd').replace('Đ', 'D');
        normalized = Normalizer.normalize(normalized, Normalizer.Form.NFD);
        normalized = COMBINING_MARKS.matcher(normalized).replaceAll("");
        normalized = normalized.toLowerCase();
        normalized = NON_ALNUM.matcher(normalized).replaceAll("");

        return normalized.isBlank() ? FALLBACK_BASE : normalized;
    }

    public String generateUniqueUsername(String fullName) {
        String base = slugify(fullName);
        String candidate = base;
        int suffix = 1;

        while (authUserProfileRepository.existsByUsername(candidate)) {
            candidate = base + suffix;
            suffix++;
        }

        return candidate;
    }

    // Tạo profile mới kèm username duy nhất. Nếu hai request cùng lúc lấy trùng username
    // (race giữa existsByUsername và save), unique constraint sẽ chặn insert thứ hai;
    // ta bắt lỗi đó, sinh lại username và thử lưu thêm một lần nữa.
    public AuthUserProfile createProfileForUser(AuthUser user) {
        AuthUserProfile profile = new AuthUserProfile();
        profile.setUser(user);
        profile.setUsername(generateUniqueUsername(user.getFullName()));

        try {
            return authUserProfileRepository.save(profile);
        } catch (DataIntegrityViolationException ex) {
            profile.setUsername(generateUniqueUsername(user.getFullName()));
            return authUserProfileRepository.save(profile);
        }
    }
}
