package com.marketinganalytics.platform.service.integration;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.security.SecureRandom;
import java.time.Instant;
import java.util.Base64;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Short-lived store for the OAuth {@code state} parameter, so the callback
 * can be tied back to the platform that started the flow and rejected if
 * forged (CSRF) or replayed after expiry — an in-memory map is sufficient
 * since the whole authorize-then-callback round trip normally takes seconds.
 */
@Component
public class OAuthStateStore {

    private record Entry(Long platformId, Instant expiresAt) {
    }

    private final Map<String, Entry> states = new ConcurrentHashMap<>();
    private final SecureRandom secureRandom = new SecureRandom();

    public String createState(Long platformId) {
        byte[] bytes = new byte[24];
        secureRandom.nextBytes(bytes);
        String token = Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
        states.put(token, new Entry(platformId, Instant.now().plusSeconds(600)));
        return token;
    }

    /** Consumes (single-use) the state token, returning the platform it was issued for, or null if invalid/expired. */
    public Long consume(String token) {
        Entry entry = states.remove(token);
        if (entry == null || Instant.now().isAfter(entry.expiresAt())) {
            return null;
        }
        return entry.platformId();
    }

    @Scheduled(fixedRate = 15 * 60 * 1000)
    public void purgeExpired() {
        Instant now = Instant.now();
        states.entrySet().removeIf(e -> now.isAfter(e.getValue().expiresAt()));
    }
}
