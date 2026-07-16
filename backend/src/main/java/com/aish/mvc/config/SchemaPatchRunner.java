package com.aish.mvc.config;

import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.core.annotation.Order;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Order(0)
public class SchemaPatchRunner implements ApplicationRunner {

    private static final Logger log = LoggerFactory.getLogger(SchemaPatchRunner.class);

    private static final String NOTIFICATION_TYPE_CONSTRAINT_PATCH = """
            ALTER TABLE notifications DROP CONSTRAINT IF EXISTS notifications_type_check;
            ALTER TABLE notifications ADD CONSTRAINT notifications_type_check
              CHECK (type IN ('REPORT_CREATED', 'REPORT_RESOLVED', 'DOCUMENT_SCREENED',
                              'COMMENT_UNDER_REVIEW', 'COMMENT_REVIEWED'));
            """;

    private final JdbcTemplate jdbcTemplate;

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
    }
}
