package com.aish.mvc.repository.doc;

import com.aish.mvc.entity.auth.AuthRole;
import com.aish.mvc.entity.auth.AuthUser;
import com.aish.mvc.entity.doc.DocDocument;
import com.aish.mvc.entity.enums.DocumentVisibility;
import com.aish.mvc.entity.enums.IngestStatus;
import com.aish.mvc.entity.enums.ModerationStatus;
import com.aish.mvc.entity.enums.UserStatus;
import com.aish.mvc.repository.auth.AuthRoleRepository;
import com.aish.mvc.repository.auth.AuthUserRepository;
import jakarta.persistence.EntityManager;
import org.hibernate.Hibernate;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.PageRequest;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * findMetadataScanCandidates() phải nạp SẴN chủ sở hữu. Lượt quét metadata đêm chạy ngoài
 * transaction và còn gọi stampMetadataCheck (@Modifying(clearAutomatically = true)) giữa chừng,
 * nên tới lúc gửi thông báo lệch metadata thì thực thể đã detached: user còn là proxy lười sẽ
 * ném LazyInitializationException, và lỗi đó bị khối catch của scheduler nuốt mất — chủ tài liệu
 * âm thầm không nhận được thông báo nào.
 *
 * <p>Dùng @SpringBootTest + @Transactional như DocFileRepositoryTest (repo này không còn
 * @DataJpaTest ở Spring Boot 4); @Transactional rollback nên không để lại dữ liệu thật.
 */
@SpringBootTest
@Transactional
class DocDocumentRepositoryMetadataScanTest {

    @Autowired
    private DocDocumentRepository docDocumentRepository;

    @Autowired
    private AuthUserRepository authUserRepository;

    @Autowired
    private AuthRoleRepository authRoleRepository;

    @Autowired
    private EntityManager entityManager;

    private AuthUser persistUser() {
        AuthRole role = authRoleRepository.findByRoleName("USER")
                .orElseThrow(() -> new IllegalStateException("Seed role USER không tồn tại - kiểm tra DbSeedRunner"));

        AuthUser user = new AuthUser();
        user.setFullName("Metadata Scan User " + System.nanoTime());
        user.setStatus(UserStatus.ACTIVE);
        user.setRole(role);
        return authUserRepository.save(user);
    }

    private DocDocument persistScanCandidate(AuthUser owner) {
        DocDocument doc = DocDocument.builder()
                .title("Metadata scan candidate " + System.nanoTime())
                .user(owner)
                .visibility(DocumentVisibility.PUBLIC)
                .moderationStatus(ModerationStatus.APPROVED)
                .ingestStatus(IngestStatus.INGESTED)
                .build();
        return docDocumentRepository.save(doc);
    }

    private List<DocDocument> scanCandidates() {
        return docDocumentRepository.findMetadataScanCandidates(
                DocumentVisibility.PUBLIC, ModerationStatus.APPROVED, IngestStatus.INGESTED,
                PageRequest.of(0, 50));
    }

    @Test
    void candidatesComeBackWithOwnerAlreadyInitialized() {
        AuthUser owner = persistUser();
        DocDocument candidate = persistScanCandidate(owner);
        // Xoá context để query chạy thật, không trả về thực thể đang có sẵn trong bộ nhớ.
        entityManager.flush();
        entityManager.clear();

        DocDocument loaded = scanCandidates().stream()
                .filter(d -> d.getId().equals(candidate.getId()))
                .findFirst()
                .orElseThrow(() -> new AssertionError("Tài liệu vừa tạo phải nằm trong danh sách quét"));

        // Không nạp sẵn thì đây là proxy lười -> scheduler vỡ khi đã detached.
        assertTrue(Hibernate.isInitialized(loaded.getUser()),
                "Chủ sở hữu phải được nạp sẵn trong query candidate");
        assertFalse(loaded.getUser().getFullName().isBlank());
    }

    @Test
    void alreadyScannedDocumentIsNotACandidateAgain() {
        AuthUser owner = persistUser();
        DocDocument candidate = persistScanCandidate(owner);
        docDocumentRepository.stampMetadataCheck(candidate.getId(), "KHOP", java.time.LocalDateTime.now());
        entityManager.flush();
        entityManager.clear();

        boolean stillListed = scanCandidates().stream()
                .anyMatch(d -> d.getId().equals(candidate.getId()));

        assertFalse(stillListed, "Tài liệu vừa quét xong không được lấy lại ở lượt kế tiếp");
    }
}
