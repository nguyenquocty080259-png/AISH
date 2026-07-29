package com.aish.mvc.service.doc;

import com.aish.mvc.entity.enums.ModerationKeywordType;
import com.aish.mvc.service.ai.ToxicKeywordFilter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

@Service
@RequiredArgsConstructor
public class NamingModerationService {

    private final ToxicKeywordFilter toxicKeywordFilter;

    public String validate(String value) {
        String cleaned = value == null ? "" : value.trim();
        if (cleaned.length() < 3
                || cleaned.chars().allMatch(Character::isDigit)
                || isSingleRepeatedCharacter(cleaned)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "error.naming.invalid");
        }
        if (toxicKeywordFilter.matches(cleaned, ModerationKeywordType.NAMING)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "error.naming.badWord");
        }
        return cleaned;
    }

    private boolean isSingleRepeatedCharacter(String value) {
        if (value.isEmpty()) return false;
        int first = value.codePointAt(0);
        return value.codePoints().allMatch(codePoint -> codePoint == first);
    }
}
