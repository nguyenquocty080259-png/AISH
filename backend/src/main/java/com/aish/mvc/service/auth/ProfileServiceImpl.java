package com.aish.mvc.service.auth;

import com.aish.mvc.dto.auth.OnboardingRequest;
import com.aish.mvc.dto.auth.ProfileResponse;
import com.aish.mvc.dto.auth.UpdateProfileRequest;
import com.aish.mvc.entity.auth.AuthAccount;
import com.aish.mvc.entity.auth.AuthUser;
import com.aish.mvc.entity.auth.AuthUserProfile;
import com.aish.mvc.repository.auth.AuthAccountRepository;
import com.aish.mvc.repository.auth.AuthUserProfileRepository;
import com.aish.mvc.repository.auth.AuthUserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.List;
import java.util.UUID;
import java.util.function.Consumer;


@Service
@RequiredArgsConstructor
public class ProfileServiceImpl implements ProfileService {

    private final AuthAccountRepository authAccountRepository;
    private final AuthUserProfileRepository authUserProfileRepository;
    private final AuthUserRepository authUserRepository;
    private final UsernameGenerator usernameGenerator;

    private static final long MAX_FILE_SIZE = 5 * 1024 * 1024; // 5MB

    private static final List<String> ALLOWED_TYPES = List.of(
            "image/jpeg",
            "image/png",
            "image/jpg",
            "image/webp"
    );

    private static final String AVATAR_UPLOAD_DIR = "uploads/avatars";

    @Override
    public ProfileResponse getMyProfile() {

        String email = getCurrentEmail();

        AuthAccount account = authAccountRepository.findByIdentifier(email).orElseThrow(() -> new RuntimeException("Account not found"));

        AuthUser user = account.getUser();

        AuthUserProfile profile = authUserProfileRepository.findByUserId(user.getId())
                .orElseGet(() -> usernameGenerator.createProfileForUser(user));

        return mapToResponse(user, profile);
    }

    @Override
    public ProfileResponse updateMyProfile(UpdateProfileRequest request) {

        String email = getCurrentEmail();

        AuthAccount account = authAccountRepository
                .findByIdentifier(email)
                .orElseThrow(() ->
                        new RuntimeException("Account not found"));

        AuthUser user = account.getUser();

        AuthUserProfile profile = authUserProfileRepository
                .findByUserId(user.getId())
                .orElseGet(() -> usernameGenerator.createProfileForUser(user));
        // ==========================
        // VALIDATE trashRetentionDays: null cho phép (dùng mặc định 30), ngoài ra phải 1..90
        // ==========================
        if (request.getTrashRetentionDays() != null
                && (request.getTrashRetentionDays() < 1 || request.getTrashRetentionDays() > 90)) {
            throw new IllegalArgumentException(
                    "Số ngày giữ tài liệu trong thùng rác phải từ 1 đến 90.");
        }
        // AuthUser
        user.setFullName(request.getFullName());
        authUserRepository.save(user);
        // AuthUserProfile (username không nhận từ request nữa - do hệ thống tự sinh)
        profile.setBio(request.getBio());

        profile.setDob(request.getDob());

        profile.setGender(request.getGender());

        profile.setPhoneNumber(request.getPhoneNumber());

        profile.setUniversity(request.getUniversity());

        profile.setFaculty(request.getFaculty());

        profile.setMajor(request.getMajor());

        profile.setCountry(request.getCountry());

        profile.setCity(request.getCity());

        profile.setGithubUrl(request.getGithubUrl());

        profile.setLinkedinUrl(request.getLinkedinUrl());

        profile.setWebsiteUrl(request.getWebsiteUrl());

        profile.setTrashRetentionDays(request.getTrashRetentionDays());

        authUserProfileRepository.save(profile);

        return mapToResponse(user, profile);
    }

    // Onboarding là partial-update: chỉ field khác blank trong request mới được ghi đè, không
    // bao giờ null-hoá giá trị đã có (khác updateMyProfile - ghi đè toàn bộ). dob luôn bắt buộc
    // (validate ở @Valid trên controller); fullName bắt buộc CÓ nếu user hiện chưa có (vd. GitHub
    // OAuth không trả name) - nếu user đã có fullName rồi thì request có thể bỏ trống.
    @Override
    public ProfileResponse completeOnboarding(OnboardingRequest request) {

        String email = getCurrentEmail();

        AuthAccount account = authAccountRepository
                .findByIdentifier(email)
                .orElseThrow(() -> new RuntimeException("Account not found"));

        AuthUser user = account.getUser();

        AuthUserProfile profile = authUserProfileRepository
                .findByUserId(user.getId())
                .orElseGet(() -> usernameGenerator.createProfileForUser(user));

        String requestedFullName = blankToNull(request.getFullName());

        if (requestedFullName != null) {
            user.setFullName(requestedFullName);
            authUserRepository.save(user);
        } else if (isBlank(user.getFullName())) {
            throw new IllegalArgumentException("Họ tên không được để trống");
        }

        profile.setDob(request.getDob());

        applyIfPresent(request.getBio(), profile::setBio);
        applyIfPresent(request.getGender(), profile::setGender);
        applyIfPresent(request.getPhoneNumber(), profile::setPhoneNumber);
        applyIfPresent(request.getUniversity(), profile::setUniversity);
        applyIfPresent(request.getFaculty(), profile::setFaculty);
        applyIfPresent(request.getMajor(), profile::setMajor);
        applyIfPresent(request.getCountry(), profile::setCountry);
        applyIfPresent(request.getCity(), profile::setCity);
        applyIfPresent(request.getGithubUrl(), profile::setGithubUrl);
        applyIfPresent(request.getLinkedinUrl(), profile::setLinkedinUrl);
        applyIfPresent(request.getWebsiteUrl(), profile::setWebsiteUrl);

        authUserProfileRepository.save(profile);

        return mapToResponse(user, profile);
    }

    @Override
    public String uploadAvatar(MultipartFile file) {

        // =============================
        // Validate file
        // =============================

        if (file.isEmpty()) {
            throw new IllegalArgumentException("Vui lòng chọn ảnh đại diện.");
        }

        if (file.getSize() > MAX_FILE_SIZE) {
            throw new IllegalArgumentException("Kích thước ảnh không được vượt quá 5MB.");
        }

        String contentType = file.getContentType();

        if (contentType == null || !ALLOWED_TYPES.contains(contentType)) {
            throw new IllegalArgumentException("Chỉ chấp nhận ảnh JPG, JPEG, PNG hoặc WEBP.");
        }

        // =============================
        // Lấy user hiện tại
        // =============================

        String email = getCurrentEmail();

        AuthAccount account = authAccountRepository
                .findByIdentifier(email)
                .orElseThrow(() -> new RuntimeException("Account not found"));

        AuthUser user = account.getUser();

        try {

            // =============================
            // Tạo thư mục nếu chưa tồn tại
            // =============================

            Path uploadDir = Paths.get(AVATAR_UPLOAD_DIR);

            if (!Files.exists(uploadDir)) {
                Files.createDirectories(uploadDir);
            }

            // =============================
            // Sinh tên file
            // =============================

            String originalFilename = file.getOriginalFilename();

            String extension = "";

            if (originalFilename != null && originalFilename.contains(".")) {
                extension = originalFilename.substring(originalFilename.lastIndexOf("."));
            }

            String filename = UUID.randomUUID() + extension;

            // =============================
            // Lưu file
            // =============================

            Path destination = uploadDir.resolve(filename);

            Files.copy(
                    file.getInputStream(),
                    destination,
                    StandardCopyOption.REPLACE_EXISTING
            );

            // =============================
            // Xóa avatar cũ (nếu có)
            // =============================

            if (user.getAvatarUrl() != null && !user.getAvatarUrl().isBlank()) {

                try {

                    String oldFilename = Paths.get(user.getAvatarUrl()).getFileName().toString();

                    Path oldFile = uploadDir.resolve(oldFilename);

                    Files.deleteIfExists(oldFile);

                } catch (Exception ignored) {
                    // Không ảnh hưởng nếu xóa thất bại
                }

            }
            // =============================
            // Cập nhật DB
            // =============================

            String avatarUrl = "/uploads/avatars/" + filename;

            user.setAvatarUrl(avatarUrl);
            authUserRepository.save(user);

            return avatarUrl;
        } catch (IOException e) {

            throw new RuntimeException("Không thể lưu avatar.", e);
        }
    }
    private static boolean isBlank(String value) {
        return value == null || value.isBlank();
    }

    private static String blankToNull(String value) {
        return isBlank(value) ? null : value;
    }

    private static void applyIfPresent(String value, Consumer<String> setter) {
        if (!isBlank(value)) {
            setter.accept(value);
        }
    }

    private String getCurrentEmail() {

        Authentication authentication =
                SecurityContextHolder
                        .getContext()
                        .getAuthentication();

        return authentication.getName();
    }

    private ProfileResponse mapToResponse(
            AuthUser user,
            AuthUserProfile profile
    ) {

        ProfileResponse response = new ProfileResponse();

        response.setUserId(user.getId());

        response.setFullName(user.getFullName());

        response.setAvatarUrl(user.getAvatarUrl());

        response.setUsername(profile.getUsername());

        response.setBio(profile.getBio());

        response.setDob(profile.getDob());

        response.setGender(profile.getGender());

        response.setPhoneNumber(profile.getPhoneNumber());

        response.setUniversity(profile.getUniversity());

        response.setFaculty(profile.getFaculty());

        response.setMajor(profile.getMajor());

        response.setCountry(profile.getCountry());

        response.setCity(profile.getCity());

        response.setGithubUrl(profile.getGithubUrl());

        response.setLinkedinUrl(profile.getLinkedinUrl());

        response.setWebsiteUrl(profile.getWebsiteUrl());

        response.setTrashRetentionDays(profile.getTrashRetentionDays());

        return response;
    }
}