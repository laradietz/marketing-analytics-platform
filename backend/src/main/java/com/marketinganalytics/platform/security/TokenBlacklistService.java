package com.marketinganalytics.platform.security;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Logout strategy for stateless JWTs: since a JWT cannot be "deleted" server
 * side, logging out revokes the specific token by remembering it until it
 * would have expired anyway. An in-memory store is sufficient for a single
 * instance; a multi-instance deployment would back this with Redis instead.
 */
@Service
public class TokenBlacklistService {

    private final Map<String, Instant> revokedTokens = new ConcurrentHashMap<>();

    public void revoke(String token, Instant expiresAt) {
        revokedTokens.put(token, expiresAt);
    }

    public boolean isRevoked(String token) {
        return revokedTokens.containsKey(token);
    }

    @Scheduled(fixedRate = 15 * 60 * 1000)
    public void purgeExpired() {
        Instant now = Instant.now();
        revokedTokens.values().removeIf(now::isAfter);
        revokedTokens.entrySet().removeIf(e -> now.isAfter(e.getValue()));
    }
}
