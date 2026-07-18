package com.aish.mvc.repository.doc;

import com.aish.mvc.entity.auth.AuthRole;
import com.aish.mvc.entity.auth.AuthUser;
import com.aish.mvc.entity.doc.DocDocument;
import com.aish.mvc.entity.doc.DocFile;
import com.aish.mvc.entity.enums.UserStatus;
import com.aish.mvc.repository.auth.AuthRoleRepository;
import com.aish.mvc.repository.auth.AuthUserRepository;
import com.aish.mvc.repository.doc.DocDocumentRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * Spring Boot 4.0.6 trong repo này KHÔNG có @DataJpaTest/TestEntityManager nữa (module
 * spring-boot-test-autoconfigure đã bỏ hẳn orm/jpa test slice) - nên dùng @SpringBootTest +
 * @Transactional giống hệt BackendApplicationTests (context load thật, DB Postgres dev thật),
 * @Transactional rollback sau mỗi test nên không để lại dữ liệu thật.
 *
 * Xác nhận 2 hành vi cốt lõi của quota (A2b): SUM null -> 0 khi user chưa có file nào, và
 * tài liệu trong thùng rác (deletedAt != null) VẪN được tính vào dung lượng đã dùng.
 */
@SpringBootTest
@Transactional
class DocFileRepositoryTest {

    @Autowired
    private DocFileRepository docFileRepository;

    @Autowired
    private DocDocumentRepository docDocumentRepository;

    @Autowired
    private AuthUserRepository authUserRepository;

    @Autowired
    private AuthRoleRepository authRoleRepository;

    private AuthUser persistUser() {
        AuthRole role = authRoleRepository.findByRoleName("USER")
                .orElseThrow(() -> new IllegalStateException("Seed role USER không tồn tại - kiểm tra AuthRoleSeedRunner"));

        AuthUser user = new AuthUser();
        user.setFullName("Quota Test User " + System.nanoTime());
        user.setStatus(UserStatus.ACTIVE);
        user.setRole(role);
        return authUserRepository.save(user);
    }

    private DocDocument persistDocument(AuthUser owner) {
        DocDocument doc = DocDocument.builder()
                .title("Quota test doc " + System.nanoTime())
                .user(owner)
                .build();
        return docDocumentRepository.save(doc);
    }

    private void persistFile(DocDocument doc, String resourceType, long size) {
        DocFile file = DocFile.builder()
                .fileName("f.txt")
                .fileUrl("f-" + System.nanoTime() + ".txt")
                .resourceType(resourceType)
                .fileSize(size)
                .document(doc)
                .build();
        docFileRepository.save(file);
    }

    @Test
    void sumsAreZeroForUserWithNoFiles() {
        AuthUser user = persistUser();

        assertEquals(0L, docFileRepository.sumLocalFileSizeByUserId(user.getId()));
        assertEquals(0L, docFileRepository.sumCloudFileSizeByUserId(user.getId()));
    }

    @Test
    void sumsSplitCorrectlyByResourceType() {
        AuthUser user = persistUser();
        DocDocument doc = persistDocument(user);
        persistFile(doc, "local", 1000L);
        persistFile(doc, "raw", 2000L);
        persistFile(doc, "image", 3000L);

        assertEquals(1000L, docFileRepository.sumLocalFileSizeByUserId(user.getId()));
        assertEquals(5000L, docFileRepository.sumCloudFileSizeByUserId(user.getId()));
    }

    @Test
    void trashedDocumentFilesStillCountTowardUsage() {
        AuthUser user = persistUser();
        DocDocument doc = persistDocument(user);
        persistFile(doc, "local", 1500L);

        // Xoá mềm (thùng rác) - bytes vẫn còn chiếm disk nên phải vẫn được tính.
        doc.setDeletedAt(LocalDateTime.now());
        docDocumentRepository.save(doc);

        assertEquals(1500L, docFileRepository.sumLocalFileSizeByUserId(user.getId()));
    }
}
