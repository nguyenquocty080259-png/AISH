package com.aish.mvc.service.doc;

import com.aish.mvc.entity.enums.ModerationKeywordType;
import com.aish.mvc.service.ai.ToxicKeywordFilter;
import org.junit.jupiter.api.Test;
import org.springframework.web.server.ResponseStatusException;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class NamingModerationServiceTest {

    @Test
    void trimsAndAcceptsNormalName() {
        ToxicKeywordFilter filter = mock(ToxicKeywordFilter.class);
        NamingModerationService service = new NamingModerationService(filter);
        assertEquals("Giải tích", service.validate("  Giải tích  "));
    }

    @Test
    void rejectsShortDigitsOnlyAndRepeatedCharacterNames() {
        NamingModerationService service = new NamingModerationService(mock(ToxicKeywordFilter.class));
        assertThrows(ResponseStatusException.class, () -> service.validate("ab"));
        assertThrows(ResponseStatusException.class, () -> service.validate("111"));
        assertThrows(ResponseStatusException.class, () -> service.validate("aaaaaa"));
    }

    @Test
    void rejectsNamingKeyword() {
        ToxicKeywordFilter filter = mock(ToxicKeywordFilter.class);
        when(filter.matches("xyzt-name collection", ModerationKeywordType.NAMING)).thenReturn(true);
        NamingModerationService service = new NamingModerationService(filter);

        ResponseStatusException exception = assertThrows(
                ResponseStatusException.class,
                () -> service.validate("xyzt-name collection"));
        // Service ném KEY i18n; GlobalExceptionHandler mới tra ra chuỗi theo Accept-Language.
        assertEquals("error.naming.badWord", exception.getReason());
    }
}
