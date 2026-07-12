package com.aish.mvc.tools.seed;

import org.springframework.stereotype.Component;

import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class SeedCredentialRegistry {

    private final Map<String, String> credentials = new ConcurrentHashMap<>();

    public void register(String email, String plainPassword) {
        credentials.put(normalize(email), plainPassword);
    }

    public Optional<String> getPassword(String email) {
        if (email == null) {
            return Optional.empty();
        }
        return Optional.ofNullable(credentials.get(normalize(email)));
    }

    private String normalize(String email) {
        return email.trim().toLowerCase(Locale.ROOT);
    }
}
