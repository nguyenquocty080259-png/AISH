package com.aish.mvc.service.auth;

import com.aish.mvc.entity.auth.AuthUser;
import com.aish.mvc.entity.auth.AuthUserProfile;
import com.aish.mvc.repository.auth.AuthUserProfileRepository;
import org.junit.jupiter.api.Test;
import org.springframework.dao.DataIntegrityViolationException;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class UsernameGeneratorTest {

    @Test
    void slugifyStripsVietnameseDiacriticsAndLowercases() {
        AuthUserProfileRepository repository = mock(AuthUserProfileRepository.class);
        UsernameGenerator generator = new UsernameGenerator(repository);

        assertEquals("nguyenvanan", generator.slugify("Nguyễn Văn An"));
    }

    @Test
    void slugifyMapsDVietnameseLetterToPlainD() {
        AuthUserProfileRepository repository = mock(AuthUserProfileRepository.class);
        UsernameGenerator generator = new UsernameGenerator(repository);

        assertEquals("dova", generator.slugify("Đỗ Va"));
    }

    @Test
    void slugifyFallsBackToUserWhenFullNameIsNullOrEmpty() {
        AuthUserProfileRepository repository = mock(AuthUserProfileRepository.class);
        UsernameGenerator generator = new UsernameGenerator(repository);

        assertEquals("user", generator.slugify(null));
        assertEquals("user", generator.slugify("   "));
        assertEquals("user", generator.slugify("!!!"));
    }

    @Test
    void generateUniqueUsernameAppendsSmallestAvailableSuffixOnCollision() {
        AuthUserProfileRepository repository = mock(AuthUserProfileRepository.class);
        when(repository.existsByUsername("quty")).thenReturn(true);
        when(repository.existsByUsername("quty1")).thenReturn(true);
        when(repository.existsByUsername("quty2")).thenReturn(false);
        UsernameGenerator generator = new UsernameGenerator(repository);

        assertEquals("quty2", generator.generateUniqueUsername("Quty"));
    }

    @Test
    void createProfileForUserRetriesOnceOnRaceDuringSave() {
        AuthUserProfileRepository repository = mock(AuthUserProfileRepository.class);
        when(repository.existsByUsername(any())).thenReturn(false);
        doThrow(new DataIntegrityViolationException("duplicate"))
                .doAnswer(invocation -> invocation.getArgument(0))
                .when(repository).save(any(AuthUserProfile.class));
        UsernameGenerator generator = new UsernameGenerator(repository);
        AuthUser user = new AuthUser();
        user.setFullName("Race User");

        AuthUserProfile profile = generator.createProfileForUser(user);

        assertEquals("raceuser", profile.getUsername());
        verify(repository, times(2)).save(any(AuthUserProfile.class));
    }
}
