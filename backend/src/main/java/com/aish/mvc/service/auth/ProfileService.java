package com.aish.mvc.service.auth;

import com.aish.mvc.dto.auth.OnboardingRequest;
import com.aish.mvc.dto.auth.ProfileResponse;
import com.aish.mvc.dto.auth.UpdateProfileRequest;
import org.springframework.web.multipart.MultipartFile;

/**
 * Hồ sơ cá nhân của user: xem, cập nhật thông tin, hoàn tất onboarding (lần đầu đăng nhập),
 * và đổi ảnh đại diện.
 */
public interface ProfileService {
    // Lấy hồ sơ của user đang đăng nhập.
    ProfileResponse getMyProfile();

    // Cập nhật toàn bộ hồ sơ (ghi đè các trường được gửi lên).
    ProfileResponse updateMyProfile(UpdateProfileRequest request);

    // Hoàn tất thiết lập hồ sơ lần đầu — chỉ ghi đè các trường có giá trị, không null-hoá phần còn lại.
    ProfileResponse completeOnboarding(OnboardingRequest request);

    // Tải lên ảnh đại diện mới, trả về đường dẫn ảnh đã lưu.
    String uploadAvatar(MultipartFile file);
}
