package com.aish.mvc.config;

import com.aish.mvc.service.auth.UsernameGenerator;
import org.junit.jupiter.api.Test;
import org.springframework.boot.ApplicationArguments;
import org.springframework.jdbc.core.JdbcTemplate;

import java.util.List;
import java.util.Map;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.contains;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class SchemaPatchRunnerTest {

    @Test
    void backfillsUsernameForEveryProfileWithNullUsername() {
        JdbcTemplate jdbcTemplate = mock(JdbcTemplate.class);
        UsernameGenerator usernameGenerator = mock(UsernameGenerator.class);
        when(jdbcTemplate.queryForList(contains("WHERE p.username IS NULL"))).thenReturn(List.of(
                Map.of("profile_id", 1L, "full_name", "Nguyễn Văn An"),
                Map.of("profile_id", 2L, "full_name", "Nguyễn Văn An")));
        when(usernameGenerator.generateUniqueUsername("Nguyễn Văn An"))
                .thenReturn("nguyenvanan", "nguyenvanan1");
        SchemaPatchRunner runner = new SchemaPatchRunner(jdbcTemplate, usernameGenerator);

        runner.run(mock(ApplicationArguments.class));

        verify(jdbcTemplate).update(contains("username = ? WHERE id = ?"), eq("nguyenvanan"), eq(1L));
        verify(jdbcTemplate).update(contains("username = ? WHERE id = ?"), eq("nguyenvanan1"), eq(2L));
        verify(jdbcTemplate).update(contains("SET username = NULL WHERE username = ''"));
    }

    @Test
    void doesNothingWhenNoProfileNeedsBackfill() {
        JdbcTemplate jdbcTemplate = mock(JdbcTemplate.class);
        UsernameGenerator usernameGenerator = mock(UsernameGenerator.class);
        when(jdbcTemplate.queryForList(contains("WHERE p.username IS NULL"))).thenReturn(List.of());
        SchemaPatchRunner runner = new SchemaPatchRunner(jdbcTemplate, usernameGenerator);

        runner.run(mock(ApplicationArguments.class));

        verify(jdbcTemplate, never()).update(contains("username = ? WHERE id = ?"), anyString(), eq(1L));
    }
}
