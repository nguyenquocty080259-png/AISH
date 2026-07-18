package com.aish.mvc.config;

import com.aish.mvc.service.auth.UsernameGenerator;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.core.annotation.Order;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;

@Component
@RequiredArgsConstructor
@Order(0)
public class SchemaPatchRunner implements ApplicationRunner {

    private static final Logger log = LoggerFactory.getLogger(SchemaPatchRunner.class);

    private static final String NOTIFICATION_TYPE_CONSTRAINT_PATCH = """
            ALTER TABLE notifications DROP CONSTRAINT IF EXISTS notifications_type_check;
            ALTER TABLE notifications ADD CONSTRAINT notifications_type_check
              CHECK (type IN ('REPORT_CREATED', 'REPORT_RESOLVED', 'DOCUMENT_SCREENED',
                              'COMMENT_UNDER_REVIEW', 'COMMENT_REVIEWED', 'METADATA_MISMATCH'));
            """;

    private static final String AI_USAGE_MESSAGE_FK_PATCH = """
            DO $$
            DECLARE fk_name text;
            BEGIN
              FOR fk_name IN
                SELECT tc.constraint_name
                FROM information_schema.table_constraints tc
                JOIN information_schema.key_column_usage kcu
                  ON kcu.constraint_name = tc.constraint_name
                 AND kcu.constraint_schema = tc.constraint_schema
                WHERE tc.table_schema = current_schema()
                  AND tc.table_name = 'ai_usage_logs'
                  AND tc.constraint_type = 'FOREIGN KEY'
                  AND kcu.column_name = 'message_id'
              LOOP
                EXECUTE format('ALTER TABLE ai_usage_logs DROP CONSTRAINT IF EXISTS %I', fk_name);
              END LOOP;
              ALTER TABLE ai_usage_logs ALTER COLUMN message_id DROP NOT NULL;
            END $$;
            """;

    // A2b: MAX_UPLOAD_LOCAL_BYTES/MAX_UPLOAD_CLOUD_BYTES đổi tên thành MAX_FILE_LOCAL_BYTES/
    // MAX_FILE_CLOUD_BYTES (xem SystemSettingService) - đã có sẵn trên DB của mọi dev/env cũ.
    // Rename giữ nguyên value (carry-over) MIỄN LÀ key mới chưa tồn tại; nếu key mới đã có sẵn
    // (vd. app đã seed mặc định trước khi patch này chạy) thì chỉ xoá hàng cũ, không ghi đè.
    // Idempotent: lần chạy thứ 2 trở đi, hàng MAX_UPLOAD_* không còn tồn tại -> cả 2 câu lệnh
    // đều 0 dòng ảnh hưởng, không lỗi.
    private static final String RENAME_UPLOAD_LIMIT_KEYS_PATCH = """
            UPDATE system_settings SET setting_key = 'MAX_FILE_LOCAL_BYTES'
            WHERE setting_key = 'MAX_UPLOAD_LOCAL_BYTES'
              AND NOT EXISTS (SELECT 1 FROM system_settings WHERE setting_key = 'MAX_FILE_LOCAL_BYTES');
            DELETE FROM system_settings WHERE setting_key = 'MAX_UPLOAD_LOCAL_BYTES';

            UPDATE system_settings SET setting_key = 'MAX_FILE_CLOUD_BYTES'
            WHERE setting_key = 'MAX_UPLOAD_CLOUD_BYTES'
              AND NOT EXISTS (SELECT 1 FROM system_settings WHERE setting_key = 'MAX_FILE_CLOUD_BYTES');
            DELETE FROM system_settings WHERE setting_key = 'MAX_UPLOAD_CLOUD_BYTES';
            """;

    private static final String DROP_AI_PROMPTS_PATCH = """
            DO $$
            DECLARE fk_name text;
            BEGIN
              FOR fk_name IN
                SELECT tc.constraint_name
                FROM information_schema.table_constraints tc
                JOIN information_schema.key_column_usage kcu
                  ON kcu.constraint_name = tc.constraint_name
                 AND kcu.constraint_schema = tc.constraint_schema
                WHERE tc.table_schema = current_schema()
                  AND tc.table_name = 'ai_conversations'
                  AND tc.constraint_type = 'FOREIGN KEY'
                  AND kcu.column_name = 'prompt_id'
              LOOP
                EXECUTE format('ALTER TABLE ai_conversations DROP CONSTRAINT IF EXISTS %I', fk_name);
              END LOOP;
              ALTER TABLE ai_conversations DROP COLUMN IF EXISTS prompt_id;
              DROP TABLE IF EXISTS ai_prompts;
            END $$;
            """;

    private final JdbcTemplate jdbcTemplate;
    private final UsernameGenerator usernameGenerator;

    @Value("${spring.ai.openai.chat.options.model}")
    private String configuredChatModel;

    @Override
    public void run(ApplicationArguments args) {
        // Hibernate 6 creates enum check constraints when a table is first created, but ddl-auto=update
        // never refreshes them. Extend the constraint's allowed list whenever NotificationType gains a value.
        try {
            jdbcTemplate.execute(NOTIFICATION_TYPE_CONSTRAINT_PATCH);
            log.info("Applied notifications_type_check startup schema patch.");
        } catch (Exception exception) {
            log.warn("Could not apply notifications_type_check startup schema patch; startup will continue: {}",
                    exception.getMessage());
        }

        try {
            jdbcTemplate.execute(AI_USAGE_MESSAGE_FK_PATCH);
            log.info("Applied ai_usage_logs message_id FK/nullability startup schema patch.");
        } catch (Exception exception) {
            log.warn("Could not patch ai_usage_logs.message_id; startup will continue: {}", exception.getMessage());
        }

        try {
            jdbcTemplate.execute(DROP_AI_PROMPTS_PATCH);
            log.info("Applied ai_prompts removal startup schema patch.");
        } catch (Exception exception) {
            log.warn("Could not drop ai_prompts/prompt_id; startup will continue: {}", exception.getMessage());
        }

        try {
            jdbcTemplate.execute(RENAME_UPLOAD_LIMIT_KEYS_PATCH);
            log.info("Applied MAX_UPLOAD_*_BYTES -> MAX_FILE_*_BYTES rename startup schema patch.");
        } catch (Exception exception) {
            log.warn("Could not rename MAX_UPLOAD_*_BYTES settings; startup will continue: {}", exception.getMessage());
        }

        try {
            jdbcTemplate.update("""
                    INSERT INTO ai_models
                      (model_key, display_name, input_price_per1m, output_price_per1m, is_active, created_at)
                    VALUES (?, 'Llama 3.3 70B (Groq)', 0.59, 0.79, true, CURRENT_TIMESTAMP)
                    ON CONFLICT (model_key) DO UPDATE SET
                      input_price_per1m = EXCLUDED.input_price_per1m,
                      output_price_per1m = EXCLUDED.output_price_per1m,
                      is_active = true
                    """, configuredChatModel);
            log.info("Ensured configured Groq chat model exists in ai_models.");
        } catch (Exception exception) {
            log.warn("Could not seed configured Groq chat model; startup will continue: {}", exception.getMessage());
        }

        // username hiện là slug tự sinh từ fullName (không còn do người dùng nhập). Một hàng
        // cũ từng lưu '' (chuỗi rỗng) vi phạm unique constraint với mọi lần lưu blank khác ->
        // 500 hàng loạt. Coi '' như NULL rồi backfill lại bằng cùng thuật toán sinh username.
        try {
            int cleared = jdbcTemplate.update("UPDATE auth_user_profiles SET username = NULL WHERE username = ''");
            log.info("Cleared {} blank username row(s) in auth_user_profiles.", cleared);
        } catch (Exception exception) {
            log.warn("Could not clear blank usernames; startup will continue: {}", exception.getMessage());
        }

        try {
            backfillMissingUsernames();
        } catch (Exception exception) {
            log.warn("Could not backfill missing usernames; startup will continue: {}", exception.getMessage());
        }
    }

    private void backfillMissingUsernames() {
        List<Map<String, Object>> rows = jdbcTemplate.queryForList("""
                SELECT p.id AS profile_id, u.full_name AS full_name
                FROM auth_user_profiles p
                JOIN auth_users u ON u.id = p.user_id
                WHERE p.username IS NULL
                """);

        for (Map<String, Object> row : rows) {
            Long profileId = ((Number) row.get("profile_id")).longValue();
            String fullName = (String) row.get("full_name");
            String username = usernameGenerator.generateUniqueUsername(fullName);

            try {
                jdbcTemplate.update(
                        "UPDATE auth_user_profiles SET username = ? WHERE id = ?", username, profileId);
            } catch (DataIntegrityViolationException ex) {
                username = usernameGenerator.generateUniqueUsername(fullName);
                jdbcTemplate.update(
                        "UPDATE auth_user_profiles SET username = ? WHERE id = ?", username, profileId);
            }
        }

        if (!rows.isEmpty()) {
            log.info("Backfilled username for {} profile(s).", rows.size());
        }
    }
}
